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

        Set<Project> allProject = project.getAllprojects();
        System.out.println("All Project: "+allProject.toString());
        // 실시간 의존성 추가
        compileDeps = project.getConfigurations().getByName("compile").getDependencies();
        compileDeps.add(project.getDependencies().create(SDK_INFO_FOR_DEPENDENCY));
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

        if (!hasDependency)
        	compileDeps.add(project.getDependencies().create(SDK_INFO_FOR_DEPENDENCY));
        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                System.out.println("After Evaluate!!!");
                TaskContainer tc = project.getTasks();
        		//System.out.println("Task Container: "+tc.toString());

			    Iterator<Task> it = tc.iterator();
		        while(it.hasNext()) {
		        	// Processing when Release Task existing in compile tasks
		        	Task compileTask = it.next();

	            	Pattern flavorPattern = Pattern.compile(":app:compile(\\w+)(Release|Debug)JavaWithJavac");
	        		Matcher flavorMatcher = flavorPattern.matcher( compileTask.toString() );
	        		if( flavorMatcher.find() ) {
		        		//if (compileTask.getPath().equals(":app:compileDebugJavaWithJavac")) {
		            	//Task complieTask = tc.getByPath(":app:lint");
				        System.out.println("Task Name: "+compileTask.getName() + ", Task Desc: "+compileTask.getDescription());
				        compileTask.doLast(new Action<Task>() {
				        	@Override
				        	public void execute(Task task) {
				        		System.out.println("Task Name: "+task.getName() + ", Task Desc: "+task.getDescription());

				        		String flavor = "";
				        		String buildType = "";
								Pattern flavorPattern = Pattern.compile(":app:compile(\\w+)(Release|Debug)JavaWithJavac");
				        		Matcher flavorMatcher = flavorPattern.matcher( task.toString() );
				        		if( flavorMatcher.find() ) {
				        			flavor = flavorMatcher.group(1).toLowerCase();
				        			Pattern bulidTypePattern = Pattern.compile(":app:compile"+flavorMatcher.group(1)+"(\\w+)JavaWithJavac");
				        			Matcher buildTypeMatcher = bulidTypePattern.matcher(task.toString());
				        			if (buildTypeMatcher.find())
				        				buildType = buildTypeMatcher.group(1).toLowerCase();
				        		}
				        		// Get Activity List on Manifest.xml
				                //String fileName = "app/build/intermediates/manifests/full/debug/AndroidManifest.xml";
				                String fileName = "app/build/intermediates/manifests/full/";
				                if (!flavor.equals("")) {
				                	fileName += flavor+"/";
				                }
				                fileName += buildType+"/AndroidManifest.xml";

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
						            NodeList activityList = applicationList.item(0).getChildNodes();
						            System.out.println("-----------------------");

						            // This find 'activity'
						            for (int index = 0; index < activityList.getLength(); index++) {
						                Node nNode = activityList.item(index);
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

							                    // File location it
							                    String buildLocation = "app/build/intermediates/classes/";
							                    if (!flavor.equals("")) {
								                	buildLocation += flavor+"/";
								                }
								                buildLocation += buildType+"/";
							                    //String buildLocation = "app/build/intermediates/classes/debug/";
							                    /*
							                    // TODO 해당 앱의 패키지로 변경할것
							                    File location = new File(buildLocation + "com/example/bigasslayout/bigasslayout");
							                    if (location.isDirectory()) {
							                    	for (String tmp : location.list()) {
														System.out.println(tmp);
													}
												}*/

							                    System.out.println("Bulid Location : " + buildLocation + activityName);
							                    new Transformer().doTransform(
							                    	buildLocation + activityName+".class", 
							                    	new FileInputStream(buildLocation + activityName + ".class")
							                    	);
						                    }
						                }
						            }
						        } catch (Exception e) {
						            e.printStackTrace();
						        }
				        	}
				        });
		            }
		        }
            }
        });

        //*/

        System.out.println("LanternPlugin Applied");

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