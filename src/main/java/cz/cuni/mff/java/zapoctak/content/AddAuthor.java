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

public class AddAuthor extends JPanel {
    private final JTextField nameField;
    private final JComboBox<String> nationalComboBox;

    public AddAuthor() {
        this.setBorder(TitleBorder.create("Přidat autora"));
        nameField = new JTextField(20);
        nationalComboBox = new JComboBox<>(getAvailableCountries());
        setupLayout();
        setupSubmitButton();
    }

    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = getGridBagConstraints();

        addNameLabelAndField(gbc);
        addNationalLabelAndComboBox(gbc);
    }

    private GridBagConstraints getGridBagConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private void addNameLabelAndField(GridBagConstraints gbc) {
        JLabel nameLabel = new JLabel("Jméno Autora");
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(nameLabel, gbc);

        gbc.gridx = 1;
        add(nameField, gbc);
    }

    private void addNationalLabelAndComboBox(GridBagConstraints gbc) {
        JLabel nationalLabel = new JLabel("Národnost");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(nationalLabel, gbc);

        gbc.gridx = 1;
        add(nationalComboBox, gbc);
    }

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

    private void addAuthorAction(ActionEvent e) {
        String name = nameField.getText();
        String national = (String) nationalComboBox.getSelectedItem();

        if (checkAuthorData(name, national)) {
            insertAuthorToDb(name, national);
        }
    }

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

    private void insertAuthorToDb(String name, String national) {
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

    private String[] getAvailableCountries() {
        Locale[] locales = Locale.getAvailableLocales();
        Set<String> countries = new TreeSet<>();
        for (Locale locale : locales) {
            countries.add(locale.getDisplayCountry());
        }
        return countries.toArray(new String[0]);
    }

    private void resetForm() {
        nameField.setText("");
        nationalComboBox.setSelectedIndex(0);
    }
}
