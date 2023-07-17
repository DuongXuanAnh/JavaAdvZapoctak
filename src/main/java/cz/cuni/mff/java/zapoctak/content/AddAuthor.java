package cz.cuni.mff.java.zapoctak.content;

import cz.cuni.mff.java.zapoctak.config.Config;
import cz.cuni.mff.java.zapoctak.global.Notification;
import cz.cuni.mff.java.zapoctak.global.TitleBorder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;


/**
 * This class represents a panel for adding a new author.
 *
 * @author Duong Xuan Anh
 * @version 1.0
 */

public class AddAuthor extends JPanel {
    /**
     * A JTextField to take the name of the author.
     */
    private JTextField nameField;

    /**
     * A JComboBox to select the nationality of the author.
     */
    private JComboBox<String> nationalComboBox;
    public AddAuthor(){
        this.setBorder(TitleBorder.create("Přidat autora"));

        // Set the layout of the panel.
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel nameLabel = new JLabel("Jméno Autora");
        nameField = new JTextField(20);

        JLabel nationalLabel = new JLabel("Národnost");

        Locale[] locales = Locale.getAvailableLocales();
        Set<String> countries = new TreeSet<String>();
        for (Locale locale : locales) {
            countries.add(locale.getDisplayCountry());
        }

        nationalComboBox = new JComboBox<>(countries.toArray(new String[0]));

        JButton submitButton = new JButton("Přidat autora");
        submitButton.addActionListener(this::addAuthorAction);

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(nameLabel, gbc);

        gbc.gridx = 1;
        add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(nationalLabel, gbc);

        gbc.gridx = 1;
        add(nationalComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(submitButton, gbc);
    }


    /**
     * The addAuthorAction method is called when the submit button is clicked.
     * It checks if the name field is empty and if not, adds the author to the database.
     *
     * @param e The ActionEvent object.
     */
    private void addAuthorAction(ActionEvent e) {
        String name = nameField.getText();
        String national = (String) nationalComboBox.getSelectedItem();

        if (name.isEmpty()) {
            Notification.showErrorMessage("Jméno autora nesmí být prázdné");
            return;
        }

        if (national == null || national.isEmpty()) {
            Notification.showErrorMessage("Národnost autora musí být vybrána");
            return;
        }

        try (Connection conn = Config.getConnection();
             PreparedStatement checkStatement = conn.prepareStatement("SELECT id, jmeno FROM autor WHERE jmeno = ?");
             PreparedStatement insertStatement = conn.prepareStatement("INSERT INTO autor (jmeno, narodnost) VALUES (?, ?)")) {

            checkStatement.setString(1, name);
            ResultSet resultSet = checkStatement.executeQuery();

            if (resultSet.next()) {
                JOptionPane.showMessageDialog(AddAuthor.this, "Autor " + name + " již už existuje.", "Autor již existuje", JOptionPane.WARNING_MESSAGE);
            } else {
                insertStatement.setString(1, name);
                insertStatement.setString(2, national);
                int rowsInserted = insertStatement.executeUpdate();
                if (rowsInserted > 0) {
                    Notification.showSuccessMessage("Autor " + name + " byl úspěšně přidán");
                    resetForm();
                }
            }
        } catch (SQLException ex) {
            Notification.showErrorMessage("Nastal problém, zkuste to znovu nebo informujte IT oddělení");
        }
    }
    
    /**
     * This method resets the form.
     */
    private void resetForm() {
        nameField.setText("");
        nationalComboBox.setSelectedIndex(0);
    }
}
