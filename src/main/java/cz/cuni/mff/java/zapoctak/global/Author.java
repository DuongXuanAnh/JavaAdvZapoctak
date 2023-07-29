/**
 * Represents an Author in a library management system.
 * An Author is identified by a unique ID and has a name associated with it.
 */
package cz.cuni.mff.java.zapoctak.global;

public class Author {
    private final int id;
    private final String name;

    /**
     * Constructs an Author object with the specified ID and name.
     *
     * @param id   The unique ID of the Author.
     * @param name The name of the Author.
     */
    public Author(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Retrieves the unique ID of the Author.
     *
     * @return The unique ID of the Author.
     */
    public int getId() {
        return id;
    }

    /**
     * Returns a string representation of the Author.
     * The string representation is the name of the Author.
     *
     * @return The name of the Author.
     */
    @Override
    public String toString() {
        return name;
    }
}
