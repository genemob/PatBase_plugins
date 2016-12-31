package patmob.patbase.monitor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import org.json.JSONArray;
import org.json.JSONObject;
import patmob.core.PatmobPlugin;
import patmob.patbase.PatbaseRestApi;

/**
 *
 * @author Piotr
 */
public class PatbaseMonitorPlugin implements PatmobPlugin {
    /**
     * monitorProject: JSONObject with a JSONArray in the field "Families"
     * JSONArray consists of JSONObjects{"Pub Number","Family","Keywords"}
     * from a tab-separated values txt file.
     */
    JSONObject monitorProject = null;
    /**
     * annotatedFamilies: full current family data from PatBase API.
     *  key: family id String
     *  value: PatBase patent family JSONObject created and 
     *         annotated by the AlertRunner thread
     */
    HashMap<String, JSONObject> annotatedFamilies = new HashMap();
    boolean stopAlerts = false;

    @Override
    public String getName() {
        return "PatBase Monitor";
    }

    @Override
    public void doJob() {
        if (!PatbaseRestApi.isInitialized) {
            System.out.println("monitor init: " + PatbaseRestApi.initialize(
                    coreAccess.getController().getPatmobProperty("patmobProxy"),
                    "patbase_api@sanofi.com", "4uhHab4Hz"));
        }
        showGui();
    }

   private void showGui() {
        TableModel model = getCustomModel();
        if (model == null) {
            System.out.println("PatbaseMonitorPlugin - NULL model");
            return;
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MonitorFrame_1(PatbaseMonitorPlugin.this, model)
                        .setVisible(true);
            }
        });
    }

    private TableModel getCustomModel() {
        //LOAD DATA FROM TAB SEPARATED TXT FILE
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == javax.swing.JFileChooser.APPROVE_OPTION) {
            // load this file in jTable1
            JSONArray families = new JSONArray();
            try {
                try (BufferedReader br = new BufferedReader(
                        new FileReader(chooser.getSelectedFile()))) {
                    String line;
                    monitorProject = new JSONObject();
                    monitorProject.put("Families", families);
                    while ((line = br.readLine()) != null) {
                        String[] row = line.split("\t");
                        monitorProject.getJSONArray("Families")
                                .put(new JSONObject()
                                        .put("Pub Number", row[0])
                                        .put("Family", row[1])
                                        .put("Keywords", row[2]));
                    }
                }
            } catch (Exception x) {
                System.out.println("PatbaseMonitorPlugin.getCustomModel: " + x);
            }
        }
        return new MonitorTableModel(monitorProject);
    }

    private class AlertRunner implements Runnable {
        String query;
        
        public AlertRunner(String q) {
            query = q;
        }
        
        @Override
        public void run() {
            JSONObject pResult = PatbaseRestApi.query(query,
                    PatbaseRestApi.SEARCHRESULTSBIB, "1", null, "2", "test");
            JSONArray origFamilies = pResult.getJSONArray("Families");
            for (int i=0; i<origFamilies.length(); i++) {
                JSONObject family = origFamilies.getJSONObject(i);
                annotatedFamilies.put(family.getString("Family"), family);
            }
        }
    }
    
    public void stopAlertRunner() {
        stopAlerts = true;
    }
    
    public void runAlert() {
        //set up alert thread
        StringBuilder sb = new StringBuilder("PN=(");
        for (int i=0; i<monitorProject.getJSONArray("Families").length(); i++) {
            sb.append(monitorProject.getJSONArray("Families")
                    .getJSONObject(i).getString("Pub Number"))
                    .append(" OR ");
        }
        String pQuery = "(" + sb.substring(0, sb.length()-4) + ")" + ")";
        System.out.println("pQuery: " + pQuery);
        //start alert thread
        AlertRunner alertRunner = new AlertRunner(pQuery);
        new Thread(alertRunner).start();
    }
}
