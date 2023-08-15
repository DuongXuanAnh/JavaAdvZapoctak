package cz.cuni.mff.java.zapoctak.content;

import cz.cuni.mff.java.zapoctak.config.Config;
import cz.cuni.mff.java.zapoctak.global.BookData;
import cz.cuni.mff.java.zapoctak.global.Notification;
import com.toedter.calendar.JDateChooser;
import cz.cuni.mff.java.zapoctak.global.TitleBorder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * The AddDocument class represents a JPanel that allows users to add a new document.
 */
public class AddDocument extends JPanel {

    JComboBox<String> typeComboBox;
    JDateChooser dateChooser;
    JTextField customerIdField;

    JLabel nameLabel;
    JLabel birthDateLabel;

    boolean validCustomer;

    ArrayList<BookData> chosenBooks = new ArrayList<>();

    ArrayList<JSpinner> spinners = new ArrayList<>();
    JLabel totalPriceDisplayed;
    /**
     * Constructs a new AddDocument panel, initializing all required components
     * for adding a document and setting up the layout. The panel allows the user
     * to create an order either for borrowing or purchasing books.
     */
    public AddDocument(){
        this.setBorder(TitleBorder.create("Vytvořit objednávku"));
        typeComboBox = new JComboBox<>(new String[]{"Půjčit", "Koupit"});
        dateChooser = new JDateChooser();
        customerIdField = new JTextField(20);
        totalPriceDisplayed = new JLabel("");
        setupLayout();
        setupSubmitButton();
    }
    /**
     * Initializes the "Přidat doklad" (Add Document) button and configures its layout and action listener.
     * When the button is pressed, it calls the {@code submitDocument} method to handle the submission of the document.
     */
    private void setupSubmitButton() {
        JButton submitDocumentButton = new JButton("Přidat doklad");
        GridBagConstraints gbc = getGridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 51;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        add(submitDocumentButton, gbc);

        submitDocumentButton.addActionListener(e -> {
            submitDocument();
        });
    }

    /**
     * Submits the document by performing a series of validations and operations.
     * It calculates the total price, validates the selected date and option, formats the date,
     * and inserts the document along with its associated items and customer information.
     *
     * <p>The method also handles various error cases such as empty cart, invalid date, and non-numeric customer ID,
     * providing appropriate error notifications to the user.</p>
     *
     * <p>If the document is successfully created, it resets the fields and notifies the user of success.</p>
     */
    private void submitDocument() {
        double totalPrice = calculateTotalPrice();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        if(totalPrice == 0){
            Notification.showErrorMessage("Košík je prázdný");
            return;
        }

        Date currentDate = new Date();
        Date chosenDate = dateChooser.getDate();

        String selectedOption = (String) typeComboBox.getSelectedItem();

        if(selectedOption.equals("Půjčit") && chosenDate == null) {
            Notification.showErrorMessage("Vyberte prosím datum");
            return;
        }

        if(chosenDate != null && chosenDate.before(currentDate)) {
            Notification.showErrorMessage("Vybraný den musí být v budoucnosti");
            return;
        }

        if(nameLabel == null || birthDateLabel== null || !validCustomer){
            Notification.showErrorMessage("Neplatný zákazník");
            return;
        }

        String dateRentTo = selectedOption.equals("Koupit") ? null : format.format(chosenDate);

        int customerID;

        try {
            customerID = Integer.parseInt(customerIdField.getText());
        } catch (NumberFormatException e) {
            Notification.showErrorMessage("Customer ID musí být číslo");
            return;
        }

        int documentId = 0;
        try {
            documentId = insertDocument(totalPrice, dateRentTo);
        } catch (SQLException e) {
            Notification.showErrorMessage("Nastal problém při vložení dokumentu, zkuste to znovu nebo informujte IT oddělení");
            System.out.println(e);
        }

        if (documentId != -1) {
            try {
                insertDocumentItems(documentId);
            } catch (SQLException e) {
                Notification.showErrorMessage("Nastal problém při vložení dokumentu, zkuste to znovu nebo informujte IT oddělení");
                System.out.println(e);
            }
            insertDocumentCustomer(documentId, customerID);
            resetFields();
            Notification.showSuccessMessage("Objednávka byla úspěšně vytvořena.");
        }
    }


    /**
     * Configures the layout of the panel, setting it to {@link GridBagLayout}.
     * Calls other methods to add components like combo boxes, date choosers, labels, and spinners,
     * and sets their positions within the layout using {@link GridBagConstraints}.
     */
    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = getGridBagConstraints();

        addTypeAndComboBox(gbc);
        addDateAndDateChooser(gbc);
        addCustomerIdAndLabel(gbc);
        addChosenBookAndAmountSpinner(gbc);
        displayTotalPrice(gbc);
    }

    /**
     * Adds the total price label and the total price value to the layout.
     * It utilizes {@link GridBagConstraints} to set the grid positions for both components
     * and triggers an update to the displayed total price.
     *
     * @param gbc the {@link GridBagConstraints} object used to set the layout constraints
     */
    private void displayTotalPrice(GridBagConstraints gbc) {
        JLabel totalPriceLabel = new JLabel("Celková cena:");
        gbc.gridx = 0;
        gbc.gridy = 50;
        add(totalPriceLabel, gbc);

        updateTotalPrice();
        gbc.gridx = 1;
        add(totalPriceDisplayed, gbc);
    }

    /**
     * Displays the chosen book in the layout with its title, price, and a spinner to select the quantity.
     * The method also adds a button to remove the chosen book from the order.
     *
     * @param gbc           the {@link GridBagConstraints} object used to set the layout constraints
     * @param book          the {@link BookData} object containing the information of the selected book
     * @param positionIndex the position index in the layout for displaying the chosen book
     */
    private void showChosenBook(GridBagConstraints gbc, BookData book, int positionIndex){
        JLabel chosenBook = new JLabel("* " + book.getTitle() + " - " + book.getPrice() + "Kč");
        gbc.gridx = 0;
        gbc.gridy = 7 + positionIndex;
        add(chosenBook, gbc);

        SpinnerModel model = new SpinnerNumberModel(1, 1, book.getAmount(), 1);
        JSpinner spinner = new JSpinner(model);
        spinners.add(spinner);
        spinner.addChangeListener(e -> updateTotalPrice());
        gbc.gridx = 1;
        gbc.gridy = 7 + positionIndex;
        add(spinner, gbc);

        JButton removeOrderBookButton = new JButton("X");
        removeOrderBookButton.addActionListener(e -> {
            removeOrderBook(book, chosenBook, spinner, removeOrderBookButton);
        });
        gbc.gridx = 2;
        gbc.gridy = 7 + positionIndex;
        add(removeOrderBookButton, gbc);
    }

    /**
     * Removes the chosen book from the order. It takes care of updating the UI by removing the associated components,
     * revalidating and repainting the container, and updating the total price.
     * It also deletes the book's ID from a file.
     *
     * @param book                the {@link BookData} object containing the information of the book to be removed
     * @param chosenBook          the {@link JLabel} displaying the chosen book
     * @param spinner             the {@link JSpinner} for selecting the book's quantity
     * @param removeOrderBookButton the {@link JButton} for removing the chosen book
     */
    private void removeOrderBook(BookData book, JLabel chosenBook, JSpinner spinner, JButton removeOrderBookButton) {
        chosenBooks.remove(book);
        spinners.remove(spinner);
        remove(chosenBook);
        remove(spinner);
        updateTotalPrice();
        remove(removeOrderBookButton);
        revalidate();
        repaint();
        deleteBookIDFromFile(book.getId());
    }

    /**
     * Deletes the specified book ID from the 'bookIDs.txt' file.
     * It does this by creating a temporary file that excludes the book ID and then replacing the original file.
     *
     * @param bookId the ID of the book to be deleted from the file
     */
    private void deleteBookIDFromFile(int bookId) {
        try {
            Path filePath = Paths.get("bookIDs.txt");
            Path tempFilePath = Paths.get("tempBookIDs.txt");

            // Read all lines from the original file, excluding the one with the bookId
            Files.write(tempFilePath, Files.lines(filePath)
                    .filter(line -> !line.trim().equals(Integer.toString(bookId)))
                    .collect(Collectors.toList()));

            // Replace the original file with the updated one
            Files.delete(filePath);
            Files.move(tempFilePath, filePath);
        } catch (IOException e) {
            System.out.println("Nastal problém při mazání knihy z 'bookIDs.txt'.");
            e.printStackTrace();
        }
    }

    /**
     * Adds the label for chosen books to the layout and then reads and displays the chosen books from a file.
     * If the list of chosen books is empty, it prints a message to the console.
     * Otherwise, it calls the {@link #showChosenBook} method for each book, passing the correct position index.
     *
     * @param gbc the {@link GridBagConstraints} object used to set the layout constraints
     */
    private void addChosenBookAndAmountSpinner(GridBagConstraints gbc) {
        JLabel ChosenBookTitleLabel = new JLabel("Vybrané knihy:");
        gbc.gridx = 0;
        gbc.gridy = 6;
        add(ChosenBookTitleLabel, gbc);

        readChosenBookFromFile();

        if (chosenBooks.isEmpty()) {
            System.out.println("Seznam vybraných knih je prázdný.");
        } else {
            int positionIndex = 0;
            for (BookData book : chosenBooks) {
                showChosenBook(gbc, book, positionIndex);
                positionIndex++;
            }
        }
    }

    /**
     * Adds a customer ID label and corresponding text field to the layout.
     *
     * @param gbc the {@link GridBagConstraints} object used to set the layout constraints
     */
    private void addCustomerIdAndLabel(GridBagConstraints gbc) {
        JLabel customerIDLabel = new JLabel("ID zákaznika:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(customerIDLabel, gbc);

        gbc.gridx = 1;
        add(customerIdField, gbc);

        customerIdField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        removeOldCustomerDetails();
                        showCorrespondingCustomer();
                    } catch (SQLException e) {
                        Notification.showErrorMessage("Nastal problém při načítání zákazniků, zkuste to znovu nebo informujte IT oddělení");
                        System.out.println(e);
                    }
                }
            }
        });
    }

    /**
     * Fetches and displays the details of a customer with a given ID.
     * This method attempts to retrieve the name and birthdate of a customer
     * based on the ID entered into the customerIdField. If a customer with
     * the provided ID is found, their details are displayed on the GUI.
     * Otherwise, a "Not found" message is shown for both the name and birthdate.
     *
     * @throws SQLException if there's an error accessing the database.
     */
    private void showCorrespondingCustomer() throws SQLException {
        String idString = customerIdField.getText();

        if(idString.isEmpty()){
            Notification.showErrorMessage("Zadejte ID zákazníka");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idString);
        } catch (NumberFormatException e) {
            Notification.showErrorMessage("Zadané ID není platné");
            return;
        }

        try (Connection conn = Config.getConnection()) {
            String sql = "SELECT jmeno, datum_narozeni FROM zakaznik WHERE id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            String name = "Not found";
            String datumNarozeni = "Not found";

            if(resultSet.next()){
                name = resultSet.getString("jmeno");
                datumNarozeni = resultSet.getString("datum_narozeni");
                validCustomer = true;
            }else{
                validCustomer = false;
            }

            displayCustomerDetails(name, datumNarozeni);
        }
    }

    /**
     * Displays the provided customer's name and birthdate on the GUI.
     * <p>
     * This method updates the GUI with the provided customer details, setting
     * them to respective labels. If no customer details are found, the
     * labels will show a "Not found" message.
     * </p>
     *
     * @param name the name of the customer or "Not found" if not found.
     * @param birthDate the birthdate of the customer or "Not found" if not found.
     */
    private void displayCustomerDetails(String name, String birthDate) {
        GridBagConstraints gbc = new GridBagConstraints();

        nameLabel = new JLabel("Jméno: " + name);
        birthDateLabel = new JLabel("Datum narození: " + birthDate);

        gbc.gridx = 1;
        gbc.gridy = 4;
        add(nameLabel, gbc);

        gbc.gridy = 5;
        add(birthDateLabel, gbc);

        revalidate();
        repaint();
    }

    /**
     * Removes the previously displayed customer details from the GUI.
     * <p>
     * This method checks if the customer details labels (nameLabel and birthDateLabel)
     * are currently displayed on the GUI and removes them if they are.
     * </p>
     */
    private void removeOldCustomerDetails() {
        if (nameLabel != null) {
            remove(nameLabel);
        }
        if (birthDateLabel != null) {
            remove(birthDateLabel);
        }
    }

    /**
     * Adds a date label and corresponding date chooser component to the layout.
     *
     * @param gbc the {@link GridBagConstraints} object used to set the layout constraints
     */
    private void addDateAndDateChooser(GridBagConstraints gbc) {
        JLabel dateLabel = new JLabel("Datum vrácení:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(dateLabel, gbc);
        gbc.gridx = 1;
        add(dateChooser, gbc);
    }

    /**
     * Adds a type label and corresponding combo box component to the layout.
     * Sets an action listener on the combo box to toggle the visibility of the date chooser based on the selected option.
     *
     * @param gbc the {@link GridBagConstraints} object used to set the layout constraints
     */
    private void addTypeAndComboBox(GridBagConstraints gbc) {
        JLabel typeLabel = new JLabel("Typ:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(typeLabel, gbc);
        gbc.gridx = 1;
        add(typeComboBox, gbc);

        typeComboBox.addActionListener(e -> {
            String selectedOption = (String) typeComboBox.getSelectedItem();
            dateChooser.setVisible(!selectedOption.equals("Koupit"));
        });
    }

    /**
     * Creates and returns a {@link GridBagConstraints} object with initial configurations.
     *
     * @return a {@link GridBagConstraints} object with set insets and fill
     */
    private GridBagConstraints getGridBagConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    /**
     * Reads the chosen book IDs from the 'bookIDs.txt' file and adds the corresponding {@link BookData} objects to the chosenBooks list.
     * Retrieves the book details from the database using the {@link #getBookTitleAndPriceFromDB} method.
     */
    public void readChosenBookFromFile() {
        try {
            File file = new File("bookIDs.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String id = scanner.nextLine();
                BookData book = null;
                try {
                    book = getBookTitleAndPriceFromDB(Integer.parseInt(id));
                } catch (SQLException e) {
                    Notification.showErrorMessage("Nastal problém při načítání dat knihy, zkuste to znovu nebo informujte IT oddělení");
                    System.out.println(e);
                }
                chosenBooks.add(book);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Nepodařilo se nalézt soubor 'bookIDs.txt'.");
        }
    }

    /**
     * Retrieves book details such as title, price, amount, and ID from the database for the given book ID.
     * It queries the database and populates a {@link BookData} object with the retrieved information.
     *
     * If no data is found for the given book ID, an error message will be displayed using {@link Notification#showErrorMessage}.
     *
     * @param bookId The ID of the book to retrieve details for.
     * @return A {@link BookData} object containing the title, price, amount, and ID of the book.
     *         Returns an empty {@link BookData} object if no data is found for the given book ID.
     * @throws SQLException If there is an issue querying the database. Errors are logged and displayed to the user.
     */
    public BookData getBookTitleAndPriceFromDB(int bookId) throws SQLException{
        BookData bookData = new BookData();
        try (Connection conn = Config.getConnection();
             PreparedStatement statement = conn.prepareStatement("SELECT id, nazev, cena, amount FROM kniha WHERE id = ?")) {

            statement.setInt(1, bookId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                bookData.setTitle(resultSet.getString("nazev"));
                bookData.setPrice(resultSet.getDouble("cena"));
                bookData.setAmount(resultSet.getInt("amount"));
                bookData.setId(resultSet.getInt("id"));
            } else {
                System.out.println("Nenalezeno žádné data pro knihu s ID: " + bookId);
                Notification.showErrorMessage("Nenalezeno žádné data pro knihu s ID: " + bookId);
            }
        }


        return bookData;
    }

    /**
     * Calculates the total price of all chosen books. The method iterates through the list of chosen books
     * and multiplies each book's price by the corresponding quantity selected by the user in the spinner.
     *
     * @return The calculated total price for all chosen books.
     */
    public double calculateTotalPrice() {
        double totalPrice = 0;
        for (int i = 0; i < chosenBooks.size(); i++) {
            BookData book = chosenBooks.get(i);
            JSpinner spinner = spinners.get(i);
            totalPrice += book.getPrice() * ((Number) ((SpinnerNumberModel) spinner.getModel()).getValue()).intValue();
        }
        return totalPrice;
    }

    /**
     * Updates the total price displayed on the user interface. It calculates the total price by calling
     * {@link #calculateTotalPrice()} and then sets the text on the label, or any other UI component used to
     * display the total price, with the calculated value and currency symbol.
     */
    private void updateTotalPrice() {
        double totalPrice = calculateTotalPrice();
        totalPriceDisplayed.setText(totalPrice + " Kč");
    }

    /**
     * Inserts a new document into the "doklad" table with the specified total price and optional rental date.
     * This method constructs the appropriate SQL statement based on whether a date is provided for the rental
     * and executes the statement, returning the generated document ID if the insertion is successful.
     *
     * @param totalPrice The total price of the document to be inserted.
     * @param dateRentTo An optional date indicating the rental end date for the document. If null, only the total price is inserted.
     * @return The generated document ID if the insertion is successful, or -1 if an error occurs.
     *
     * @throws SQLException If there is a problem executing the SQL statement, such as constraint violations,
     *                      problems with the connection to the database, or other SQL-related issues.
     */
    private int insertDocument(double totalPrice, String dateRentTo) throws SQLException {
        String sql_doklad = (dateRentTo == null) ?
                "INSERT INTO doklad (totalPrice) VALUES (?)" :
                "INSERT INTO doklad (datumTo, totalPrice) VALUES (?, ?)";
        int ducumentId = -1;
        try (PreparedStatement statement_doklad = Config.getConnection().prepareStatement(sql_doklad, Statement.RETURN_GENERATED_KEYS)) {
            if (dateRentTo == null) {
                statement_doklad.setDouble(1, totalPrice);
            } else {
                statement_doklad.setString(1, dateRentTo);
                statement_doklad.setDouble(2, totalPrice);
            }
            int rowsInserted = statement_doklad.executeUpdate();
            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = statement_doklad.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        ducumentId = generatedKeys.getInt(1);
                    }
                }
            }
        }
//        catch (SQLException ex) {
//            System.out.println("Nastal problém při vložení dokumentu, zkuste to znovu nebo informujte IT oddělení");
//            ex.printStackTrace();
//        }
        return ducumentId;
    }

    /**
     * Inserts items into the "doklad_kniha" table and updates the "kniha" table for the specified document.
     * This method iterates through the chosen books, updating their amounts in the "kniha" table and
     * inserting the relationship between the document and the books into the "doklad_kniha" table.
     * After performing these operations, it resets the panel using the "resetPanel" method.
     *
     * @param documentId The unique identifier of the document for which the items are to be inserted.
     *
     * @throws SQLException If there is a problem executing the SQL statements, such as constraint violations,
     *                      problems with the connection to the database, or other SQL-related issues.
     */
    private void insertDocumentItems(int documentId) throws SQLException {
        for (int i = 0; i < chosenBooks.size(); i++) {
            BookData book = chosenBooks.get(i);
            JSpinner spinner = spinners.get(i);
            int amount = ((Number)spinner.getValue()).intValue();

            try (PreparedStatement statement_kniha = Config.getConnection().prepareStatement("UPDATE kniha SET amount = amount - ? WHERE id = ?")) {
                statement_kniha.setInt(1, amount);
                statement_kniha.setInt(2, book.getId());
                statement_kniha.executeUpdate();
            }

            try (PreparedStatement statement_doklad_kniha = Config.getConnection().prepareStatement("INSERT INTO doklad_kniha (id_doklad, id_kniha, amount) VALUES (?, ?, ?)")) {
                statement_doklad_kniha.setInt(1, documentId);
                statement_doklad_kniha.setInt(2, book.getId());
                statement_doklad_kniha.setInt(3, amount);
                statement_doklad_kniha.executeUpdate();
            }
        }

       resetPanel();
    }

    /**
     * Inserts a record into the "doklad_zakaznik" table, associating a specific document with a customer.
     * This method is used to create a relationship between a document and a customer in the database.
     *
     * @param ducumentId The unique identifier of the document to be associated with the customer.
     * @param customerID The unique identifier of the customer to be associated with the document.
     *
     * @throws SQLException If there is a problem executing the SQL statement, such as a constraint violation
     *                      or a problem with the connection to the database.
     */
    private void insertDocumentCustomer(int ducumentId, int customerID) {
        try (PreparedStatement statement_doklad_zakaznik = Config.getConnection().prepareStatement("INSERT INTO doklad_zakaznik (id_doklad, id_zakaznik) VALUES (?, ?)")) {
            statement_doklad_zakaznik.setInt(1, ducumentId);
            statement_doklad_zakaznik.setInt(2, customerID);
            statement_doklad_zakaznik.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("Nastal problém při vložení záznamu doklad_zakaznik, zkuste to znovu nebo informujte IT oddělení");
            ex.printStackTrace();
        }
    }

    /**
     * Resets all fields, clearing the chosen books, customer ID, date, and emptying the 'bookIDs.txt' file.
     * Updates the total price, revalidates, and repaints the container.
     */
    private void resetFields() {
        // Clear books
        for (int i = 0; i < chosenBooks.size(); i++) {
            removeOrderBook(chosenBooks.get(i), new JLabel(), spinners.get(i), new JButton());
        }

        customerIdField.setText("");

        dateChooser.setDate(null);

        try {
            PrintWriter writer = new PrintWriter("bookIDs.txt");
            writer.print("");
            writer.close();
        } catch (IOException e) {
            System.out.println("Nastal problém při mazání souboru 'bookIDs.txt'.");
            e.printStackTrace();
        }

        updateTotalPrice();

        revalidate();
        repaint();

    }

    /**
     * Empties the contents of the 'bookIDs.txt' file.
     */
    private void deleteFileContents() {

        try {
            PrintWriter writer = new PrintWriter("bookIDs.txt");
            writer.print("");
            writer.close();
        } catch (IOException e) {
            System.out.println("Nastal problém při mazání souboru 'bookIDs.txt'.");
            e.printStackTrace();
        }

    }

    /**
     * This method resets the form.
     */
    void resetPanel(){
        deleteFileContents();
        AddDocument newAddDocument = new AddDocument();
        getParent().add(newAddDocument, "addDocument");
        CardLayout cardLayout = (CardLayout) getParent().getLayout();
        cardLayout.show(getParent(), "addDocument");
    }

}
