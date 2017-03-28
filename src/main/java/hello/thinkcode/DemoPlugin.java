package hello.thinkcode;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import hello.asm.Transformer;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.io.FileInputStream;

public class DemoPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {

        Set<Project> allProject = project.getAllprojects();
        System.out.println("All Project: "+allProject.toString());
        compileDeps = project.getConfigurations().getByName("compile").getDependencies();
        compileDeps.add(project.getDependencies().create("com.lantern.lantern:app-debug:0.1.13@aar"));
        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                System.out.println("After Evaluate!!!");
                TaskContainer tc = project.getTasks();
        		System.out.println("Task Container: "+tc.toString());
                
		        //Task complieTask = tc.getByPath(":app:compileDebugSources");
		        Task complieTask = tc.getByPath(":app:compileDebugJavaWithJavac");
		        System.out.println("Task Name: "+complieTask.getName() + ", Task Desc: "+complieTask.getDescription());
		        complieTask.doLast(new Action<Task>() {
		        	@Override
		        	public void execute(Task task) {
		        		System.out.println("Task Name: "+task.getName() + ", Task Desc: "+task.getDescription());
						
		        		// Get Activity List on Manifest.xml
		                String fileName = "app/build/intermediates/manifests/full/debug/AndroidManifest.xml";
				        try {
				            File fXmlFile = new File(fileName);
				            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				            Document doc = dBuilder.parse(fXmlFile);
				            doc.getDocumentElement().normalize();

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
				                    String activityName = getActivityName(eElement);
				                    if (activityName != null) {
				                    	activityName = activityName.replace(".", "/");
					                    System.out.println("Activity name : " + activityName);
					                    System.out.println("--- do transform ---");

					                    // File location it
					                    String buildLocation = "app/build/intermediates/classes/debug/";
					                    File location = new File(buildLocation + "com/example/bigasslayout/bigasslayout");
					                    if (location.isDirectory()) {
					                    	for (String tmp : location.list()) {
												System.out.println(tmp);
											}
										}

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
        });

        //*/

        System.out.println("DemoPlugin Applied");

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