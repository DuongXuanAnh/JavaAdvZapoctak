package cz.cuni.mff.java.zapoctak.content;

import cz.cuni.mff.java.zapoctak.config.Config;
import cz.cuni.mff.java.zapoctak.global.Notification;
import cz.cuni.mff.java.zapoctak.global.TitleBorder;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AddBook extends JPanel {

    private final JTextField nameField;

    private ArrayList<JComboBox<String>> authorComboBoxes;
    private final JComboBox<String> authorComboBox;
    private JComboBox<String> genresComboBox;
    private final JFormattedTextField priceField;
    private final JFormattedTextField yearField;
    private final JSpinner quantitySpinner;
    private final JTextArea descriptionArea;
//    private final JButton addAuthorButton;

    public AddBook(){
        this.setBorder(TitleBorder.create("Přidat knihu"));

        nameField = new JTextField(20);
        authorComboBox = new JComboBox<>();
        fillComboBoxWithAuthors(authorComboBox);
        authorComboBoxes = new ArrayList<>();
        genresComboBox = new JComboBox<>();
        priceField = new JFormattedTextField(createNumberFormatter());
        yearField = new JFormattedTextField(createIntFormatter());
        quantitySpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        descriptionArea = new JTextArea(10, 20);

        setupLayout();
        setupSubmitButton();
    }

    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = getGridBagConstraints();

        addNameLabelAndField(gbc);
        addAuthorLabelAndComboBox(gbc);
        addAuthorButton(gbc);

        addGenresLabelAndComboBox(gbc);
        addPriceLabelAndField(gbc);
        addYearLabelAndField(gbc);
        addQuantityLabelAndSpinner(gbc);
        addDescriptionLabelAndTextArea(gbc);
    }


    private GridBagConstraints getGridBagConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private void addNameLabelAndField(GridBagConstraints gbc) {
        JLabel nameLabel = new JLabel("Název");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(nameLabel, gbc);

        gbc.gridx = 1;
        add(nameField, gbc);
    }

    private void addAuthorLabelAndComboBox(GridBagConstraints gbc) {
        JLabel nameLabel = new JLabel("Autor");
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(nameLabel, gbc);

        gbc.gridx = 1;
        authorComboBoxes.add(authorComboBox);
        add(authorComboBox, gbc);
    }

    private void addAuthorButton(GridBagConstraints gbc){
        JButton addAuthorButton = new JButton("Přidat autora");
        gbc.gridx = 2;
        gbc.gridy = 2;
        add(addAuthorButton, gbc);

        addAuthorButton.addActionListener(e -> {
            addNewAuthorField(gbc);
        });
    }

    private void addNewAuthorField(GridBagConstraints gbc) {

        JComboBox<String> newAuthorComboBox = new JComboBox<>();
        fillComboBoxWithAuthors(newAuthorComboBox);
        authorComboBoxes.add(newAuthorComboBox);

        gbc.gridx = 1;
        gbc.gridy = 2 + authorComboBoxes.size();
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

    private void removeAuthorComboBox(JComboBox<String> comboBoxToRemove, JButton buttonToRemove) {
        remove(comboBoxToRemove);
        remove(buttonToRemove);
        authorComboBoxes.remove(comboBoxToRemove);
        revalidate();
        repaint();
    }

    private void addGenresLabelAndComboBox(GridBagConstraints gbc) {
        JLabel nameLabel = new JLabel("Žánr");
        gbc.gridx = 0;
        gbc.gridy = 100;
        add(nameLabel, gbc);

        gbc.gridx = 1;

        String[] zanry = {"Sci-fi (vědeckofantastický)", "Romantika", "Thriller", "Detektivka", "Fantasy", "Horor", "Komedie", "Akční", "Drama", "Historický"};
        Arrays.sort(zanry);
        genresComboBox = new JComboBox<>(zanry);
        add(genresComboBox, gbc);
    }

    private void addPriceLabelAndField(GridBagConstraints gbc) {
        JLabel priceLabel = new JLabel("Cena (Kč)");
        gbc.gridx = 0;
        gbc.gridy = 101;
        add(priceLabel, gbc);

        gbc.gridx = 1;
        add(priceField, gbc);
    }


    private void addYearLabelAndField(GridBagConstraints gbc){
        JLabel yearLabel = new JLabel("Rok vydání");
        gbc.gridx = 0;
        gbc.gridy = 102;
        add(yearLabel, gbc);

        gbc.gridx = 1;
        add(yearField, gbc);
    }

    private void addQuantityLabelAndSpinner(GridBagConstraints gbc) {
        JLabel quantityLabel = new JLabel("Počet kusů");
        gbc.gridx = 0;
        gbc.gridy = 103;
        add(quantityLabel, gbc);

        gbc.gridx = 1;
        add(quantitySpinner, gbc);
    }

    private void addDescriptionLabelAndTextArea(GridBagConstraints gbc) {
        JLabel descriptionLabel = new JLabel("Popis:");
        gbc.gridx = 0;
        gbc.gridy = 104;
        add(descriptionLabel, gbc);
        gbc.gridx = 1;
        add(descriptionArea, gbc);
    }

    private void setupSubmitButton() {
        JButton submitButton = new JButton("Přidat knihu");
        GridBagConstraints gbc = getGridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1000;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        add(submitButton, gbc);

        submitButton.addActionListener(e -> {
            submitBook();
        });
    }
    private NumberFormatter createNumberFormatter() {
        NumberFormat format = NumberFormat.getNumberInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Double.class);
        formatter.setMinimum(0.0);
        formatter.setMaximum(Double.MAX_VALUE);
        formatter.setAllowsInvalid(false);
        formatter.setCommitsOnValidEdit(true);
        return formatter;
    }

    private NumberFormatter createIntFormatter() {
        NumberFormat format = NumberFormat.getIntegerInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(0);
        formatter.setMaximum(Integer.MAX_VALUE);
        formatter.setAllowsInvalid(false);
        formatter.setCommitsOnValidEdit(true);
        return formatter;
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

    private void fillComboBoxWithAuthors(JComboBox<String> comboBox) {
        ArrayList<String> authors = loadAuthorsFromDB();
        for (String author : authors) {
            comboBox.addItem(author);
        }
    }

    private void submitBook() {
        String bookName = nameField.getText();
        Double bookPrice = (Double) priceField.getValue();
        Integer bookYear = (Integer) yearField.getValue();
        Integer bookQuantity = (Integer) quantitySpinner.getValue();
        String bookDescription = descriptionArea.getText();
        String bookGenre = (String) genresComboBox.getSelectedItem();

        // Pro každý ComboBox v authorComboBoxes získáme vybranou položku
        ArrayList<String> bookAuthors = new ArrayList<>();
        for (JComboBox<String> authorComboBox : authorComboBoxes) {
            bookAuthors.add((String) authorComboBox.getSelectedItem());
        }

        if (bookName.isEmpty() || bookGenre.isEmpty() || bookDescription.isEmpty() || bookAuthors.isEmpty() || bookPrice == null || bookYear == null || bookQuantity == null) {
            Notification.showErrorMessage("Všechna pole musí být vyplněna");
            return;
        }

        if (hasDuplicateAuthors(authorComboBoxes)) {
            Notification.showErrorMessage("Autoři nesmí být stejní");
            return;
        }

        // Předpokládáme, že máte metodu pro vložení knihy do databáze, která přijímá tyto parametry
        insertBookIntoDB(bookName, bookAuthors, bookGenre, bookPrice, bookYear, bookQuantity, bookDescription);
    }

    private void insertBookIntoDB(String name, ArrayList<String> authorNames, String genre, double price, int year, int quantity, String description) {

        try (Connection conn = Config.getConnection();
             PreparedStatement insertStatement = conn.prepareStatement("INSERT INTO `kniha`(nazev, rok_vydani, cena, zanr, amount, popis) VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {

            insertStatement.setString(1, name);
            insertStatement.setInt(2, year);
            insertStatement.setDouble(3, price);
            insertStatement.setString(4, genre);
            insertStatement.setInt(5, quantity);
            insertStatement.setString(6, description);

            int rowsInserted = insertStatement.executeUpdate();

            if (rowsInserted > 0) {
                // získání ID nové knihy
                ResultSet generatedKeys = insertStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int bookId = generatedKeys.getInt(1);

                    // vložení odpovídajících záznamů do tabulky kniha_autor
                    for (String authorName : authorNames) {
                        insertBookAuthorRecord(conn, bookId, authorName);
                    }
                }
                Notification.showSuccessMessage("Kniha \"" + name + "\" byla úspěšně přidána");
            }
        } catch (SQLException ex) {
            System.out.println("Error inserting book: " + ex.getMessage());
            Notification.showErrorMessage("Chyba, zkuste to znovu nebo kontaktujte IT oddělení");
        }
    }

    private void insertBookAuthorRecord(Connection conn, int bookId, String authorName) throws SQLException {
        try (PreparedStatement authorIdStatement = conn.prepareStatement("SELECT id FROM `autor` WHERE jmeno = ?")) {
            authorIdStatement.setString(1, authorName);
            ResultSet authorIdResult = authorIdStatement.executeQuery();
            if (authorIdResult.next()) {
                int authorId = authorIdResult.getInt("id");

                try (PreparedStatement bookAuthorInsertStatement = conn.prepareStatement("INSERT INTO kniha_autor(id_kniha, id_autor) VALUES (?,?)")) {
                    bookAuthorInsertStatement.setInt(1, bookId);
                    bookAuthorInsertStatement.setInt(2, authorId);
                    int bookAuthorRowsInserted = bookAuthorInsertStatement.executeUpdate();
                    if (bookAuthorRowsInserted > 0) {
                        System.out.println("New book_author record inserted successfully!");
                    }
                } catch (SQLException ex) {
                    System.out.println("Error inserting book_author record: " + ex.getMessage());
                    Notification.showErrorMessage("Chyba, zkuste to znovu nebo kontaktujte IT oddělení");
                }
            } else {
                System.out.println("Author not found: " + authorName);
                Notification.showErrorMessage(authorName + "Nebyl nalezen");
            }
        } catch (SQLException ex) {
            System.out.println("Error getting author ID: " + ex.getMessage());
            Notification.showErrorMessage("Chyba, zkuste to znovu nebo kontaktujte IT oddělení");
        }
    }

    private boolean hasDuplicateAuthors(ArrayList<JComboBox<String>> authorComboBoxes) {
        Set<String> authorNames = new HashSet<>();

        for (JComboBox<String> comboBox : authorComboBoxes) {
            String selectedAuthor = (String) comboBox.getSelectedItem();
            if (selectedAuthor == null || selectedAuthor.isEmpty()) {
                continue;
            }
            if (!authorNames.add(selectedAuthor)) {
                return true;
            }
        }
        return false;
    }


}
