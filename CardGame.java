import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CardGame {
    private final int numPlayers;            // Number of players participating in the game.
    private final List<Player> players;      // List to hold all player instances.
    private final List<CardDeck> decks;      // List to hold all deck instances.
    private final List<Card> pack;           // List to represent the entire pack of cards. Uses instances of Card.
    private volatile Integer winnerId;       // Shared variable to store the winner's ID (thread-safe).
    private volatile boolean gameWon;        // Flag to indicate if the game has been won (thread-safe).

    // Constructor to initialize the game with the number of players and the card pack file
    public CardGame(int numPlayers, String packFilePath) throws IOException {
        this.numPlayers = numPlayers;         // Assign number of players from the parameter. 
        this.players = new ArrayList<>();     // Initialize the player list.
        this.decks = new ArrayList<>();       // Initialize the deck list.
        this.pack = new ArrayList<>();        // Initialize the pack list.
        this.winnerId = null;                 // No winner initially.
        this.gameWon = false;                 // Initialize gameWon to false. Game is not won initially.
        loadPack(packFilePath);               // Load the pack of cards from the specified file.
        initializeGame();                     // Calls the method that sets up players, decks, and distribute cards.
    }

    // Sets the gameWon flag to true when a player wins.
    public void setGameWon() {
        gameWon = true;
    }

    // Gets the value of the gameWon flag.
    public boolean getGameWon() {
        return gameWon;
    }

    // Load the pack of cards from a file, each line containing a single card value.
    private void loadPack(String filePath) throws IOException {
        // Tries to the load the file path.
        try (BufferedReader bufferedReaderOfFilePath = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = bufferedReaderOfFilePath.readLine()) != null) {
                int value = Integer.parseInt(line.trim());   // Parse the card value to an int.
                pack.add(new Card(value));                  // Add card to the pack.
            }
        }
    }

    // Initialize players, decks, and distribute cards.
    private void initializeGame() {
        // Create one deck for each player.
        for (int i = 0; i < numPlayers; i++) {
            decks.add(new CardDeck());
        }

        // Create players and assign decks
        for (int i = 0; i < numPlayers; i++) {
            CardDeck leftDeck = decks.get(i);                     // Player's left deck.
            CardDeck rightDeck = decks.get((i + 1) % numPlayers); // Player's right deck.
            players.add(new Player(i + 1, leftDeck, rightDeck));  // Create a new player. Passing in the player id and the left and right decks.
        }

        // Distributes cards to players and decks.
        distributeInitialCards();
    }

    // Distribute 4 cards to each player's hand and 4 to each deck.
    private void distributeInitialCards() {
        int index = 0;  // Track the next card to distribute.

        // Deal 4 cards to each player's hand.
        for (int i = 0; i < 4; i++) {
            for (Player player : players) {
                player.addInitialCards(pack.get(index++));  // Calls the method in player class to add card to player's hand.
            }
        }

        // Add 4 cards to each deck.
        for (int j = 0; j < 4; j++) {
            for (CardDeck deck : decks) {
                deck.addCard(pack.get(index++)); // // Calls the method in deck class to add card to the deck.
            }
        }
    }

    // Creates the deck log files after the game is finished.
    public void deckFileCreation() {
        int counter = 1;
        // Iterates through each deck and writes to the file the contents of the deck.
        for (CardDeck currentDeck : decks) {
            String currentDeckFileName = ("deck" + counter + "_output.txt");
            String currentDeckFileContents = currentDeck.toString();

            try (FileWriter writer = new FileWriter(currentDeckFileName, false)) {
                System.out.println(currentDeckFileContents + "\n"); // One card of the deck is written on each line.
                writer.write(currentDeckFileContents + "\n");
            } catch (IOException e) {
                e.printStackTrace(); // Handles any errors encouted when trying to write to the deck files. 
            }

            counter++;
        }
    }

    // Notify all players that a winner has been declared.
    public synchronized void notifyAllPlayers(int id) {
        winnerId = id;           // Set the winner's ID.
        stopAllPlayers();        // Stop all player threads.
        deckFileCreation();      // Create deck output files.
    }

    // Stops all losing player threads
    private void stopAllPlayers() {
        for (Player player : players) {
            player.stopRunning(winnerId, numPlayers);  // Calls the method that notifies each losing player of the game's end.
        }
    }

    // Get the winner's ID
    public Integer getWinnerId() {
        return winnerId;
    }

    // Start the game by launching all player threads.
    public void startGame() {
        for (Player player : players) {
            player.setMain(this);  // Pass this CardGame instance to the players.
            player.start();        // Start the player's thread.
        }
    }

    // Custom exception for invalid file input.
    static class InvalidFileException extends Exception {
        public InvalidFileException(String message) {
            super(message);
        }
    }

    // Validate the pack file to ensure it meets the game's requirements.
    public static void validateTextFile(String filePath) throws InvalidFileException, IOException {
    
        // Check if the filePath is null. If it is, throw an exception with a relevant message.
        if (filePath == null) {
            throw new InvalidFileException("The file is null.");
        }

        Path path = Paths.get(filePath);// Convert the filePath string into a Path object for file system operations.
        
        // Check if the file does not have a ".txt" extension. If so, throw an exception.
        if (!filePath.endsWith(".txt")) {
            throw new InvalidFileException("The file does not have a .txt extension.");
        }
    
        // Check if the file exists and if it is a regular file (not a directory). If not, throw an exception.
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new InvalidFileException("The file does not exist or is not a valid file.");
        }
    
        // Check if the file is empty. If it is, throw an exception.
        if (Files.size(path) == 0) {
            throw new InvalidFileException("The file is empty.");
        }
    }

    public static void main(String[] args) {
        // Gathers the input from the user inputted into the command line. 
        Scanner inputsFromUser = new Scanner(System.in);

        int numPlayers = 0;
        // Variable that is looped through until valid input given.
        boolean validInput = false;

        // Prompt user for the number of players until valid input is received
        while (!validInput) {
            
            // Ask the user to enter the number of players for the game
            System.out.println("Please enter the number of players: ");
            
            try {
                // Get the user's input as an integer representing the number of players
                numPlayers = inputsFromUser.nextInt();
                
                // Check if the number of players is 2 or greater (since there must be at least 2 players)
                if (numPlayers >= 2) {
                    // If the input is valid (>= 2), set validInput to true to exit the loop
                    validInput = true; 
                } else {
                    // If the input is less than 2, notify the user that it is invalid
                    System.out.println("Invalid input. Please enter an integer 2 or above.");
                }
            } catch (Exception e) {
                // If the user enters a non-integer value, notify them of the invalid input
                System.out.println("Invalid input. Please enter a valid integer.");
                
                // Clear the invalid input from the scanner buffer so that the user can try again
                inputsFromUser.nextLine();
            }
        }

        String packFilePath = "";
        validInput = false;

        inputsFromUser.nextLine();

        // Prompt user for the location of the card pack file until a valid input is received
        while (!validInput) {
            
            // Display a message asking the user to enter the location of the card pack file
            System.out.println("Please enter the location of the pack to load: ");
            
            // Read the user's input (path of the card pack file) as a string
            packFilePath = inputsFromUser.nextLine();

            // Check if the user input is an empty string
            if (packFilePath.equals("")) {
                // If input is empty, show an error message and prompt again
                System.out.println("Error: Please enter a file location.");
            } else {
                // Check if the file path ends with ".txt" to ensure it's a text file
                if (packFilePath.endsWith(".txt")) {
                    try {
                        // Attempt to count the number of lines in the file to validate its size
                        long lineCount = Files.lines(Paths.get(packFilePath)).count();
                        
                        // Verify if the file contains the correct number of lines (8 * number of players)
                        if (lineCount != 8 * numPlayers) {
                            // If the line count is not valid, notify the user about the expected number of cards
                            System.out.println("Invalid pack size. Expected " + (8 * numPlayers) + " cards.");
                        } else {
                            // If the pack size is valid, set validInput to true to exit the loop
                            validInput = true;
                        }
                    } catch (IOException e) {
                        // Handle any I/O errors (e.g., file not found or inaccessible)
                        System.out.println("An error occurred while reading the file: " + e.getMessage());
                    }
                } else {
                    // If the file doesn't have a ".txt" extension, notify the user
                    System.out.println("Error: The filename must end with .txt. Please try again.");
                }
            }
        }
    
        inputsFromUser.close(); //Close the input scanner. 

        // Starts the card game 
        try {
            CardGame game = new CardGame(numPlayers, packFilePath); // Initialising the CardGame class
            game.startGame();
        } catch (IOException e) {
            System.out.println("An unexpected error occurred while starting the game. Please try again: " + e.getMessage());
        }
    }
}
