package cz.cuni.mff.java.zapoctak.content;

import cz.cuni.mff.java.zapoctak.config.Config;
import cz.cuni.mff.java.zapoctak.global.Notification;
import cz.cuni.mff.java.zapoctak.global.TitleBorder;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ReturnBook extends JPanel {

    private final JTextField documentID;
    private JPanel containerPanel;
    private JTable currentTable;
    private JPanel tablePanel;
    private JLabel customerNameLabel;
    private JScrollPane scrollPane;

    private int currDocumentID;

    private final ArrayList<Integer> bookIdList;
    private final ArrayList<Integer> amountRentBooksList;


    public ReturnBook() {
        this.setBorder(TitleBorder.create("Vrácení knihy"));
        documentID = new JTextField(11);
        bookIdList = new ArrayList<>();
        amountRentBooksList= new ArrayList<>();
        setupLayout();
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        containerPanel = new JPanel();
        containerPanel.setLayout(new GridBagLayout());

        addDocumentIDLabelAndField();
        add(containerPanel, BorderLayout.NORTH);

        tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        add(tablePanel, BorderLayout.CENTER);
    }

    private void addReturnButton() {
        JButton returnBookButton = new JButton("Vrátit knihy");
        returnBookButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentTable != null) {
                    // here
                    System.out.println(currDocumentID);
                    updateBookAmnoutInDB();
                    deleteDocumentFromDB();
                    deleteDocumentBook();
                    deleteDocumentCustomer();
                    clearTableAndCustomerName();
                    Notification.showSuccessMessage("Knihy byly vráceny");
                }
            }
        });
        tablePanel.add(returnBookButton, BorderLayout.SOUTH);
        tablePanel.revalidate();
        tablePanel.repaint();
    }

    private void deleteDocumentCustomer() {
        String deleteDokladZakaznikSql = "DELETE FROM doklad_zakaznik WHERE id_doklad = ?";
        PreparedStatement deleteDokladZakaznikStatement = null;
        try {
            deleteDokladZakaznikStatement = Config.getConnection().prepareStatement(deleteDokladZakaznikSql);
            deleteDokladZakaznikStatement.setInt(1, currDocumentID);
            deleteDokladZakaznikStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private void deleteDocumentBook() {
        String deleteDokladKnihaSql = "DELETE FROM doklad_kniha WHERE id_doklad = ?";
        PreparedStatement deleteDokladKnihaStatement = null;
        try {
            deleteDokladKnihaStatement = Config.getConnection().prepareStatement(deleteDokladKnihaSql);
            deleteDokladKnihaStatement.setInt(1, currDocumentID);
            deleteDokladKnihaStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteDocumentFromDB() {
        String deleteDokladSql = "DELETE FROM doklad WHERE id = ?";
        PreparedStatement deleteDokladStatement = null;
        try {
            deleteDokladStatement = Config.getConnection().prepareStatement(deleteDokladSql);
            deleteDokladStatement.setInt(1, currDocumentID);
            deleteDokladStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private void updateBookAmnoutInDB(){
        for (int i = 0; i < bookIdList.size(); i++){
            int bookID = bookIdList.get(i);
            int bookAmount = amountRentBooksList.get(i);
            String updateBookAmountSql = "UPDATE kniha SET amount=amount + ? WHERE id=?";
            PreparedStatement updateBookAmountStatement = null;
            try {
                updateBookAmountStatement = Config.getConnection().prepareStatement(updateBookAmountSql);
                updateBookAmountStatement.setInt(1, bookAmount);
                updateBookAmountStatement.setInt(2, bookID);
                updateBookAmountStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private void addDocumentIDLabelAndField() {
        JLabel documentIDLabel = new JLabel("ID objednávky:");
        GridBagConstraints gbc = getGridBagConstraints(0, 0);
        containerPanel.add(documentIDLabel, gbc);

        gbc = getGridBagConstraints(1, 0);
        containerPanel.add(documentID, gbc);

        JButton findButton = new JButton("Hledat");
        findButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDocument();
                revalidate();
                repaint();
            }
        });
        gbc = getGridBagConstraints(2, 0);
        containerPanel.add(findButton, gbc);
    }

    private Object[][] getTableDataFromResultSet(ResultSet resultSet) throws SQLException {
        resultSet.last();
        int numRows = resultSet.getRow();
        resultSet.beforeFirst();

        Object[][] tableData = new Object[numRows][4];

        int row = 0;
        while (resultSet.next()) {
            String nazev = resultSet.getString("nazev");
            String datum = resultSet.getString("datum");
            String datumTo = resultSet.getString("datumTo");
            String amount = resultSet.getString("amount");

            tableData[row][0] = nazev;
            tableData[row][1] = datum;
            tableData[row][2] = datumTo;
            tableData[row][3] = amount;
            row++;
        }

        return tableData;
    }

    private void addCustomerLabel(String customerName, int customerID) {
        if (customerNameLabel != null) {
            containerPanel.remove(customerNameLabel); // Remove old label if it exists
        }
        customerNameLabel = new JLabel("Zákazník: " + customerName + " ( ID: " + customerID + ")");
        GridBagConstraints gbc = getGridBagConstraints(0, 1);
        containerPanel.add(customerNameLabel, gbc);
        containerPanel.revalidate();
        containerPanel.repaint();
    }

    private void addTableToPanel(Object[][] tableData) {
        String[] columnNames = {"Nazev", "Datum", "DatumTo", "Amount"};

        DefaultTableModel tableModel = new DefaultTableModel(tableData, columnNames);
        currentTable = new JTable(tableModel);
        scrollPane = new JScrollPane(currentTable);

        tablePanel.removeAll(); // Remove old table if it exists
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        addReturnButton(); // Add the button after the table is created
        tablePanel.revalidate();
        tablePanel.repaint();
    }

    private void showDocument() {
        String documentIDText = documentID.getText();

        if(documentIDText.isEmpty() || !documentIDText.matches("\\d+")){
            Notification.showErrorMessage("ID objednávky je nevalidní");
            return;
        }

        try (Connection conn = Config.getConnection()) {
            String sql = "SELECT * FROM document WHERE dokladID = ?";
            PreparedStatement statement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);


            statement.setInt(1, Integer.parseInt(documentIDText));

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                addCustomerLabel(resultSet.getString("jmeno"), resultSet.getInt("zakaznikID"));

                bookIdList.add(resultSet.getInt("bookID"));
                amountRentBooksList.add(resultSet.getInt("amount"));

                Object[][] tableData = getTableDataFromResultSet(resultSet);
                addTableToPanel(tableData);
                currDocumentID = Integer.parseInt(documentIDText);
            } else {
                Notification.showErrorMessage("ID objednávky neexistuje");
                System.out.println("No records found for document ID " + documentIDText);
            }

        } catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private GridBagConstraints getGridBagConstraints(int gridx, int gridy) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        return gbc;
    }

    private void clearTableAndCustomerName() {
        if (scrollPane != null) {
            tablePanel.remove(scrollPane);
            scrollPane = null;
            currentTable = null;
        }
        if (customerNameLabel != null) {
            containerPanel.remove(customerNameLabel);
            customerNameLabel = null;
        }
        documentID.setText("");
        tablePanel.revalidate();
        tablePanel.repaint();
        containerPanel.revalidate();
        containerPanel.repaint();
    }

}
