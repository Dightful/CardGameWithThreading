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
    private CardGame cardgame; 

    public Player(int id, CardDeck leftDeck, CardDeck rightDeck) {
        this.id = id;
        this.preferredValue = id;
        this.leftDeck = leftDeck;
        this.rightDeck = rightDeck;
        this.outputFileName = "player" + id + "_output.txt";

        //Clearing contents of the output file
        try (FileWriter writer = new FileWriter(outputFileName, false)) {
            // FileWriter opened in non-append mode clears the file
        } catch (IOException e) {
            System.out.println("An error occurred while clearing the file: " + e.getMessage());
        }
    }

     // Setter to pass Main reference to each Player instance
    public void setMain(CardGame cardgame) {
        this.cardgame = cardgame;
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
        //Relevant messeges upon winning the game
        logAction("player " + id + " wins");

        logAction("player " + id + " exits");

        logAction("player " + id + " final hand: " + hand + "\n");

        cardgame.notifyAllPlayers(id);  // Notify Main class of win

    }


    public void stopRunning(int winnerId, int numPlayers) {
        // Adding all the messeges to the losing players files
        if (winnerId != id) {
            //This is the file name for that losing player
            String LosingMessage1 = "player " + winnerId + " has informed player " + id + " that player " + winnerId + " has won";
            String LosingMessage2 = "player " + id + " exits";
            String LosingMessage3 = "player " + id + " final hand: " + hand;

            try (FileWriter writer = new FileWriter(outputFileName, true)) {
                System.out.println(LosingMessage1  + "\n");
                writer.write(LosingMessage1 + "\n");
                System.out.println(LosingMessage2  + "\n");
                writer.write(LosingMessage2 + "\n");
                System.out.println(LosingMessage3  + "\n");
                writer.write(LosingMessage3 + "\n");

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return;
        
    }

    public void addInitialCard(Card card) {
        hand.add(card);
    }

    @Override
    public void run() {
        
        // Log the initial hand dealt to this player to their output file
        logAction("player " + id + " initial hand " + hand + "\n");

        // Check if the player has a winning hand immediately after receiving their initial cards
        if (hasWinningHand()) {
            // If the player already has a winning hand, declare a win and exit the method
            declareWin();
            cardgame.notifyAllPlayers(id);  // Notify Main class of win
            return;  // Exit as this player has won
        }

        // Main game loop: This loop continues until the game is over (i.e., someone has won)
        while (!cardgame.getGameWon()) {
            // Attempt to draw a card from the deck to the player's left
            Card drawnCard = drawCard();
            
            // If the left deck is empty (no card drawn), continue the loop
            // This will retry until a card is available in the left deck
            if (drawnCard == null) continue;

            // Add the drawn card to the player's hand
            hand.add(drawnCard);
            
            // Log the action of drawing a card from the left deck, recording the card's value and deck's identifier
            logAction("player " + id + " draws a " + drawnCard.getValue() + " from deck " + id);

            // Find a card in the hand that is not of the player's preferred value
            // Players have a preferred card value based on their ID (e.g., player 1 prefers cards of value 1)
            // We use Streams here to efficiently filter and find a random card that isn't the preferred value
            Random random = new Random();

            // Randomly select a card from the list of non-preferred cards
            List<Card> nonPreferredCards = hand.stream()
                                            .filter(card -> card.getValue() != preferredValue)
                                            .collect(Collectors.toList());

            Card discardCard = nonPreferredCards.get(random.nextInt(nonPreferredCards.size()));

            // Check if a discardable card was found
            //if (discardCard != null) {
                // Remove the chosen card from the hand (this keeps hand size to 4)
            hand.remove(discardCard);

            // Discard the selected card to the deck on the player's right
            // This operation is synchronized in CardDeck to ensure thread safety
            discardCard(discardCard);

            // Log the discard action, indicating the card's value and the deck it was discarded to
            logAction("player " + id + " discards a " + discardCard.getValue() + " to deck " + ((id % 4) + 1));
            // Logging the current hand
            logAction("player " + id + " current hand is " + hand + "\n");
            


            //Checks if the hand is a winning hand
            int firstValue = hand.get(0).getValue();
            if (hand.stream().allMatch(card -> card.getValue() == firstValue)) {
                //Setting the game field gameWon to true, to stop all other player thread loops.
                cardgame.setGameWon();
                declareWin();
                return; // Stop current player's thread
            }
        }
    }
}