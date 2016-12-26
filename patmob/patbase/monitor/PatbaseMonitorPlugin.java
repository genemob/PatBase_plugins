package patmob.patbase.monitor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import patmob.core.PatmobPlugin;
import patmob.patbase.PatbaseRestApi;

/**
 *
 * @author nm54935
 */
public class PatbaseMonitorPlugin implements PatmobPlugin {
    HashMap<String, JSONObject> annotatedFamilies = new HashMap();

    @Override
    public String getName() {
        return "PatBase Monitor";
    }

    @Override
    public void doJob() {
        
//        org.patmob.core.table.TableFrame.main(new String[0]);
        
        System.out.println(PatbaseRestApi.initialize(coreAccess.getController()
                .getPatmobProperty("patmobProxy"),
                "patbase_api@sanofi.com", "4uhHab4Hz"));
        showGui();
    }
    
    
    private void showGui() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
//                new MonitorFrame_0(PatbaseMonitorPlugin.this).setVisible(true);
                new MonitorFrame_1(PatbaseMonitorPlugin.this).setVisible(true);
            }
        });        
    }
    
    public void runAlert(String params[]) {
        // params[0] - PNs
        // params[1] - date from
        // params[2] - date to
        
        System.out.println("Running alert...");
        //retrieve all patent families
        String pQuery = "(" + params[0] + ")";
System.out.println("pQuery: " + pQuery);        
        JSONObject pResult = PatbaseRestApi.query(pQuery, 
                PatbaseRestApi.SEARCHRESULTSBIB, "1", null, "2", "test");
//System.out.println("pResult: " + pResult);                
        JSONArray origFamilies = pResult.getJSONArray("Families");
//                annotatedFamilies = new JSONArray();
        // ANNOTATED FAMILIES???

        
        
        String[] fieldsToCopy = new String[]{"Family", "ProbableAssignee",
                                             "Title", "Abstract"};
        for (int i=0; i<origFamilies.length(); i++) {
            JSONObject origFamily = origFamilies.getJSONObject(i);
            JSONObject smallCopy = new JSONObject(origFamily, fieldsToCopy);
            
            // keep all patent families, filter by length of arrays
            smallCopy.put("New Publications", new JSONArray());
            smallCopy.put("New Legal Status", new JSONArray());
            smallCopy.put("PAIR Bulk Data", new JSONArray());
            
            JSONArray origPubs = origFamily.getJSONArray("Publications");
            for (int j=0; j<origPubs.length(); j++) {
                JSONObject origPub = origPubs.getJSONObject(j);
                //check for country/pub date
                if ((origPub.getString("PD").compareTo(params[1]))>=0 &&
                        (origPub.getString("PD").compareTo(params[2]))<=0) {
//System.out.println(origPub.getString("PN") + " :: " + origPub.getString("PD"));
                    JSONArray newPubs = smallCopy.optJSONArray("New Publications");
                    if (newPubs!=null) {
                        newPubs.put(origPub);
                    }
                }
                //collect US for PAIR Bulk data query
                if (origPub.getString("CC").equals("US")) {
                    JSONArray pairBulk = smallCopy.optJSONArray("PAIR Bulk Data");
                    if (pairBulk!=null) {
                        pairBulk.put(origPub.getString("PN"));
                    }
//                    System.out.println(origPub.getString("PN") + " for PAIR Bulk Data");
                }
            }
            annotatedFamilies.put(smallCopy.getString("Family"), smallCopy);
//                    put(smallCopy);
        }
        
        
        // Get the legal status
System.out.println("now to LEGAL STATUS!");
        Set<String> keys = annotatedFamilies.keySet();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            String familyID = it.next();
            JSONObject patbaseFamily = annotatedFamilies.get(familyID);
            
            JSONObject jOb = PatbaseRestApi.runMethod((PatbaseRestApi.GETFAMILYLS), 
                    new BasicNameValuePair("family", familyID));
//            System.out.println(jOb.toString(2));
            JSONArray legStati = jOb.getJSONArray("LegalStatus");
            //all legal status objects for this family
            for (int k=0; k<legStati.length(); k++) {
                JSONObject legStatus = legStati.getJSONObject(k);
                
                if ((legStatus.getString("PRSDate").compareTo(params[1]))>=0 &&
                        (legStatus.getString("PRSDate").compareTo(params[2]))<=0) {
                    patbaseFamily.getJSONArray("New Legal Status").put(legStatus);
                }
            }

System.out.println(familyID + ": " + patbaseFamily.toString(2));
//Set<String> keys = annotatedFamilies.keySet();
//for (String key -> annotatedFamilies) {
//            
//        }
            
//            System.out.println(patbaseFamily.getString("Family") + " :: "
//                    + patbaseFamily.getString("Title"));
//            System.out.println("New Publications:  " + patbaseFamily.getJSONArray("New Publications")
//                    .toString(2));
//            System.out.println("New Legal Status:  " + patbaseFamily.getJSONArray("New Legal Status")
//                    .toString(2));
        }
    }
}
