package patmob.patbase;

import patmob.core.PatmobPlugin;

/**
 *
 * @author Piotr
 */
public class MethodDemoPlugin implements PatmobPlugin {

    @Override
    public String getName() {
        return "PatBase REST API Methods Demo";
    }

    @Override
    public void doJob() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MethodDemo(coreAccess.getController()
                        .getPatmobProperty("patmobProxy")).setVisible(true);
            }
        });
    }
    
}
