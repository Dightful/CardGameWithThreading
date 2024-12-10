# CardGameWithThreading
A simple card game, using Java threading so the players play the game simultaneously.
No higher than JDK 22

The output files will be outputted to where the director that the cmd is located in. 

Need maven with these dependencies:



From copied README:

To run from the JAR file, do:

java -jar CardGame.jar

Please note that the directory where the CMD is currently located will be the destination for the output files (e.g., player1_output.txt). We recommend setting the CMD directory to the location of the cards.jar file.

You will be prompted to enter the number of players and then provide the location of a pack file. Please note that only .txt files are accepted. Each card denomination should be on a new line with no extra whitespace, strings, or symbols. If the program indicates an issue with your pack file, review it carefully to ensure that it does not contain any invalid characters.

When referencing the pack file, you can use either an absolute or relative filepath. To reference the file by name alone (e.g., "pack.txt"), ensure the pack file is in the same directory as the compiled .class files or the .jar executable. If issues arise, switch to using the absolute path for the pack file.

Testing:
The tests are written using JUnit 5. The dependencies file is given that contain all the dependencies that need to be put in the pom.xml file. The tests must be ran in Maven.

Set up all the Classes and test Classes so that they follow this file structure:

![Screenshot 2024-12-10 024108](https://github.com/user-attachments/assets/66a6def8-9a7d-41a2-a2f0-80606b0503c5)


pack.txt has been given in the same file as the test classes.
Once all the classes are put in this format and all the dependencies have been added, you will need to sync the maven project in your IDE.
After this, you can go to TestSuite.Jave to run the test for all the classes or select individual classes for testing.

Footnotes:
When reviewing the deck files after a game, you may notice that some decks contain more or fewer cards than others. This is because, in a multithreaded environment, actions from different threads can overlap, even at the same timestamp. Thus, in the same moment that the winning player declares victory, due to the nature of threading, another player might still complete their turn. This can result in a card being taken from one deck and added to another before the game fully stops, causing some decks to have one less card while others have one more. This behavior is a normal outcome of this multithreaded system.

Developed by Matthew Dawson and Jacob Nixon for the University of Exeter, module code ECM2414.
