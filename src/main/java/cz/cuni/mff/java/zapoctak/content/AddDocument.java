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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
        typeComboBox = new JComboBox<>(new String[]{"Pujčit", "Koupit"});
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
        System.out.println("Document submited");
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

}
