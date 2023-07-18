package cz.cuni.mff.java.zapoctak.content;

import cz.cuni.mff.java.zapoctak.config.Config;
import cz.cuni.mff.java.zapoctak.global.Notification;
import cz.cuni.mff.java.zapoctak.global.TitleBorder;

import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The AddCustomer class represents a JPanel that allows users to add a new customer.
 * This includes fields to input the name and birthdate of the customer.
 * It also includes checks for the validity of the input data.
 */
public class AddCustomer extends JPanel {

    private final JTextField nameField;
    private final JDateChooser dateChooser;
    /**
     * The constructor initializes the panel, including the layout, text fields, and submit button.
     */
    public AddCustomer(){
        this.setBorder(TitleBorder.create("Přidat zákazníka"));
            nameField = new JTextField(20);
            dateChooser = new JDateChooser();
            setupLayout();
            setupSubmitButton();
        }

    /**
     * Configures the layout for the panel.
     */
        private void setupLayout() {
                setLayout(new GridBagLayout());
                GridBagConstraints gbc = getGridBagConstraints();

                addNameLabelAndField(gbc);
                addDateLabelAndChooser(gbc);
        }

    /**
     * Creates a GridBagConstraints object for the layout setup.
     * @return GridBagConstraints object with specific configurations.
     */
        private GridBagConstraints getGridBagConstraints() {
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(5, 5, 5, 5);
                gbc.fill = GridBagConstraints.HORIZONTAL;
                return gbc;
        }

    /**
     * Adds a JLabel and JTextField for the customer's name to the panel.
     * @param gbc The GridBagConstraints for the layout setup.
     */
        private void addNameLabelAndField(GridBagConstraints gbc) {
                JLabel nameLabel = new JLabel("Jméno zákazníka");
                gbc.gridx = 0;
                gbc.gridy = 1;
                add(nameLabel, gbc);

                gbc.gridx = 1;
                add(nameField, gbc);
        }

    /**
     * Adds a JLabel and JDateChooser for the customer's birthdate to the panel.
     * @param gbc The GridBagConstraints for the layout setup.
     */
        private void addDateLabelAndChooser(GridBagConstraints gbc) {
                JLabel dateLabel = new JLabel("Datum:");
                dateChooser.setDateFormatString("dd.MM.yyyy");
                gbc.gridx = 0;
                gbc.gridy = 2;
                add(dateLabel, gbc);

                gbc.gridx = 1;
                add(dateChooser, gbc);
        }

    /**
     * Sets up the submit button with its action listener.
     * When clicked, it takes the input data and, if valid, inserts a new customer into the database.
     */
        private void setupSubmitButton() {
                JButton submitButton = new JButton("Přidat zákazníka");
                GridBagConstraints gbc = getGridBagConstraints();
                gbc.gridx = 1;
                gbc.gridy = 4;
                gbc.fill = GridBagConstraints.NONE;
                gbc.anchor = GridBagConstraints.CENTER;
                add(submitButton, gbc);

                submitButton.addActionListener(e -> {
                        String customerName = nameField.getText();
                        Date selectedDate = dateChooser.getDate();
                        if (checkCustomerData(customerName, selectedDate)) {
                                insertCustomerToDb(customerName, selectedDate);
                        }
                });
        }

    /**
     * Checks the input data for validity.
     * The customer name must not be empty, and the date must be a past date.
     * @param customerName The input customer's name.
     * @param selectedDate The selected date from the date chooser.
     * @return True if data is valid, false otherwise.
     */
        private boolean checkCustomerData(String customerName, Date selectedDate) {
            if(customerName.isEmpty()){
                Notification.showErrorMessage("Jméno nesmí být prázdné");
                return false;
            }

            if (selectedDate == null) {
                Notification.showErrorMessage("Datum nebyl vybrán.");
                return false;
            }

            if(!selectedDate.before(new Date())){
                Notification.showErrorMessage("Datum musí být v minulosti.");
                return false;
            }
            return true;
        }

    /**
     * Inserts a new customer into the database with the provided name and birthdate.
     * @param customerName The customer's name.
     * @param selectedDate The customer's birthdate.
     */
        private void insertCustomerToDb(String customerName, Date selectedDate) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String dateStr = format.format(selectedDate);

            try (Connection conn = Config.getConnection()) {
                String sql = "INSERT INTO zakaznik (jmeno, datum_narozeni) VALUES (?, ?)";
                PreparedStatement statement = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
                statement.setString(1, customerName);
                statement.setString(2, dateStr);
                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                    ResultSet rs = statement.getGeneratedKeys();
                    if (rs.next()) {
                        int generatedId = rs.getInt(1);
                        Notification.showSuccessMessage("Nový zákazník byl založen s ID: " + generatedId);
                        resetForm();
                    } else {
                        Notification.showErrorMessage("Nastala chyba, prosím zkuste to znovu");
                    }
                }
            } catch (SQLException ex) {
                Notification.showErrorMessage("Chyba, zkuste to znovu nebo kontaktujte IT oddělení");
                System.out.println(ex);
            }
        }

    /**
     * This method resets the form.
     */
    private void resetForm() {
        nameField.setText("");
        dateChooser.setDate(null);
    }
}
