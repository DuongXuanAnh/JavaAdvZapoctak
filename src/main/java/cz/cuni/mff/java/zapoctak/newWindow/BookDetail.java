package cz.cuni.mff.java.zapoctak.newWindow;

import cz.cuni.mff.java.zapoctak.config.Config;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
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

    public BookDetail(int bookId) {
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
        loadDataFromDatabase(bookId);

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
        addAuthorButton(gbc);
        addConfirmButton(gbc);
        addCancelButton(gbc);
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


//        gbc.gridx = 1;
//        authorComboBoxes.add(authorComboBox);
//        add(authorComboBox, gbc);

    }

    private void addAuthorButton(GridBagConstraints gbc){
        JButton addAuthorButton = new JButton("Přidat autora");
        gbc.gridx = 1;
        gbc.gridy = 7;
        add(addAuthorButton, gbc);

        addAuthorButton.addActionListener(e -> {
            addNewAuthorCombobox(gbc);
        });
    }

    private void loadDataFromDatabase(int bookId) {
        try (Connection conn = Config.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM kniha WHERE id = ?")) {

            stmt.setInt(1, bookId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String title = rs.getString("nazev");
                    titleTextField.setText(title);

                    String genre = rs.getString("zanr");
                    genresComboBox.setSelectedItem(genre);

                    int year = rs.getInt("rok_vydani");
                    yearField.setValue(year);

                    double price = rs.getDouble("cena");
                    priceField.setValue(price);

                    int quantity = rs.getInt("amount");
                    quantitySpinner.setValue(quantity);

                    String description = rs.getString("popis");
                    description = insertLineBreaks(description, 100);
                    descriptionArea.setText(description);

                    ArrayList<String> authors = loadAuthorsForBook(bookId);

                    int row = 8;
                    for (String author : authors) {
                        JComboBox<String> authorComboBox = new JComboBox<>();
                        fillComboBoxWithAuthors(authorComboBox);
                        authorComboBox.setSelectedItem(author);
                        authorComboBoxes.add(authorComboBox);
                        GridBagConstraints gbc = getGridBagConstraints();
                        gbc.gridx = 1;
                        gbc.gridy = row;
                        add(authorComboBox, gbc);

                        JButton removeAuthorButton = new JButton("X");
                        gbc.gridx = 2;
                        row++;
                        add(removeAuthorButton, gbc);

                        removeAuthorButton.addActionListener(e -> {
                            removeAuthorComboBox(authorComboBox, removeAuthorButton);
                        });
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    private String insertLineBreaks(String text, int lineLength) {
        StringBuilder sb = new StringBuilder(text);
        int offset = 0;
        while (offset < sb.length()) {
            offset = offset + lineLength;
            if (offset < sb.length()) {
                sb.insert(offset, "\n");
            }
        }
        return sb.toString();
    }

    private ArrayList<String> loadAuthorsForBook(int bookId) {
        ArrayList<String> authors = new ArrayList<>();
        try (Connection conn = Config.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT autor_id, jmeno FROM kniha_autor_view WHERE id = ?")) {
            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    authors.add(rs.getString("jmeno"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return authors;
    }

    private void removeAuthorComboBox(JComboBox<String> comboBoxToRemove, JButton buttonToRemove) {
        remove(comboBoxToRemove);
        remove(buttonToRemove);
        authorComboBoxes.remove(comboBoxToRemove);
        revalidate();
        repaint();
    }

    private void addNewAuthorCombobox(GridBagConstraints gbc) {

        JComboBox<String> newAuthorComboBox = new JComboBox<>();
        fillComboBoxWithAuthors(newAuthorComboBox);
        authorComboBoxes.add(newAuthorComboBox);

        gbc.gridx = 1;
        gbc.gridy = 20 + authorComboBoxes.size();
        add(newAuthorComboBox, gbc);

        JButton removeAuthorButton = new JButton("X");
        gbc.gridx = 2;
        add(removeAuthorButton, gbc);

        removeAuthorButton.addActionListener(e -> {
            removeAuthorComboBox(newAuthorComboBox, removeAuthorButton);
        });

        revalidate();
        repaint();
    }

    private void addConfirmButton(GridBagConstraints gbc){
        JButton addConfirmButton = new JButton("Uložit změny");
        gbc.gridx = 1;
        gbc.gridy = 100;
        add(addConfirmButton, gbc);

        addConfirmButton.addActionListener(e -> {

        });
    }

    private void addCancelButton(GridBagConstraints gbc){
        JButton addCancelButton = new JButton("Cancel");
        gbc.gridx = 2;
        gbc.gridy = 100;
        add(addCancelButton, gbc);

        addCancelButton.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            frame.dispose();
        });
    }

}