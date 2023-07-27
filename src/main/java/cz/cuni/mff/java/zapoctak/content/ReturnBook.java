package cz.cuni.mff.java.zapoctak.content;

import cz.cuni.mff.java.zapoctak.global.TitleBorder;

import javax.swing.*;
import java.awt.*;

public class ReturnBook extends JPanel {

    private JTextField documentID;
    public ReturnBook(){
        this.setBorder(TitleBorder.create("Vrácení knihy"));
        documentID = new JTextField(11);
        setupLayout();
    }

    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = getGridBagConstraints();

        addDocumentIDLabelAndField(gbc);
    }

    private void addDocumentIDLabelAndField(GridBagConstraints gbc) {
        JLabel documentIDLabel = new JLabel("ID objednávky:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(documentIDLabel, gbc);

        gbc.gridx = 1;
        add(documentID, gbc);

        gbc.gridx = 2;
        JButton findButton = new JButton("Hledat");
        findButton.addActionListener(e -> {
            showDocument();
        });
        add(findButton, gbc);
    }

    private void showDocument() {
        
    }


    private GridBagConstraints getGridBagConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

}
