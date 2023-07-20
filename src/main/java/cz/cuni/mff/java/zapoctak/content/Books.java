package cz.cuni.mff.java.zapoctak.content;

import cz.cuni.mff.java.zapoctak.config.Config;
import cz.cuni.mff.java.zapoctak.global.Notification;
import cz.cuni.mff.java.zapoctak.global.TitleBorder;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;

public class Books extends JPanel {

    private final JTextField titleField;
    private final JComboBox<String> authorComboBox;

    private final JTable bookTable;

    public Books(){
        this.setBorder(TitleBorder.create("Knihy"));

        titleField = new JTextField(30);
        authorComboBox = new JComboBox<>();
        bookTable = createTable();

        setupLayout();

    }

    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = getGridBagConstraints();

        addTitleLabelAndField(gbc);
        addNameLabelAndField(gbc);
        addTable(gbc);
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
}
