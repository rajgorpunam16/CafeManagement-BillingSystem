/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package pro;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *
 * @author ADMIN
 */
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;


public class delitem extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(delitem.class.getName());

    /**
     * Creates new form delitem
     */
    public delitem() {
    initComponents();
        loadItemsIntoTable();
        jTable1.setDefaultRenderer(Object.class, new CategoryColorRenderer());

        // IMAGE PREVIEW ON CLICK
        jTable1.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                int row = jTable1.rowAtPoint(evt.getPoint());
                int col = jTable1.columnAtPoint(evt.getPoint());
                if (col == 3 && row != -1) {
                    String path = getImagePath(row);
                    if (path != null && !path.isEmpty()) {
                        ImageIcon original = new ImageIcon(path);
                        Image img = original.getImage().getScaledInstance(500, -1, Image.SCALE_SMOOTH);
                        JLabel label = new JLabel(new ImageIcon(img));
                        JScrollPane scroll = new JScrollPane(label);
                        scroll.setPreferredSize(new Dimension(520, 350));
                        JOptionPane.showMessageDialog(null, scroll, "Menu Item Image", JOptionPane.PLAIN_MESSAGE);
                    }
                }
            }
        });}
   

public class CategoryColorRenderer extends DefaultTableCellRenderer {
      @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setIcon(null);

            if (column == 3) {
                setHorizontalAlignment(CENTER);
                setText("");
                if (value instanceof Icon) setIcon((Icon) value);
                setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                return this;
            }

            if (isSelected) {
                setBackground(Color.BLACK);
                setForeground(Color.WHITE);
                return this;
            }

            String category = (String) table.getValueAt(row, 2);
            switch (category.toLowerCase()) {
                case "drinks": setBackground(Color.PINK); break;
                case "snacks": setBackground(Color.ORANGE); break;
                case "desserts": setBackground(Color.RED); break;
                default: setBackground(Color.WHITE);
            }
            setForeground(Color.BLACK);
            return this;
        }
}
    private void loadItemsIntoTable() {
      try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT item_name, price, category, image_path FROM menu_items";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            DefaultTableModel model = new DefaultTableModel(
                    new String[]{"Item Name", "Price ", "Category", "Image", "Path"}, 0) {
                public Class<?> getColumnClass(int column) { return column == 3 ? Icon.class : String.class; }
                public boolean isCellEditable(int r, int c) { return false; }
            };

            while (rs.next()) {
                String name = rs.getString("item_name");
                String price = rs.getString("price");
                String category = rs.getString("category");
                String path = rs.getString("image_path");

                ImageIcon icon = null;
                if (path != null && !path.isEmpty()) {
                    icon = new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(60, 40, Image.SCALE_SMOOTH));
                }
                model.addRow(new Object[]{name, price, category, icon, path});
            }

            jTable1.setModel(model);
            jTable1.setRowHeight(60);
            jTable1.getColumnModel().getColumn(4).setMinWidth(0);
            jTable1.getColumnModel().getColumn(4).setMaxWidth(0);
            jTable1.getColumnModel().getColumn(4).setWidth(0);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading items: " + e.getMessage());
        }
}

    private String getImagePath(int row) {
        return (String) jTable1.getValueAt(row, 4);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(jTable1);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 170, 1060, 410));

        jButton1.setBackground(new java.awt.Color(255, 204, 255));
        jButton1.setFont(new java.awt.Font("Serif", 1, 24)); // NOI18N
        jButton1.setText("Remove Item");
        jButton1.addActionListener(this::jButton1ActionPerformed);
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 600, 240, 60));

        jLabel1.setFont(new java.awt.Font("Monotype Corsiva", 1, 100)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Delete Menu Item");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 20, -1, -1));

        jLabel2.setFont(new java.awt.Font("Serif", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 204, 255));
        jLabel2.setText("Choose an item below to remove from the menu");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 120, -1, -1));

        jButton3.setBackground(java.awt.Color.red);
        jButton3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton3.setForeground(new java.awt.Color(255, 255, 255));
        jButton3.setText("X");
        jButton3.addActionListener(this::jButton3ActionPerformed);
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(1260, 30, 60, -1));

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pro/15.png"))); // NOI18N
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1400, 740));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
 int row = jTable1.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "No item selected. Please choose an item to remove.");
            return;
        }
        String itemName = (String) jTable1.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete " + itemName + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection con = DBConnection.getConnection()) {
                PreparedStatement pst = con.prepareStatement("DELETE FROM menu_items WHERE item_name=?");
                pst.setString(1, itemName);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Menu item deleted!");
                loadItemsIntoTable();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Could not delete the item.Error: " + e.getMessage());
            }
        }
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
               this.dispose(); 
               new waitsec().setVisible(true);        // TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed

    /**
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
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new delitem().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
