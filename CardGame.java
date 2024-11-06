import java.io.*;
import java.util.*;

public class CardGame {
    private final int numPlayers;            // Number of players participating in the game
    private final List<Player> players;      // List to hold all player instances
    private final List<CardDeck> decks;      // List to hold all deck instances
    private final List<Card> pack;           // List to represent the entire pack of cards

    /**
     * Constructor to initialize the game with the specified number of players and pack file.
     * @param numPlayers The number of players in the game.
     * @param packFilePath The path to the pack file containing card values.
     * @throws IOException if an error occurs reading the pack file.
     */
    public CardGame(int numPlayers, String packFilePath) throws IOException {
        this.numPlayers = numPlayers;
        this.players = new ArrayList<>();
        this.decks = new ArrayList<>();
        this.pack = new ArrayList<>();
        
        loadPack(packFilePath);    // Load cards from the pack file into the pack list
        initializeGame();          // Initialize players and decks with the loaded cards
    }

    /**
     * Loads the card pack from the specified file. 
     * Each line in the file should contain a single integer representing a card value.
     * @param filePath The path to the pack file.
     * @throws IOException if an error occurs reading the file.
     */
    private void loadPack(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Read each line and convert it to a Card object
            while ((line = br.readLine()) != null) {
                int value = Integer.parseInt(line.trim());   // Parse and trim whitespace
                pack.add(new Card(value));                   // Add the card to the pack list
            }
        }
        
        // Check if the pack size matches 8 * number of players
        if (pack.size() != 8 * numPlayers) {
            throw new IllegalArgumentException("Invalid pack size. Expected " + (8 * numPlayers) + " cards.");
        }
    }

    /**
     * Initializes the game by creating players and decks, and distributing the cards.
     */
    private void initializeGame() {
        // Create decks, one for each player (no constructor needed for CardDeck, it's automatically empty)
        for (int i = 0; i < numPlayers; i++) {
            decks.add(new CardDeck());  // Initialize an empty deck for each player
        }

        // Create players and assign each player a left and right deck
        for (int i = 0; i < numPlayers; i++) {
            CardDeck leftDeck = decks.get(i);                       // Deck to player's left
            CardDeck rightDeck = decks.get((i + 1) % numPlayers);   // Deck to player's right
            players.add(new Player(i + 1, leftDeck, rightDeck));    // Create player with ID and assign decks
        }

        // Distribute cards to players and decks in round-robin fashion
        distributeInitialCards();
    }

    /**
     * Distributes cards from the pack to each player's hand and to each deck.
     */
    private void distributeInitialCards() {
        int index = 0;  // Keeps track of the card being assigned

        // First, distribute 4 cards to each player's hand
        for (int i = 0; i < 4; i++) {
            for (Player player : players) {
                player.addInitialCard(pack.get(index++));  // Add card to the player's hand
            }
        }

        // Adding 4 cards to each deck in a round-robin fashion.
        for (int j = 0; j < 4; j++) {
            for (CardDeck deck : decks) {
                deck.addCard(pack.get(index++));           
            }
        }
    }

    /**
     * Starts the game by launching each player's thread.
     */
    public void startGame() {
        for (Player player : players) {
            player.start();  // Start each player's thread
        }
        //for in players:
            //Stop player thread
    }


    public static void main(String[] args) {
        Scanner inputsFromUser = new Scanner(System.in);  // Create a Scanner object for user input

        int numPlayers = 0;
        boolean validInput = false;

        // Asking the user for the number of players
        while (!validInput) {
            // Getting the number of players
            System.out.print("Please enter the number of players: ");
            try {
                numPlayers = inputsFromUser.nextInt();
                if (numPlayers >= 2) {
                    validInput = true; // Set to true if the input was correct
                } else {
                    System.out.println("Invalid input. Please enter an integer 2 or above.");
                }
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a valid integer.");
                inputsFromUser.nextLine(); // Clear the invalid input
            }
        }

        // Asking the user for the pack file path
        System.out.println("Please enter the location of the pack to load:");
        inputsFromUser.nextLine(); // Consume the remaining newline
        String packFilePath = inputsFromUser.nextLine();  // Read user input for the pack file path

        // Closing the scanner to prevent memory leaks
        inputsFromUser.close();

        try {
            // Create and start the game
            CardGame game = new CardGame(numPlayers, packFilePath);
            game.startGame();  // Begin the game by starting all player threads

        } catch (NumberFormatException e) {
            System.out.println("Invalid number of players. Please enter a valid integer.");
        } catch (IOException e) {
            System.out.println("Error reading the pack file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
}
