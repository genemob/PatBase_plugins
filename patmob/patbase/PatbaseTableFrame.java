package patmob.patbase;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.URL;
import javax.swing.JFileChooser;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.json.JSONArray;
import org.json.JSONObject;

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
                    "\n\n  Probable Assignee: " + pbFam.getString("ProbableAssignee") +
                    "\n  First Inventor: " + pbFam.getString("FirstInventor") +
                    "\n  Title: " + enTitle(pbFam) +
                    "\n  Abstract: " + enAbstract(pbFam)
            );
        });        
    }
    
    private String enTitle(JSONObject o) {
        String title = o.getString("Title");
        if (title.contains("  ")) {
            title = title.substring(0, title.lastIndexOf("  "));
        }
        return title;
    }
    
    private String enAbstract(JSONObject o) {
        String abs = o.getString("Abstract");
        if (abs.contains("   ")) {
            abs = abs.substring(0, abs.lastIndexOf("   "));
        }
        return abs;
    }
    
    private TableModel getCustomModel(JSONObject jObject) {
        JSONArray famArray = jObject.getJSONArray("Families");
        switch (jObject.getString("ResultType")) {
            case PatbaseRestApi.SEARCHRESULTS:
                return getPlainModel(famArray);
            case PatbaseRestApi.SEARCHRESULTSBIB:
                return getBibModel(famArray);
            default:
                return null;
        }
    }
    
    private TableModel getPlainModel(JSONArray famArray) {
        Object[][] data = new Object[famArray.length()][];
        for (int i=0; i<famArray.length(); i++) {
            JSONObject o = famArray.getJSONObject(i);
            data[i] = new Object[]{
                false,
                o.getString("Title"),
                o.getString("ProbableAssignee"),
                o.getString("PatentNumber"),
                o.getString("EarliestPubDate"),
                Integer.parseInt(o.getString("MemberCount"))
            };
        }
        Object[] colNames = new Object[]{
            "Select",
            "Title",
            "ProbableAssignee",
            "PatentNumber",
            "EarliestPubDate",
            "MemberCount"
        };   
        return new DefaultTableModel(data, colNames){
            //override getColumnClass to render boolean as checkbox
            @Override
            public Class getColumnClass(int c) {
                return getValueAt(0, c).getClass();
            }
        };
    }

    private TableModel getBibModel(JSONArray famArray) {
        Object[][] data = new Object[famArray.length()][];
        for (int i=0; i<famArray.length(); i++) {
            JSONObject o = famArray.getJSONObject(i);
            data[i] = new Object[]{
                false,
                o.getString("ProjectName"),
                o.getString("Title"),
                o.getString("ProbableAssignee"),
                o.getString("PatentNumber"),
                o.getString("PD"),
                o.getInt("mCount")
            };
        }
        Object[] colNames = new Object[]{
            "Select",
            "Project",
            "Title",
            "ProbableAssignee",
            "PatentNumber",
            "Date",
            "Count"
        };   
        return new DefaultTableModel(data, colNames){
            //override getColumnClass to render boolean as checkbox
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
        pbExpressButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        saveMenuItem = new javax.swing.JMenuItem();
        writeMenuItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        selectMenuItem = new javax.swing.JMenuItem();
        deselectMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("PatBase Table");

        jSplitPane1.setDividerLocation(100);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jTable1.setModel(myModel);
        jScrollPane1.setViewportView(jTable1);

        jSplitPane1.setTopComponent(jScrollPane1);

        pbExpressButton.setText("PatBase Express");
        pbExpressButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pbExpressButtonActionPerformed(evt);
            }
        });

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
                .addComponent(pbExpressButton)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pbExpressButton))
        );

        jSplitPane1.setRightComponent(jPanel1);

        jMenu1.setText("File");

        saveMenuItem.setText("Save to Database...");
        saveMenuItem.setEnabled(false);
        jMenu1.add(saveMenuItem);

        writeMenuItem.setText("Write to File...");
        writeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                writeMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(writeMenuItem);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Table");

        selectMenuItem.setText("Select All");
        selectMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(selectMenuItem);

        deselectMenuItem.setText("Deselect All");
        deselectMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deselectMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(deselectMenuItem);

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

    private void pbExpressButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pbExpressButtonActionPerformed
        // TODO add your handling code here:
        int viewRow = jTable1.getSelectedRow();
        final int modelRow = jTable1.convertRowIndexToModel(viewRow);
        JSONObject pbFam = 
                myJOb.getJSONArray("Families").getJSONObject(modelRow);
        String pn = pbFam.getString("PatentNumber");
        if (pn.contains(" ")) {
            pn = pn.substring(0, pn.indexOf(" "));
        } else {
            if (Character.isLetter(pn.charAt(pn.length()-2))) {
                pn = pn.substring(0, pn.length()-2);
            } else if (Character.isLetter(pn.charAt(pn.length()-1))) {
                pn = pn.substring(0, pn.length()-1);
            }
        }
        String url = "http://www.patbase.com/express/default.asp?saction=P-" + pn;
        try {
            Desktop.getDesktop().browse(new URL(url).toURI());
        } catch (Exception ex) {System.out.println("PB Express: " + ex);}
    }//GEN-LAST:event_pbExpressButtonActionPerformed

    private void setCheckboxes(boolean sellection) {
        int selColIndex = 0;
        for (int s=0; s<jTable1.getColumnCount(); s++) {
            if (jTable1.getColumnName(s).equals("Select")) {
                selColIndex = s;
                break;
            }
        }
        for (int i=0; i<jTable1.getRowCount(); i++) {
            jTable1.setValueAt(sellection, i, selColIndex);
        }
    }
    
    private void writeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_writeMenuItemActionPerformed
        int selColIndex = 0;
        for (int s=0; s<jTable1.getColumnCount(); s++) {
//            System.out.println(jTable1.getColumnName(s));
            if (jTable1.getColumnName(s).equals("Select")) {
                selColIndex = s;
                break;
            }
        }
        
        JFileChooser fc = new JFileChooser();
        int i = fc.showSaveDialog(null);
        if (i==JFileChooser.APPROVE_OPTION) {
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(
                        fc.getSelectedFile()));
                bw.write(
                        "Patent Number" +
                        "\tPatBase Family" +
                        "\tProbable Assignee" +
                        "\tFirst Inventor" +
                        "\tTitle" +
                        "\tAbstract" + "\n");
                bw.flush();
//                writeChildren(bw, collection, "\t");
                for (int j=0; j<jTable1.getRowCount(); j++) {
                    if ((boolean)jTable1.getValueAt(j,selColIndex)==true) {
                        int modelRow = jTable1.convertRowIndexToModel(j);
                        JSONObject pbFam = myJOb.getJSONArray("Families")
                                .getJSONObject(modelRow);
                        bw.write(
                                pbFam.getString("PatentNumber") +
                                "\t" + pbFam.getString("Family") +
                                "\t" + pbFam.getString("ProbableAssignee") +
                                "\t" + pbFam.getString("FirstInventor") +
                                "\t" + enTitle(pbFam) +
                                "\t" + enAbstract(pbFam) + "\n"
                        );
                        bw.flush();
                    }
                }
                bw.close();
            } catch (Exception x) {System.out.println("PatmobDesktop.saveNodeToTextFile" + x);}
        }
        
//        for (int i=0; i<jTable1.getRowCount(); i++) {
////            System.out.println(jTable1.getValueAt(i, selColIndex));
//            
//            if ((boolean)jTable1.getValueAt(i,selColIndex)==true) {
//                int modelRow = jTable1.convertRowIndexToModel(i);
//                JSONObject pbFam = 
//                        myJOb.getJSONArray("Families").getJSONObject(modelRow);
//                System.out.println(pbFam.getString("PatentNumber") + " :: " +
//                        pbFam.getString("ProbableAssignee"));
//            }
//        }
    }//GEN-LAST:event_writeMenuItemActionPerformed

    private void selectMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectMenuItemActionPerformed
        setCheckboxes(true);
    }//GEN-LAST:event_selectMenuItemActionPerformed

    private void deselectMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deselectMenuItemActionPerformed
        setCheckboxes(false);
    }//GEN-LAST:event_deselectMenuItemActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem deselectMenuItem;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JButton pbExpressButton;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JMenuItem selectMenuItem;
    private javax.swing.JMenuItem writeMenuItem;
    // End of variables declaration//GEN-END:variables
}
