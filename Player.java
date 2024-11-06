import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Player extends Thread {
    private final int id;
    private final int preferredValue;
    private final List<Card> hand = new ArrayList<>();
    private final CardDeck leftDeck;
    private final CardDeck rightDeck;
    private final String outputFileName;
    private volatile boolean hasWon = false;

    public Player(int id, CardDeck leftDeck, CardDeck rightDeck) {
        this.id = id;
        this.preferredValue = id;
        this.leftDeck = leftDeck;
        this.rightDeck = rightDeck;
        this.outputFileName = "player" + id + "_output.txt";
    }

    // Check if all cards in the hand have the same value
    private boolean hasWinningHand() {
        int firstValue = hand.get(0).getValue();
        return hand.stream().allMatch(card -> card.getValue() == firstValue);
    }

    // Draw a card from the left deck
    private Card drawCard() {
        return leftDeck.drawCard();
    }

    // Discard a card (not of preferred value) to the right deck
    private void discardCard(Card card) {
        rightDeck.addCard(card);
    }

    // Write output to player’s file
    private void logAction(String message) {
        try (FileWriter writer = new FileWriter(outputFileName, true)) {
            System.out.println(message + "\n");
            writer.write(message + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //Declares a win and logs the appropriate messeges. Also notifies the other players of the game win
    private void declareWin() {
        hasWon = true;
        System.out.println("player " + id + " wins");
        logAction("player " + id + " wins");
        notifyAllPlayers();
    }

    @Override
    public void run() {
        // Log the initial hand dealt to this player to their output file
        logAction("player " + id + " initial hand " + hand);

        // Check if the player has a winning hand immediately after receiving their initial cards
        if (hasWinningHand()) {
            // If the player already has a winning hand, declare a win and exit the method
            declareWin();
            return;  // Exit as this player has won
        }

        // Main game loop: This loop continues until the game is over (i.e., someone has won)
        while (!hasWon) {
            // Attempt to draw a card from the deck to the player's left
            Card drawnCard = drawCard();
            
            // If the left deck is empty (no card drawn), continue the loop
            // This will retry until a card is available in the left deck
            if (drawnCard == null) continue;

            // Add the drawn card to the player's hand
            hand.add(drawnCard);
            
            // Log the action of drawing a card from the left deck, recording the card's value and deck's identifier
            logAction("player " + id + " draws a " + drawnCard.getValue() + " from deck " + leftDeck);

            // Find a card in the hand that is not of the player's preferred value
            // Players have a preferred card value based on their ID (e.g., player 1 prefers cards of value 1)
            // We use Streams here to efficiently filter and find a random card that isn't the preferred value
            Random random = new Random();

            List<Card> nonPreferredCards = hand.stream()
                                            .filter(card -> card.getValue() != preferredValue)
                                            .collect(Collectors.toList());

            // Randomly select a card from the list of non-preferred cards, if any are available if not then discardCard is null
            Card discardCard = nonPreferredCards.isEmpty() ? null : 
                            nonPreferredCards.get(random.nextInt(nonPreferredCards.size()));

            // Check if a discardable card was found
            if (discardCard != null) {
                // Remove the chosen card from the hand (this keeps hand size to 4)
                hand.remove(discardCard);

                // Discard the selected card to the deck on the player's right
                // This operation is synchronized in CardDeck to ensure thread safety
                discardCard(discardCard);

                // Log the discard action, indicating the card's value and the deck it was discarded to
                logAction("player " + id + " discards a " + discardCard.getValue() + " to deck " + rightDeck);
            }

            // After drawing and discarding, check if the player now has a winning hand
            if (hasWinningHand()) {
                // If the player has a winning hand, declare a win and break out of the loop
                declareWin();
            } else {
                // If the player does not yet have a winning hand, log their current hand
                logAction("player " + id + " current hand is " + hand);
            }
        }
    }

    

    private int notifyAllPlayers() {
        return id;
    }

    public void addInitialCard(Card card) {
        hand.add(card);
    }
}