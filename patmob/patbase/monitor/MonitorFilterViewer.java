package patmob.patbase.monitor;

import javax.swing.JTextArea;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Piotr
 */
public class MonitorFilterViewer implements FilterViewer {
    JSONObject displayedFamily;
    JTextArea newLegalStatusTextArea, 
            newPublicationsTextArea,
            pairBulkDataTextArea,
            rawJsonTextArea;
    // DISPLAY CONTROLS
    String displayedCCs = "",
            dateFrom = "",
            dateTo = "";
    
    public void setDisplayedCCs(String s) {
        displayedCCs = s;
    }
    
    public void setDateFrom(String s) {
        dateFrom = s;
    }
    
    public void setDateTo(String s) {
        dateTo = s;
    }
    
    public MonitorFilterViewer(JTextArea legal, JTextArea pubs, 
            JTextArea pair, JTextArea raw) {
        newLegalStatusTextArea = legal;
        newPublicationsTextArea = pubs;
        pairBulkDataTextArea = pair;
        rawJsonTextArea = raw;
    }

    @Override
    public boolean display(JSONObject oj) {
        boolean displayed = false;
        displayedFamily = oj;
        try {
            rawJsonTextArea.setText(displayedFamily.toString(1));
            displayPublications();
            displayPairData();
        }catch (Exception x) {
            System.out.println("MonitorFilterViewer.display: " + x);
        }
        return displayed;
    }
    
    private void displayPairData() {
        pairBulkDataTextArea.setText(displayedFamily
                .getJSONObject("PairData").toString(1));
    }
    
    private void displayPublications() {
        JSONArray publicationsArray = displayedFamily
                .getJSONArray("Publications");
        
        if (displayedCCs.equals("") && dateFrom.equals("") && dateTo.equals("") ) {
            newPublicationsTextArea.setText(publicationsArray.toString(1));
        } else {
            JSONArray newPubsArray = new JSONArray();
            for (int i=0; i<publicationsArray.length(); i++) {
                if (!displayedCCs.equals("") &&
                        !displayedCCs.contains(publicationsArray.getJSONObject(i).getString("CC"))) {
                    continue;
                }
//System.out.println("dateFrom: " + dateFrom);
//System.out.println("!dateFrom.equals(\"\"): " + !dateFrom.equals(""));
//System.out.println("getString(\"PD\"): " + publicationsArray.getJSONObject(i).getString("PD"));
//boolean a = dateFrom.compareTo(publicationsArray.getJSONObject(i).getString("PD"))>0;
//System.out.println("dateFrom.compareTo(PD): " + a);
//System.out.println("\n***\n");
                if (!dateFrom.equals("") &&
                        dateFrom.compareTo(publicationsArray.getJSONObject(i).getString("PD"))>0) {
                    continue;
                }
//System.out.println("dateTo: " + dateTo);
//System.out.println("!dateTo.equals(\"\"): " + !dateTo.equals(""));
//System.out.println("getString(\"PD\"): " + publicationsArray.getJSONObject(i).getString("PD"));
//boolean b = dateTo.compareTo(publicationsArray.getJSONObject(i).getString("PD"))<0;
//System.out.println("dateTo.compareTo(PD): " + b);
//System.out.println("\n***\n");
                if (!dateTo.equals("") &&
                        dateTo.compareTo(publicationsArray.getJSONObject(i).getString("PD"))<0) {
                    continue;
                }
                newPubsArray.put(publicationsArray.getJSONObject(i));
            }
            newPublicationsTextArea.setText(newPubsArray.toString(1));
        }
    }
}
