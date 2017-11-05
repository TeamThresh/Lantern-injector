package com.lantern.lantern;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler; 
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.DependencyResolutionListener;
import org.gradle.api.artifacts.ResolvableDependencies;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.lantern.asm.Transformer;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Iterator;
import java.io.FileInputStream;
import java.lang.NullPointerException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LanternPlugin implements Plugin<Project> {
	public static final String CONF_LANTERN_SDK = "lantern";
	DependencySet compileDeps;
	private final String SDK_INFO_FOR_DEPENDENCY = "com.lantern:lantern-sdk:0.1.67";

    @Override
    public void apply(Project project) {

		System.out.println("Test Version");
        Set<Project> allProject = project.getAllprojects();
        System.out.println("All Project: "+allProject.toString());

        compileDeps = project.getConfigurations().getByName("compile").getDependencies();
        if (!hasDependency(compileDeps)) {
			dynamicDependencyInject(compileDeps, project, SDK_INFO_FOR_DEPENDENCY);
		}

		project.afterEvaluate(new Action<Project>() {
			@Override
			public void execute(Project project) {
				System.out.println("After Evaluate!!!");
				TaskContainer tc = project.getTasks();
				System.out.println("Task Container: "+tc.toString());

				Iterator<Task> it = tc.iterator();
				while(it.hasNext()) {
					// Processing when Release Task existing in compile tasks
					Task compileTask = it.next();

					// Find Javac Task
					Pattern javacPattern = Pattern.compile(":app:compile(\\w*)(Release|Debug)JavaWithJavac");
					Matcher javacMatcher = javacPattern.matcher( compileTask.toString() );
					if( javacMatcher.find() ) {
						//if (compileTask.getPath().equals(":app:compileDebugJavaWithJavac")) {
						//Task complieTask = tc.getByPath(":app:lint");
						findBuildType(compileTask);
					}
				}
			}
    	});
        System.out.println("LanternPlugin Applied");

    }

    private static boolean hasDependency(DependencySet compileDeps) {
    	// 의존성 확인
        //compileDeps.add(project.getDependencies().create("com.lantern.lantern:app-debug:0.1.13@aar"));
        boolean hasDependency = false;
        Iterator<Dependency> it = compileDeps.iterator();
        while (it.hasNext()) {
            Dependency dep = it.next();

            if (dep.getName().equals("lantern-sdk")
                && dep.getGroup().equals("com.lantern")) {
                hasDependency = true;
            }
        }

        return hasDependency;
    }

    private static void dynamicDependencyInject(DependencySet compileDeps, Project project, String name) {
        // 실시간 의존성 추가
		compileDeps.add(project.getDependencies().create(name));
    }

    private static void findBuildType(Task compileTask) {
    	System.out.println("Task Name: "+compileTask.getName() + ", Task Desc: "+compileTask.getDescription());
        compileTask.doLast(new Action<Task>() {
        	@Override
        	public void execute(Task task) {
        		System.out.println("Task Name: "+task.getName() + ", Task Desc: "+task.getDescription());

        		// Find Flavor and Build type
        		String flavor = "";
        		String buildType = "";
				Pattern flavorPattern = Pattern.compile(":app:compile(\\w+)(Release|Debug)JavaWithJavac");
        		Matcher flavorMatcher = flavorPattern.matcher( task.toString() );
        		if( flavorMatcher.find() ) {
        			// If exist flavor 
        			flavor = flavorMatcher.group(1).toLowerCase();
        			Pattern bulidTypePattern = Pattern.compile(":app:compile"+flavorMatcher.group(1)+"(\\w+)JavaWithJavac");
        			Matcher buildTypeMatcher = bulidTypePattern.matcher(task.toString());
        			if (buildTypeMatcher.find())
        				buildType = buildTypeMatcher.group(1).toLowerCase();
        		} else {
        			// no flavor
        			Pattern bulidTypePattern = Pattern.compile(":app:compile(\\w+)JavaWithJavac");
        			Matcher buildTypeMatcher = bulidTypePattern.matcher(task.toString());
        			if (buildTypeMatcher.find())
        				buildType = buildTypeMatcher.group(1).toLowerCase();
        		}

        		// Get Activity List on Manifest.xml
        		NodeList activityList = getActivityList(getManifestLocation(flavor, buildType));

                // File location it
                String buildLocation = "app/build/intermediates/classes/";
                if (!flavor.equals("")) {
                	buildLocation += flavor+"/";
                }
                buildLocation += buildType+"/";

		        // This find 'activity'
		        for (int index = 0; index < activityList.getLength(); index++) {
		            Node nNode = activityList.item(index);
					doInject(nNode, buildLocation);
		        }
        	}
        });
    }

    private static String getManifestLocation(String flavor, String buildType) {
		String fileName = "app/build/intermediates/manifests/full/";
		if (!flavor.equals("")) {
			fileName += flavor+"/";
		}
		fileName += buildType+"/AndroidManifest.xml";
		System.out.println("Manifest Location : "+fileName);

    	return fileName;
    }

	private static NodeList getActivityList(String fileName) {
		// Get Activity List on Manifest.xml
		//String fileName = "app/build/intermediates/manifests/full/debug/AndroidManifest.xml";

		NodeList activityList = null;
		try {
			File fXmlFile = new File(fileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			String packageName = doc.getDocumentElement().getAttribute("package");
			packageName = packageName.replace(".", "/");

			// Search Element name that have 'activity' in 'application' 
			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
			NodeList applicationList = doc.getElementsByTagName("application");
			activityList = applicationList.item(0).getChildNodes();

			System.out.println("-----------------------");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return activityList;
	}

    private static void doInject(Node nNode, String buildLocation) {

        // This find 'activity'
        if (nNode.getNodeType() == Node.ELEMENT_NODE
            && nNode.getNodeName().equals("activity")) {

        	// If found activity node do change .class file
            Element eElement = (Element) nNode;
        	// TODO 앱의 액티비티인지 패키지명으로 검사할 것 
            String activityName = getActivityName(eElement);
            if (activityName != null) {
            	activityName = activityName.replace(".", "/");
                System.out.println("Activity name : " + activityName);
                System.out.println("--- do transform ---");

                System.out.println("Bulid Location : " + buildLocation + activityName);
				try {
					new Transformer().doTransform(
						buildLocation + activityName+".class", 
						new FileInputStream(buildLocation + activityName + ".class")
					);
				} catch (Exception e) {
					e.printStackTrace();
				}
            }
        }
    }

    private static String getActivityName(Element eElement) {
        //System.out.println(eElement.getAttributes());
        String sTag = "android:name";
        String enabled = "android:enabled";
        if (eElement.getAttribute(enabled).equals("false"))
        	return null;
        else 
        	return eElement.getAttribute(sTag);
    }
}