package org.game;

import java.util.LinkedList;
import java.util.Queue;

// Represents a deck of cards that players can discard to and pick up from.
// The deck is implemented as a linked list queue to maintain the order of cards.
// All methods are synchronized to make them thread-safe in this multi-threaded environment.
public class CardDeck {
    // A queue to hold the cards in the deck.
    // The LinkedList implementation ensures FIFO (First In, First Out) behavior. To keep in line with the style of a deck of cards.
    private final Queue<Card> deck = new LinkedList<>();

    // Add a card to the bottom of the deck.
    public synchronized void addCard(Card card) {
        if (card == null) {
            throw new NullPointerException("Card value can not be null");
        }
        deck.offer(card); // Adds the card to the queue.
    }

    // Draw (remove) a card from the top of the deck.
    public synchronized Card drawCard() {
        return deck.poll(); // Removes and returns the card from the front of the queue.
    }

    // Check if the deck is empty.
    public synchronized boolean isEmpty() {
        return deck.isEmpty(); // Returns true if the queue is empty.
    }

    // Returns a string representation of the deck.
    @Override
    public synchronized String toString() {
        return deck.toString(); // Returns the deck as a string format.
    }
}
