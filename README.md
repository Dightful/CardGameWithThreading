# CardGameWithThreading
A simple card game, using Java threading so the players play the game simultaneously.
No higher than JDK 22

The output files will be outputted to where the director that the cmd is located in. 

Need maven with these dependencies:



From copied README:

Compiling the main program
To compile the main program, do:

javac CardGame.java
Creating a JAR file
To create a JAR file containing the main program, first recompile the program. Then, do:

jar -cfe cards.jar CardGame *
Running the main program
To run the main program, do:

java CardGame
Alternatively, to run from the JAR file, do:

java -jar cards.jar
You will be prompted to input the number of players and then be asked to provide the location of a pack file. Please note that only .txt files are accepted, with all card denominations on a new line, with no whitespace or strings or any other symbols. If the executable tells you that there is a problem with your pack file, please check again and ensure that none of these illegal characters are appearing anywhere.

When referencing the pack file, you may use an absolute or relative filepath. Please note that in order to simply reference the file by name (e.g., simply using "pack.txt"), you must make sure that the pack file sits in the same directory as the .class compiled files or the .jar executable. If there are problems with this, please revert to using the absolute path of the desired pack file.

Testing
The tests are written using JUnit 4. junit.jar and hamcrest-core.jar are provided in the submission to run the tests.

Compiling the tests
To compile the tests, do:

javac --class-path ".;junit.jar" TestSuite.java
Running the tests
To run all the tests, do:

java -class-path ".;junit.jar;hamcrest-core.jar" org.junit.runner.JUnitCore TestSuite
TestSuite is a class that runs all the tests.

Footnotes *USE THIS
When viewing the deck files after a game, it is possible that some decks may have more/less cards. This is normal and to be expected - due to the nature of the multithreaded environment, the game may suddenly end while certain decks have less cards than others.

Please note that the number of players is decided by the user and the pack should be provided by the user as well.

Developed by Talhaa Hussain and Reuben Kurian for the University of Exeter, module code ECM2414.
