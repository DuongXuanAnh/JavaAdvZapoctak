package cz.cuni.mff.java.zapoctak.content;

import cz.cuni.mff.java.zapoctak.config.Config;
import cz.cuni.mff.java.zapoctak.global.Author;
import cz.cuni.mff.java.zapoctak.global.Notification;
import cz.cuni.mff.java.zapoctak.global.TitleBorder;
import cz.cuni.mff.java.zapoctak.newWindow.BookDetail;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * The {@code Books} class represents a panel within the application that displays
 * and manages information related to books. This class includes functionality to
 * view and filter books, select authors from a combo box, manage the layout and
 * content of a table displaying book information, and interact with other components for managing book data.
 */
public class Books extends JPanel {

    private final JTextField titleField;
    private final JComboBox<Author> authorComboBox;
    private final DefaultTableModel tableModel;

    private final JTable bookTable;
    private final Author noneAuthor = new Author(-1, "");

    public Books(){
        this.setBorder(TitleBorder.create("Knihy"));

        titleField = new JTextField(30);

        authorComboBox = new JComboBox<>();
        authorComboBox.addItem(noneAuthor);
        fillComboBoxWithAuthors(authorComboBox);

        tableModel = new DefaultTableModel();
        bookTable = createTable();

        setupLayout();
        setupSubmitButton();
        setupListeners();
        try {
            updateTableModel();
        } catch (SQLException e) {
            Notification.showErrorMessage("Problém se připojení k databázi");
            System.out.println(e);
        }
    }
    /**
     * Sets up the layout for the panel, configuring the appearance and placement of
     * components within the panel using a {@code GridBagLayout}. The layout consists
     * of a title label and field, an author name label and combo box, and a table to
     * display the books.
     *
     * <p>The placement of these components is defined using {@code GridBagConstraints},
     * which is obtained from the {@code getGridBagConstraints()} method.
     */
    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = getGridBagConstraints();

        addTitleLabelAndField(gbc);
        addNameLabelAndField(gbc);
        addTable(gbc);
    }
    /**
     * Initializes the listeners for various interactive components within the panel.
     * This includes setting up listeners for changes to the title field, author combo box,
     * and handling double-click events on the table.
     *
     * <p>The specific listeners are configured in the {@code setupTitleFieldListener},
     * {@code setupAuthorComboBoxListener}, and {@code setupDoubleClickListener} methods.
     */
    private void setupListeners() {
        setupTitleFieldListener();
        setupAuthorComboBoxListener();
        setupDoubleClickListener();
    }
    /**
     * Returns a configured {@code GridBagConstraints} object that defines constraints
     * for layout components. The returned constraints have horizontal filling
     * and consistent insets, which determine the spacing between neighboring components.
     *
     * @return the {@code GridBagConstraints} object with set parameters for layout
     */
    private GridBagConstraints getGridBagConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    /**
     * Adds a title label and text field for entering the title of a book to the panel.
     * The method takes {@code GridBagConstraints} as a parameter to specify the placement
     * and appearance of the label and text field within the layout.
     *
     * @param gbc the {@code GridBagConstraints} object that defines layout constraints
     */
    private void addTitleLabelAndField(GridBagConstraints gbc) {
        JLabel authorLabel = new JLabel("Název knihy: ");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(authorLabel, gbc);

        gbc.gridx = 1;
        add(titleField, gbc);
    }
    /**
     * Adds a name label and a combo box for selecting the author of a book to the panel.
     * The method takes {@code GridBagConstraints} as a parameter to specify the placement
     * and appearance of the label and combo box within the layout.
     *
     * @param gbc the {@code GridBagConstraints} object that defines layout constraints
     */
    private void addNameLabelAndField(GridBagConstraints gbc) {
        JLabel authorLabel = new JLabel("Autor: ");
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(authorLabel, gbc);

        gbc.gridx = 1;
        add(authorComboBox, gbc);
    }
    /**
     * Creates a {@code JTable} for displaying books with columns for ID, title, number of copies, and price.
     * The table's selection model is configured to listen for row selection events, providing the ID of the selected book.
     * The table does not allow direct editing of the cells by the user.
     *
     * @return the newly created {@code JTable} configured with the appropriate columns and behavior
     */
    private JTable createTable() {
        String[] columnNames = {"ID", "Název", "Počet kusů", "Cena"};
        tableModel.setColumnIdentifiers(columnNames);
        JTable table = new JTable(tableModel);
        table.setDefaultEditor(Object.class, null);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { // Ensure we only handle the final event, not intermediate ones
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) { // Check if any row is selected
                    Object idObject = table.getValueAt(selectedRow, 0);
                    if (idObject != null) {
                        int bookId = (int) idObject;
                    }
                }
            }
        });

        return table;
    }
    /**
     * Updates the table model by querying the database for books, filtered by title and author if specified.
     * The data from the result set is extracted and added as rows to the table model.
     */
    public void updateTableModel() throws SQLException {
        String titleQuery = titleField.getText().trim();
        Author selectedAuthor = (Author) authorComboBox.getSelectedItem();

        String sql;
        if (selectedAuthor == noneAuthor) {
            sql = "SELECT id, nazev, amount, cena FROM kniha WHERE nazev LIKE ?";
        } else {
            sql = "SELECT id, nazev, amount, cena FROM kniha_autor_view WHERE nazev LIKE ? AND autor_id = ?";
        }

        try (Connection conn = Config.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + titleQuery + "%");

            if (selectedAuthor != noneAuthor) {
                stmt.setInt(2, selectedAuthor.getId());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                tableModel.setRowCount(0); // Clear existing data

                while (rs.next()) {
                    int id = rs.getInt("id");
                    String title = rs.getString("nazev");
                    int amount = rs.getInt("amount");
                    double cena = rs.getDouble("cena");

                    tableModel.addRow(new Object[]{id, title, amount, cena});
                }
            }
        }
        revalidate();
        repaint();
    }
    /**
     * Adds a table with book details to the panel, displaying information such as ID, title, number of copies, and price.
     * The table is placed inside a scroll pane for ease of navigation.
     *
     * @param gbc the {@code GridBagConstraints} object that defines layout constraints
     */
    private void addTable(GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0; // add this

        JScrollPane scrollPane = new JScrollPane(bookTable);
        add(scrollPane, gbc);
    }
    /**
     * Sets up and adds a submit button to the panel that adds the selected book to the cart.
     * The button's action listener is configured to call the {@link #addBookToCart()} method when clicked.
     */
    private void setupSubmitButton() {
        JButton submitButton = new JButton("Přidat knihu do košíku");
        GridBagConstraints gbc = getGridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        add(submitButton, gbc);

        submitButton.addActionListener(e -> {
            addBookToCart();
        });
    }
    /**
     * Adds the selected book from the table to the cart by obtaining its ID and writing it to a file.
     * The method also checks if the selected book has an amount greater than 0 and shows appropriate error messages
     * if no book is selected or if the selected book is not available.
     */
    private void addBookToCart(){
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow != -1) {
            Object idObject = bookTable.getValueAt(selectedRow, 0);
            Object amountObject = bookTable.getValueAt(selectedRow, 2); // Get the amount of the book
            if (idObject != null) {
                if (amountObject != null && (int) amountObject > 0) { // Check if the amount is greater than 0
                    int bookId = (int) idObject;
                    writeBookIdToFile(bookId);
                } else {
                    Notification.showErrorMessage("Tato kniha již není k dispozici.");
                }
            } else {
                Notification.showErrorMessage("Vyberte řádek s platným ID knihy.");
            }
        } else {
            Notification.showErrorMessage("Nebyla vybrána žádná kniha. Vyberte knihu, prosím.");
        }
    }
    /**
     * Writes the provided book ID to a file, creating the file if it doesn't already exist.
     * If the book ID is already in the file, it shows an error message.
     *
     * @param bookId the ID of the book to be written to the file
     */
    private void writeBookIdToFile(int bookId) {
        try {
            File file = new File("bookIDs.txt");

            if (!file.exists()) {
                file.createNewFile();
            }

            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.equals(String.valueOf(bookId))) {
                    JOptionPane.showMessageDialog(this, "Toto ID knihy už bylo přidáno.");
                    return;
                }
            }
            scanner.close();

            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);
            pw.println(bookId);
            pw.close();
            Notification.showSuccessMessage("Kniha byla přidána do košíku");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    /**
     * Sets up a listener for changes to the title field. Calls {@link #printTitleFieldChange()} for all changes.
     */
    private void setupTitleFieldListener() {
        titleField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                printTitleFieldChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                printTitleFieldChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                printTitleFieldChange();
            }
        });
    }
    /**
     * Sets up a listener for the author combo box, printing the selected author's ID and updating the table model.
     */
    private void setupAuthorComboBoxListener() {
        authorComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    Author selectedAuthor = (Author) authorComboBox.getSelectedItem();
                    System.out.println("authorComboBox changed: " + selectedAuthor.getId());
                    try {
                        updateTableModel();
                    } catch (SQLException ex) {
                        Notification.showErrorMessage("Problém se připojení k databázi");
                        System.out.println(e);
                    }
                }
            }
        });
    }
    /**
     * Prints the current text of the title field and updates the table model.
     */
    private void printTitleFieldChange() {
        System.out.println("titleField changed: " + titleField.getText());
        try {
            updateTableModel();
        } catch (SQLException e) {
            Notification.showErrorMessage("Problém se připojení k databázi");
            System.out.println(e);
        }
    }
    /**
     * Populates the given combo box with authors from the database.
     *
     * @param comboBox the combo box to fill with authors
     */
    private void fillComboBoxWithAuthors(JComboBox<Author> comboBox) {
        ArrayList<Author> authors = null;
        try {
            authors = loadAuthorsFromDB();
            for (Author author : authors) {
                comboBox.addItem(author);
            }
        } catch (SQLException e) {
            Notification.showErrorMessage("Nepodařilo se načíst autory z databáze");
            System.out.println(e);
        }

    }
    /**
     * Retrieves a list of authors from the database.
     *
     * @return the list of authors
     */
    private ArrayList<Author> loadAuthorsFromDB() throws SQLException {
        ArrayList<Author> authors = new ArrayList<>();
        try (Connection conn = Config.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, jmeno FROM autor")) {

            while (rs.next()) {
                authors.add(new Author(rs.getInt("id"), rs.getString("jmeno")));
            }
        }
        return authors;
    }

    /**
     * Sets up a double-click listener for the book table, calling {@link #handleDoubleClick()} when a double-click is detected.
     */
    private void setupDoubleClickListener() {
        bookTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleDoubleClick();
                }
            }
        });
    }
    /**
     * Handles a double click on the book table, extracting the selected book's ID and calling {@link #openDifferentPanel(int)}.
     */
    private void handleDoubleClick() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow != -1) {
            Object idObject = bookTable.getValueAt(selectedRow, 0);
            if (idObject != null) {
                int bookId = (int) idObject;
                openDifferentPanel(bookId);
            }
        }
    }
    /**
     * Opens a new panel displaying details for the given book ID. The panel is displayed in a new JFrame.
     *
     * @param bookId the ID of the book for which to display details
     */
    private void openDifferentPanel(int bookId) {
        // Create an instance of the BookDetail class
        BookDetail bookDetail = new BookDetail(bookId, this);

        // Create a new JFrame to display the BookDetail content
        JFrame bookDetailFrame = new JFrame("Book Details");
        bookDetailFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        bookDetailFrame.add(bookDetail);
        bookDetailFrame.pack();
        bookDetailFrame.setVisible(true);

    }

}
