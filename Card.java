package org.game;

// Represents a single card in a card game.
// Each card has a fixed integer value that defines its rank or type.
public class Card {
    // The value of the card, e.g., rank or point value, stored as an integer.
    private final int value;

    // Constructs a Card object with a specific int value.
    public Card(Integer value) {
        // Checking that the value passed in is a valid card value.
        if (value == null || value < 0) {
            throw new IllegalArgumentException("Value must be non-negative integer"); // Handling errors via throwing the appropiate error and message.
        }

        this.value = value; // Assigns the provided value to the card.
    }

    // Retrieves the value of the card.
    public Integer getValue() {
        return value; // Returns the card's value.
    }

    // Returns a string representation of the card int value.
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
