package patmob.plugin.patbase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import patmob.core.PatmobPlugin;
import patmob.core.TreeBranchEditor_2;
import patmob.data.PatentCollectionList;
import patmob.data.PatentCollectionMap;
import patmob.data.PatentTreeNode;

/**
 *
 * @author Piotr
 */
public class PatBasePost implements PatmobPlugin {
    PatBaseFrame frame;
    String patbaseWeek = "";
    boolean stopAlerts = false;
    //key = family name; value = projects which have this family
    private HashMap<String,String> familyProjectMap;
    private PatentCollectionMap allFamilies;
    
    @Override
    public String getName() {
//        return "PatBase HTTP POST";
        return "PatBase API Plug-in";
    }

    @Override
    public void doJob() {
        showGui();
    }
    
    //default format
    public void runAlerts(String queryFilePath, String updateCmd,
            PatBaseQueryResultFormat format) {
        // 20250209: init these collections for each run
        familyProjectMap = new HashMap<>();                 
        allFamilies = new PatentCollectionMap();
        
        ArrayList<String> jobs = new ArrayList<>();
        try {
            try (BufferedReader br = new BufferedReader(
                         new FileReader(new File(queryFilePath)))) {
                String line;
                while ((line=br.readLine())!=null) {
                    if (line.contains("\t")) {
                        jobs.add(line);
                    }
                }
                AlertRunner alertRunner = new AlertRunner(
                        jobs.toArray(new String[1]), updateCmd,
                        new PatentCollectionList("Alert Results"), format);
                new Thread(alertRunner).start();
            }
        } catch (Exception x) {
            System.out.println("PatBasePost.runAlerts: " + x);
        }
    }
    
    public void stopAlertRunner() {
        stopAlerts = true;
    }

    private class AlertRunner implements Runnable {
        String[] allQueries;
        String updateCmd;
        PatentCollectionList alertResults;
        PatBaseQueryResultFormat alertFormat;
        
        public AlertRunner(String[] queries, String updateString,
                PatentCollectionList results, PatBaseQueryResultFormat format) {
            allQueries = queries;
            updateCmd = updateString;
            //NOT USED
            alertResults = results;
            alertFormat = format;
        }
        
        @Override
        public void run() {
            for (int i=0; i<allQueries.length; i++) {
                if (stopAlerts) {
                    break;
                }
                String[] myJob = allQueries[i].split("\t");
                String projectName = myJob[0],
                        fullQuery = "(" + myJob[1] + ") and (" + updateCmd +")";
                frame.appendLogText("\nSubmitting " + projectName + "... ");
                PatentTreeNode projectNode = PatBaseAPI.query(
                        fullQuery, "1", "100", "-1", alertFormat);
                String summary = projectNode.getDescription();
                frame.appendLogText(summary.substring(
                        0, summary.indexOf("for query")));
                if (projectNode.size()>0) {                    
                    // store families in familyProjectMap
                    Iterator<PatentTreeNode> projectFamilies = 
                            projectNode.getChildren().iterator();
                    while (projectFamilies.hasNext()) {
                        PatentTreeNode family = projectFamilies.next();
                        String famName = family.getName();
                        if (familyProjectMap.containsKey(famName)) {
                            String familyProjects = familyProjectMap.get(famName);
                            familyProjectMap.put(famName, 
                                    familyProjects + ", " + projectName);
                        } else {
                            familyProjectMap.put(famName, projectName);
                            allFamilies.addChild(family);
                        }
                    }
                }
            }
            
            // re-create projects from familyProjectMap and make allProjects
            PatentCollectionMap allProjects = new PatentCollectionMap("PMap");
            Iterator<Entry<String,String>> iterator = 
                    familyProjectMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String,String> entry = iterator.next();
                String familyName = entry.getKey(),
                        projectName = entry.getValue();
                if (allProjects.containsKey(projectName)) {
                    PatentTreeNode project = allProjects.get(projectName);
                    project.addChild(allFamilies.get(familyName));
                } else {
                    PatentCollectionList project = 
                            new PatentCollectionList(projectName);
                    project.addChild(allFamilies.get(familyName));
                    allProjects.addChild(project);
                }
            }
            
            stopAlerts = false;
            new TreeBranchEditor_2(
                    allProjects, coreAccess.getController()).setVisible(true);
        }
    }
    
    // GUI format
    public void runQuery(String cmd, String fromRec, String toRec, String sort,
            PatBaseQueryResultFormat format) {
        PatentTreeNode docs = PatBaseAPI.query(cmd, fromRec, toRec, sort, format);
        if (docs!=null) {
            new TreeBranchEditor_2(
                docs, coreAccess.getController()).setVisible(true);
        }        
    }
    
//    public void runQuery2AP(String cmd, String fromRec, String toRec) {
//        PatentTreeNode docs = PatBaseAPI.query2AP(cmd, fromRec, toRec);
//        if (docs!=null) {
//            new TreeBranchEditor_2(
//                docs, coreAccess.getController()).setVisible(true);
//        }        
//    }
    
    private void showGui() {
        frame = new PatBaseFrame(PatBasePost.this);
        frame.setLogText("Sending HTTP POST request to PatBase API...");
        frame.setVisible(true);
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                String patbaseApiStatus = PatBaseAPI.initialize(
                        coreAccess.getController().getPatmobProperty("patmobProxy"),
                        "sapmas", "765sanofi245");
                if (patbaseApiStatus.length()==4) {
                    patbaseWeek = patbaseApiStatus;
                    frame.setLogText("OK");
                    frame.setPbWeekField(patbaseWeek);
                    frame.setUpdateCmdField(
                            "UE=" + patbaseWeek + "US or " +
                            "UE=" + patbaseWeek + "EP or " +
                            "UE=" + patbaseWeek + "WO");
                } else {
                    frame.setLogText(patbaseApiStatus);
                }
            }
        });
        
    }
    
}
