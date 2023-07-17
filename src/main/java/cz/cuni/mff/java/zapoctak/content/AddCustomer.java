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
public class AddCustomer extends JPanel {

    private final JTextField nameField;
    private final JDateChooser dateChooser;

    public AddCustomer(){
        this.setBorder(TitleBorder.create("Přidat zákazníka"));
            nameField = new JTextField(20);
            dateChooser = new JDateChooser();
            setupLayout();
            setupSubmitButton();
        }

        private void setupLayout() {
                setLayout(new GridBagLayout());
                GridBagConstraints gbc = getGridBagConstraints();

                addNameLabelAndField(gbc);
                addDateLabelAndChooser(gbc);
        }

        private GridBagConstraints getGridBagConstraints() {
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(5, 5, 5, 5);
                gbc.fill = GridBagConstraints.HORIZONTAL;
                return gbc;
        }


        private void addNameLabelAndField(GridBagConstraints gbc) {
                JLabel nameLabel = new JLabel("Jméno zákazníka");
                gbc.gridx = 0;
                gbc.gridy = 1;
                add(nameLabel, gbc);

                gbc.gridx = 1;
                add(nameField, gbc);
        }

        private void addDateLabelAndChooser(GridBagConstraints gbc) {
                JLabel dateLabel = new JLabel("Datum:");
                dateChooser.setDateFormatString("dd.MM.yyyy");
                gbc.gridx = 0;
                gbc.gridy = 2;
                add(dateLabel, gbc);

                gbc.gridx = 1;
                add(dateChooser, gbc);
        }

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
                    } else {
                        Notification.showErrorMessage("Nastala chyba, prosím zkuste to znovu");
                    }
                }
            } catch (SQLException ex) {
                Notification.showErrorMessage("Chyba, zkuste to znovu nebo kontaktujte IT oddělení");
            }
        }
}
