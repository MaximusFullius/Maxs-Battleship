
import java.util.Locale;
import java.util.Scanner;

public class Game {
    public static int turnCountP1;
    public static int turnCountP2;
    public static int turnCountTotal;
    protected static final String AdminPass = "1234";

    public static void Help(char chMode, char chDifficulty){
        System.out.println("-------------------- BEGINNING OF HELP SECTION --------------------");
        System.out.println("This is the game board. There are enemy ships in this board, but they are currently hidden in" +
                "\na fog of war. The objective of the game are to find and sink all the enemy ships on this board before " +
                "\nthe enemy sinks all of yours...");

        if (chMode == 'S'){
            System.out.println("\nSince you are playing in single player mode, however, and AI hasn't been fully implemented" +
                    "\ninto this game yet, you don't need to worry about the enemy destroying any of your ships." +
                    "\nYou only need to focus on finding the enemy ships.");
        }

        System.out.println("\nEvery cell on this board has a coordinate, as marked by the vertical and horizontal number" +
                "\naxes on the sides of the board. On each turn, you may either say 'fire' and enter a coordinate in the format of a " +
                "\nletter, a number (like this: fire B2), or enter a power.");

        System.out.println("\nIn order to sink a ship, you must damage every cell of the ship. Different ships have " +
                "\ndifferent lengths and thus different numbers of cells to destroy before they are fully sunk." +
                "\nDestroyers are 2 cells long, Cruisers are 3 cells long, Battleships are 4 cells long, and" +
                "\nCarriers are 5 cells long.\n");

        switch(chDifficulty){
            case 'B':
                System.out.println("Beginner boards have one Destroyer.\n");
                break;

            case 'I':
                System.out.println("Intermediate boards have one Destroyer, one Cruiser and one Battleship.\n");
                break;

            case 'A':
                System.out.println("Advanced boards have one Destroyer, two Cruisers, one Battleship, and one Carrier.\n");
                break;
        }

        System.out.println("There are 3 types of special powers in this game: Missiles, Submarines and Drones.");
        System.out.println("\nMissiles explode in a 3x3 cell explosion, centered at their target coordinate." +
                "\nall ship parts within the blast radius will be damaged. Any cells within the blast radius that " +
                "\ngo outside the board will not damage any ships. Type \"missile\" to deploy a missile.\n");

        System.out.println("Submarines will fire a devastating nuclear torpedo that will instantly sink any ship it manages to hit." +
                "\nType \"submarine a,2 \" to deploy a submarine.\n");

        System.out.println("Drones will scan a random row or column of the board and tell you how many ships it sees in that row or column." +
                "\nThe drone will never scan the same row or column twice and it saves valuable time. Type \"drone\" to deploy a drone,\n");

        System.out.println("To enable debug mode, type \"ADMIN\" during the singleplayer/multiplayer selection step and enter the password. (See the readme).");
        System.out.println("-------------------- END OF HELP SECTION --------------------\n");
    }

    // Convert column letter (A-I) to 1-based index. Returns -1 if invalid.
    public static int letterToIndex(String s) {
        if (s == null || s.length() == 0) return -1;
        s = s.toUpperCase(Locale.ROOT);
        char c = s.charAt(0);
        if (c < 'A' || c > 'I') return -1;
        return (c - 'A') + 1;
    }

    // Parse coordinate input robustly.
    // Accepts: "fire b2", "fire b,2", "b2", "b,2", "B 2", "2B", etc.
    // Returns int[2] where [0]=colZeroBased, [1]=rowZeroBased, or null if invalid.
    public static int[] parseCoordinate(String raw){
        if (raw == null) return null;
        raw = raw.trim();
        if (raw.length() == 0) return null;

        // If command plus coord, take last token as coord (e.g., "fire b2")
        String token = raw;
        if (raw.contains(" ")) {
            String[] parts = raw.split("\\s+");
            token = parts[parts.length - 1];
        }

        token = token.toUpperCase(Locale.ROOT).replaceAll("\\s+", "");
        token = token.replaceAll(",", "");

        // Now token can be like "B2" or "2B" or "B12" etc.
        String letters = token.replaceAll("[^A-Z]", "");
        String digits = token.replaceAll("[^0-9]", "");

        int col = -1;
        int row = -1;

        if (letters.length() == 1 && digits.length() >= 1) {
            col = letterToIndex(letters) - 1; // zero-based
            try {
                row = Integer.parseInt(digits) - 1; // zero-based
            } catch (Exception e) {
                return null;
            }
            return new int[]{col, row};
        }

        // Support reversed like "2B" (digits then letter)
        if (digits.length() >= 1 && letters.length() == 1 && token.indexOf(digits) == 0){
            try {
                row = Integer.parseInt(digits) - 1;
            } catch (Exception e){
                return null;
            }
            col = letterToIndex(letters) - 1;
            return new int[]{col, row};
        }

        return null;
    }

    public static void main(String[] args){
        turnCountP1 = 0;
        turnCountP2 = 0;
        turnCountTotal = 0;
        System.out.println("~~~~~ Welcome to Battleship. Code by Maxwell Fuller ~~~~~");
        System.out.println("This is the classic game of battleship, but with a few fun twists.");
        System.out.println("You can deploy devastating torpedo submarines, spy drones or massive missiles!");
        System.out.println("You can play by yourself or with a friend on the same computer.");

        Scanner scn = new Scanner(System.in);
        boolean bModeValid = false;
        String ModeInput = "";
        String strOutput = "";
        boolean bAdmin = false;
        while (!bModeValid){
            System.out.println("\nSelect game mode. \"S\" is singleplayer. \"T\" is multiplayer: ");
            ModeInput = scn.nextLine();
            ModeInput = ModeInput.toUpperCase(Locale.ROOT);
            if (ModeInput.equals("ADMIN")){ // Toggle admin mode
                bModeValid = false;
                if (bAdmin){
                    bAdmin = false;
                    System.out.println("Administrator mode disabled.");
                }
                else{
                    int attempts = 0;
                    while (!bAdmin){
                        System.out.print("\npassword: ");
                        ModeInput = scn.nextLine();
                        if (ModeInput.equals(AdminPass)){
                            System.out.println("Welcome, administrator.");
                            bAdmin = true;
                        }
                        else{
                            bAdmin = false;
                            System.out.println("Access Denied.");
                            attempts++;
                            if (attempts >= 3){
                                System.out.println("Password attempt limit exceeded. Terminating Program...");
                                System.exit(0);
                            }
                            else{
                                System.out.println((3-attempts) + " attempts remaining...");
                            }
                        }
                    }
                }
            }
            else{
                if (ModeInput.equals("S") || ModeInput.equals("T")){
                    strOutput = "\nInput Accepted. ";
                    switch(ModeInput){
                        case "S":
                            strOutput += "Single player mode selected.";
                            break;

                        case "T":
                            strOutput += "Multiplayer mode selected.";
                            break;
                    }
                    System.out.println(strOutput);
                    bModeValid = true;
                }
                else{
                    System.out.println("Error: Invalid input for game mode. please try again.");
                }
            }
            if (bAdmin){
                System.out.println("** Administrator mode Enabled **");
            }
        }

        boolean bDifficultyValid = false;
        String DiffInp = "";
        while(!bDifficultyValid){
            System.out.println("\nSelect board size.");
            System.out.println("\"B\" is Beginner (3x3 with 1 ship), \"I\" is Intermediate (6x6 with 3 ships), and \"A\" is Advanced (9x9 with 5 ships): ");
            DiffInp = scn.nextLine();
            DiffInp = DiffInp.toUpperCase(Locale.ROOT);
            if ((DiffInp.equals("B") || DiffInp.equals("I")) || DiffInp.equals("A")){
                strOutput = "\nInput Accepted. ";
                switch(DiffInp){
                    case "B":
                        strOutput += "Beginner-sized board (3x3, 1 ship) selected.";
                        break;

                    case "I":
                        strOutput += "Intermediate-sized board (6x6, 3 ships) selected.";
                        break;

                    case "A":
                        strOutput += "Advanced-sized board (9x9, 5 ships) selected.";
                        break;
                }
                System.out.println(strOutput);
                bDifficultyValid = true;
            }
            else{
                System.out.println("Error: Invalid input for board size. Please try again.");
            }
        }
        char chDifficulty = DiffInp.charAt(0);
        char chMode = ModeInput.charAt(0);

        Board b1 = new Board(chDifficulty, chMode);
        if (chMode == 'T'){
            System.out.println("\nGame ready to start! The game will start with P1's boards on the screen, " +
                    "so make sure P2 isn't looking at the screen at the beginning!!\nPress enter to begin...");
        }
        else{ // singleplayer mode
            System.out.println("\nGame ready to start! Press enter to begin...");
        }
        String strInput = scn.nextLine();

        Help(chMode, chDifficulty); // Always display the help section once at the beginning of the game.

        boolean bGameContinue = true;
        boolean bInputValid;

        if (chMode == 'S'){ // Single player mode
            turnCountTotal = 0;
            while(bGameContinue){
                bInputValid = false;

                // Show enemy (P2) on top (hidden), your ships (P1) bottom (revealed)
                if (bAdmin){
                    System.out.println("[ENEMY'S SHIPS]");
                    System.out.println(b1.display(2, true));
                    System.out.println("---------------------------------");
                    System.out.println("[YOUR SHIPS]");
                    System.out.println(b1.display(1, false));
                } else {
                    System.out.println("[ENEMY'S SHIPS]");
                    System.out.println(b1.print(2, true));
                    System.out.println("---------------------------------");
                    System.out.println("[YOUR SHIPS]");
                    System.out.println(b1.display(1, false));
                }

                System.out.println("Turns: " + turnCountTotal);

                while(!bInputValid){
                    System.out.println("Type your input here. Type \"help\" to see the help section:");
                    strInput = scn.nextLine();
                    String cleaned = strInput.trim();

                    if (cleaned.equalsIgnoreCase("help")){
                        Help(chMode, chDifficulty);
                        bInputValid = false;
                        continue;
                    }
                    if (cleaned.equalsIgnoreCase("drone")){
                        String out = b1.drone(1,2);
                        System.out.println(out);
                        turnCountTotal += 1;
                        bInputValid = true;
                        break;
                    }

                    int[] coord = parseCoordinate(cleaned);
                    if (coord == null){
                        System.out.println("Error: Invalid input. Please try again.");
                        bInputValid = false;
                        continue;
                    }

                    int col = coord[0];
                    int row = coord[1];

                    // Validate coords within board size
                    boolean onBoard = true;
                    if (col < 0 || row < 0) onBoard = false;
                    else {
                        if (chDifficulty == 'B') onBoard = (col <= 2 && row <= 2);
                        else if (chDifficulty == 'I') onBoard = (col <= 5 && row <= 5);
                        else onBoard = (col <= 8 && row <= 8);
                    }
                    if (!onBoard){
                        System.out.println("Error: Invalid Coordinates.");
                        bInputValid = false;
                        continue;
                    }

                    // Determine command (fire/missile/submarine) from input
                    String lower = cleaned.toLowerCase(Locale.ROOT);
                    if (lower.startsWith("fire") || lower.matches("^[a-iA-I].*") || lower.matches("^[0-9].*")){
                        // Fire command
                        strOutput = b1.fire(col, row, 2, 1, false);
                        System.out.println(strOutput);
                        turnCountTotal += 1;
                        bInputValid = true;
                    } else if (lower.startsWith("missile")){
                        strOutput = b1.missile(col, row, 1, 2);
                        System.out.println(strOutput);
                        turnCountTotal += 1;
                        bInputValid = true;
                    } else if (lower.startsWith("submarine")){
                        strOutput = b1.submarine(col, row, 1, 2);
                        System.out.println(strOutput);
                        turnCountTotal += 1;
                        bInputValid = true;
                    } else {
                        // If input was just a coord like "b2" we consider it a fire above via regex; otherwise invalid
                        System.out.println("Error: Invalid input. Please try again.");
                        bInputValid = false;
                    }
                } // end input loop

                // After player's valid action, if game still ongoing, AI takes its turn
                if (bGameContinue){
                    System.out.println("It is now the AI's turn. Press enter to continue...");
                    strInput = scn.nextLine();
                    String aiResult = b1.aiTakeTurn();
                    System.out.println(aiResult);

                    // If AI sank all player's ships, end game
                    if (aiResult.toLowerCase(Locale.ROOT).contains("all of player 1's ships have been sunk") ||
                        aiResult.toLowerCase(Locale.ROOT).contains("ai wins")){
                        bGameContinue = false;
                    }

                    turnCountTotal += 1;
                }

                System.out.println("Press enter to continue...");
                strInput = scn.nextLine();
            }
            // END GAME SEQUENCE - Single player
            System.out.println("Total Turns: " + turnCountTotal);
            System.out.println("Thank you for playing! Goodbye.");
        }
        else { // Multiplayer mode unchanged - uses Board's fire/missile/sub/drone
            boolean bP1turn = true;
            turnCountP1 = 0;
            turnCountP2 = 0;
            turnCountTotal = 0;

            while(true){
                bInputValid = false;
                if (bAdmin){
                    if (bP1turn) {
                        System.out.println("[ENEMY'S SHIPS]");
                        System.out.println(b1.display(2, true));
                        System.out.println("---------------------------------");
                        System.out.println("[YOUR SHIPS]");
                        System.out.println(b1.display(1, false));
                    } else {
                        System.out.println("[ENEMY'S SHIPS]");
                        System.out.println(b1.display(1, true));
                        System.out.println("---------------------------------");
                        System.out.println("[YOUR SHIPS]");
                        System.out.println(b1.display(2, false));
                    }
                } else {
                    if (bP1turn) {
                        System.out.println("[ENEMY'S SHIPS]");
                        System.out.println(b1.print(2, true));
                        System.out.println("---------------------------------");
                        System.out.println("[YOUR SHIPS]");
                        System.out.println(b1.display(1, false));
                    } else {
                        System.out.println("[ENEMY'S SHIPS]");
                        System.out.println(b1.print(1, true));
                        System.out.println("---------------------------------");
                        System.out.println("[YOUR SHIPS]");
                        System.out.println(b1.display(2, false));
                    }
                }

                turnCountTotal = turnCountP1 + turnCountP2;
                System.out.println("Turns: " + turnCountTotal);

                String[] strSplit = new String[2];
                String[] coordSplit = new String[2];
                int[] intSplit = new int[2];
                boolean bCoordsValid = false;
                boolean bGameContinueLocal = true;
                boolean bWinner = false;

                while(!bInputValid){
                    if (bP1turn) System.out.println("It is player 1's turn."); else System.out.println("It is player 2's turn.");
                    System.out.print("Type your input here. Type \"help\" to view the help section:");
                    strInput = scn.nextLine();
                    String cleaned = strInput.trim();

                    if (cleaned.equalsIgnoreCase("help")){
                        Help(chMode, chDifficulty);
                        bInputValid = false;
                        continue;
                    }
                    if (cleaned.equalsIgnoreCase("drone")){
                        if (bP1turn) {
                            strOutput = b1.drone(1, 2);
                            turnCountP1 += 1;
                        } else {
                            strOutput = b1.drone(2, 1);
                            turnCountP2 += 1;
                        }
                        System.out.println(strOutput);
                        bInputValid = true;
                        break;
                    }

                    int[] coord = parseCoordinate(cleaned);
                    if (coord == null){
                        System.out.println("Error: Invalid input. Please try again.");
                        bInputValid = false;
                        continue;
                    }

                    int col = coord[0];
                    int row = coord[1];

                    // Validate coords within board size
                    boolean onBoard = true;
                    if (col < 0 || row < 0) onBoard = false;
                    else {
                        if (chDifficulty == 'B') onBoard = (col <= 2 && row <= 2);
                        else if (chDifficulty == 'I') onBoard = (col <= 5 && row <= 5);
                        else onBoard = (col <= 8 && row <= 8);
                    }
                    if (!onBoard){
                        System.out.println("Error: Invalid Coordinates.");
                        bInputValid = false;
                        continue;
                    }

                    String lower = cleaned.toLowerCase(Locale.ROOT);
                    if (lower.startsWith("fire") || lower.matches("^[a-iA-I].*") || lower.matches("^[0-9].*")){
                        if (bP1turn) {
                            strOutput = b1.fire(col, row, 2, 1, false);
                        } else {
                            strOutput = b1.fire(col, row, 1, 2, false);
                        }

                        if (strOutput.contains("WIN") || strOutput.contains("YOU WIN")){
                            if (bP1turn) bWinner = true; else bWinner = false;
                            bInputValid = true;
                            bGameContinueLocal = false;
                        } else {
                            if (strOutput.equals("INVALID COORDINATES")){
                                System.out.println(strOutput);
                                bInputValid = false;
                            } else {
                                System.out.println(strOutput);
                                if (bP1turn) turnCountP1 += 1; else turnCountP2 += 1;
                                bInputValid = true;
                            }
                        }
                    } else if (lower.startsWith("missile")){
                        if (bP1turn) strOutput = b1.missile(col, row, 1, 2);
                        else strOutput = b1.missile(col, row, 2, 1);

                        if (strOutput.contains("WIN")){
                            if (bP1turn) turnCountP1 += 1; else turnCountP2 += 1;
                            bInputValid = true;
                            bGameContinueLocal = false;
                            System.out.println(strOutput);
                        } else {
                            if (strOutput.equals("No more power points remaining. Cannot fire missile.") || strOutput.startsWith("Missile firing sequence failed")){
                                System.out.println(strOutput);
                                bInputValid = false;
                            } else {
                                System.out.println(strOutput);
                                if (bP1turn) turnCountP1 += 1; else turnCountP2 += 1;
                                bInputValid = true;
                            }
                        }
                    } else if (lower.startsWith("submarine")){
                        if (bP1turn) strOutput = b1.submarine(col, row, 1, 2);
                        else strOutput = b1.submarine(col, row, 2, 1);

                        if (strOutput.contains("WIN")){
                            System.out.println(strOutput);
                            if (bP1turn) turnCountP1 += 1; else turnCountP2 += 1;
                            bInputValid = true;
                            bGameContinueLocal = false;
                        } else {
                            if (strOutput.startsWith("Submarine deployment failed")){
                                System.out.println(strOutput);
                                bInputValid = false;
                            } else {
                                System.out.println(strOutput);
                                if (bP1turn) turnCountP1 += 1; else turnCountP2 += 1;
                                bInputValid = true;
                            }
                        }
                    } else {
                        System.out.println("Error: Invalid input. Please try again.");
                        bInputValid = false;
                    }
                } // end multiplayer input loop

                if (bP1turn){
                    bP1turn = false;
                    if (bGameContinueLocal) {
                        System.out.println("It is now the end of Player 1's turn.");
                        System.out.println("Please turn the screen to player 2 and look away from the screen for the duration if their turn." +
                                "\nPress enter to continue..");
                    }
                    strInput = scn.nextLine();
                } else {
                    bP1turn = true;
                    if (bGameContinueLocal) {
                        System.out.println("It is now the end of Player 2's turn.");
                        System.out.println("Please turn the screen to player 1 and look away from the screen for the duration if their turn." +
                                "\nPress enter to continue..");
                    }
                    strInput = scn.nextLine();
                }

                if (!bGameContinueLocal){
                    System.out.println("Game Over!");
                    System.out.println("Total Turns: " + (turnCountP1 + turnCountP2));
                    if (bWinner) System.out.println("Congratulations Player 1!"); else System.out.println("Congratulations Player 2!");
                    System.out.println("Thank you for playing! Goodbye.");
                    break;
                }

            } // end multiplayer loop
        } // end multiplayer branch

        scn.close();
    }
}
