
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CardGame {
    private final int numPlayers;            // Number of players participating in the game
    private final List<Player> players;      // List to hold all player instances
    private final List<CardDeck> decks;      // List to hold all deck instances
    private final List<Card> pack;           // List to represent the entire pack of cards
    private volatile Integer winnerId;  // Shared variable to store winner ID
    private volatile boolean gameWon;


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
        this.winnerId = null;
        this.gameWon = false;
        
        loadPack(packFilePath);    // Load cards from the pack file into the pack list
        initializeGame();          // Initialize players and decks with the loaded cards
    }


    public void setGameWon() {
        gameWon = true;
    }

    public boolean getGameWon() {
        return gameWon;
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

    public void deckFileCreation(){

        int counter = 1;
        for (CardDeck currentDeck : decks) {
            String currentDeckFileName = ("deck" + counter + "_output.txt");
            String currentDeckFileContents = currentDeck.toString();

            try (FileWriter writer = new FileWriter(currentDeckFileName, false)) {
                System.out.println(currentDeckFileContents + "\n");
                writer.write(currentDeckFileContents + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }

            counter++;
        }      
    }

    public synchronized void notifyAllPlayers(int id) {
        
        winnerId = id;           // Store winner's ID
        stopAllPlayers();        // Stop all players

        deckFileCreation();
        
        
    }

    // Stop all player threads
    private void stopAllPlayers() {
        for (Player player : players) {
            player.stopRunning(winnerId, numPlayers);
            //player.interrupt();      // Notify each player thread to stop
        }
    }
     
    //Get method for the winner's Id
    public Integer getWinnerId() {
        return winnerId;
    }

    /**
     * Starts the game by launching each player's thread.
     */
    public void startGame() {

        for (Player player : players) {
            player.setMain(this);  // Pass reference of Main to each Player
            player.start();   // Start each player's thread
        }
         
    }

    // Custom exception for invalid files
    static class InvalidFileException extends Exception {
        public InvalidFileException(String message) {
            super(message);
        }
    }


    public static void validateTextFile(String filePath) throws InvalidFileException, IOException {
        // Check if the file is null
        if (filePath == null) {
            throw new InvalidFileException("The file is null.");
        }
        
        // Convert the string path to a Path object
        Path path = Paths.get(filePath);

        // Check if the file has a .txt extension
        if (!filePath.endsWith(".txt")) {
            throw new InvalidFileException("The file does not have a .txt extension.");
        }

        // Check if the file exists and is not a directory
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new InvalidFileException("The file does not exist or is not a valid file.");
        }

        // Check if the file is empty
        if (Files.size(path) == 0) {
            throw new InvalidFileException("The file is empty.");
        }
    }


    public static void main(String[] args) {

        Scanner inputsFromUser = new Scanner(System.in);  // Create a Scanner object for user input

        int numPlayers = 0;
        boolean validInput = false;

        // Asking the user for the number of players
        while (!validInput) {
            // Getting the number of players
            System.out.println("Please enter the number of players: ");
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

        String packFilePath = "";
        validInput = false;

        inputsFromUser.nextLine();

        // Asking the user for the location of the file
        while (!validInput) {
            

            System.out.println("Please enter the location of the pack to load: ");
            packFilePath = inputsFromUser.nextLine();  // Read user input for the pack file path
            
            if (packFilePath.equals("")) {
                System.out.println("Error: Please enter a file location.");
            }

            else {

                if (packFilePath.endsWith(".txt")) {

                    long lineCount = 0;
                    try 
                    {
                        lineCount = Files.lines(Paths.get(packFilePath)).count();

                        // Check if the pack size matches 8 * number of players
                        if (lineCount != 8 * numPlayers) {
                            System.out.println("Invalid pack size. Expected " + (8 * numPlayers) + " cards.");
                            validInput = false;
                        } else {
                            validInput = true; // Exit loop if filename is valid
                        }

                    } catch (IOException e) {
                        System.out.println("An error occurred while reading the file: " + e.getMessage());
                    }
                }
                else {
                System.out.println("Error: The filename must end with .txt. Please try again.");
                }
            }


            // try {      
            //     try 
            //      {   
            //     // Using my own personal function to validate the file
            //     validateTextFile(givenFilePathName);

            // } catch (InvalidFileException | IOException e) {
            //     System.err.println(e.getMessage());
            // }


            // } catch (Exception e) {
            //     System.out.println("Invalid input for file. Please enter a valid file location.");
            //     inputsFromUser.nextLine(); // Clear the invalid input
            // }
        }

        

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
