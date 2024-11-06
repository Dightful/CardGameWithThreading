import java.util.LinkedList;
import java.util.Queue;

public class CardDeck {
    private final Queue<Card> deck = new LinkedList<>();

    // Add card to the bottom of the deck
    public synchronized void addCard(Card card) {
        deck.offer(card);
    }

    // Draw card from the top of the deck
    public synchronized Card drawCard() {
        return deck.poll();
    }

    // Check if the deck is empty
    public synchronized boolean isEmpty() {
        return deck.isEmpty();
    }

    @Override
    public synchronized String toString() {
        return deck.toString();
    }
}