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
/**
 * A JPanel class for returning books in a library management system.
 * Allows users to search for a document ID, view the associated customer details,
 * and the books associated with the document. Users can return the books and perform
 * necessary database operations for updating book quantities and deleting records.
 */
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
    /**
     * Sets up the layout of the ReturnBook panel.
     * Configures the container panel and table panel.
     */
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
    /**
     * Adds a "Return Book" button to the table panel.
     * When clicked, this button initiates the process of returning books.
     * It updates book quantities in the database and deletes associated records.
     * Also, clears the table and customer information from the panel.
     */
    private void addReturnButton() {
        JButton returnBookButton = new JButton("Vrátit knihy");
        returnBookButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentTable != null) {
                    try {
                        updateBookAmnoutInDB();
                    } catch (SQLException ex) {
                        Notification.showErrorMessage("Akce se nepodařila, zkuste to znovu");
                        System.out.println(ex);
                    }
                    try {
                        deleteDocumentFromDB();
                    } catch (SQLException ex) {
                        Notification.showErrorMessage("Akce se nepodařila, zkuste to znovu");
                        System.out.println(ex);
                    }
                    try {
                        deleteDocumentBook();
                    } catch (SQLException ex) {
                        Notification.showErrorMessage("Akce se nepodařila, zkuste to znovu");
                        System.out.println(ex);
                    }
                    try {
                        deleteDocumentCustomer();
                    } catch (SQLException ex) {
                        Notification.showErrorMessage("Akce se nepodařila, zkuste to znovu");
                        System.out.println(ex);
                    }
                    clearTableAndCustomerName();
                    Notification.showSuccessMessage("Knihy byly vráceny");
                }
            }
        });
        tablePanel.add(returnBookButton, BorderLayout.SOUTH);
        tablePanel.revalidate();
        tablePanel.repaint();
    }
    /**
     * Deletes the associated customer records from the database for the current document.
     */
    private void deleteDocumentCustomer()throws SQLException {
        String deleteDokladZakaznikSql = "DELETE FROM doklad_zakaznik WHERE id_doklad = ?";
        try (PreparedStatement deleteDokladZakaznikStatement = Config.getConnection().prepareStatement(deleteDokladZakaznikSql)) {
            deleteDokladZakaznikStatement.setInt(1, currDocumentID);
            deleteDokladZakaznikStatement.executeUpdate();
        }
    }

    /**
     * Deletes the associated book records from the database for the current document.
     */
    private void deleteDocumentBook() throws SQLException {
        String deleteDokladKnihaSql = "DELETE FROM doklad_kniha WHERE id_doklad = ?";
        try (PreparedStatement deleteDokladKnihaStatement = Config.getConnection().prepareStatement(deleteDokladKnihaSql)) {
            deleteDokladKnihaStatement.setInt(1, currDocumentID);
            deleteDokladKnihaStatement.executeUpdate();
        }
    }
    /**
     * Deletes a document from the database with the specified ID.
     */
    private void deleteDocumentFromDB() throws SQLException {
        String deleteDokladSql = "DELETE FROM doklad WHERE id = ?";
        try (PreparedStatement deleteDokladStatement = Config.getConnection().prepareStatement(deleteDokladSql)) {
            deleteDokladStatement.setInt(1, currDocumentID);
            deleteDokladStatement.executeUpdate();
        }
    }
    /**
     * Updates the book quantities in the database for the returned books.
     * Uses the 'bookIdList' and 'amountRentBooksList' to determine the changes.
     */
    private void updateBookAmnoutInDB() throws SQLException {
        for (int i = 0; i < bookIdList.size(); i++) {
            int bookID = bookIdList.get(i);
            int bookAmount = amountRentBooksList.get(i);
            String updateBookAmountSql = "UPDATE kniha SET amount=amount + ? WHERE id=?";
            try (PreparedStatement updateBookAmountStatement = Config.getConnection().prepareStatement(updateBookAmountSql)) {
                updateBookAmountStatement.setInt(1, bookAmount);
                updateBookAmountStatement.setInt(2, bookID);
                updateBookAmountStatement.executeUpdate();
            }
        }
    }

    /**
     * Adds the label and text field for entering the document ID.
     * Also, adds the "Hledat" (Find) button to initiate the search for the document.
     */
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
                try {
                    showDocument();
                } catch (SQLException ex) {
                    Notification.showErrorMessage("Nastala chyba, zkuste to znovu");
                }
                revalidate();
                repaint();
            }
        });
        gbc = getGridBagConstraints(2, 0);
        containerPanel.add(findButton, gbc);
    }

    /**
     * Retrieves the table data from a ResultSet.
     * Converts the data into a 2D array suitable for JTable.
     *
     * @param resultSet The ResultSet containing the table data.
     * @return A 2D array containing the table data.
     * @throws SQLException If a database error occurs while retrieving data.
     */
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
    /**
     * Adds the customer label to the container panel with the provided customer details.
     * Replaces the old label if it already exists.
     *
     * @param customerName The name of the customer.
     * @param customerID   The ID of the customer.
     */
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
    /**
     * Adds a JTable to the table panel with the provided table data.
     * The table data should contain information about the books associated with the document.
     *
     * @param tableData A 2D array containing the table data.
     */
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
    /**
     * Shows the document associated with the entered document ID.
     * Fetches data from the database and updates the UI accordingly.
     * If the document ID is invalid or not found, displays an error message.
     */
    private void showDocument() throws SQLException {
        String documentIDText = documentID.getText();

        if (documentIDText.isEmpty() || !documentIDText.matches("\\d+")) {
            Notification.showErrorMessage("ID objednávky je nevalidní");
            return;
        }

        try (Connection conn = Config.getConnection();
             PreparedStatement statement = conn.prepareStatement("SELECT * FROM document WHERE dokladID = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

            statement.setInt(1, Integer.parseInt(documentIDText));

            try (ResultSet resultSet = statement.executeQuery()) {
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
            }
        } catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }
    /**
     * Retrieves a GridBagConstraints object for arranging components in the container panel.
     *
     * @param gridx The horizontal position in the grid.
     * @param gridy The vertical position in the grid.
     * @return A GridBagConstraints object.
     */
    private GridBagConstraints getGridBagConstraints(int gridx, int gridy) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        return gbc;
    }
    /**
     * Clears the table panel and container panel by removing the table and customer label.
     * Also clears the document ID text field.
     * Updates the UI to reflect the changes.
     */
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
