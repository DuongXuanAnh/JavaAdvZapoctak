package cz.cuni.mff.java.zapoctak.newWindow;

import cz.cuni.mff.java.zapoctak.config.Config;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

public class BookDetail extends JPanel {
    private JTextField titleTextField;

    private JComboBox<String> genresComboBox;
    private JFormattedTextField yearField;
    private JFormattedTextField priceField;
    private JSpinner quantitySpinner;
    private JTextArea descriptionArea;
    private ArrayList<JComboBox<String>> authorComboBoxes;
    private JComboBox<String> authorComboBox;

    public BookDetail() {
        titleTextField = new JTextField(50);
        genresComboBox = new JComboBox<>();
        priceField = new JFormattedTextField();
        yearField = new JFormattedTextField();
        quantitySpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        descriptionArea = new JTextArea(10, 20);
        authorComboBox = new JComboBox<>();
        fillComboBoxWithAuthors(authorComboBox);
        authorComboBoxes = new ArrayList<>();
        setupLayout();
    }

    private void fillComboBoxWithAuthors(JComboBox<String> authorComboBox) {
        ArrayList<String> authors = loadAuthorsFromDB();
        for (String author : authors) {
            authorComboBox.addItem(author);
        }
    }

    private ArrayList<String> loadAuthorsFromDB() {
        ArrayList<String> authors = new ArrayList<>();
        try (Connection conn = Config.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, jmeno FROM autor")) {

            while (rs.next()) {
                authors.add(rs.getString("jmeno"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return authors;
    }

    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = getGridBagConstraints();
        addTitleLabelAndField(gbc);
        addGenresLabelAndComboBox(gbc);
        addPriceLabelAndField(gbc);
        addYearLabelAndField(gbc);
        addQuantityLabelAndSpinner(gbc);
        addDescriptionLabelAndTextArea(gbc);
        addAuthorLabelAndComboBox(gbc);
    }

    private GridBagConstraints getGridBagConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private void addTitleLabelAndField(GridBagConstraints gbc) {
        JLabel nameLabel = new JLabel("Název");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(nameLabel, gbc);

        gbc.gridx = 1;
        add(titleTextField, gbc);
    }

    private void addGenresLabelAndComboBox(GridBagConstraints gbc) {
        JLabel nameLabel = new JLabel("Žánr");
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(nameLabel, gbc);

        gbc.gridx = 1;

        String[] zanry = {"Sci-fi (vědeckofantastický)", "Romantika", "Thriller", "Detektivka", "Fantasy", "Horor", "Komedie", "Akční", "Drama", "Historický"};
        Arrays.sort(zanry);
        genresComboBox = new JComboBox<>(zanry);
        add(genresComboBox, gbc);
    }
    private void addYearLabelAndField(GridBagConstraints gbc){
        JLabel yearLabel = new JLabel("Rok vydání");
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(yearLabel, gbc);

        gbc.gridx = 1;
        add(yearField, gbc);
    }
    private void addPriceLabelAndField(GridBagConstraints gbc) {
        JLabel priceLabel = new JLabel("Cena (Kč)");
        gbc.gridx = 0;
        gbc.gridy = 4;
        add(priceLabel, gbc);

        gbc.gridx = 1;
        add(priceField, gbc);
    }

    private void addQuantityLabelAndSpinner(GridBagConstraints gbc) {
        JLabel quantityLabel = new JLabel("Počet kusů");
        gbc.gridx = 0;
        gbc.gridy = 5;
        add(quantityLabel, gbc);

        gbc.gridx = 1;
        add(quantitySpinner, gbc);
    }
    private void addDescriptionLabelAndTextArea(GridBagConstraints gbc) {
        JLabel descriptionLabel = new JLabel("Popis:");
        gbc.gridx = 0;
        gbc.gridy = 6;
        add(descriptionLabel, gbc);
        gbc.gridx = 1;
        add(descriptionArea, gbc);
    }

    private void addAuthorLabelAndComboBox(GridBagConstraints gbc) {
        JLabel nameLabel = new JLabel("Autor");
        gbc.gridx = 0;
        gbc.gridy = 7;
        add(nameLabel, gbc);

        gbc.gridx = 1;
        authorComboBoxes.add(authorComboBox);
        add(authorComboBox, gbc);
    }
}