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

    public Books(){
        this.setBorder(TitleBorder.create("Knihy"));

        Connection conn = Config.getConnection();

        if (conn == null) {
            Notification.showErrorMessage("Cannot establish database connection");
            return;
        }

        // Nastavíme BoxLayout
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Vytvoříme a přidáme label a combobox na stejný panel
        JPanel comboBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel comboBoxLabel = new JLabel("Combobox:");
        String[] comboBoxItems = {"Item 1", "Item 2", "Item 3"};
        JComboBox<String> comboBox = new JComboBox<>(comboBoxItems);
        comboBoxPanel.add(comboBoxLabel);
        comboBoxPanel.add(comboBox);
        this.add(comboBoxPanel);

        // Vytvoříme a přidáme label a textbox na stejný panel
        JPanel textBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel textBoxLabel = new JLabel("Textbox:");
        JTextField textBox = new JTextField(10);  // 10 sloupců
        textBoxPanel.add(textBoxLabel);
        textBoxPanel.add(textBox);
        this.add(textBoxPanel);

        // Vytvoříme a přidáme tabulku se 4 sloupci
        String[] columnNames = {"Column 1", "Column 2", "Column 3", "Column 4"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        this.add(scrollPane);

        // Vytvoříme a přidáme tlačítko "Přidat do košíku"
        JButton button = new JButton("Přidat do košíku");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Tady můžete přidat kód, který se spustí po stisknutí tlačítka
                System.out.println("Button clicked");
            }
        });
        this.add(button);
    }
}
