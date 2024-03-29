/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package patmob.patbase;

import patmob.plugin.patbase.PatBaseQueryResultFormat;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Piotr
 */
public class PatbaseQueryFrame extends javax.swing.JFrame {
    PatbaseRestPlugin parent;
//    PatBaseQueryResultFormat defaultFormat;

    /**
     * Creates new form PatBaseFrame
     * @param plugin
     */
    public PatbaseQueryFrame(PatbaseRestPlugin plugin) {
        parent = plugin;
        initComponents();
//        defaultFormat = new PatBaseQueryResultFormat();
//        afterDateTextField.setText(defaultFormat.afterDate());
//        selectCountriesTextField.setText(defaultFormat.selectCountries());
    }
    
    // ALERT LOG TextArea
    public void setLogText(String txt) {
        alertLogArea.setText(txt);
    }
    public String getLogText() {
        return alertLogArea.getText();
    }
    public void appendLogText(String txt) {
        alertLogArea.append(txt);
    }
    
    public void appendQueryLogText (String txt) {
        queryLogTextArea.append(txt + "\n");
    }

    // Current PatBase Week TextField
    public void setPbWeekField(String txt) {
        pbWeekField.setText(txt);
    }
    public String getPbWeekField() {
        return pbWeekField.getText();
    }

    // Update Cmd TextField
    public void setUpdateCmdField(String txt) {
        updateCmdField.setText(txt);
    }
    public String getUpdateCmdField() {
        return updateCmdField.getText();
    }

    // Query File TextField
    public void setQueryFileField(String txt) {
        queryFileField.setText(txt);
    }
    public String getQueryFileField() {
        return queryFileField.getText();
    }

    //PatBase Query tab
    public void setCmdText(String txt) {
        cmdTextArea.setText(txt);
    }
    public String getCmdText() {
        return cmdTextArea.getText();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        pbWeekField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        updateCmdField = new javax.swing.JTextField();
        queryFileButton = new javax.swing.JButton();
        queryFileField = new javax.swing.JTextField();
        runAlertsButton = new javax.swing.JButton();
        stopAlertsButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        alertLogArea = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        cmdTextArea = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        fromRecField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        toRecField = new javax.swing.JTextField();
        QueryRunButton = new javax.swing.JButton();
        QueryCancelButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        sortComboBox = new javax.swing.JComboBox();
        jScrollPane3 = new javax.swing.JScrollPane();
        queryLogTextArea = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("PatBase REST API");

        jLabel3.setText("Current PatBase Week");

        pbWeekField.setEditable(false);

        jLabel4.setText("Update Command");

        queryFileButton.setText("Query File");
        queryFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                queryFileButtonActionPerformed(evt);
            }
        });

        runAlertsButton.setText("Run Alerts");
        runAlertsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runAlertsButtonActionPerformed(evt);
            }
        });

        stopAlertsButton.setText("Stop");
        stopAlertsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopAlertsButtonActionPerformed(evt);
            }
        });

        alertLogArea.setColumns(20);
        alertLogArea.setLineWrap(true);
        alertLogArea.setRows(5);
        alertLogArea.setWrapStyleWord(true);
        jScrollPane2.setViewportView(alertLogArea);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pbWeekField))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(updateCmdField))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(queryFileButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(queryFileField))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(runAlertsButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(stopAlertsButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(pbWeekField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(updateCmdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(queryFileButton)
                    .addComponent(queryFileField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(runAlertsButton)
                    .addComponent(stopAlertsButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("PatBase Alerts", jPanel2);

        jPanel1.setName("PatBaseQueryPanel"); // NOI18N

        cmdTextArea.setColumns(20);
        cmdTextArea.setLineWrap(true);
        cmdTextArea.setRows(5);
        cmdTextArea.setToolTipText("Type PatBase Command");
        cmdTextArea.setWrapStyleWord(true);
        jScrollPane1.setViewportView(cmdTextArea);

        jLabel1.setText("From record:");

        fromRecField.setText("1");
        fromRecField.setPreferredSize(new java.awt.Dimension(30, 20));

        jLabel2.setText("To record:");

        toRecField.setText("10");
        toRecField.setPreferredSize(new java.awt.Dimension(30, 20));

        QueryRunButton.setText("Run");
        QueryRunButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                QueryRunButtonActionPerformed(evt);
            }
        });

        QueryCancelButton.setText("Cancel");

        jLabel5.setText("Sort:");

        sortComboBox.setMaximumRowCount(11);
        sortComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Priority Date Asc.", "Priority Date Desc.", "Assignee", "Inventor", "Publication Date Asc.", "Publication Date Desc.", "Patent number", "Most Cited", "Relevance", "Family Number Asc.", "Family Number Desc." }));
        sortComboBox.setSelectedIndex(1);
        sortComboBox.setToolTipText("");

        queryLogTextArea.setColumns(20);
        queryLogTextArea.setRows(5);
        jScrollPane3.setViewportView(queryLogTextArea);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fromRecField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(toRecField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sortComboBox, 0, 131, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(QueryRunButton)
                        .addGap(18, 18, 18)
                        .addComponent(QueryCancelButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(fromRecField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(toRecField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(sortComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(QueryRunButton)
                    .addComponent(QueryCancelButton))
                .addContainerGap())
        );

        jTabbedPane1.addTab("PatBase Query", jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void QueryRunButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_QueryRunButtonActionPerformed
//        parent.runQuery2AP(
        parent.runQuery(
                cmdTextArea.getText(),
                fromRecField.getText(),
                toRecField.getText(),
                Integer.toString(sortComboBox.getSelectedIndex() + 1));
//                "-1"); //, formatFromGUI());
    }//GEN-LAST:event_QueryRunButtonActionPerformed

//    private PatBaseQueryResultFormat formatFromGUI() {
//        String countries = null, date = null;
//        if (!allCountriesRadioButton.isSelected()) {
//            countries = selectCountriesTextField.getText();
//        }
//        if (!anyDateRadioButton.isSelected()) {
//            date = afterDateTextField.getText();
//        }
//        return new PatBaseQueryResultFormat(countries, date);
//    }
    
    private void stopAlertsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopAlertsButtonActionPerformed
        parent.stopAlertRunner();
    }//GEN-LAST:event_stopAlertsButtonActionPerformed

    private void queryFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_queryFileButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == javax.swing.JFileChooser.APPROVE_OPTION) {
            setQueryFileField(chooser.getSelectedFile().getPath());
        }
    }//GEN-LAST:event_queryFileButtonActionPerformed

    private void runAlertsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runAlertsButtonActionPerformed
        parent.runAlerts(getQueryFileField(), getUpdateCmdField());
    }//GEN-LAST:event_runAlertsButtonActionPerformed

    /*
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(PatbaseQueryFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PatbaseQueryFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PatbaseQueryFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PatbaseQueryFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PatbaseQueryFrame(null).setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton QueryCancelButton;
    private javax.swing.JButton QueryRunButton;
    private javax.swing.JTextArea alertLogArea;
    private javax.swing.JTextArea cmdTextArea;
    private javax.swing.JTextField fromRecField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField pbWeekField;
    private javax.swing.JButton queryFileButton;
    private javax.swing.JTextField queryFileField;
    private javax.swing.JTextArea queryLogTextArea;
    private javax.swing.JButton runAlertsButton;
    private javax.swing.JComboBox sortComboBox;
    private javax.swing.JButton stopAlertsButton;
    private javax.swing.JTextField toRecField;
    private javax.swing.JTextField updateCmdField;
    // End of variables declaration//GEN-END:variables
}
