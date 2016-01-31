package patmob.patbase;

import patmob.patbase.table.PatbaseQueryTableFrame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import patmob.core.PatmobPlugin;
import patmob.data.ops.impl.register.AllPubsRegisterRequest;
import patmob.data.ops.impl.register.RegisterRequestParams;
import patmob.patbase.table.PatbaseAlertTableFrame;

/**
 *
 * @author Piotr
 */
public class PatbaseRestPlugin implements PatmobPlugin {
    PatbaseQueryFrame frame;
    // default current, but can be defined by user
    String patbaseWeek = "";
    boolean stopAlerts = false;
    //key = family name; value = projects which have this family
    private HashMap<String,String> familyProjectMap;
    private JSONObject allFamilies;
    //smaller copy of SearchResultsBIB array
    private JSONArray dupFamilies;
    
    @Override
    public String getName() {
        return "PatBase REST API Plug-in";
    }

    @Override
    public void doJob() {
        showGui();
    }
    
    public void runAlerts(String queryFilePath, String updateCmd, String queryWeek) {
        patbaseWeek = queryWeek;
        coreAccess.getController().setPatmobProperty(
                "patbaseAlertQueriesFile", queryFilePath);
        coreAccess.getController().savePatmobProperties();
        familyProjectMap = new HashMap<>();                 
        allFamilies = new JSONObject().put("Families", new JSONArray());
        allFamilies.put("ResultType", PatbaseRestApi.SEARCHRESULTSBIB);
        dupFamilies = new JSONArray();
        
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

    /**
     * Get the specific data for each publication, using the GetMember method
     * of PatBase API, and use it to update the table.
     * If no data for EP publication, call EP Register to get the corresponding
     * WO "Publication Reference", and use it at PatBase API.
     */
    private class AlertUpdater implements Runnable {
        PatbaseAlertTableFrame table;
        
        public AlertUpdater(PatbaseAlertTableFrame ptf) {
            table = ptf;
        }
        
        @Override
        public void run() {
            for (int m=0; m<allFamilies.getJSONArray("Families").length(); m++){
                JSONObject family = allFamilies.getJSONArray("Families")
                        .getJSONObject(m);
                String uePN = family.getString("uePatentNumber");
                String[] p = uePN.split(" ");
                System.out.println("PN: " + uePN);
                JSONObject equivalentData = PatbaseRestApi.runMethod(
                        PatbaseRestApi.GETMEMBER,
                        new BasicNameValuePair("pn", p[0]),
                        new BasicNameValuePair("kd", p[1]),
                        new BasicNameValuePair("ft", "true"));
                
                if (equivalentData!=null) {
                    try {
                        String claimsTxt = equivalentData.getJSONArray("FullText")
                                .getJSONObject(0).getString("Claims");
                        String imgData = "";
                        StringBuilder invBuilder = new StringBuilder();
                        JSONArray invArray = equivalentData.getJSONArray("Inventors");
                        for (int i=0; i<invArray.length(); i++) {
                            JSONObject inv = invArray.optJSONObject(i);
                            invBuilder.append(inv.optString("INV") + "; ");
                        }
                        
                        // * Get images from claims
                        int start = claimsTxt.indexOf("[FTIMG");
                        if (start>-1) {
                            int end = claimsTxt.indexOf("]", start);
                            String id = claimsTxt.substring(start+7, end);
                            imgData = "<img src=\"https://www.patbase.com/getimg/ftimg.asp?id=" + id + "\">";// height=\"150\" width=\"150\">";
                        }
                        // * Get mosaic
//                        String[] pnData = uePN.split(" ");
//                        imgData = "<img src=\"http://www.patbase.com/phpmosaic/getone.php?uePN=" 
//                                        + pnData[0] + 
//                                        "&kd=" + pnData[1] +
//                                        "&pg=1\" height=\"150\" width=\"150\">";
                        
                        JSONObject updateMember = family.getJSONObject("UpdateMember");
                        updateMember.put("PA", equivalentData.optString("ProbableAssignee"));
                        updateMember.put("TI", equivalentData.optString("Title"));
                        updateMember.put("AB", equivalentData.optString("Abstract"));
                        updateMember.put("IMG", imgData);
                        updateMember.put("CL", claimsTxt);
                        updateMember.put("INV", invBuilder.substring(0, invBuilder.length()-2));
                        
                        if (uePN.startsWith("EP")) {
                            String allPubs = getPubsFromRegister(p[0]);
                            updateMember.put("REG", allPubs);
                            //check the WO for missing data
                            if ((claimsTxt.equals("")||updateMember.optString("AB").equals("")) && allPubs.contains("WO")) {
                                String woPN = allPubs.substring(allPubs.indexOf("WO"), allPubs.indexOf(" ", allPubs.indexOf("WO")));
                                woPN = woPN.substring(0,2) + woPN.substring(4);
                                JSONObject equivalentData2 = PatbaseRestApi.runMethod(
                                        PatbaseRestApi.GETMEMBER,
                                        new BasicNameValuePair("pn", woPN),
                                        new BasicNameValuePair("ft", "true"));
                                claimsTxt = equivalentData2.optJSONArray("FullText").getJSONObject(0).optString("Claims");
                                String imgData2 = "";
                                start = claimsTxt.indexOf("[FTIMG");
                                if (start>-1) {
                                    int end = claimsTxt.indexOf("]", start);
                                    String id = claimsTxt.substring(start+7, end);
                                    imgData2 = "<img src=\"https://www.patbase.com/getimg/ftimg.asp?id=" 
                                                    + id + "\">"; // width=200px height=\"150\"
                                }
                                updateMember.put("PA", updateMember.optString("PA") + " :: (" +woPN+") " + equivalentData2.optString("ProbableAssignee"));
                                updateMember.put("TI", updateMember.optString("TI") + " :: (" +woPN+") " + equivalentData2.optString("Title"));
                                updateMember.put("AB", updateMember.optString("AB") + " :: (" +woPN+") " + equivalentData2.optString("Abstract"));
                                updateMember.put("IMG", updateMember.optString("IMG") + " :: (" +woPN+") " + imgData2);
                                updateMember.put("CL", updateMember.optString("CL") + " :: (" +woPN+") " + claimsTxt);
                            } else {
                                // check application for abstract missing in B1/B2
                                if (updateMember.optString("AB").equals("") && (uePN.endsWith("B1")||uePN.endsWith("B2"))) {
                                    JSONObject equivalentData3 = PatbaseRestApi.runMethod(
                                            PatbaseRestApi.GETMEMBER,
                                            new BasicNameValuePair("pn", p[0]));
                                    updateMember.put("AB", updateMember.optString("AB") + " :: "+ equivalentData3.optString("Abstract"));
                                }
                            }
                        }
                    } catch (Exception x) {System.out.println("AlertUpdater: " + x);}
                }
            }
        }
    }
    
    private String getPubsFromRegister(String pn) {
        String allPubs = "";
        try {
            RegisterRequestParams searchParams = 
                    new RegisterRequestParams(new String[]{pn});
            AllPubsRegisterRequest rr = new AllPubsRegisterRequest(searchParams);
            searchParams = rr.submitCall();
            ArrayList<String> rows = searchParams.getResultRows();
            allPubs = rows.get(0);
        } catch (Exception x) {System.out.println("getPubsFromRegister: " + x);}
        return allPubs;
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
                    // Add families from 'projectResults' to 'allFamilies'
                    // if not there already - use 'familyProjectMap' to map
                    // projects on families
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
                    frame.appendLogText("----------");
                }
            }
            
            // construct compact version of family objects, (i) retaining
            // "Family", "Title", "ProbableAssignee", "Abstract", "FirstInventor"
            // "PatentNumber" (basic) fields; (ii) adding "ProjectName", "PD", 
            // "uePatentNumber" (update), "mCount", "UpdateMember" fields
            for (int m=0; m<allFamilies.getJSONArray("Families").length(); m++){
                JSONObject o = allFamilies.getJSONArray("Families").getJSONObject(m);
                o.put("ProjectName", familyProjectMap.get(o.getString("Family")));
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
                                        "PatentNumber",
                                        "ProjectName",
                                        "mCount",
                                        "Title",
                                        "ProbableAssignee",
                                        "Abstract",
                                        "FirstInventor"});
                            dupo.put("uePatentNumber", pnString);
                            dupo.put("PD", pub.getString("PD"));
                            dupo.put("UpdateMember", new JSONObject());
                            dupFamilies.put(dupo);
                    }
                }
            }
            
            allFamilies.put("Families", dupFamilies);
            
            stopAlerts = false;
            
            java.awt.EventQueue.invokeLater(() -> {
                PatbaseAlertTableFrame table = new PatbaseAlertTableFrame(allFamilies, PatbaseRestPlugin.this);
                table.setVisible(true);
                
                AlertUpdater alertUpdater = new AlertUpdater(table);
                new Thread(alertUpdater).start();
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
            new PatbaseQueryTableFrame(qResult, this).setVisible(true);
            frame.appendQueryLogText(qResult.getString("Results")
                    + " results for [" + cmd + "]");
        });
    }    
    
    private void showGui() {
        frame = new PatbaseQueryFrame(PatbaseRestPlugin.this);
        frame.setLogText("Sending HTTP POST request to PatBase API...");
        frame.setQueryFileField(coreAccess.getController()
                .getPatmobProperty("patbaseAlertQueriesFile"));
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
//                    "piotr.masiakowski@sanofi.com", "ip4638"));
                    "patbase_api@sanofi.com", "4uhHab4Hz"));
            if (PatbaseRestApi.isInitialized) {
                JSONObject jOb = PatbaseRestApi.runMethod(
                        PatbaseRestApi.GETWEEK, null);
                if (jOb!=null) {
//                    patbaseWeek =  jOb.getString("Week");
//                    frame.setPbWeekField(patbaseWeek);
//                    frame.setUpdateCmdField(
//                            "UE=" + patbaseWeek + "US or " +
//                            "UE=" + patbaseWeek + "EP or " +
//                            "UE=" + patbaseWeek + "WO");
                    String currentWeek = jOb.getString("Week");
                    frame.setPbWeekField(currentWeek);
                    frame.setUpdateCmdField(currentWeek);
                }
            }
        });
    }
}
