package patmob.patbase.table;

import java.awt.Color;
import patmob.patbase.*;
import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.TreeSet;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import patmob.convert.PNFormat;
import patmob.core.PatmobPlugin;
import patmob.core.TreeBranchEditor_2;
import patmob.data.PatentCollectionList;
import patmob.data.PatentCollectionMap;
import patmob.data.PatentDocument;
import patmob.data.PatentTreeNode;

/**
 * Dedicated table to display only alert results.
 * @author Piotr
 */
public class PatbaseAlertTableFrame extends javax.swing.JFrame {
    TableModel tableModel = null;
    JSONObject alertResults = null;
    PatmobPlugin plugin;
    
    Highlighter hilit;
    Highlighter.HighlightPainter painter;
    TableRowSorter<TableModel> sorter;

    public PatbaseAlertTableFrame(JSONObject result, PatmobPlugin p) {
        plugin = p;
        alertResults = result;
        tableModel = getCustomModel(alertResults);
        initComponents();
//        jTable1.setAutoCreateRowSorter(true);
        jTable1.getColumn("Select").setMaxWidth(45);
        jTable1.getColumn("Select").setResizable(false);
        
        // display detail about the selected modelRow
        jTable1.getSelectionModel()
                .addListSelectionListener(new AlertSelectionListener());
        
        // update data if user edited project name
        // Java 8
//        jTable1.getModel().addTableModelListener((TableModelEvent e) -> {
//            int modelRow = e.getFirstRow();
//            int column = e.getColumn();
//            TableModel model = (TableModel)e.getSource();
//            String columnName = model.getColumnName(column);
//            Object newData = model.getValueAt(modelRow, column);
//            if (columnName.equals("Project")) {
//                JSONObject pbFam = 
//                        alertResults.getJSONArray("Families").getJSONObject(modelRow);
//                pbFam.put("ProjectName", newData);
//            }
//        });
        
        // Java 7
//        jTable1.getModel().addTableModelListener(new TableModelListener() {
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int modelRow = e.getFirstRow();
                int column = e.getColumn();
                TableModel model = (TableModel)e.getSource();
                String columnName = model.getColumnName(column);
                Object newData = model.getValueAt(modelRow, column);
                if (columnName.equals("Project")) {
                    JSONObject pbFam = 
                            alertResults.getJSONArray("Families").getJSONObject(modelRow);
                    pbFam.put("ProjectName", newData);
                }
            }
        });
        
        // find in biblio/claims
        hilit = new DefaultHighlighter();
        painter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
        jEditorPane1.setHighlighter(hilit);
        
        //filter in table
        sorter = new TableRowSorter<>(tableModel);
        jTable1.setRowSorter(sorter);
    }
    
    private class AlertSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                int viewRow = jTable1.getSelectedRow();
                if (viewRow>=0) {
                    int modelRow = jTable1.convertRowIndexToModel(viewRow);
                    JSONObject pbFam = alertResults
                            .getJSONArray("Families").getJSONObject(modelRow);
                    JSONObject ueMember = pbFam.getJSONObject("UpdateMember");
                    showInfo(pbFam, ueMember);                    
                } else {        // selected row hidden by filter?
                    jEditorPane1.setText("");                   
                }
            }
        }

        void showInfo(JSONObject pbFam, JSONObject ueMember) {
            // do not scroll to the end of text in JEditorPane
            final DefaultCaret caret = (DefaultCaret) jEditorPane1.getCaret();
            caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
            
            String uePN = pbFam.getString("uePatentNumber");
            String info = 
                    "<html>" +
                    "<b>PatBase Family</b>: " + pbFam.getString("Family") + "<br>" + 
                    "<b>Basic Publication</b>: " + pbFam.getString("PatentNumber") + "<br>" + 
                    "<b>Number of Family Members</b>: " + pbFam.getInt("mCount") + "<br>" + 
                    "<b>Probable Assignee</b>: " + pbFam.getString("ProbableAssignee") + "<br>" + 
//                    "<b>First Inventor</b>: " + pbFam.getString("FirstInventor") + "<br>" + 
                    "<b>Title</b>: " + enTitle(pbFam) + "<br>" + 
                    "<b>Abstract</b>: " + enAbstract(pbFam) + "<br>" + 
                    "<hr><p/>" + 
                    
                    "<b>PN: <span style=\"color:" + getColor(uePN) + "\">" + uePN + "</span></b><br>" + 
                    "<b>PD</b>: " + pbFam.getString("PD") + "<br>" + 
                    "<b>REG</b>: " + ueMember.optString("REG") + "<br>" + 
                    "<b>PA</b>: " + ueMember.optString("PA") + "<br>" + 
                    "<b>TI</b>: " + ueMember.optString("TI") + "<br>" + 
                    "<b>AB</b>: " + ueMember.optString("AB") + "<br>" + 
                    "<b>INV</b>: " + ueMember.optString("INV") + "<br>" + 
                    "<b>IMG</b>: " + ueMember.optString("IMG") + "<br>" + 
                    "<b>CL</b>: " + ueMember.optString("CL") + 
                    "</html>";
            jEditorPane1.setText(info);
            findFieldActionPerformed(null);
        }
        
        /**
         * HTML color of update publication number, depending on kind code.
         * @param uePN
         * @return 
         */
        String getColor(String uePN) {
            String[] pn = uePN.split(" ");
            if (pn[0].startsWith("WO")) {
                if (pn[1].equals("A1")||pn[1].equals("A2")) {
                    return "red";
                } else
                    return "blue";
            } else if (pn[0].startsWith("EP")) {
                if (pn[1].equals("A1")||pn[1].equals("A2")||pn[1].equals("B1")||pn[1].equals("B2")) {
                    return "red";
                } else
                    return "blue";
            } else 
                return "red";
        }
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
        Object[][] data = new Object[famArray.length()][];
        for (int i=0; i<famArray.length(); i++) {
            JSONObject o = famArray.getJSONObject(i);
            data[i] = new Object[]{
                false,
                o.getString("ProjectName"),
                o.getString("uePatentNumber"),
                o.getString("PD"),
                o.getString("Title"),
                o.getString("ProbableAssignee")
//                o.getInt("mCount")
            };
        }
        Object[] colNames = new Object[]{
            "Select",
            "Project",
            "PatentNumber",
            "Date",
            "Title",
            "ProbableAssignee"
//            "Count"
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
        jEditorPane1 = new javax.swing.JEditorPane();
        patOfficeButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        findField = new javax.swing.JTextField();
        foundLabel = new javax.swing.JLabel();
        filterLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        filterField = new javax.swing.JTextField();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        saveMenuItem = new javax.swing.JMenuItem();
        writeMenuItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        selectMenuItem = new javax.swing.JMenuItem();
        deselectMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        treeMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("PatBase Alert Table");

        jSplitPane1.setDividerLocation(150);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jTable1.setModel(tableModel);
        jScrollPane1.setViewportView(jTable1);

        jSplitPane1.setTopComponent(jScrollPane1);

        pbExpressButton.setText("PatBase Express");
        pbExpressButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pbExpressButtonActionPerformed(evt);
            }
        });

        jEditorPane1.setEditable(false);
        jEditorPane1.setContentType("text/html");
        jScrollPane2.setViewportView(jEditorPane1);

        patOfficeButton.setText("Patent Office");
        patOfficeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                patOfficeButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("FIND");

        findField.setToolTipText("");
        findField.setPreferredSize(new java.awt.Dimension(60, 20));
        findField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findFieldActionPerformed(evt);
            }
        });

        foundLabel.setText("0");
        foundLabel.setToolTipText("");
        foundLabel.setOpaque(true);
        foundLabel.setPreferredSize(new java.awt.Dimension(20, 14));

        filterLabel.setBackground(new java.awt.Color(255, 255, 255));
        filterLabel.setText("0");
        filterLabel.setToolTipText("");
        filterLabel.setPreferredSize(new java.awt.Dimension(60, 14));

        jLabel2.setText("FILTER");

        filterField.setPreferredSize(new java.awt.Dimension(60, 20));
        filterField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 43, Short.MAX_VALUE)
                .addComponent(pbExpressButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(patOfficeButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 67, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(findField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(foundLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pbExpressButton)
                    .addComponent(patOfficeButton)
                    .addComponent(jLabel1)
                    .addComponent(findField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(foundLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filterLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(filterField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jSplitPane1.setRightComponent(jPanel1);

        jMenu1.setText("File");

        saveMenuItem.setText("Save to JSON File...");
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(saveMenuItem);

        writeMenuItem.setText("Write to HTML Table File...");
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
        jMenu2.add(jSeparator1);

        treeMenuItem.setText("Convert to Tree");
        treeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                treeMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(treeMenuItem);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 606, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void pbExpressButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pbExpressButtonActionPerformed
        showURL("http://www.patbase.com/express/default.asp?saction=P-" + getPN());
    }//GEN-LAST:event_pbExpressButtonActionPerformed

    private void showURL(String url) {
        try {
            Desktop.getDesktop().browse(new URL(url).toURI());
        } catch (Exception ex) {System.out.println("showURL: " + ex);}        
    }
    private String getPN() {
        int viewRow = jTable1.getSelectedRow();
        final int modelRow = jTable1.convertRowIndexToModel(viewRow);
        JSONObject pbFam = 
                alertResults.getJSONArray("Families").getJSONObject(modelRow);
        String pn = pbFam.getString("uePatentNumber");
        if (pn.contains(" ")) {
            pn = pn.substring(0, pn.indexOf(" "));
        } else {
            if (Character.isLetter(pn.charAt(pn.length()-2))) {
                pn = pn.substring(0, pn.length()-2);
            } else if (Character.isLetter(pn.charAt(pn.length()-1))) {
                pn = pn.substring(0, pn.length()-1);
            }
        }
        return pn;
    }
    
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
    
    /**
     * 
     * @param columnName
     * @return -1 if not found
     */
    private int getColumnIndex(String columnName) {
        int colIndex = -1;
        for (int i=0; i<jTable1.getColumnCount(); i++) {
            if (jTable1.getColumnName(i).equals(columnName)) {
                colIndex = i;
                break;
            }
        }
        return colIndex;
    }
    
    /**
     * 
     * @return Selected rows grouped by project. Length = 0 if none selected.
     */
    JSONObject getSelectedProjects() {
        JSONObject projects = new JSONObject();
        int selColIndex = getColumnIndex("Select");
        for (int j=0; j<jTable1.getRowCount(); j++) {
            if ((boolean)jTable1.getValueAt(j,selColIndex)==true) {
                int modelRow = jTable1.convertRowIndexToModel(j);
                JSONObject pbFam = alertResults.getJSONArray("Families")
                        .getJSONObject(modelRow);
                projects.append(pbFam.optString("ProjectName"), pbFam);
            }
        }
        return projects;
    }
    
    String blue = "#9999FF",
            red = "#FF9999",
            projectHilite;
    
    void writeProject(JSONArray project, BufferedWriter bw) throws IOException {
        if (projectHilite.equals(red)) {
            projectHilite = blue;
        } else {
            projectHilite = red;
        }
        for (int i=0; i<project.length(); i++) {
            JSONObject pbFam = project.getJSONObject(i);
            bw.write(getTableRow(pbFam, projectHilite));
            bw.flush();
        }
    }
    
    private void writeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_writeMenuItemActionPerformed
        String headColor = "#FFD700";
        String tableTitle = "TABLE TITLE";
        projectHilite = red;
//        int selColIndex = getColumnIndex("Select");
        
        //group selected rows by project
        JSONObject selectedProjects = getSelectedProjects();
        if (selectedProjects.length()==0) {
            JOptionPane.showMessageDialog(rootPane, "No rows selected.");
        } else {
            JFileChooser fc = new JFileChooser();
            int i = fc.showSaveDialog(null);
            if (i==JFileChooser.APPROVE_OPTION) {
                try {
                    Iterator<String> it = selectedProjects.keys();
                    //sort projects
                    TreeSet<String> sortedProjects = new TreeSet();
                    while (it.hasNext()) {
                        sortedProjects.add(it.next());
                    }
                    it = sortedProjects.iterator();
                    
                    BufferedWriter bw = new BufferedWriter(new FileWriter(
                            fc.getSelectedFile()));
                    bw.write(
                            "<!DOCTYPE html><html>" +
                            "<head><style>" +
                            "table {border-collapse:collapse; width:100%;}" +
                            "tr {vertical-align:top;}" +
                            "td {border-bottom:1px solid #ddd;}" +
                            "</style></head>" +
                            "<body><table>" +
                            "<tr style=\"text-align:center; background-color:" + headColor + "\">" +
                            "<th colspan=\"7\">" + tableTitle + "</th>" + 
                            "</tr>" +
                            "<tr style=\"text-align:center; background-color:" + headColor + "\">" +
                            "<th style=\"width:80px\">Project</th>" +
                            "<th style=\"width:120px\">Publication</th>" +
                            "<th style=\"width:100px\">Date</th>" +
                            "<th style=\"width:200px\">Title</th>" +
                            "<th style=\"width:150px\">Assignee</th>" +
                            "<th colspan=\"2\">Abstract</th>" +
                            "<th style=\"width:150px\">Inventors</th>" +
                            "</tr>");
                    bw.flush();

                    while (it.hasNext()) {
                        writeProject(selectedProjects.getJSONArray(it.next()), bw);
                    }
                    
                    bw.write("</table></body></html>");
                    bw.close();
                } catch (Exception x) {System.out.println("PatmobDesktop.saveNodeToTextFile" + x);}
            }
        }
    }//GEN-LAST:event_writeMenuItemActionPerformed

    
    static void tryIt() {
        String headColor = "#FFD700";
        String tableTitle = "Patent Alert 2016 week 1";
//        int selColIndex = getColumnIndex("Select");
        JFileChooser fc = new JFileChooser();
        int i = fc.showSaveDialog(null);
        if (i==JFileChooser.APPROVE_OPTION) {
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(
                        fc.getSelectedFile()));
                bw.write(
                        "<!DOCTYPE html><html>" +
                        "<head><style>" +
                        "table {border-collapse:collapse; width:100%;}" +
                        "tr {vertical-align:top;}" +
                        "td {border-bottom:1px solid #ddd;}" +
                        "</style></head>" +
                        "<body><table>" +
                        "<tr style=\"text-align:center; background-color:" + headColor + "\">" +
                        "<th colspan=\"7\"><h3>" + tableTitle + "</h3></th>" + 
                        "</tr>" +
                        "<tr style=\"text-align:center; background-color:" + headColor + "\">" +
                        "<th style=\"width:80px\">Project</th>" +
                        "<th style=\"width:120px\">Publication</th>" +
                        "<th style=\"width:100px\">Date</th>" +
                        "<th style=\"width:200px\">Title</th>" +
                        "<th style=\"width:150px\">Assignee</th>" +
                        "<th colspan=\"2\">Abstract</th>" +
                        "</tr>");
                bw.flush();
                bw.write(
                        "<tr>" +
                        "<td style=\"background-color:blue\">" + "IRAK" + "</td>" +
                        "<td><a href=\"http://www.patbase.com/express/default.asp?saction=P-US9212190\" target=\"pbView\">" + "US9212190 BB" + "</a></td>" +
                        "<td>20151215</td>" + 
                        "<td>IRAK INHIBITORS AND USES THEREOF</td>" + 
                        "<td>NIMBUS IRIS INC</td>" + 
                        "<td style=\"width:200px\"><img src=\"https://www.patbase.com/getimg/ftimg.asp?id=77996770\" width=200px></td>" + 
                        "<td style=\"width:650px\">The present invention provides compounds, compositions thereof, and methods of using the same.</td>" +  
                        "</tr>");
                bw.flush();
                bw.write("</table></body></html>");
                bw.close();
            } catch (Exception x) {System.out.println("PatmobDesktop.saveNodeToTextFile" + x);}
        }
    }
    public static void main(String args[]) {
        tryIt();
    }
    
    String getTableRow(JSONObject pbFam, String projectHilite) {
        JSONObject ueMember = pbFam.getJSONObject("UpdateMember");
        String uePN = pbFam.optString("uePatentNumber");
        String[] pn = uePN.split(" ");
        String row = 
                "<tr>" +
                "<td style=\"background-color:" + projectHilite + "\">" + pbFam.optString("ProjectName") + "</td>" +
                "<td><a href=\"http://www.patbase.com/express/default.asp?saction=P-" + pn[0] + "\" target=\"pbView\">" + uePN + "</a></td>" +
                "<td>" + parseDate(pbFam.optString("PD")) + "</td>" + 
                "<td>" + parseMemberString(ueMember.optString("TI")) + "</td>" + 
                "<td>" + parseMemberString(ueMember.optString("PA")) + "</td>" + 
                "<td style=\"width:200px\">" + parseMemberString(ueMember.optString("IMG"))
                        .replace(">", " width=200px>") + "</td>" +              // img tag in the table displays full-size image
                "<td style=\"width:650px\">" + parseMemberString(ueMember.optString("AB")) + "</td>" +  
                "<td>" + ueMember.optString("INV") + "</td>" +
                "</tr>";
        return row;
    }
    
    String parseDate(String s) {
        return s.substring(0,4) + "-" + s.substring(4,6) + "-" + s.substring(6,8);
    }
    
    String parseMemberString(String s) {
        if (s.contains(" :: ")) {
            String[] a = s.split(" :: ");
            for (String a1 : a) {
                if (!a1.equals("")) {
                    return a1;
                }
            }
        }
        return s;
    }
    
    private void deselectMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deselectMenuItemActionPerformed
        setCheckboxes(false);
    }//GEN-LAST:event_deselectMenuItemActionPerformed

    private void patOfficeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_patOfficeButtonActionPerformed
        String pn = getPN();
        PatentDocument pd = new PatentDocument(pn);
        switch (pd.getCountry()) {
            case "US":
                pn = PNFormat.getPN(pd, PNFormat.USPTO);
                if (pn.length()==7) {
                    showURL("http://patft1.uspto.gov/netacgi/nph-Parser?patentnumber=" + pn);
                } else {
                    showURL("http://appft.uspto.gov/netacgi/nph-Parser?Sect1=PTO1"
                            + "&Sect2=HITOFF&d=PG01&p=1&u=%2Fnetahtml%2FPTO%2Fsrchnum.html"
                            + "&r=1&f=G&l=50&s1=%22" + pn + "%22.PGNR.");
                }
                break;
            default:
                pn = PNFormat.getPN(pd, PNFormat.EPO);
                if (pd.getCountry().equals("WO")) {
                    showURL("https://patentscope.wipo.int/search/en/detail.jsf?docId=WO" + pn);
                } else {
                    showURL("http://worldwide.espacenet.com/publicationDetails/biblio?CC="
                            + pd.getCountry() + "&NR=" + pn);
                }
        }
    }//GEN-LAST:event_patOfficeButtonActionPerformed

    private PatentTreeNode getTreeNode() {
        PatentCollectionMap rootNode = new PatentCollectionMap();
        int selColIndex = getColumnIndex("Select");
        for (int j=0; j<jTable1.getRowCount(); j++) {
            if ((boolean)jTable1.getValueAt(j,selColIndex)==true) {
                int modelRow = jTable1.convertRowIndexToModel(j);
                JSONObject pbFam = alertResults.getJSONArray("Families")
                        .getJSONObject(modelRow);
                String projectName = pbFam.getString("ProjectName");
                PatentCollectionList projectNode;
                if (rootNode.containsKey(projectName)) {
                    projectNode = (PatentCollectionList) rootNode.get(projectName);
                } else {
                    projectNode = new PatentCollectionList(projectName);
                    rootNode.addChild(projectNode);
                }
                String pn = pbFam.getString("uePatentNumber");
                PatentDocument doc = new PatentDocument(pn);
                projectNode.addChild(doc);
            }
        }
        if (rootNode.size()==0) rootNode = null;
        return rootNode;
    }
        
    private void treeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_treeMenuItemActionPerformed
        final PatentTreeNode rootNode = getTreeNode();
        if (rootNode==null) {
            JOptionPane.showMessageDialog(rootPane, "No rows selected.");
        } else {
            // Java 8
//            java.awt.EventQueue.invokeLater(() -> {
//                new TreeBranchEditor_2(rootNode, 
//                        PatbaseRestPlugin.coreAccess.getController())
//                        .setVisible(true);
//            });
            
            // Java 7
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new TreeBranchEditor_2(rootNode, 
                            PatbaseRestPlugin.coreAccess.getController())
                            .setVisible(true);
                }
            });
        }
    }//GEN-LAST:event_treeMenuItemActionPerformed

    private void selectMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectMenuItemActionPerformed
        setCheckboxes(true);
    }//GEN-LAST:event_selectMenuItemActionPerformed

    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
            JFileChooser fc = new JFileChooser();
            int i = fc.showSaveDialog(null);
            if (i==JFileChooser.APPROVE_OPTION) {
                try {
                    try (BufferedWriter bw = new BufferedWriter(new FileWriter(
                            fc.getSelectedFile()))) {
                        bw.write(alertResults.toString(2));
                    }
                } catch (IOException | JSONException x) {
                    System.out.println("PatbaseAlertTableFrame.JSON: " + x);
                }
            }
    }//GEN-LAST:event_saveMenuItemActionPerformed

    /**
     * Hilite the string typed in the field in the document. Not case-sensitive.
     * @param evt 
     */
    private void findFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findFieldActionPerformed

        hilit.removeAllHighlights();
        String searchString = findField.getText().toLowerCase();
        if (searchString.length()<1) return;
        
        String content = "";
        Document doc = jEditorPane1.getDocument();
        int len = doc.getLength();
        try {
            content = doc.getText(0, len).toLowerCase();
        } catch (Exception x){
            System.out.println(x);
        }
        int end = 0, foundCount = 0;
        
        while (end<content.length()) {
            int index = content.indexOf(searchString, end);
            if (index>=0) {
                try {
                    end = index + searchString.length();
                    hilit.addHighlight(index, end, painter);
                    foundCount++;
                } catch (Exception x) {
                    System.out.println("hilit: " + x);
                }
            } else break;
        }
        foundLabel.setText(Integer.toString(foundCount));
    }//GEN-LAST:event_findFieldActionPerformed

    private void filterFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterFieldActionPerformed
        RowFilter<TableModel, Object> rf;
        //If current expression doesn't parse, don't update.
        try {
            rf = RowFilter.regexFilter("(?i)" + filterField.getText());
        } catch (java.util.regex.PatternSyntaxException e) {
            System.out.println("*** RowFilter ***");
            return;
        }
        sorter.setRowFilter(rf);
        filterLabel.setText(Integer.toString(jTable1.getRowCount())
                    + "/" + Integer.toString(tableModel.getRowCount()));
    }//GEN-LAST:event_filterFieldActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem deselectMenuItem;
    private javax.swing.JTextField filterField;
    private javax.swing.JLabel filterLabel;
    private javax.swing.JTextField findField;
    private javax.swing.JLabel foundLabel;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JButton patOfficeButton;
    private javax.swing.JButton pbExpressButton;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JMenuItem selectMenuItem;
    private javax.swing.JMenuItem treeMenuItem;
    private javax.swing.JMenuItem writeMenuItem;
    // End of variables declaration//GEN-END:variables
}
