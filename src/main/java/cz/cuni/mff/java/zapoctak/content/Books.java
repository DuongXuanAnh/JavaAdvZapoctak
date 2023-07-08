package cz.cuni.mff.java.zapoctak.content;

import cz.cuni.mff.java.zapoctak.global.TitleBorder;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class Books extends JPanel {

    public Books(){
        this.setBorder(TitleBorder.create("Knihy"));
    }

}
