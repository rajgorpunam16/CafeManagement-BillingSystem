

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package pro;

import java.sql.*;
import java.awt.BorderLayout;
import net.proteanit.sql.DbUtils;

import org.jfree.chart.*;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import javax.swing.table.DefaultTableModel;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;

public class repo extends javax.swing.JFrame {
    String currentCondition = "1=1";
    private static final java.util.logging.Logger logger =
        java.util.logging.Logger.getLogger(repo.class.getName());
public repo() {

    initComponents();
    setLocationRelativeTo(null);

    loadTable("SELECT * FROM bills"); // ✅ SHOW ALL TABLE DATA
    loadData("all");                  // ✅ charts + summary (overall)
}public void loadStats(String condition) {
    try (Connection con = getConnection()) {

        String query = "SELECT COUNT(DISTINCT b.bill_id) AS total_sales, " +
                       "COUNT(DISTINCT b.customer_name) AS customers, " +
                       "SUM(b.grand_total) AS revenue, " +
                       "SUM(bi.quantity) AS products " +
                       "FROM bills b " +
                       "JOIN bill_items bi ON b.bill_id = bi.bill_id " +
                       "WHERE " + condition;

        PreparedStatement pst = con.prepareStatement(query);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {

            jLabel6.setText(rs.getString("total_sales")); // Total Sales
            jLabel9.setText(rs.getString("customers"));   // Customers

            // Handle NULL safely
            String revenue = rs.getString("revenue");
            String products = rs.getString("products");

            jLabel7.setText("₹ " + (revenue == null ? "0" : revenue)); // Revenue
            jLabel8.setText(products == null ? "0" : products);        // Products
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}

public Connection getConnection() {
    try {
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/cafe",
            "root",
            "root"   // 👉 PUT YOUR REAL PASSWORD
        );
    } catch (Exception e) {
        System.out.println(e);
        return null;
    }
}
public void showCategoryPieChart(String condition) {
    try (Connection con = getConnection()) {

        String query = "SELECT bi.category, SUM(bi.row_total) AS total " +
                       "FROM bill_items bi " +
                       "JOIN bills b ON bi.bill_id = b.bill_id " +
                       "WHERE " + condition + " " +
                       "GROUP BY bi.category";

        PreparedStatement pst = con.prepareStatement(query);
        ResultSet rs = pst.executeQuery();

        DefaultPieDataset dataset = new DefaultPieDataset();

        boolean hasData = false;

        while (rs.next()) {
            String category = rs.getString("category");
            double total = rs.getDouble("total");

            if (category != null && !category.trim().isEmpty()) {
                dataset.setValue(category, total);
                hasData = true;
            }
        }

        if (!hasData) {
            System.out.println("No category data found!");
            return;
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "Sales by Category", dataset, true, true, false);

        jPanel2.removeAll();
        jPanel2.setLayout(new BorderLayout());
        jPanel2.add(new ChartPanel(chart), BorderLayout.CENTER);
        jPanel2.revalidate();
        jPanel2.repaint();

    } catch (Exception e) {
        e.printStackTrace();
    }
}
public void showItemPieChart(String condition) {
    try (Connection con = getConnection()) {

        String query = "SELECT bi.item_name, SUM(bi.row_total) AS total " +
                       "FROM bill_items bi " +
                       "JOIN bills b ON bi.bill_id = b.bill_id " +
                       "WHERE " + condition + " " +
                       "GROUP BY bi.item_name";

        PreparedStatement pst = con.prepareStatement(query);
        ResultSet rs = pst.executeQuery();

        DefaultPieDataset dataset = new DefaultPieDataset();

        while (rs.next()) {
            String item = rs.getString("item_name");
            double total = rs.getDouble("total");

            if (item != null && !item.trim().isEmpty()) {
                dataset.setValue(item, total);
            }
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "Sales by Item", dataset, true, true, false);

        jPanel2.removeAll();
        jPanel2.setLayout(new BorderLayout());
        jPanel2.add(new ChartPanel(chart), BorderLayout.CENTER);
        jPanel2.revalidate();
        jPanel2.repaint();

    } catch (Exception e) {
        e.printStackTrace();
    }
}public void loadData(String type) {

    if (type.equals("today")) {
        currentCondition = "DATE(b.bill_date) = CURDATE()";
    } 
    else if (type.equals("week")) {
        currentCondition = "YEARWEEK(b.bill_date,1)=YEARWEEK(CURDATE(),1)";
    } 
    else if (type.equals("month")) {
        currentCondition = "MONTH(b.bill_date)=MONTH(CURDATE()) AND YEAR(b.bill_date)=YEAR(CURDATE())";
    } 
    else {
        currentCondition = "1=1";
    }

    // 🔥 ALL UPDATES HERE
    showBarChart(currentCondition);
    showCategoryPieChart(currentCondition);
    loadStats(currentCondition);   // ✅ FIX
}
public void loadTable(String query) {
    try {
        Connection con = getConnection();
        ResultSet rs = con.createStatement().executeQuery(query);
        jTable1.setModel(DbUtils.resultSetToTableModel(rs));
    } catch (Exception e) {
        e.printStackTrace();
    }
}
public void showPieChart(String condition) {
    try (Connection con = getConnection()) {

        // 🔥 JOIN bills + bill_items
        String query = "SELECT bi.item_name, SUM(bi.quantity) AS total_qty " +
                       "FROM bill_items bi " +
                       "JOIN bills b ON bi.bill_id = b.bill_id " +
                       "WHERE " + condition + " " +
                       "GROUP BY bi.item_name";

        PreparedStatement pst = con.prepareStatement(query);
        ResultSet rs = pst.executeQuery();

        DefaultPieDataset dataset = new DefaultPieDataset();

        while (rs.next()) {
            dataset.setValue(
                rs.getString("item_name"),
                rs.getDouble("total_qty")
            );
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "Top Selling Items",
                dataset,
                true,
                true,
                false
        );

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelGenerator(
            new StandardPieSectionLabelGenerator("{0} = {2}")
        );

        // ✅ refresh panel
        jPanel2.removeAll();
        jPanel2.setLayout(new BorderLayout());
        jPanel2.add(new ChartPanel(chart), BorderLayout.CENTER);
        jPanel2.validate();

    } catch (Exception e) {
        System.out.println(e);
    }
}
public void showBarChart(String condition) {
    try (Connection con = getConnection()) {

        String query = "SELECT DATE(b.bill_date) AS dt, SUM(b.grand_total) AS total " +
                       "FROM bills b WHERE " + condition + " " +
                       "GROUP BY dt ORDER BY dt";

        PreparedStatement pst = con.prepareStatement(query);
        ResultSet rs = pst.executeQuery();

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        while (rs.next()) {
            dataset.setValue(rs.getDouble("total"), "Revenue", rs.getString("dt"));
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Revenue", "Date", "Amount", dataset);

        jPanel1.removeAll();
        jPanel1.setLayout(new BorderLayout());
        jPanel1.add(new ChartPanel(chart), BorderLayout.CENTER);
        jPanel1.revalidate();
        jPanel1.repaint();

    } catch (Exception e) {
        e.printStackTrace();
    }
}
/**
 *
 * @author ADMIN
 */

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Segoe UI", 3, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Total earnings (₹):");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 70, 220, -1));

        jLabel2.setFont(new java.awt.Font("SansSerif", 3, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Number of bills generated:");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 310, 50));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 3, 24)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Total quantity of products sold:");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 370, -1));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 3, 24)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Unique customers served:");
        getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 170, 320, -1));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 204, 255));
        getContentPane().add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 70, 100, 40));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 204, 255));
        getContentPane().add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 30, 30, 30));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 204, 255));
        getContentPane().add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 120, 50, 40));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 204, 255));
        getContentPane().add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 170, 40, 30));

        jButton7.setBackground(java.awt.Color.red);
        jButton7.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton7.setText("X");
        jButton7.addActionListener(this::jButton7ActionPerformed);
        getContentPane().add(jButton7, new org.netbeans.lib.awtextra.AbsoluteConstraints(1280, 20, 80, 30));

        jButton1.setBackground(new java.awt.Color(255, 204, 255));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton1.setText("Today");
        jButton1.addActionListener(this::jButton1ActionPerformed);
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 260, 110, 40));

        jButton2.setBackground(new java.awt.Color(255, 204, 255));
        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton2.setText("This Week");
        jButton2.addActionListener(this::jButton2ActionPerformed);
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 260, 110, 40));

        jButton3.setBackground(new java.awt.Color(255, 204, 255));
        jButton3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton3.setText("This Month");
        jButton3.addActionListener(this::jButton3ActionPerformed);
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 260, 110, 40));
        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 310, 350, 370));

        jButton4.setBackground(new java.awt.Color(204, 255, 255));
        jButton4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton4.setText("Item Chart");
        jButton4.addActionListener(this::jButton4ActionPerformed);
        getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 260, 170, 40));

        jButton5.setBackground(new java.awt.Color(204, 255, 255));
        jButton5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton5.setText("Category Chart");
        jButton5.addActionListener(this::jButton5ActionPerformed);
        getContentPane().add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 260, 170, 40));

        jButton6.setBackground(new java.awt.Color(255, 204, 255));
        jButton6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton6.setText("Overall");
        jButton6.addActionListener(this::jButton6ActionPerformed);
        getContentPane().add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 260, 110, 40));
        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 310, 980, 370));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 160, 500, 140));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pro/17.png"))); // NOI18N
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1450, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
loadTable("SELECT * FROM bills WHERE DATE(bill_date)=CURDATE()");
loadData("today");  // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
loadTable("SELECT * FROM bills WHERE YEARWEEK(bill_date,1)=YEARWEEK(CURDATE(),1)");
loadData("week");// TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
   loadTable("SELECT * FROM bills WHERE MONTH(bill_date)=MONTH(CURDATE()) AND YEAR(bill_date)=YEAR(CURDATE())");
loadData("month"); // TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
loadTable("SELECT * FROM bills");
loadData("all");        // TODO add your handling code here:
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
showItemPieChart(currentCondition); // for item        // TODO add your handling code here:
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
  showCategoryPieChart(currentCondition);      // TODO add your handling code here:
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
adminsec a = new adminsec();
    a.setVisible(true);
    this.dispose();            // TODO add your handling code here:
    }//GEN-LAST:event_jButton7ActionPerformed

    /**
     * @param args the command line arguments
     */
public static void main(String args[]) {

    java.awt.EventQueue.invokeLater(new Runnable() {
        public void run() {
            new repo().setVisible(true); // ✅ correct
        }
    });

}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
