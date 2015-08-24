package patmob.patbase;

import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author nm54935
 */
public class PatbaseTableFrame extends javax.swing.JFrame {
    TableModel myModel = null;
    JSONObject myJOb = null;

    public PatbaseTableFrame(JSONObject patbaseSearchResults) {
        myJOb = patbaseSearchResults;
        myModel = getCustomModel(myJOb);
        initComponents();
        jTable1.setAutoCreateRowSorter(true);
        jTable1.getColumn("Select").setMaxWidth(45);
        jTable1.getColumn("Select").setResizable(false);
        
        jTable1.getSelectionModel().addListSelectionListener(
                // *** lambda expression ***
                (ListSelectionEvent event) -> {
            int viewRow = jTable1.getSelectedRow();
            final int modelRow = jTable1.convertRowIndexToModel(viewRow);
            JSONObject pbFam = 
                    myJOb.getJSONArray("Families").getJSONObject(modelRow);
            
            jTextArea1.setText(
                    "  Patent Number: " + pbFam.getString("PatentNumber") +
                    "\n  PatBase Family: " + pbFam.getString("Family") +
//                    "\n  Member Count: " + pbFam.getString("MemberCount") +
//                    "\n  Earliest Publication Date: " + pbFam.getString(
//                            "EarliestPubDate") +
//                    "\n  Back Citations: " + pbFam.getString("BackCitations") +
//                    "\n  Forward Citations: " + pbFam.getString("ForwardCitations") +
                            
                    "\n\n  Probable Assignee: " + pbFam.getString("ProbableAssignee") +
                    "\n  First Inventor: " + pbFam.getString("FirstInventor") +
                    "\n  Title: " + pbFam.getString("Title") +
                    "\n  Abstract: " + pbFam.getString("Abstract")
            );
        });        
    }
    
    private TableModel getCustomModel(JSONObject jObject) {
        JSONArray famArray = jObject.getJSONArray("Families");
        Object[][] data = new Object[famArray.length()][];
        for (int i=0; i<famArray.length(); i++) {
            JSONObject o = famArray.getJSONObject(i);
//System.out.println("*** RUN " + i + ": "+ o.toString(2));
            String projName;
            try{
            projName = o.getString("ProjectName");
            }catch (Exception x){
                System.out.println(x);
                projName = "NA";
            }
//            if (projName==null) projName = "NA";
//System.out.println(projName);
            data[i] = new Object[]{
                false,
                    projName,
                o.getString("Title"),
                o.getString("ProbableAssignee"),
                o.getString("PatentNumber"),
//                o.getString("EarliestPubDate"),
//                o.getString("Abstract")
            };
        }
        Object[] colNames = new Object[]{
            "Select",
                "Project",
            "Title",
            "ProbableAssignee",
            "PatentNumber",
//            "EarliestPubDate",
//            "Abstract"
        };
        return new DefaultTableModel(data, colNames){
            @Override
            public Class getColumnClass(int c) {
                return getValueAt(0, c).getClass();
            }
        };
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("PatBase Table");

        jSplitPane1.setDividerLocation(100);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jTable1.setModel(myModel);
        jScrollPane1.setViewportView(jTable1);

        jSplitPane1.setTopComponent(jScrollPane1);

        jButton1.setText("jButton1");

        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setWrapStyleWord(true);
        jScrollPane2.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jButton1)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1))
        );

        jSplitPane1.setRightComponent(jPanel1);

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Table");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables
}
