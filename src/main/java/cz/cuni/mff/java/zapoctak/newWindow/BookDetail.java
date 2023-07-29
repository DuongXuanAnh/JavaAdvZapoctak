package cz.cuni.mff.java.zapoctak.newWindow;

import cz.cuni.mff.java.zapoctak.config.Config;
import cz.cuni.mff.java.zapoctak.content.Books;
import cz.cuni.mff.java.zapoctak.global.Notification;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BookDetail extends JPanel {
    private final JTextField titleTextField;

    private JComboBox<String> genresComboBox;
    private final JFormattedTextField yearField;
    private final JFormattedTextField priceField;
    private final JSpinner quantitySpinner;
    private final JTextArea descriptionArea;
    private final ArrayList<JComboBox<String>> authorComboBoxes;
    private JComboBox<String> authorComboBox;

    private final int bookId;
    private Books book;

    /**
     * Constructs a new BookDetail panel for editing book information.
     *
     * @param bookId The ID of the book to be edited.
     * @param book   The parent Books panel to which this BookDetail belongs.
     */
    public BookDetail(int bookId, Books book) {
        this.bookId = bookId;
        titleTextField = new JTextField(50);
        genresComboBox = new JComboBox<>();
        priceField = new JFormattedTextField(createNumberFormatter());
        yearField = new JFormattedTextField(createIntFormatter());
        quantitySpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        descriptionArea = new JTextArea(10, 20);
        authorComboBox = new JComboBox<>();
        fillComboBoxWithAuthors(authorComboBox);
        authorComboBoxes = new ArrayList<>();

        this.book = book;

        setupLayout();
        loadDataFromDatabase(bookId);

    }
    /**
     * This method populates a given JComboBox with author names retrieved from the database.
     *
     * @param authorComboBox The JComboBox to be filled with author names.
     */
    private void fillComboBoxWithAuthors(JComboBox<String> authorComboBox) {
        ArrayList<String> authors = loadAuthorsFromDB();
        for (String author : authors) {
            authorComboBox.addItem(author);
        }
    }

    /**
     * This class provides methods to interact with the database to retrieve a list of authors.
     * It establishes a connection to the database using the {@link Config} class and executes
     * an SQL query to fetch author names from the "autor" table.
     */
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

    /**
        * setupLayout
     */
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
    /**
     * Returns a new instance of GridBagConstraints with predefined settings for grid layout.
     *
     * @return The GridBagConstraints with predefined settings.
     */
    private GridBagConstraints getGridBagConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    /**
     * Adds a label "Název" (Title) and a JTextField for entering the title of the book to the GUI panel at the specified GridBagConstraints position.
     *
     * @param gbc The GridBagConstraints specifying the position for the label and JTextField.
     */
    private void addTitleLabelAndField(GridBagConstraints gbc) {
        JLabel nameLabel = new JLabel("Název");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(nameLabel, gbc);

        gbc.gridx = 1;
        add(titleTextField, gbc);
    }

    /**
     * Adds a label "Žánr" (Genre) and a JComboBox for selecting the genre of the book to the GUI panel at the specified GridBagConstraints position.
     *
     * @param gbc The GridBagConstraints specifying the position for the label and JComboBox.
     */
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
    /**
     * Adds a label "Rok vydání" and a JSpinner for entering the year of publication to the GUI panel at the specified GridBagConstraints position.
     *
     * @param gbc The GridBagConstraints specifying the position for the label and JSpinner.
     */
    private void addYearLabelAndField(GridBagConstraints gbc){
        JLabel yearLabel = new JLabel("Rok vydání");
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(yearLabel, gbc);

        gbc.gridx = 1;
        add(yearField, gbc);
    }
    /**
     * Adds a label "Cena (Kč)" and a JTextField for entering the price of the book to the GUI panel at the specified GridBagConstraints position.
     *
     * @param gbc The GridBagConstraints specifying the position for the label and JTextField.
     */
    private void addPriceLabelAndField(GridBagConstraints gbc) {
        JLabel priceLabel = new JLabel("Cena (Kč)");
        gbc.gridx = 0;
        gbc.gridy = 4;
        add(priceLabel, gbc);

        gbc.gridx = 1;
        add(priceField, gbc);
    }
    /**
     * Adds a label "Počet kusů" and a JSpinner for selecting the quantity of books to the GUI panel at the specified GridBagConstraints position.
     *
     * @param gbc The GridBagConstraints specifying the position for the label and JSpinner.
     */
    private void addQuantityLabelAndSpinner(GridBagConstraints gbc) {
        JLabel quantityLabel = new JLabel("Počet kusů");
        gbc.gridx = 0;
        gbc.gridy = 5;
        add(quantityLabel, gbc);

        gbc.gridx = 1;
        add(quantitySpinner, gbc);
    }
    /**
     * Adds a label "Popis:" and a JTextArea for entering book description to the GUI panel at the specified GridBagConstraints position.
     *
     * @param gbc The GridBagConstraints specifying the position for the label and JTextArea.
     */
    private void addDescriptionLabelAndTextArea(GridBagConstraints gbc) {
        JLabel descriptionLabel = new JLabel("Popis:");
        gbc.gridx = 0;
        gbc.gridy = 6;
        add(descriptionLabel, gbc);
        gbc.gridx = 1;
        add(descriptionArea, gbc);
    }
    /**
     * Adds a label "Autor" and a JComboBox for selecting authors to the GUI panel at the specified GridBagConstraints position.
     *
     * @param gbc The GridBagConstraints specifying the position for the label and JComboBox.
     */
    private void addAuthorLabelAndComboBox(GridBagConstraints gbc) {
        JLabel nameLabel = new JLabel("Autor");
        gbc.gridx = 0;
        gbc.gridy = 7;
        add(nameLabel, gbc);
    }

    /**
     * Adds an "Add Author" button to the GUI panel at the specified GridBagConstraints position.
     * This button allows the user to add a new author ComboBox to the GUI when clicked.
     *
     * @param gbc The GridBagConstraints specifying the position for the "Add Author" button.
     */
    private void addAuthorButton(GridBagConstraints gbc){
        JButton addAuthorButton = new JButton("Přidat autora");
        gbc.gridx = 1;
        gbc.gridy = 7;
        add(addAuthorButton, gbc);

        addAuthorButton.addActionListener(e -> {
            addNewAuthorCombobox(gbc);
        });
    }
    /**
     * Loads data for a specific book from the database and populates the corresponding input fields.
     *
     * @param bookId The ID of the book to load data for.
     */
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
    /**
     * Inserts line breaks into the given text at regular intervals to fit within the specified line length.
     *
     * @param text       The text to insert line breaks into.
     * @param lineLength The maximum length of each line after inserting line breaks.
     * @return The modified text with line breaks inserted.
     */
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
    /**
     * Loads the list of authors associated with a book from the database.
     *
     * @param bookId The ID of the book for which to retrieve the authors.
     * @return An ArrayList containing the names of authors associated with the book.
     */
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
    /**
     * Removes an author combobox and its associated remove button from the panel.
     *
     * @param comboBoxToRemove The author combobox to remove from the panel.
     * @param buttonToRemove   The remove button associated with the author combobox.
     */
    private void removeAuthorComboBox(JComboBox<String> comboBoxToRemove, JButton buttonToRemove) {
        remove(comboBoxToRemove);
        remove(buttonToRemove);
        authorComboBoxes.remove(comboBoxToRemove);
        revalidate();
        repaint();
    }

    /**
     * Adds a new author combobox to the panel with the specified GridBagConstraints.
     * The combobox is filled with authors, and a remove button is added next to it.
     * When the remove button is clicked, the associated author combobox is removed from the panel.
     *
     * @param gbc The GridBagConstraints to use for placing the combobox and remove button in the panel.
     */
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
    /**
     * Adds a confirm button to the panel with the specified GridBagConstraints.
     * When the button is clicked, the method calls the confirmBookUpdate() method to save the changes to the book.
     *
     * @param gbc The GridBagConstraints to use for placing the button in the panel.
     */
    private void addConfirmButton(GridBagConstraints gbc){
        JButton addConfirmButton = new JButton("Uložit změny");
        gbc.gridx = 1;
        gbc.gridy = 100;
        add(addConfirmButton, gbc);

        addConfirmButton.addActionListener(e -> {

            confirmBookUpdate();

        });
    }
    /**
     * Confirms the update of the book with the provided information and saves the changes to the database.
     * If any required field is empty or there are duplicate authors, an error notification is shown.
     * Otherwise, the book's information is updated in the database, and the number of authors is printed to the console.
     */
    public void confirmBookUpdate(){
        String bookName = titleTextField.getText();
        Double bookPrice = (Double) priceField.getValue();
        Integer bookYear = (Integer) yearField.getValue();
        Integer bookQuantity = (Integer) quantitySpinner.getValue();
        String bookDescription = descriptionArea.getText();
        String bookGenre = (String) genresComboBox.getSelectedItem();
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

        updateBookInDB(bookId, bookName, bookAuthors, bookGenre, bookPrice, bookYear, bookQuantity, bookDescription);

        System.out.println(bookAuthors.size());
    }
    /**
     * Updates the book in the database with the provided information.
     *
     * @param bookId The ID of the book to be updated.
     * @param name The name of the book.
     * @param authorNames An ArrayList containing the names of the authors associated with the book.
     * @param genre The genre of the book.
     * @param price The price of the book.
     * @param year The year of publication of the book.
     * @param quantity The quantity of the book in stock.
     * @param description The description of the book.
     */
    private void updateBookInDB(int bookId, String name, ArrayList<String> authorNames, String genre, double price, int year, int quantity, String description) {
        try (Connection conn = Config.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE kniha SET nazev=?, zanr=?, cena=?, rok_vydani=?, amount=?, popis=? WHERE id=?")) {

            // Set the parameters for the prepared statement
            stmt.setString(1, name);
            stmt.setString(2, genre);
            stmt.setDouble(3, price);
            stmt.setInt(4, year);
            stmt.setInt(5, quantity);
            stmt.setString(6, description);
            stmt.setInt(7, bookId);

            // Execute the update query
            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                updateAuthorsForBook(bookId, authorNames);
                System.out.println("Book updated successfully!");
                Notification.showSuccessMessage("Kniha byla upravena");
                book.updateTableModel();


            } else {
                System.out.println("Failed to update the book!");
                Notification.showSuccessMessage("Nastala chyba, zkuste to znovu!");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    /**
     * Updates the authors associated with a book in the database.
     *
     * @param bookId The ID of the book whose authors are to be updated.
     * @param authorNames An ArrayList containing the names of the authors to be associated with the book.
     */
    private void updateAuthorsForBook(int bookId, ArrayList<String> authorNames) {
        try (Connection conn = Config.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM kniha_autor WHERE id_kniha=?")) {

            // Delete all existing authors associated with the book
            stmt.setInt(1, bookId);
            stmt.executeUpdate();

            // Insert the new authors into the kniha_autor table
            for (String authorName : authorNames) {
                insertAuthorForBook(bookId, authorName);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    /**
     * Inserts an author for a book in the database.
     *
     * @param bookId The ID of the book to associate with the author.
     * @param authorName The name of the author to be inserted.
     */
    private void insertAuthorForBook(int bookId, String authorName) {
        try (Connection conn = Config.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO kniha_autor (id_kniha, id_autor) VALUES (?, (SELECT id FROM autor WHERE jmeno=?))")) {

            stmt.setInt(1, bookId);
            stmt.setString(2, authorName);
            stmt.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    /**
     * Adds a "Cancel" button to the panel at the specified GridBagConstraints.
     * When clicked, the "Cancel" button will close the window containing the panel.
     *
     * @param gbc The GridBagConstraints specifying the position of the "Cancel" button.
     */
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
    /**
     * Checks whether there are any duplicate authors selected in the provided list of author ComboBoxes.
     *
     * @param authorComboBoxes An ArrayList of JComboBoxes containing author names.
     * @return true if there are duplicate authors selected, false otherwise.
     */
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

    /**
     Creates and returns a NumberFormatter for formatting the price field.
     @return NumberFormatter for formatting the price field
     */
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

    /**
     Creates and returns a NumberFormatter for formatting the year field.
     @return NumberFormatter for formatting the year field
     */
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

}