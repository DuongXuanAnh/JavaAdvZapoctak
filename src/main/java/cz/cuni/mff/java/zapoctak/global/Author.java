package cz.cuni.mff.java.zapoctak.global;

public class Author {
    private final int id;
    private final String name;

    public Author(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }
}