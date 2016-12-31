package patmob.patbase.monitor;

import javax.swing.JTextArea;
import org.json.JSONObject;

/**
 *
 * @author Piotr
 */
public class MonitorFilterViewer implements FilterViewer {
    JTextArea newLegalStatusTextArea, 
            newPublicationsTextArea,
            pairBulkDataTextArea,
            rawJsonTextArea;
    
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
        rawJsonTextArea.setText(oj.toString(1));
//        System.out.println(oj.toString(1).substring(0, 100));
        return displayed;
    }
    
}
