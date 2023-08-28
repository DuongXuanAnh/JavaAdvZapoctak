package cz.cuni.mff.java.zapoctak;

import cz.cuni.mff.java.zapoctak.content.AddBook;
import cz.cuni.mff.java.zapoctak.content.AddCustomer;
import cz.cuni.mff.java.zapoctak.content.AddDocument;
import cz.cuni.mff.java.zapoctak.content.AddAuthor;
import cz.cuni.mff.java.zapoctak.content.Books;
import cz.cuni.mff.java.zapoctak.content.ReturnBook;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The main class that starts the Book Manager application.
 */
public class Main {

    /**
     * The main method that launches the Book Manager application.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("Book Manager");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        // Create the menu panel with buttons to switch between different panels
        JPanel menuPanel = new JPanel(new GridLayout(1, 0));
        JButton authorButton = new JButton("Přidat autora");
        JButton bookButton = new JButton("Přidat knihu");
        JButton customerButton = new JButton("Přidat zákazníka");
        JButton documentButton = new JButton("Objednávka");
        JButton booksButton = new JButton("Knihy");
        JButton returnBookButton = new JButton("Vrátit knihu");

        // Create the card panel to hold the different panels
        JPanel cardPanel = new JPanel(new CardLayout());
        JPanel booksPanel = new Books();

        cardPanel.add(booksPanel, "books");

        // Add ActionListeners to each button to switch the displayed panel
        authorButton.addActionListener(e -> {
            JPanel authorPanel = new AddAuthor();
            cardPanel.add(authorPanel, "author");
            CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
            cardLayout.show(cardPanel, "author");
        });

        bookButton.addActionListener(e -> {
            JPanel addBookPanel = new AddBook();
            cardPanel.add(addBookPanel, "book");
            CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
            cardLayout.show(cardPanel, "book");
        });

        customerButton.addActionListener(e -> {
            JPanel customerPanel = new AddCustomer();
            cardPanel.add(customerPanel, "customer");
            CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
            cardLayout.show(cardPanel, "customer");
        });

        documentButton.addActionListener(e -> {
            JPanel documentPanel = new AddDocument();
            cardPanel.add(documentPanel, "document");

            CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
            cardLayout.show(cardPanel, "document");
        });

        booksButton.addActionListener(e -> {
            JPanel booksPanel2 = new Books();
            cardPanel.add(booksPanel2, "books");
            CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
            cardLayout.show(cardPanel, "books");
        });

        returnBookButton.addActionListener(e -> {
            JPanel returnBookPanel2 = new ReturnBook();
            cardPanel.add(returnBookPanel2, "returnBook");
            CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
            cardLayout.show(cardPanel, "returnBook");
        });

        // Add the buttons to the menu panel
        menuPanel.add(booksButton);
        menuPanel.add(authorButton);
        menuPanel.add(bookButton);
        menuPanel.add(customerButton);
        menuPanel.add(documentButton);
        menuPanel.add(returnBookButton);

        // Add the menu panel and card panel to the frame
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(menuPanel, BorderLayout.NORTH);
        frame.getContentPane().add(cardPanel, BorderLayout.CENTER);

        frame.pack();
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = env.getDefaultScreenDevice();
        device.setFullScreenWindow(frame);
        frame.setVisible(true);
        clearFile();
    }

    /**
     * Clears the "bookIDs.txt" file when the application is shut down.
     * This method is called from the main method using a shutdown hook.
     */
    public static void clearFile() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Files.deleteIfExists(Paths.get("bookIDs.txt"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }
}
