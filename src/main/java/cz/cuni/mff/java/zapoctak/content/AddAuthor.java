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
 * A class representing a panel for adding a new author to the database.
 * This panel consists of fields for author's name and nationality, as well as a submit button.
 */
public class AddAuthor extends JPanel {
    private final JTextField nameField;
    private final JComboBox<String> nationalComboBox;

    /**
     * Constructs the AddAuthor panel with necessary fields and setup.
     */
    public AddAuthor() {
        this.setBorder(TitleBorder.create("Přidat autora"));
        nameField = new JTextField(20);
        nationalComboBox = new JComboBox<>(getAvailableCountries());
        setupLayout();
        setupSubmitButton();
    }

    /**
     * Setups the layout for the panel.
     */
    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = getGridBagConstraints();

        addNameLabelAndField(gbc);
        addNationalLabelAndComboBox(gbc);
    }

    /**
     * Creates and returns a GridBagConstraints object with specified settings.
     *
     * @return A GridBagConstraints object with specific insets and horizontal fill.
     */
    private GridBagConstraints getGridBagConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    /**
     * Adds the label and text field for the author's name to the panel using the given GridBagConstraints.
     *
     * @param gbc The GridBagConstraints used to specify the position and size of the components.
     */
    private void addNameLabelAndField(GridBagConstraints gbc) {
        JLabel nameLabel = new JLabel("Jméno Autora");
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(nameLabel, gbc);

        gbc.gridx = 1;
        add(nameField, gbc);
    }

    /**
     * Adds the label and combo box for the author's nationality to the panel using the given GridBagConstraints.
     *
     * @param gbc The GridBagConstraints used to specify the position and size of the components.
     */
    private void addNationalLabelAndComboBox(GridBagConstraints gbc) {
        JLabel nationalLabel = new JLabel("Národnost");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(nationalLabel, gbc);

        gbc.gridx = 1;
        add(nationalComboBox, gbc);
    }

    /**
     * Sets up the submit button for the form, with associated action listeners for adding the author.
     */
    private void setupSubmitButton() {
        JButton submitButton = new JButton("Přidat autora");
        GridBagConstraints gbc = getGridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(submitButton, gbc);

        submitButton.addActionListener(this::addAuthorAction);
    }

    /**
     * Takes an ActionEvent from the submit button and attempts to add the author to the database.
     *
     * @param e The ActionEvent that was triggered by the submit button.
     */
    private void addAuthorAction(ActionEvent e) {
        String name = nameField.getText();
        String national = (String) nationalComboBox.getSelectedItem();

        if (checkAuthorData(name, national)) {
            try {
                insertAuthorToDb(name, national);
            } catch (SQLException ex) {
                Notification.showErrorMessage("Nastal problém při vkládání do databáze, zkuste to znovu nebo informujte IT oddělení");
            }
        }
    }

    /**
     * Validates the author's data. Returns false if the name or nationality is empty, otherwise returns true.
     *
     * @param name     The name of the author.
     * @param national The nationality of the author.
     * @return True if data is valid, otherwise false.
     */
    private boolean checkAuthorData(String name, String national) {
        if (name.isEmpty()) {
            Notification.showErrorMessage("Jméno autora nesmí být prázdné");
            return false;
        }

        if (national == null || national.isEmpty()) {
            Notification.showErrorMessage("Národnost autora musí být vybrána");
            return false;
        }
        return true;
    }

    /**
     * Inserts the author's data into the database.
     *
     * @param name     The name of the author.
     * @param national The nationality of the author.
     */
    private void insertAuthorToDb(String name, String national) throws SQLException {
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
        }
    }

    /**
     * Retrieves a list of available countries for the nationality selection.
     *
     * @return An array of available countries.
     */
    private String[] getAvailableCountries() {
        Locale[] locales = Locale.getAvailableLocales();
        Set<String> countries = new TreeSet<>();
        for (Locale locale : locales) {
            countries.add(locale.getDisplayCountry());
        }
        return countries.toArray(new String[0]);
    }

    /**
     * Resets the form to its initial state.
     */
    private void resetForm() {
        nameField.setText("");
        nationalComboBox.setSelectedIndex(0);
    }
}
