/*
 * LogWin.java
 *
 * Created on 12 ???? 2007 ?., 11:49
 */

package jroastviewer;

/**
 *
 * @author  leo
 */
public class LogWin extends javax.swing.JFrame
{
    
    /** Creates new form LogWin */
    public LogWin()
    {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        CloseBut = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        dbgTextArea = new javax.swing.JTextArea();

        setTitle("RV Log window");
        CloseBut.setText("Close window");
        CloseBut.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                CloseButActionPerformed(evt);
            }
        });

        dbgTextArea.setColumns(20);
        dbgTextArea.setEditable(false);
        dbgTextArea.setRows(5);
        jScrollPane1.setViewportView(dbgTextArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 779, Short.MAX_VALUE)
                    .addComponent(CloseBut))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(CloseBut)
                .addContainerGap())
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void CloseButActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_CloseButActionPerformed
    {//GEN-HEADEREND:event_CloseButActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_CloseButActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton CloseBut;
    private javax.swing.JTextArea dbgTextArea;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
   
    javax.swing.JTextArea getDbgOut() { return dbgTextArea;};
    
}
