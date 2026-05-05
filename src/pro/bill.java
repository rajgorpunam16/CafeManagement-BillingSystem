/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package pro;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;
import javax.swing.SpinnerNumberModel;
import java.io.FileWriter;
import java.io.IOException;
/**
 *
 * @author ADMIN
 */
public class bill extends javax.swing.JFrame {
   private long currentBillID;

    /**
     * Creates new form bill
     */
    public bill() {
        initComponents();
        loadCategories();
         jSpinner1.setModel(new SpinnerNumberModel(1, 1, null, 1));

    }
    
private void saveBillToDatabase(String customer, long billID) {
    try (Connection con = DBConnection.getConnection()) {

        // 1️⃣ Calculate totals
        double subtotal = 0;
        for (int i = 0; i < jTable1.getRowCount(); i++) {
            subtotal += (double) jTable1.getValueAt(i, 4);
        }

        double gst = subtotal * 0.18;
        double grandTotal = subtotal + gst;

        LocalDateTime now = LocalDateTime.now();

        // 2️⃣ INSERT INTO bills (ONLY ONCE)
        String sqlBill = "INSERT INTO bills (bill_id, customer_name, gst, grand_total, bill_date) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pstBill = con.prepareStatement(sqlBill);

 pstBill.setLong(1, billID);
        pstBill.setString(2, customer);
        pstBill.setDouble(3, gst);
        pstBill.setDouble(4, grandTotal);
        pstBill.setTimestamp(5, java.sql.Timestamp.valueOf(now));

        pstBill.executeUpdate(); // ✅ single insert

        // 3️⃣ INSERT INTO bill_items (MULTIPLE ROWS)
      String sqlItems = "INSERT INTO bill_items (bill_id, item_name, category, quantity, price, row_total) VALUES (?, ?, ?, ?, ?, ?)";    PreparedStatement pstItems = con.prepareStatement(sqlItems);
for (int i = 0; i < jTable1.getRowCount(); i++) {

    String item = (String) jTable1.getValueAt(i, 0);
    String category = (String) jTable1.getValueAt(i, 1); // ✅ ADD THIS
    int qty = (int) jTable1.getValueAt(i, 2);
    double price = (double) jTable1.getValueAt(i, 3);
    double total = (double) jTable1.getValueAt(i, 4);

    pstItems.setLong(1, billID);
    pstItems.setString(2, item);
    pstItems.setString(3, category); // ✅ NEW
    pstItems.setInt(4, qty);
    pstItems.setDouble(5, price);
    pstItems.setDouble(6, total);

    pstItems.addBatch();
}

        pstItems.executeBatch(); // ✅ multiple insert

        javax.swing.JOptionPane.showMessageDialog(this, "Bill saved successfully!");

    } catch (Exception e) {
        javax.swing.JOptionPane.showMessageDialog(this, "Error saving bill: " + e.getMessage());
    }
}
    private void deleteSelectedItem() {
    // Get the table model
    javax.swing.table.DefaultTableModel model = 
        (javax.swing.table.DefaultTableModel) jTable1.getModel();

    // Get the selected row index
    int selectedRow = jTable1.getSelectedRow();

    if (selectedRow != -1) {
        // Confirm before deleting
        int confirm = javax.swing.JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this item?",
            "Confirm Deletion",
            javax.swing.JOptionPane.YES_NO_OPTION
        );

        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            // Remove the row from the model
            model.removeRow(selectedRow);

            javax.swing.JOptionPane.showMessageDialog(
                this,
                "Item deleted successfully!"
            );
        }
    } else {
        javax.swing.JOptionPane.showMessageDialog(
            this,
            "Please select an item to delete."
        );
    }
}

    private void loadCategories() {
    try (Connection con = DBConnection.getConnection()) {
        String sql = "SELECT DISTINCT category FROM menu_items";
        PreparedStatement pst = con.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        jComboBox1.removeAllItems();
        while (rs.next()) {
            jComboBox1.addItem(rs.getString("category"));
        }
    } catch (Exception e) {
        javax.swing.JOptionPane.showMessageDialog(this, "Error loading categories: " + e.getMessage());
    }
}
    private double getPriceFromDB(String itemName) {
    try (Connection con = DBConnection.getConnection()) {
        String sql = "SELECT price FROM menu_items WHERE item_name=?";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setString(1, itemName);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            return rs.getDouble("price");
        }
    } catch (Exception e) {
        javax.swing.JOptionPane.showMessageDialog(this, "Error fetching price: " + e.getMessage());
    }
    return 0;
}
    private void calculateTotals() {
    double subtotal = 0;
    for (int i = 0; i < jTable1.getRowCount(); i++) {
       subtotal += (double) jTable1.getValueAt(i, 4);
    }
    double gst = subtotal * 0.18; // 18% GST
    double grandTotal = subtotal + gst;

    jTextArea1.setText("Subtotal: " + subtotal +
                       "\nGST (18%): " + gst +
                       "\nGrand Total: " + grandTotal);
}
   private long generateBillID() {
    return System.currentTimeMillis(); // ✅ only number
}
    private String getCurrentDateTime() {
  DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    return dtf.format(LocalDateTime.now());
}
private void printReceipt(String customer, long billID) {
    StringBuilder receipt = new StringBuilder();
    String dateTime = getCurrentDateTime();
if (jTable1.getRowCount() == 0) {
    javax.swing.JOptionPane.showMessageDialog(this, "No items added!");
    return;
}
    receipt.append("********** RESTAURANT BILL **********\n");
   receipt.append("Bill ID   : BILL-").append(billID).append("\n");
    receipt.append("Date/Time : ").append(dateTime).append("\n");
    receipt.append("Customer  : ").append(customer).append("\n");
    receipt.append("-------------------------------------\n");
    receipt.append(String.format("%-15s %-8s %-8s %-8s\n", "Item", "Qty", "Price", "Total"));
    receipt.append("-------------------------------------\n");

    double subtotal = 0;
    for (int i = 0; i < jTable1.getRowCount(); i++) {
        String item = (String) jTable1.getValueAt(i, 0);
       int qty = (int) jTable1.getValueAt(i, 2);
double price = (double) jTable1.getValueAt(i, 3);
double total = (double) jTable1.getValueAt(i, 4);
        subtotal += total;
        receipt.append(String.format("%-15s %-8d %-8.2f %-8.2f\n", item, qty, price, total));
    }

    double gst = subtotal * 0.18;
    double grandTotal = subtotal + gst;

    receipt.append("-------------------------------------\n");
    receipt.append(String.format("Subtotal   : %.2f\n", subtotal));
    receipt.append(String.format("GST (18%%)  : %.2f\n", gst));
    receipt.append(String.format("Grand Total: %.2f\n", grandTotal));
    receipt.append("*************************************\n");
    receipt.append("   Thank you! Please visit again.\n");

    jTextArea1.setText(receipt.toString());
}


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox<>();
        jSpinner1 = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jButton6 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Baskerville Old Face", 1, 40)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Customer Name:");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 170, -1, -1));

        jTextField1.addActionListener(this::jTextField1ActionPerformed);
        getContentPane().add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 170, 190, 30));

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox1.addActionListener(this::jComboBox1ActionPerformed);
        getContentPane().add(jComboBox1, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 252, 190, 30));

        jLabel2.setFont(new java.awt.Font("Baskerville Old Face", 1, 40)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Select Category:");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 250, -1, -1));

        jLabel3.setFont(new java.awt.Font("Baskerville Old Face", 1, 40)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText(" Select Item Name:");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 340, -1, -1));

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox2.addActionListener(this::jComboBox2ActionPerformed);
        getContentPane().add(jComboBox2, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 342, 190, 30));
        getContentPane().add(jSpinner1, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 430, 120, 30));

        jLabel4.setFont(new java.awt.Font("Baskerville Old Face", 1, 40)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Quantity:");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 430, -1, -1));

        jButton1.setBackground(new java.awt.Color(255, 204, 255));
        jButton1.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        jButton1.setForeground(new java.awt.Color(51, 0, 0));
        jButton1.setText("Add Item");
        jButton1.addActionListener(this::jButton1ActionPerformed);
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 570, 110, 40));

        jButton2.setBackground(new java.awt.Color(255, 204, 255));
        jButton2.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        jButton2.setText("Clear");
        jButton2.addActionListener(this::jButton2ActionPerformed);
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 570, 110, 40));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String[]{"Item Name", "Category", "Quantity", "Price", "Total" }
        ));
        jScrollPane1.setViewportView(jTable1);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 160, 370, 340));

        jButton3.setBackground(java.awt.Color.red);
        jButton3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton3.setForeground(new java.awt.Color(255, 255, 255));
        jButton3.setText("X");
        jButton3.addActionListener(this::jButton3ActionPerformed);
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(1250, 20, 90, -1));

        jButton4.setBackground(new java.awt.Color(102, 0, 0));
        jButton4.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        jButton4.setForeground(new java.awt.Color(255, 255, 255));
        jButton4.setText("Generate Bill");
        jButton4.addActionListener(this::jButton4ActionPerformed);
        getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 570, 140, 40));

        jButton5.setBackground(new java.awt.Color(0, 0, 0));
        jButton5.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        jButton5.setForeground(new java.awt.Color(255, 204, 255));
        jButton5.setText("Export Receipt");
        jButton5.addActionListener(this::jButton5ActionPerformed);
        getContentPane().add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(1100, 570, 160, 40));

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane2.setViewportView(jTextArea1);

        getContentPane().add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(1000, 160, 320, 340));

        jButton6.setBackground(new java.awt.Color(255, 204, 255));
        jButton6.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        jButton6.setForeground(new java.awt.Color(0, 51, 0));
        jButton6.setText("Remove Item");
        jButton6.addActionListener(this::jButton6ActionPerformed);
        getContentPane().add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 570, -1, 40));

        jLabel5.setFont(new java.awt.Font("Monotype Corsiva", 1, 100)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 204, 255));
        jLabel5.setText("Billing Management System");
        getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 20, -1, -1));

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pro/17.png"))); // NOI18N
        getContentPane().add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
      
        String selectedCategory = (String) jComboBox1.getSelectedItem();
    if (selectedCategory != null) {
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT item_name FROM menu_items WHERE category=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, selectedCategory);
            ResultSet rs = pst.executeQuery();
            jComboBox2.removeAllItems();
            while (rs.next()) {
                jComboBox2.addItem(rs.getString("item_name"));
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error loading items: " + e.getMessage());
        }
    }
   // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    
    DefaultTableModel model = (DefaultTableModel) jTable1.getModel();

    // Correct fields
    String itemName = jComboBox2.getSelectedItem().toString(); // ✅ item
    String category = jComboBox1.getSelectedItem().toString(); // ✅ category
    int quantity = (Integer) jSpinner1.getValue();

    // Get price from DB (you already wrote this method 👍)
    double price = getPriceFromDB(itemName);

    if (itemName == null || itemName.isEmpty()) {
        javax.swing.JOptionPane.showMessageDialog(this, "Please select an item.");
        return;
    }

    // Calculate total
    double total = price * quantity;

    // Add row correctly
    model.addRow(new Object[]{
        itemName,     // column 1
        category,     // column 2
        quantity,     // column 3
        price,        // column 4
        total         // column 5
    });

    // Success message
    javax.swing.JOptionPane.showMessageDialog(this, "Item added successfully!");

    // Reset fields
    jSpinner1.setValue(1);
    jComboBox2.setSelectedIndex(0);

    // Update totals
    calculateTotals();

        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
     String customer = jTextField1.getText();

    // Generate ONE bill ID
   currentBillID = generateBillID();

// Pass numeric ID
printReceipt(customer, currentBillID);
saveBillToDatabase(customer, currentBillID);
     // TODO add your handling code here:
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox2ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
     try {
        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Save Receipt");

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();

            try (FileWriter writer = new FileWriter(fileToSave)) {
                writer.write("********** RESTAURANT RECEIPT **********\n");
                writer.write(String.format("Customer: %s\n", jTextField1.getText()));
                writer.write("Bill ID: BILL-" + currentBillID + "\n");
                writer.write("----------------------------------------\n");
                writer.write(String.format("%-15s %-10s %-5s %-10s %-10s\n",
                        "Item", "Category", "Qty", "Price", "Total"));
                writer.write("----------------------------------------\n");

                DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                double subtotal = 0;

                for (int i = 0; i < model.getRowCount(); i++) {
                    String item = model.getValueAt(i, 0).toString();
                    String category = model.getValueAt(i, 1).toString();
                    int qty = (Integer) model.getValueAt(i, 2);
                    double price = (Double) model.getValueAt(i, 3);
                    double total = (Double) model.getValueAt(i, 4);

                    subtotal += total;

                    writer.write(String.format("%-15s %-10s %-5d %-10.2f %-10.2f\n",
                            item, category, qty, price, total));
                }

                double gst = subtotal * 0.18; // 18% GST
                double grandTotal = subtotal + gst;

                writer.write("----------------------------------------\n");
                writer.write(String.format("Subtotal   : %.2f\n", subtotal));
                writer.write(String.format("GST (18%%)  : %.2f\n", gst));
                writer.write(String.format("Grand Total: %.2f\n", grandTotal));
                writer.write("***************************************\n");
                writer.write("       Thank you! Please visit again.\n");

                javax.swing.JOptionPane.showMessageDialog(this,
                        "Receipt exported successfully to: " + fileToSave.getAbsolutePath());
            }
        }
    } catch (IOException e) {
        javax.swing.JOptionPane.showMessageDialog(this,
                "Error exporting receipt: " + e.getMessage());
    }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
deleteSelectedItem();    // TODO add your handling code here:
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
  // Clear customer name
    jTextField1.setText("");

    // Clear table
    DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
    model.setRowCount(0); // removes all rows

    // Reset combo boxes and spinner
    jComboBox1.setSelectedIndex(0);
    jComboBox2.setSelectedIndex(0);
    jSpinner1.setValue(1);

    // Clear the receipt area
    jTextArea1.setText("");
    
    javax.swing.JOptionPane.showMessageDialog(this, "Form cleared!");        // TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
    this.dispose(); // closes current window
     new waitsec().setVisible(true); // if you have a main menu page
    
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
        java.awt.EventQueue.invokeLater(() -> new bill().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
