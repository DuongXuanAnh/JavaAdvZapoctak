package cz.cuni.mff.java.zapoctak.global;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * This class provides a utility method for creating titled borders with a specific style.
 */
public class TitleBorder {
    /**
     * Creates a titled border with the specified title.
     *
     * @param title The title to be displayed on the border.
     * @return A TitledBorder with the given title, a black line border, centered title text,
     * and Arial Bold 20pt font.
     */
    public static TitledBorder create(String title){
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                title,
                TitledBorder.CENTER,
                TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 20));
        return titledBorder;
    }
}
