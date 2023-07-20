package cz.cuni.mff.java.zapoctak.content;

import cz.cuni.mff.java.zapoctak.config.Config;
import cz.cuni.mff.java.zapoctak.global.Author;
import cz.cuni.mff.java.zapoctak.global.Notification;
import cz.cuni.mff.java.zapoctak.global.TitleBorder;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Books extends JPanel {

    private final JTextField titleField;
    private final JComboBox<Author> authorComboBox;

    private final JTable bookTable;

    public Books(){
        this.setBorder(TitleBorder.create("Knihy"));

        titleField = new JTextField(30);
        authorComboBox = new JComboBox<>();
        fillComboBoxWithAuthors(authorComboBox);
        bookTable = createTable();

        setupLayout();
        setupListeners();
    }

    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = getGridBagConstraints();

        addTitleLabelAndField(gbc);
        addNameLabelAndField(gbc);
        addTable(gbc);
    }

    private void setupListeners() {
        setupTitleFieldListener();
        setupAuthorComboBoxListener();
    }

    private GridBagConstraints getGridBagConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private void addTitleLabelAndField(GridBagConstraints gbc) {
        JLabel authorLabel = new JLabel("Název knihy: ");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(authorLabel, gbc);

        gbc.gridx = 1;
        add(titleField, gbc);
    }
    private void addNameLabelAndField(GridBagConstraints gbc) {
        JLabel authorLabel = new JLabel("Autor: ");
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(authorLabel, gbc);

        gbc.gridx = 1;
        add(authorComboBox, gbc);
    }

    private JTable createTable() {
        String[] columnNames = {"ID", "Název", "Počet kusů", "Cena"};
        String dataValues[][] = {
                {"Value 1", "Value 2", "Value 3", "Value 4"},
                {"Value 5", "Value 6", "Value 7", "Value 8"},
        };

        JTable table = new JTable(dataValues, columnNames);
        table.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = table.getSelectedRow();
            System.out.println("Selected row: " + selectedRow);
            // add more actions here
        });

        return table;
    }

    private void addTable(GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0; // add this

        JScrollPane scrollPane = new JScrollPane(bookTable);
        add(scrollPane, gbc);
    }

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

    private void setupAuthorComboBoxListener() {
        authorComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    Author selectedAuthor = (Author) authorComboBox.getSelectedItem();
                    System.out.println("authorComboBox changed: " + selectedAuthor.getId());
                }
            }
        });
    }

    private void printTitleFieldChange() {
        System.out.println("titleField changed: " + titleField.getText());
    }

    private void printComboBoxChange() {
        System.out.println("authorComboBox changed: " + authorComboBox.getSelectedItem());
    }

    private void fillComboBoxWithAuthors(JComboBox<Author> comboBox) {
        ArrayList<Author> authors = loadAuthorsFromDB();
        for (Author author : authors) {
            comboBox.addItem(author);
        }
    }
    private ArrayList<Author> loadAuthorsFromDB() {
        ArrayList<Author> authors = new ArrayList<>();
        try (Connection conn = Config.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, jmeno FROM autor")) {

            while (rs.next()) {
                authors.add(new Author(rs.getInt("id"), rs.getString("jmeno")));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return authors;
    }

}
