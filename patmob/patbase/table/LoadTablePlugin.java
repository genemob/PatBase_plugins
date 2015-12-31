package patmob.patbase.table;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javax.swing.JFileChooser;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import patmob.core.PatmobPlugin;

/**
 *
 * @author Piotr
 */
public class LoadTablePlugin implements PatmobPlugin {

    @Override
    public String getName() {
        return "Load Table from JSON File...";
    }

    @Override
    public void doJob() {
        JFileChooser fc = new JFileChooser();
        int i = fc.showOpenDialog(fc);
        if (i==JFileChooser.APPROVE_OPTION) {
            try {
                FileInputStream fis = new FileInputStream(fc.getSelectedFile());
                JSONObject tableContent = new JSONObject(new JSONTokener(fis));
                java.awt.EventQueue.invokeLater(() -> {
                    new PatbaseAlertTableFrame(tableContent, LoadTablePlugin.this)
                            .setVisible(true);
                });
            } catch (FileNotFoundException | JSONException x) {
                System.out.println("LoadTablePlugin: " + x);
            }
        }
    }
    
}
