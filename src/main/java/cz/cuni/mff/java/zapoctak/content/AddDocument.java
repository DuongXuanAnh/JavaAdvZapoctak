package cz.cuni.mff.java.zapoctak.content;

import cz.cuni.mff.java.zapoctak.config.Config;
import cz.cuni.mff.java.zapoctak.global.BookData;
import cz.cuni.mff.java.zapoctak.global.Notification;
import com.toedter.calendar.JDateChooser;
import cz.cuni.mff.java.zapoctak.global.TitleBorder;

import javax.swing.*;
import java.awt.*;
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


public class AddDocument extends JPanel {

    JComboBox<String> typeComboBox;
    JDateChooser dateChooser;
    JTextField customerIdField;

    ArrayList<BookData> chosenBooks = new ArrayList<>();

    ArrayList<JSpinner> spinners = new ArrayList<>();
    JLabel totalPriceDisplayed;

    public AddDocument(){
        this.setBorder(TitleBorder.create("Vytvořit objednávku"));
        typeComboBox = new JComboBox<>(new String[]{"Půjčit", "Koupit"});
        dateChooser = new JDateChooser();
        customerIdField = new JTextField(20);
        totalPriceDisplayed = new JLabel("");
        setupLayout();
        setupSubmitButton();
    }

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

        String dateRentTo = selectedOption.equals("Koupit") ? null : format.format(chosenDate);

        int customerID;

        try {
            customerID = Integer.parseInt(customerIdField.getText());
        } catch (NumberFormatException e) {
            Notification.showErrorMessage("Customer ID musí být číslo");
            return;
        }

        int documentId = insertDocument(totalPrice, dateRentTo);

        if (documentId != -1) {
            insertDocumentItems(documentId);
            insertDocumentCustomer(documentId, customerID);
            resetFields();
            Notification.showSuccessMessage("Objednávka byla úspěšně vytvořena.");
        }
    }



    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = getGridBagConstraints();

        addTypeAndComboBox(gbc);
        addDateAndDateChooser(gbc);
        addCustomerIdAndLabel(gbc);
        addChosenBookAndAmountSpinner(gbc);
        displayTotalPrice(gbc);
    }

    private void displayTotalPrice(GridBagConstraints gbc) {
        JLabel totalPriceLabel = new JLabel("Celková cena:");
        gbc.gridx = 0;
        gbc.gridy = 50;
        add(totalPriceLabel, gbc);

        updateTotalPrice();
        gbc.gridx = 1;
        add(totalPriceDisplayed, gbc);
    }

    private void showChosenBook(GridBagConstraints gbc, BookData book, int posisionIndex){
        JLabel chosenBook = new JLabel("* " + book.getTitle() + " - " + book.getPrice() + "Kč");
        gbc.gridx = 0;
        gbc.gridy = 5 + posisionIndex;
        add(chosenBook, gbc);

        SpinnerModel model = new SpinnerNumberModel(1, 1, book.getAmount(), 1);
        JSpinner spinner = new JSpinner(model);
        spinners.add(spinner);
        spinner.addChangeListener(e -> updateTotalPrice());
        gbc.gridx = 1;
        gbc.gridy = 5 + posisionIndex;
        add(spinner, gbc);

        JButton removeOrderBookButton = new JButton("X");
        removeOrderBookButton.addActionListener(e -> {
            removeOrderBook(book, chosenBook, spinner, removeOrderBookButton);
        });
        gbc.gridx = 2;
        gbc.gridy = 5 + posisionIndex;
        add(removeOrderBookButton, gbc);
    }

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

    private void addChosenBookAndAmountSpinner(GridBagConstraints gbc) {
        JLabel ChosenBookTitleLabel = new JLabel("Vybrané knihy:");
        gbc.gridx = 0;
        gbc.gridy = 4;
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

    private void addCustomerIdAndLabel(GridBagConstraints gbc) {
        JLabel customerIDLabel = new JLabel("ID zákaznika:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(customerIDLabel, gbc);

        gbc.gridx = 1;
        add(customerIdField, gbc);
    }

    private void addDateAndDateChooser(GridBagConstraints gbc) {
        JLabel dateLabel = new JLabel("Datum vrácení:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(dateLabel, gbc);
        gbc.gridx = 1;
        add(dateChooser, gbc);
    }
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
    private GridBagConstraints getGridBagConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    public void readChosenBookFromFile() {
        try {
            File file = new File("bookIDs.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String id = scanner.nextLine();
                BookData book = getBookTitleAndPriceFromDB(Integer.parseInt(id));
                chosenBooks.add(book);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Nepodařilo se nalézt soubor 'bookIDs.txt'.");
        }
    }

    public BookData getBookTitleAndPriceFromDB(int bookId){
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
        } catch (SQLException ex) {
            System.out.println("Nastal problém při načítání dat knihy, zkuste to znovu nebo informujte IT oddělení");
            Notification.showErrorMessage("Nastal problém při načítání dat knihy, zkuste to znovu nebo informujte IT oddělení");
            ex.printStackTrace();
        }

        return bookData;
    }

    public double calculateTotalPrice() {
        double totalPrice = 0;
        for (int i = 0; i < chosenBooks.size(); i++) {
            BookData book = chosenBooks.get(i);
            JSpinner spinner = spinners.get(i);
            totalPrice += book.getPrice() * ((Number) ((SpinnerNumberModel) spinner.getModel()).getValue()).intValue();
        }
        return totalPrice;
    }

    private void updateTotalPrice() {
        double totalPrice = calculateTotalPrice();
        totalPriceDisplayed.setText(totalPrice + " Kč");
    }

    private int insertDocument(double totalPrice, String dateRentTo) {
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
        } catch (SQLException ex) {
            System.out.println("Nastal problém při vložení dokumentu, zkuste to znovu nebo informujte IT oddělení");
            ex.printStackTrace();
        }
        return ducumentId;
    }

    private void insertDocumentItems(int documentId) {
        for (int i = 0; i < chosenBooks.size(); i++) {
            BookData book = chosenBooks.get(i);
            JSpinner spinner = spinners.get(i);
            int amount = ((Number)spinner.getValue()).intValue();

            try (PreparedStatement statement_kniha = Config.getConnection().prepareStatement("UPDATE kniha SET amount = amount - ? WHERE id = ?")) {
                statement_kniha.setInt(1, amount);
                statement_kniha.setInt(2, book.getId());
                statement_kniha.executeUpdate();
            } catch (SQLException ex) {
                System.out.println("Nastal problém při aktualizaci knihy, zkuste to znovu nebo informujte IT oddělení");
                ex.printStackTrace();
            }

            try (PreparedStatement statement_doklad_kniha = Config.getConnection().prepareStatement("INSERT INTO doklad_kniha (id_doklad, id_kniha, amount) VALUES (?, ?, ?)")) {
                statement_doklad_kniha.setInt(1, documentId);
                statement_doklad_kniha.setInt(2, book.getId());
                statement_doklad_kniha.setInt(3, amount);
                statement_doklad_kniha.executeUpdate();
            } catch (SQLException ex) {
                System.out.println("Nastal problém při vložení záznamu doklad_kniha, zkuste to znovu nebo informujte IT oddělení");
                ex.printStackTrace();
            }
        }

       resetPanel();
    }

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

    void resetPanel(){
        deleteFileContents();
        AddDocument newAddDocument = new AddDocument();
        getParent().add(newAddDocument, "addDocument");
        CardLayout cardLayout = (CardLayout) getParent().getLayout();
        cardLayout.show(getParent(), "addDocument");
    }

}
