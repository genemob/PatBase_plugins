package patmob.patbase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;
import patmob.core.PatmobPlugin;

/**
 *
 * @author Piotr
 */
public class PatbaseRestPlugin implements PatmobPlugin {
    PatbaseQueryFrame frame;
    String patbaseWeek = "";
    boolean stopAlerts = false;
    //key = family name; value = projects which have this family
    private HashMap<String,String> familyProjectMap;
    private JSONObject allFamilies;
    //smaller copy of SearchResultsBIB array
    private final JSONArray dupFamilies = new JSONArray();
    
    @Override
    public String getName() {
        return "PatBase REST API Plug-in";
    }

    @Override
    public void doJob() {
        showGui();
    }
    
    public void runAlerts(String queryFilePath, String updateCmd) {
        familyProjectMap = new HashMap<>();                 
        allFamilies = new JSONObject().put("Families", new JSONArray());
        allFamilies.put("ResultType", PatbaseRestApi.SEARCHRESULTSBIB);
        
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
                        jobs.toArray(new String[1]), updateCmd);
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
        
        public AlertRunner(String[] queries, String updateString) {
            allQueries = queries;
            updateCmd = updateString;
        }
        
        @Override
        public void run() {
            for (String allQuerie : allQueries) {
                if (stopAlerts) {
                    break;
                }
                String[] myJob = allQuerie.split("\t");
                String projectName = myJob[0],
                        fullQuery = "(" + myJob[1] + ") and (" + updateCmd +")";
                frame.appendLogText("\nSubmitting " + projectName + "... ");
                JSONObject projectResults = PatbaseRestApi.query(
                        fullQuery, PatbaseRestApi.SEARCHRESULTSBIB, 
                        "1", "100", "2", projectName);
                if (projectResults!=null) {
                    frame.appendLogText(projectResults.getString("Results") + " records");
                    JSONArray proFams = projectResults.getJSONArray("Families");
                    for (int k=0; k<proFams.length(); k++) {
                        JSONObject proFam = proFams.getJSONObject(k);
                        String famName = proFam.getString("Family");
                        if (familyProjectMap.containsKey(famName)) {
                            String famProjects = familyProjectMap.get(famName);
                            familyProjectMap.put(famName,
                                    famProjects + ", " + projectName);
                        } else {
                            familyProjectMap.put(famName, projectName);
                            allFamilies.getJSONArray("Families").put(proFam);
                        }
                    }
                } else {
                    frame.appendLogText("No records");
                }
            }
            
            for (int m=0; m<allFamilies.getJSONArray("Families").length(); m++){
                JSONObject o = allFamilies.getJSONArray("Families").getJSONObject(m);
                o.put("ProjectName", familyProjectMap.get(o.getString("Family")));
                o.put("PatentNumber", "");
                o.put("PD", "");
                JSONArray pubs = o.getJSONArray("Publications");
                o.put("mCount", pubs.length());
                for (int n=0; n<pubs.length(); n++) {
                    JSONObject pub = pubs.getJSONObject(n);
                    String pnString = pub.getString("PN") + " " +
                            pub.getString("KD");// + " " +
                    if (pub.getString("UE").equals(patbaseWeek) &&
                            (pub.getString("CC").equals("EP") ||
                            pub.getString("CC").equals("US") ||
                            pub.getString("CC").equals("WO"))) {
                            JSONObject dupo = new JSONObject(o, 
                                    new String[]{"Family",
                                        "ProjectName",
                                        "mCount",
                                        "Title",
                                        "ProbableAssignee",
                                        "Abstract",
                                        "FirstInventor"});
                            dupo.put("PatentNumber", pnString);
                            dupo.put("PD", pub.getString("PD"));
                            dupFamilies.put(dupo);
                    }
                }
            }
            
            allFamilies.put("Families", dupFamilies);
            
            stopAlerts = false;
            java.awt.EventQueue.invokeLater(() -> {
                new PatbaseTableFrame(allFamilies, PatbaseRestPlugin.this)
                        .setVisible(true);
            });
            
        }
    }
    
    public void runQuery(String cmd, String fromRec, String toRec, String sort) {
//            PatBaseQueryResultFormat format) {
        java.awt.EventQueue.invokeLater(() -> {
            JSONObject qResult = PatbaseRestApi.query(
                    cmd, PatbaseRestApi.SEARCHRESULTS, fromRec, toRec, sort, 
                    "Single query");
            qResult.put("ResultType", PatbaseRestApi.SEARCHRESULTS);
            new PatbaseTableFrame(qResult, this).setVisible(true);
            frame.appendQueryLogText(qResult.getString("Results")
                    + " results for [" + cmd + "]");
        });
    }    
    
    private void showGui() {
        frame = new PatbaseQueryFrame(PatbaseRestPlugin.this);
        frame.setLogText("Sending HTTP POST request to PatBase API...");
        frame.setVisible(true);
        
        java.awt.EventQueue.invokeLater(() -> {
            // supress invalid cookie warnings
            try {
                System.setErr(new PrintStream("patbase_cookies.txt"));
            } catch (FileNotFoundException ex) {
                System.out.println("supress warnings: " + ex);
            }
            frame.setLogText(PatbaseRestApi.initialize(
                    coreAccess.getController().getPatmobProperty("patmobProxy"),
                    "piotr.masiakowski@sanofi.com", "ip4638"));
            if (PatbaseRestApi.isInitialized) {
                JSONObject jOb = PatbaseRestApi.runMethod(
                        PatbaseRestApi.GETWEEK, null);
                if (jOb!=null) {
                    patbaseWeek =  jOb.getString("Week");
                    frame.setPbWeekField(patbaseWeek);
                    frame.setUpdateCmdField(
                            "UE=" + patbaseWeek + "US or " +
                            "UE=" + patbaseWeek + "EP or " +
                            "UE=" + patbaseWeek + "WO");
                }
            }
        });
    }
}
