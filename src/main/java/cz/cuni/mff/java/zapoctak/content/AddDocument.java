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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class AddDocument extends JPanel {

    JComboBox<String> typeComboBox;
    JDateChooser dateChooser;
    JTextField customerIdField;

    ArrayList<BookData> chosenBooks = new ArrayList<>();

    public AddDocument(){
        this.setBorder(TitleBorder.create("Vytvořit objednávku"));
        typeComboBox = new JComboBox<>(new String[]{"Pujčit", "Koupit"});
        dateChooser = new JDateChooser();
        customerIdField = new JTextField(20);
        setupLayout();

    }

    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = getGridBagConstraints();

        addTypeAndComboBox(gbc);
        addDateAndDateChooser(gbc);
        addCustomerIdAndLabel(gbc);
        addChoosenBookAndAmoutSpinner(gbc);
    }

    private void addChoosenBookAndAmoutSpinner(GridBagConstraints gbc) {
        JLabel ChosenBookTitleLabel = new JLabel("Vybrané knihy:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        add(ChosenBookTitleLabel, gbc);

        readChosenBookFromFile();

        if (chosenBooks.isEmpty()) {
            System.out.println("Seznam vybraných knih je prázdný.");
        } else {
            for (BookData book : chosenBooks) {
                System.out.println("Název: " + book.getTitle() + ", Cena: " + book.getPrice());
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
             PreparedStatement statement = conn.prepareStatement("SELECT nazev, cena FROM kniha WHERE id = ?")) {

            statement.setInt(1, bookId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                bookData.setTitle(resultSet.getString("nazev"));
                bookData.setPrice(resultSet.getDouble("cena"));
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

}
