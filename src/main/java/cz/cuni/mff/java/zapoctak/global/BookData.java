/**
 * Represents the data of a book in a library management system.
 * Contains information such as ID, title, price, and available amount of the book.
 */
package cz.cuni.mff.java.zapoctak.global;

public class BookData {

    private int id;
    private String title;
    private double price;
    private int amount;

    /**
     * Retrieves the unique ID of the book.
     *
     * @return The unique ID of the book.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique ID of the book.
     *
     * @param id The unique ID of the book.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Retrieves the title of the book.
     *
     * @return The title of the book.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Sets the title of the book.
     *
     * @param title The title of the book.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Retrieves the price of the book.
     *
     * @return The price of the book.
     */
    public double getPrice() {
        return this.price;
    }

    /**
     * Sets the price of the book.
     *
     * @param price The price of the book.
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Retrieves the available amount of the book.
     *
     * @return The available amount of the book.
     */
    public int getAmount() {
        return this.amount;
    }

    /**
     * Sets the available amount of the book.
     *
     * @param amount The available amount of the book.
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }
}
