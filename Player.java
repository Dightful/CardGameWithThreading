import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

// Represents a player in the card game.
// Each player operates in its own thread and performs actions like drawing and discarding cards.
// The goal of the player is to collect a hand of four cards with the same value.
public class Player extends Thread {
    // Unique ID for the player.
    private final int id;

    // The preferred value of cards for this player. Uses their ID value.
    private final int preferredValue;

    // The player's current hand of cards, stored as a list.
    private final List<Card> hand = new ArrayList<>();

    // References to the decks to the left and right of the player.
    private final CardDeck leftDeck;
    private final CardDeck rightDeck;

    // The file name where the player's actions are logged.
    private final String outputFileName;

    // A reference to the CardGame class, allowing the player to signal a win to other players. It is the same instance of CardGame for all players. (Defined in the Cardgame class) 
    private CardGame cardgame; 

    // Constructor to initialize a player with their ID and neighboring decks.
    public Player(int id, CardDeck leftDeck, CardDeck rightDeck) {
        this.id = id; // Assigns the player's ID.
        this.preferredValue = id; // Sets the preferred card value based on ID.
        this.leftDeck = leftDeck; // Assigns the left deck.
        this.rightDeck = rightDeck; // Assigns the right deck.
        this.outputFileName = "player" + id + "_output.txt"; // Creates a unique output file name.

        // Clears the contents of the player's output file at the start of the game.
        try (FileWriter writer = new FileWriter(outputFileName, false)) {
            // FileWriter in non-append mode clears the file.
        } catch (IOException e) { 
            System.out.println("An error occurred while clearing the file: " + e.getMessage());
        }
    }

    // Method that is called from the CardGame class. This method sets the cardgame field to the instance of the CardGame.
    // The method in the CardGame class calls this method and sets the cardgame field to be the instance of that cardgame same for all players. So when game is won, the winning player 
    //can alter a field in the same cardgame object. This is so that the other players know that the game has been won instantly. Thus they can stop playing. 
    public void setMain(CardGame cardgame) {
        this.cardgame = cardgame;
    }

    // Checks if the player has a winning hand (all cards in hand have the same value).
    private boolean hasWinningHand() {
        int firstValue = hand.get(0).getValue(); // Gets the value of the first card in hand.
        return hand.stream().allMatch(card -> card.getValue() == firstValue); // Verifies all cards match.
    }

    // Draws a card from the left deck.
    private Card drawCard() {
        return leftDeck.drawCard(); // Returns the card drawn from the left deck.
    }

    // Discards a card to the right deck.
    private void discardCard(Card card) {
        rightDeck.addCard(card); // Adds the card to the right deck.
    }

    // Logs an action to the player's output file.
    private void logAction(String message) {
        try (FileWriter writer = new FileWriter(outputFileName, true)) {
            System.out.println(message + "\n"); // Prints the message to the console.
            writer.write(message + "\n"); // Writes the message to the output file.
        } catch (IOException e) {
            e.printStackTrace(); // Prints an error if logging fails.
        }
    }

    // Method for when a player has won. It declares and logs the win. Also notifes other players via calling the method in the CardGame object.
    private void declareWin() {
        logAction("player " + id + " wins"); // Logs the win message.
        logAction("player " + id + " exits"); // Logs the exit message.
        logAction("player " + id + " final hand: " + hand + "\n"); // Logs the final hand.
        cardgame.notifyAllPlayers(id); // Notifies the CardGame object of the win. This calls a method that changes the field gameWon to True.
    }

    // Handles a losing player's final actions when another player wins.
    public void stopRunning(int winnerId, int numPlayers) {
        if (winnerId != id) { // Only execute for losing players.
            // Logs messages to the player's output file upon losing.
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
                e.printStackTrace(); // Handles any error caused by the logging.
            }
        }
        return;
    }

    // Adds the initial cards to the player's hand during setup. Called from CardGame object.
    public void addInitialCards(Card card) {
        // Appending given card from pack to player's hand.
        hand.add(card);
    }

    // Main execution method for the player's thread.
    @Override
    public synchronized void run() {
        // Logs the initial hand dealt to the player.
        logAction("player " + id + " initial hand " + hand + "\n");

        // Checks if the player has a winning hand immediately after receiving cards.
        if (hasWinningHand()) {
            cardgame.setGameWon(); // Marks the game as won.
            declareWin(); // Calls the method to declare the win for this player to the other players.
            return; // Exits the thread.
        }

        // Main game loop: Executes until the game is won.
        // While loop uses the method getGameWon in the cardgame object. To see if the game has been won or not.
        while (!cardgame.getGameWon()) {
            // Draws a card from the left deck.
            Card drawnCard = drawCard();
            if (drawnCard == null) continue; // Skips if no card is drawn.

            hand.add(drawnCard); // Adds the drawn card to the hand.
            logAction("player " + id + " draws a " + drawnCard.getValue() + " from deck " + id); // Logs the card drawn.

            // Finds a card in the hand that is not of the player's preferred value.
            Random random = new Random();
            List<Card> nonPreferredCards = hand.stream()            // Creates a list of all the non preferred cards.
                                                .filter(card -> card.getValue() != preferredValue)
                                                .collect(Collectors.toList());
            Card discardCard = nonPreferredCards.get(random.nextInt(nonPreferredCards.size())); // Chooses a random card from the list of non preferred cards.

            hand.remove(discardCard); // Removes the selected card from the hand.
            discardCard(discardCard); // Discards that card to the right deck.
            // Logs these actions.
            logAction("player " + id + " discards a " + discardCard.getValue() + " to deck " + ((id % 4) + 1));
            logAction("player " + id + " current hand is " + hand + "\n");

            // Checks if the player has a winning hand.
            if (!cardgame.getGameWon() && hasWinningHand()) {
                cardgame.setGameWon(); // Marks the game as won. By calling the setGameWon in the CardGame object.
                declareWin(); // Declares the win for this player.
                return; // Exits the thread.
            }
        }
        return; // Exits the thread.
    }
}