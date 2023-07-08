package cz.cuni.mff.java.zapoctak.global;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
/**
 * Tato třída poskytuje pomocnou metodu pro vytváření titulkovaných okrajů s konkrétním stylem.
 */
public class TitleBorder {
    /**
     * Vytvoří titulkovaný okraj s daným názvem.
     *
     * @param title název, který se má zobrazit na okraji
     * @return TitledBorder s určeným názvem, černou linkovou hranou, vycentrovaným textem názvu
     * a fontem Arial Bold 20pt
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