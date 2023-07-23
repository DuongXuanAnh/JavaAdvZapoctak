package cz.cuni.mff.java.zapoctak.content;

import cz.cuni.mff.java.zapoctak.config.Config;
import cz.cuni.mff.java.zapoctak.global.Author;
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
import java.sql.*;
import java.util.ArrayList;

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
        setupListeners();
        updateTableModel();
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
        setupDoubleClickListener();
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
                        System.out.println("Selected book ID: " + bookId);
                    }
                }
            }
        });

        return table;
    }

    public void updateTableModel() {
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
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        revalidate();
        repaint();
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
                    updateTableModel();
                }
            }
        });
    }

    private void printTitleFieldChange() {
        System.out.println("titleField changed: " + titleField.getText());
        updateTableModel();
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

    private void handleDoubleClick() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow != -1) {
            Object idObject = bookTable.getValueAt(selectedRow, 0);
            if (idObject != null) {
                int bookId = (int) idObject;
                System.out.println("Double-clicked book ID: " + bookId);
                openDifferentPanel(bookId);
            }
        }
    }

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
