
// Board.java
// Row-major internals: board[row][col]
// Boat coordinates remain (col=x, row=y)
// Hunt-target AI with missile/sub/drone support and A-I column header display (Option A)

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Board {
    private int nrows;
    private int ncols;

    // Row-major boards: board[row][col]
    private Cell[][] boardP1;
    private Cell[][] boardP1hidden;
    private Cell[][] boardP2;
    private Cell[][] boardP2hidden;

    private Boat[] boatArray;   // P1 boats
    private Boat[] boatArrayP2; // P2 boats

    private int boatsRemainingP1;
    private int boatsRemainingP2;

    private int powerPointsRemainingP1;
    private int powerPointsRemainingP2;

    private String DroneScannedRowsP1 = "";
    private String DroneScannedColsP1 = "";
    private String DroneScannedRowsP2 = "";
    private String DroneScannedColsP2 = "";

    private boolean playermode; // true = singleplayer

    // AI state / memory
    private boolean[][] aiFired;        // cells AI has fired on (row,col)
    private LinkedList<int[]> aiTargetQueue; // queue of candidate target cells for TARGET mode
    private List<int[]> aiHitList;      // unsunk hit cells (row,col)
    private boolean aiInHunt;          // true = hunt mode, false = targeting/killing
    private char aiDirection;          // 'u','d','l','r','n' unknown
    private int[] aiPrimaryHit;        // first hit for current target (row,col)
    private int[] aiLastHit;           // last successful hit in current oriented line (row,col)

    public Board(char difficulty, char mode) {
        mode = Character.toUpperCase(mode);
        difficulty = Character.toUpperCase(difficulty);

        if (mode == 'S') {
            this.playermode = true;
            switch (difficulty) {
                case 'I':
                    this.boatsRemainingP1 = 3; this.powerPointsRemainingP1 = 3;
                    this.boatsRemainingP2 = 3; this.powerPointsRemainingP2 = 3;
                    this.ncols = 6; this.nrows = 6;
                    break;
                case 'A':
                    this.boatsRemainingP1 = 5; this.powerPointsRemainingP1 = 5;
                    this.boatsRemainingP2 = 5; this.powerPointsRemainingP2 = 5;
                    this.ncols = 9; this.nrows = 9;
                    break;
                default:
                case 'B':
                    this.boatsRemainingP1 = 1; this.powerPointsRemainingP1 = 1;
                    this.boatsRemainingP2 = 1; this.powerPointsRemainingP2 = 1;
                    this.ncols = 3; this.nrows = 3;
                    break;
            }

            this.boardP1 = new Cell[this.nrows][this.ncols];
            this.boardP2 = new Cell[this.nrows][this.ncols];

            this.boardP1 = placeBoats(this.ncols, this.nrows, 1);
            this.boardP2 = placeBoats(this.ncols, this.nrows, 2);

            initAIData();
        }
        else if (mode == 'T') {
            this.playermode = false;
            switch (difficulty) {
                case 'I':
                    this.boatsRemainingP1 = 3; this.powerPointsRemainingP1 = 3;
                    this.boatsRemainingP2 = 3; this.powerPointsRemainingP2 = 3;
                    this.ncols = 6; this.nrows = 6;
                    break;
                case 'A':
                    this.boatsRemainingP1 = 5; this.powerPointsRemainingP1 = 5;
                    this.boatsRemainingP2 = 5; this.powerPointsRemainingP2 = 5;
                    this.ncols = 9; this.nrows = 9;
                    break;
                default:
                case 'B':
                    this.boatsRemainingP1 = 1; this.powerPointsRemainingP1 = 1;
                    this.boatsRemainingP2 = 1; this.powerPointsRemainingP2 = 1;
                    this.ncols = 3; this.nrows = 3;
                    break;
            }

            this.boardP1 = new Cell[this.nrows][this.ncols];
            this.boardP2 = new Cell[this.nrows][this.ncols];

            this.boardP1 = placeBoats(this.ncols, this.nrows, 1);
            this.boardP2 = placeBoats(this.ncols, this.nrows, 2);

            initAIData(); // allocated but not used for human P2
        }
        else {
            System.out.println("ERROR: invalid input for singleplayer/multiplayer mode.");
        }
    }

    private void initAIData(){
        this.aiFired = new boolean[this.nrows][this.ncols];
        this.aiTargetQueue = new LinkedList<>();
        this.aiHitList = new ArrayList<>();
        this.aiInHunt = true;
        this.aiDirection = 'n';
        this.aiPrimaryHit = null;
        this.aiLastHit = null;
    }

    // placeBoats: (cols, rows, player) -> returns row-major board[row][col]
    public Cell[][] placeBoats(int cols, int rows, int player){
        Cell[][] cellBoard = new Cell[rows][cols];
        for (int r = 0; r < rows; r++){
            for (int c = 0; c < cols; c++){
                cellBoard[r][c] = new Cell(c, r, '-');
            }
        }

        if (player == 1){
            if (rows == 3){
                this.boatArray = new Boat[1];
                cellBoard = getRandomBoatConfig("destroyer", 0, 2, cellBoard, 1);
            } else if (rows == 6){
                this.boatArray = new Boat[3];
                cellBoard = getRandomBoatConfig("destroyer", 0, 2, cellBoard, 1);
                cellBoard = getRandomBoatConfig("cruiser", 1, 3, cellBoard, 1);
                cellBoard = getRandomBoatConfig("battleship", 2, 4, cellBoard, 1);
            } else if (rows == 9){
                this.boatArray = new Boat[5];
                cellBoard = getRandomBoatConfig("destroyer", 0, 2, cellBoard, 1);
                cellBoard = getRandomBoatConfig("cruiser", 1, 3, cellBoard, 1);
                cellBoard = getRandomBoatConfig("cruiser", 2, 3, cellBoard, 1);
                cellBoard = getRandomBoatConfig("battleship", 3, 4, cellBoard, 1);
                cellBoard = getRandomBoatConfig("carrier", 4, 5, cellBoard, 1);
            } else {
                System.out.println("ERROR: Invalid input for board size in placeBoats.");
            }
        } else {
            if (rows == 3){
                this.boatArrayP2 = new Boat[1];
                cellBoard = getRandomBoatConfig("destroyer", 0, 2, cellBoard, 2);
            } else if (rows == 6){
                this.boatArrayP2 = new Boat[3];
                cellBoard = getRandomBoatConfig("destroyer", 0, 2, cellBoard, 2);
                cellBoard = getRandomBoatConfig("cruiser", 1, 3, cellBoard, 2);
                cellBoard = getRandomBoatConfig("battleship", 2, 4, cellBoard, 2);
            } else if (rows == 9){
                this.boatArrayP2 = new Boat[5];
                cellBoard = getRandomBoatConfig("destroyer", 0, 2, cellBoard, 2);
                cellBoard = getRandomBoatConfig("cruiser", 1, 3, cellBoard, 2);
                cellBoard = getRandomBoatConfig("cruiser", 2, 3, cellBoard, 2);
                cellBoard = getRandomBoatConfig("battleship", 3, 4, cellBoard, 2);
                cellBoard = getRandomBoatConfig("carrier", 4, 5, cellBoard, 2);
            } else {
                System.out.println("ERROR: Invalid input for board size in placeBoats.");
            }
        }
        return cellBoard;
    }

    // getRandomBoatConfig adapted to row-major; Boat still stores (col,row).
    public Cell[][] getRandomBoatConfig(String type, int intBoatIndex, int boatSize, Cell[][] cellB, int player) {
        int rows = cellB.length;
        int cols = cellB[0].length;
        int intRightSideX = cols - 1;
        int intBottomSideY = rows - 1;
        int randOriInt;
        char randOri = 'h';
        int maxX, maxY;
        int intOffset = boatSize - 1;
        boolean bFitFound = false;
        int randX = -1;
        int randY = -1;

        while (!bFitFound) {
            randOriInt = getRandomNumber(0,1);
            if (randOriInt == 1) { // vertical
                randOri = 'v';
                maxX = intRightSideX;
                maxY = intBottomSideY - intOffset;
            } else {
                randOri = 'h';
                maxX = intRightSideX - intOffset;
                maxY = intBottomSideY;
            }
            randX = getRandomNumber(0, Math.max(0, maxX));
            randY = getRandomNumber(0, Math.max(0, maxY));
            bFitFound = CheckEmpty(randX, randY, boatSize, randOri, cellB);
        }

        if (randOri == 'h') {
            for (int i = 0; i < boatSize; i++) {
                int col = randX + i;
                int row = randY;
                cellB[row][col].setStatus('B');
            }
        } else {
            for (int i = 0; i < boatSize; i++) {
                int col = randX;
                int row = randY + i;
                cellB[row][col].setStatus('B');
            }
        }

        if (player == 1) this.boatArray[intBoatIndex] = new Boat(type, randX, randY, randOri, intBoatIndex);
        else this.boatArrayP2[intBoatIndex] = new Boat(type, randX, randY, randOri, intBoatIndex);

        return cellB;
    }

    public boolean CheckEmpty(int X, int Y, int size, char randOri, Cell[][] cellC){
        boolean bBoatFound = false;
        try {
            if (randOri == 'h'){
                for (int x = 0; x < size; x++){
                    if (cellC[Y][X + x].get_status() == 'B') bBoatFound = true;
                }
            } else {
                for (int y = 0; y < size; y++){
                    if (cellC[Y + y][X].get_status() == 'B') bBoatFound = true;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            bBoatFound = true;
        }
        return !bBoatFound;
    }

    public int getRandomNumber(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max+1);
    }

    // Fire: x=col, y=row
    public String fire(int x, int y, int targetPlayer, int shooter, boolean bHideMisses){
        Boat targetHit;
        int health;
        String toReturn = "";

        if (x >= 0 && x <= this.ncols-1 && y >= 0 && y <= this.nrows-1){
            if (targetPlayer == 1){
                char status = this.boardP1[y][x].get_status();
                switch (status){
                    case 'B':
                        targetHit = getBoatAtCoords(x,y,1);
                        if (targetHit != null){
                            toReturn = RandomHitMessage();
                            toReturn += "\nYou hit a ship!";
                            this.boardP1[y][x].setStatus('H');
                            health = targetHit.getHealth();
                            targetHit.setHealth(health - 1);
                            if (health - 1 == 0){
                                toReturn += "\nYou've sunk an enemy " + targetHit.getType() + "!";
                                this.boatsRemainingP1 -= 1;
                                if (this.boatsRemainingP1 == 0){
                                    toReturn += "\nThere are no enemy ships remaining. YOU WIN!";
                                } else {
                                    toReturn += "\nThere are " + this.boatsRemainingP1 + " enemy ships remaining...";
                                }
                            } else {
                                // not sunk: nothing else to do
                            }
                        } else toReturn = "INVALID SHIP";
                        break;
                    case '-':
                        if (!bHideMisses){
                            toReturn += RandomMissMessage();
                            toReturn += "MISS";
                        }
                        this.boardP1[y][x].setStatus('M');
                        break;
                    case 'H':
                        toReturn += "You've already hit that part of that ship. It can't get any more destroyed!";
                        targetHit = getBoatAtCoords(x,y,1);
                        if (targetHit != null) toReturn += "\nSounds like you hit a ship!";
                        break;
                    case 'M':
                        toReturn += "\nYou've already tried shooting that spot before, and you missed! Don't remember? Too bad!";
                        break;
                    default:
                        toReturn += "";
                }
            } else if (targetPlayer == 2){
                char status = this.boardP2[y][x].get_status();
                switch (status){
                    case 'B':
                        targetHit = getBoatAtCoords(x,y,2);
                        if (targetHit != null){
                            toReturn = RandomHitMessage();
                            toReturn += "\nYou hit a ship!";
                            this.boardP2[y][x].setStatus('H');
                            health = targetHit.getHealth();
                            targetHit.setHealth(health - 1);
                            if (health - 1 == 0){
                                toReturn += "\nYou've sunk an enemy " + targetHit.getType() + "!";
                                this.boatsRemainingP2 -= 1;
                                if (this.boatsRemainingP2 == 0){
                                    toReturn += "\nThere are no enemy ships remaining. YOU WIN!";
                                } else {
                                    toReturn += "\nThere are " + this.boatsRemainingP2 + " enemy ships remaining...";
                                }
                            }
                        } else toReturn = "INVALID SHIP";
                        break;
                    case '-':
                        if (!bHideMisses){ toReturn += RandomMissMessage(); toReturn += "MISS"; }
                        this.boardP2[y][x].setStatus('M');
                        break;
                    case 'H':
                        toReturn += "You've already hit that part of that ship. It can't get any more destroyed!";
                        targetHit = getBoatAtCoords(x,y,2);
                        if (targetHit != null) toReturn += "\nSounds like you hit a ship!";
                        break;
                    case 'M':
                        toReturn += "\nYou've already tried shooting that spot before, and you missed! Don't remember? Too bad!";
                        break;
                    default:
                        toReturn += "";
                }
            } else {
                toReturn = "INVALID PLAYER";
            }
        } else {
            if (!bHideMisses){
                System.out.println("Error: Where are you aiming? Those coordinates are off the map!");
            }
            toReturn = "INVALID COORDINATES";
        }

        return toReturn;
    }

    public String RandomMissMessage(){
        int random = getRandomNumber(1, 10);
        String message = "";
        switch(random){
            case 1: message = "\nIt's a miss.\n"; break;
            case 2: message = "\nYou bombed some dirty whale poachers! Too bad they don't count...\n"; break;
            case 3: message = "\nSploosh.\n"; break;
            case 4: message = "\nNothing but ocean...\n"; break;
            case 5: message = "\nMiss!\n"; break;
            case 6: message = "\nb r i c k\n"; break;
            case 7: message = "\nwhoosh\n"; break;
            case 8: message = "\nMethinks the cannon's busted!\n"; break;
            case 9: message = "\nSome wind pushed the projectile off-course!\n"; break;
            case 10: message = "\nIt's a Miss! Darn this lazy eye.\n"; break;
            default: message = "\nYou missed.\n";
        }
        return message;
    }

    public String RandomHitMessage(){
        int rando = getRandomNumber(1, 10);
        String message = "";
        switch(rando){
            case 1: message = "\nIT'S A HIT!!\n"; break;
            case 2: message = "\nBOOM! Nice shot!\n"; break;
            case 3: message = "\n[[EXPLOSIONS!]]\n"; break;
            case 4: message = "\nULTRA KILL!\n"; break;
            case 5: message = "**OH THE HUMANITY!**\n"; break;
            case 6: message = "\nNailed it!\n"; break;
            case 7: message = "\nBULLSEYE!\n"; break;
            case 8: message = "\n**METAL CRUNCHING INTENSIFIES**\n"; break;
            case 9: message = "\nHEADSHOT!\n"; break;
            case 10: message = "\nRight between the eyes!\n"; break;
            default: message = "\nGood Shot!\n"; break;
        }
        return message;
    }

    // sink: boat coordinates are stored as (col=x, row=y) in Boat. board is [row][col].
    public String sink(Boat toSink, int sinkingplayer, boolean bShowMessages){
        char TBOri = toSink.getOrientation();
        int TBSize = toSink.getSize();
        int TBbowX = toSink.getXpos();
        int TBbowY = toSink.getYpos();
        String sunkenShipIn = toSink.getType();
        String sunkenShipOut;

        for (int i = 0; i < TBSize; i++){
            if (TBOri == 'h'){
                if (sinkingplayer == 1) this.boardP1[TBbowY][TBbowX + i].setStatus('H');
                else this.boardP2[TBbowY][TBbowX + i].setStatus('H');
            } else {
                if (sinkingplayer == 1) this.boardP1[TBbowY + i][TBbowX].setStatus('H');
                else this.boardP2[TBbowY + i][TBbowX].setStatus('H');
            }
        }

        if (sinkingplayer == 2) this.boatsRemainingP2 -= 1;
        else this.boatsRemainingP1 -= 1;
        toSink.setHealth(0);
        sunkenShipOut = sunkenShipIn.substring(0,1).toUpperCase(Locale.ROOT) + sunkenShipIn.substring(1);
        if (!bShowMessages) return "";
        String strReturn = "You've sunk an enemy " + sunkenShipOut + "!";
        if (this.boatsRemainingP1 == 0) strReturn = "All of Player 1's ships have been sunk. Congratulations, you WIN!";
        else if (this.boatsRemainingP2 == 0) strReturn = "All of Player 2's ships have been sunk. Congratulations, you WIN!";
        return strReturn;
    }

    // getBoatAtCoords: x=col y=row
    public Boat getBoatAtCoords(int x, int y, int player){
        boolean boatFound = false;
        int boatIndex = 0;
        if (x < 0 || x > this.ncols-1 || y < 0 || y > this.nrows-1) return null;
        if (player == 1){
            char s = this.boardP1[y][x].get_status();
            if (s == 'B' || s == 'H'){
                for (int i = 0; i < this.boatArray.length; i++){
                    if (this.boatArray[i].containsCoordinates(x, y)){
                        boatFound = true; boatIndex = i;
                    }
                }
                if (boatFound) return this.boatArray[boatIndex];
            }
            return null;
        } else {
            char s = this.boardP2[y][x].get_status();
            if (s == 'B' || s == 'H'){
                for (int i = 0; i < this.boatArrayP2.length; i++){
                    if (this.boatArrayP2[i].containsCoordinates(x, y)){
                        boatFound = true; boatIndex = i;
                    }
                }
                if (boatFound) return this.boatArrayP2[boatIndex];
            }
            return null;
        }
    }

    // display (debug) and print (game view)
    // Option A: print top header letters A.. and left numeric row labels; remove bottom arrows/numbers.
    public String display(int player, boolean bHideHUD){
        StringBuilder sb = new StringBuilder();

        // Top column letters
        sb.append("  ");
        for (int c = 0; c < this.ncols; c++){
            sb.append((char)('A' + c));
            if (c < this.ncols - 1) sb.append(" ");
        }
        sb.append("\n");

        if (player == 1){
            for (int r = 0; r < this.nrows; r++){
                sb.append((r+1) + " ");
                for (int c = 0; c < this.ncols; c++){
                    sb.append(this.boardP1[r][c].get_status());
                    if (c < this.ncols - 1) sb.append(" ");
                }
                sb.append("\n");
            }
        } else if (player == 2){
            for (int r = 0; r < this.nrows; r++){
                sb.append((r+1) + " ");
                for (int c = 0; c < this.ncols; c++){
                    sb.append(this.boardP2[r][c].get_status());
                    if (c < this.ncols - 1) sb.append(" ");
                }
                sb.append("\n");
            }
        } else return "Error: Invalid input for player in display method.";

        if (!bHideHUD){
            if (player == 1){
                sb.append("PP: " + this.powerPointsRemainingP1);
                if (this.playermode) sb.append(" | Enemy Ships Remaining: " +  this.boatsRemainingP2);
                else sb.append(" | Enemy Ships Remaining: " +  this.boatsRemainingP2 + " | Your ships remaining: " + this.boatsRemainingP1);
            } else {
                sb.append("PP: " + this.powerPointsRemainingP2 + " | Enemy Ships Remaining: " +  this.boatsRemainingP1 + " | Your ships remaining: " + this.boatsRemainingP2);
            }
        }
        return sb.toString();
    }

    public String print(int player, boolean bHideHUD){
        StringBuilder sb = new StringBuilder();
        if (player == 1){
            this.boardP1hidden = new Cell[this.nrows][this.ncols];
            // header
            sb.append("  ");
            for (int c = 0; c < this.ncols; c++){
                sb.append((char)('A' + c));
                if (c < this.ncols - 1) sb.append(" ");
            }
            sb.append("\n");

            for (int r = 0; r < this.nrows; r++){
                sb.append((r+1) + " ");
                for (int c = 0; c < this.ncols; c++){
                    char s = this.boardP1[r][c].get_status();
                    this.boardP1hidden[r][c] = new Cell(c, r, CellPrintStatusChecker(s));
                    sb.append(this.boardP1hidden[r][c].get_status());
                    if (c < this.ncols - 1) sb.append(" ");
                }
                sb.append("\n");
            }
        } else if (player == 2){
            this.boardP2hidden = new Cell[this.nrows][this.ncols];
            // header
            sb.append("  ");
            for (int c = 0; c < this.ncols; c++){
                sb.append((char)('A' + c));
                if (c < this.ncols - 1) sb.append(" ");
            }
            sb.append("\n");

            for (int r = 0; r < this.nrows; r++){
                sb.append((r+1) + " ");
                for (int c = 0; c < this.ncols; c++){
                    char s = this.boardP2[r][c].get_status();
                    this.boardP2hidden[r][c] = new Cell(c, r, CellPrintStatusChecker(s));
                    sb.append(this.boardP2hidden[r][c].get_status());
                    if (c < this.ncols - 1) sb.append(" ");
                }
                sb.append("\n");
            }
        } else {
            System.out.println("ERROR: Invalid input for player in display method.");
        }

        if (!bHideHUD){
            if (player == 1){
                sb.append("PP: " + this.powerPointsRemainingP1);
                if (this.playermode) sb.append(" | Enemy Ships Remaining: " +  this.boatsRemainingP2);
                else sb.append(" | Enemy Ships Remaining: " +  this.boatsRemainingP2 + " | Your ships remaining: " + this.boatsRemainingP1);
            } else {
                sb.append("PP: " + this.powerPointsRemainingP2 + " | Enemy Ships Remaining: " +  this.boatsRemainingP1 + " | Your ships remaining: " + this.boatsRemainingP2);
            }
        }

        return sb.toString();
    }

    private char CellPrintStatusChecker(char c){
        switch(c) {
            case 'H': return 'X';
            case 'M': return '0';
            case 'b': return c;
            default: return '~';
        }
    }

    // missile, submarine, drone remain mostly the same as earlier but adapted to row-major
    public String missile(int x, int y, int shooter, int targetPlayer){
        int HitCount = 0;
        int MissCount = 0;
        boolean bFireMissile = false;
        int PowerPtCost = 3;

        if (x < 0 || x > this.ncols-1 || y < 0 || y > this.nrows-1) return "Error: Input coordinates are not on the board. Missile not fired.";

        if (shooter == 1){
            if (this.powerPointsRemainingP1 >= PowerPtCost) bFireMissile = true;
            else return ("Missile firing sequence failed: Player 1 does not have " + PowerPtCost + " or more PP remaining to fire a missile.");
        } else if (shooter == 2){
            if (this.powerPointsRemainingP2 >= PowerPtCost) bFireMissile = true;
            else return ("Missile firing sequence failed: Player 2 does not have " + PowerPtCost + " or more PP remaining to fire a missile.");
        } else return "Error: invalid input for shooter.";

        if (bFireMissile){
            if (shooter == 1) this.powerPointsRemainingP1 -= PowerPtCost;
            else this.powerPointsRemainingP2 -= PowerPtCost;

            String strRes = "";
            boolean bWin = false;
            for (int yIter = y-1; yIter <= y+1; yIter++){
                for (int xIter = x-1; xIter <= x+1; xIter++){
                    if (xIter >= 0 && xIter <= this.ncols-1 && yIter >= 0 && yIter <= this.nrows-1){
                        strRes += fire(xIter, yIter, targetPlayer, shooter, true);
                        if (strRes.contains("WIN")) bWin = true;
                        else {
                            if (strRes.toLowerCase(Locale.ROOT).contains("hit")) HitCount++;
                            else MissCount++;
                        }
                    }
                }
            }
            if (bWin) return strRes;
            else return ("The missile hit " + HitCount + " times and missed " + MissCount + " times.");
        }
        return "No more power points remaining. Cannot fire missile.";
    }

    public String submarine(int x, int y, int shooter, int targetPlayer) {
        int PowerPtCost = 2;
        Boat TargetBoat;
        boolean bDeploy = false;
        String toReturn = "";

        if (x < 0 || x > this.ncols-1 || y < 0 || y > this.nrows-1) return "Error: those coordinates are off the map!";

        if (shooter == 1){
            if (this.powerPointsRemainingP1 >= PowerPtCost) bDeploy = true;
            else return "Submarine deployment failed. Player 1 does not have enough PP to deploy a submarine.";
        } else if (shooter == 2){
            if (this.powerPointsRemainingP2 >= PowerPtCost) bDeploy = true;
            else return "Submarine deployment failed. Player 2 does not have enough PP to deploy a submarine.";
        } else return "Error: invalid input for shooter.";

        if (bDeploy){
            if (shooter == 1) this.powerPointsRemainingP1 -= PowerPtCost;
            else this.powerPointsRemainingP2 -= PowerPtCost;

            if (targetPlayer == 1){
                char s = this.boardP1[y][x].get_status();
                if (s == 'B' || s == 'H'){
                    TargetBoat = getBoatAtCoords(x,y,targetPlayer);
                    toReturn = "The sub's nuclear torpedo hit! You sunk an entire " + TargetBoat.getType() + " in one hit!\n";
                    toReturn += sink(TargetBoat, 1, false);
                } else toReturn = "The submarine's torpedo missed!";
            } else {
                char s = this.boardP2[y][x].get_status();
                if (s == 'B' || s == 'H'){
                    TargetBoat = getBoatAtCoords(x,y,targetPlayer);
                    toReturn = "The sub's nuclear torpedo hit! You sunk an entire " + TargetBoat.getType() + " in one hit!\n";
                    toReturn += sink(TargetBoat, 2, false);
                } else toReturn = "The submarine's torpedo missed!";
            }
        }
        return toReturn;
    }

    public String drone(int shooter, int target){
        int DronePPCost = 1;
        boolean bDeploy = false;

        if (shooter == 1){
            if (this.powerPointsRemainingP1 >= DronePPCost){
                this.powerPointsRemainingP1 -= DronePPCost; bDeploy = true;
            } else return ("Drone deployment sequence failed: Player 1 does not have " + DronePPCost + " or more PP remaining to deploy a drone.");
        } else if (shooter == 2){
            if (this.powerPointsRemainingP2 >= DronePPCost){
                this.powerPointsRemainingP2 -= DronePPCost; bDeploy = true;
            } else return ("Drone deployment sequence failed: Player 2 does not have " + DronePPCost + " or more PP remaining to deploy a drone.");
        } else return "Error: Invalid input for drone-deploying player.";

        if (!bDeploy) return "Drone deployment failed.";

        int coin = getRandomNumber(0,1);
        int boatCount = 0;

        if (coin == 0){
            int randRow = getRandomNumber(0, this.nrows-1);
            if (shooter == 1){
                while (this.DroneScannedRowsP1.contains(Integer.toString(randRow))) randRow = getRandomNumber(0, this.nrows-1);
                this.DroneScannedRowsP1 += Integer.toString(randRow);
            } else {
                while (this.DroneScannedRowsP2.contains(Integer.toString(randRow))) randRow = getRandomNumber(0, this.nrows-1);
                this.DroneScannedRowsP2 += Integer.toString(randRow);
            }

            for (int c = 0; c < this.ncols; c++){
                if (target == 1){
                    if (this.boardP1[randRow][c].get_status() == 'B') boatCount++;
                } else {
                    if (this.boardP2[randRow][c].get_status() == 'B') boatCount++;
                }
            }
            return ("DRONE REPORT: There is " + boatCount + " unhit enemy ship(s) in row " + (randRow+1) + ". Remember this!");
        } else {
            int randCol = getRandomNumber(0, this.ncols-1);
            if (shooter == 1){
                while (this.DroneScannedColsP1.contains(Integer.toString(randCol))) randCol = getRandomNumber(0, this.ncols-1);
                this.DroneScannedColsP1 += Integer.toString(randCol);
            } else {
                while (this.DroneScannedColsP2.contains(Integer.toString(randCol))) randCol = getRandomNumber(0, this.ncols-1);
                this.DroneScannedColsP2 += Integer.toString(randCol);
            }

            for (int r = 0; r < this.nrows; r++){
                if (target == 1){
                    if (this.boardP1[r][randCol].get_status() == 'B') boatCount++;
                } else {
                    if (this.boardP2[r][randCol].get_status() == 'B') boatCount++;
                }
            }
            String colLetter = "";
            switch(randCol+1){
                case 1:
                    colLetter = "A";
                    break;
                case 2:
                    colLetter = "B";
                    break;
                case 3:
                    colLetter = "C";
                    break;
                case 4:
                    colLetter = "D";
                    break;
                case 5:
                    colLetter = "E";
                    break;
                case 6:
                    colLetter = "F";
                    break;
                case 7:
                    colLetter = "G";
                    break;
                case 8:
                    colLetter = "H";
                    break;
                case 9:
                    colLetter = "I";
                    break;
            }

            return ("DRONE REPORT: There is " + boatCount + " unhit enemy ship(s) in column " + colLetter + ". Remember this!");
        }
    }

    // -------------------------
    // AI Implementation - Hunt/Target basics
    // -------------------------

    // Mark a cell as fired for AI (row,col)
    private void aiMarkFired(int row, int col){
        if (row >=0 && row < this.nrows && col >=0 && col < this.ncols) aiFired[row][col] = true;
    }

    // Check whether AI considers cell untried
    private boolean aiUntried(int row, int col){
        if (row < 0 || row >= this.nrows || col < 0 || col >= this.ncols) return false;
        return !aiFired[row][col];
    }

    // Add adjacent valid untried neighbors to target queue (prioritize orthogonal)
    private void aiEnqueueNeighbors(int row, int col){
        int[][] dirs = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } }; // up, down, left, right (row delta, col delta)
        for (int[] d : dirs){
            int nr = row + d[0];
            int nc = col + d[1];
            if (aiUntried(nr, nc)){
                // do not add duplicates
                boolean found = false;
                for (int[] q : aiTargetQueue){
                    if (q[0] == nr && q[1] == nc) { found = true; break; }
                }
                if (!found) aiTargetQueue.addFirst(new int[]{nr, nc});
            }
        }
    }

    // Determine orientation ('v' or 'h') between two hits. Return 'n' if not aligned.
    private char aiDetermineOrientation(int[] a, int[] b){
        if (a == null || b == null) return 'n';
        if (a[0] == b[0]) return 'h'; // same row -> horizontal (col changes)
        if (a[1] == b[1]) return 'v'; // same col -> vertical (row changes)
        return 'n';
    }

    // Check if cell is within board bounds
    private boolean inBounds(int row, int col){
        return row >= 0 && row < this.nrows && col >= 0 && col < this.ncols;
    }

    // AI selects a hunt target using checkerboard parity for larger boards
    private int[] aiChooseHuntTarget(){
        // If board is small, just find any untried
        if (this.ncols <= 3){
            for (int r=0; r< nrows; r++){
                for (int c=0; c < ncols; c++){
                    if (!aiFired[r][c]) return new int[]{ r, c };
                }
            }
        }
        // Use parity: (row+col) % 2 == 0 first, then fallback
        for (int parity = 0; parity < 2; parity++){
            for (int r = 0; r < nrows; r++){
                for (int c = 0; c < ncols; c++){
                    if (((r + c) & 1) == parity && !aiFired[r][c]) return new int[]{ r, c };
                }
            }
        }
        // fallback any untried
        for (int r=0; r< nrows; r++){
            for (int c=0; c<ncols; c++){
                if (!aiFired[r][c]) return new int[]{ r, c };
            }
        }
        // Everything tried -> return random
        return new int[]{ getRandomNumber(0,nrows-1), getRandomNumber(0,ncols-1) };
    }

    // AI chooses an action (fire|missile|submarine|drone) with weights and heuristics.
    private String aiChooseActionAndExecute(){
        boolean inTargeting = (!aiTargetQueue.isEmpty() || !aiHitList.isEmpty()) && !aiInHunt;
        int r = getRandomNumber(1,100);
        String chosen = "fire";

        if (r <= 70) chosen = "fire";
        else if (r <= 80) chosen = "missile";
        else if (r <= 90) chosen = "submarine";
        else chosen = "drone";

        if (inTargeting){
            if (aiHitList.size() >= 2 && powerPointsRemainingP2 >= 2){
                if (getRandomNumber(1,100) <= 50) chosen = "submarine";
                else chosen = "fire";
            } else {
                if (getRandomNumber(1,100) <= 85) chosen = "fire";
                else chosen = "missile";
            }
        } else {
            int unknowns = 0;
            for (int rr=0; rr<nrows; rr++){
                for (int cc=0; cc<ncols; cc++) if (!aiFired[rr][cc]) unknowns++;
            }
            double unknownRatio = (double) unknowns / (nrows * ncols);
            if (unknownRatio > 0.6 && powerPointsRemainingP2 >= 1 && getRandomNumber(1,100) <= 12) chosen = "drone";
        }

        if (chosen.equals("missile") && powerPointsRemainingP2 < 3) chosen = "fire";
        if (chosen.equals("submarine") && powerPointsRemainingP2 < 2) chosen = "fire";
        if (chosen.equals("drone") && powerPointsRemainingP2 < 1) chosen = "fire";

        switch (chosen){
            case "missile":
                int[] center;
                if (!aiTargetQueue.isEmpty()){
                    center = aiTargetQueue.peekFirst();
                } else {
                    int[] h = aiChooseHuntTarget(); center = h;
                }
                String resM = missile(center[1], center[0], 2, 1);
                for (int rr = center[0]-1; rr <= center[0]+1; rr++){
                    for (int cc = center[1]-1; cc <= center[1]+1; cc++){
                        if (inBounds(rr,cc)) aiMarkFired(rr,cc);
                    }
                }
                for (int rr = center[0]-1; rr <= center[0]+1; rr++){
                    for (int cc = center[1]-1; cc <= center[1]+1; cc++){
                        if (inBounds(rr,cc)){
                            if (boardP1[rr][cc].get_status() == 'H'){
                                boolean present=false;
                                for (int[] hh: aiHitList) if (hh[0]==rr && hh[1]==cc) present=true;
                                if (!present) aiHitList.add(new int[]{rr,cc});
                            }
                        }
                    }
                }
                if (!aiHitList.isEmpty()){
                    aiInHunt = false;
                    if (aiPrimaryHit==null) aiPrimaryHit = aiHitList.get(0);
                    for (int[] hh : aiHitList) aiEnqueueNeighbors(hh[0], hh[1]);
                }
                return "AI used missile at (" + (center[1]+1) + "," + (center[0]+1) + "): " + resM;

            case "submarine":
                int tx=-1, ty=-1;
                if (!aiHitList.isEmpty()){
                    boolean chosenCell = false;
                    for (int[] hh : aiHitList){
                        int[][] dirs = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
                        for (int[] d : dirs){
                            int nr = hh[0]+d[0], nc = hh[1]+d[1];
                            if (aiUntried(nr,nc)){ ty = nr; tx = nc; chosenCell = true; break; }
                        }
                        if (chosenCell) break;
                    }
                    if (!chosenCell){
                        int[] h = aiChooseHuntTarget(); ty = h[0]; tx = h[1];
                    }
                } else {
                    int[] h = aiChooseHuntTarget(); ty = h[0]; tx = h[1];
                }
                String resS = submarine(tx, ty, 2, 1);
                aiMarkFired(ty, tx);
                if (boardP1[ty][tx].get_status() == 'H'){
                    boolean present=false;
                    for (int[] hh: aiHitList) if (hh[0]==ty && hh[1]==tx) present=true;
                    if (!present) aiHitList.add(new int[]{ty,tx});
                    aiInHunt = false;
                    if (aiPrimaryHit==null) aiPrimaryHit = new int[]{ty,tx};
                    aiEnqueueNeighbors(ty,tx);
                }
                return "AI deployed submarine at (" + (tx+1) + "," + (ty+1) + "): " + resS;

            case "drone":
                String resD = drone(2,1);
                return "AI used drone: " + resD;

            default:
            case "fire":
                int[] chosenCell;
                if (!aiTargetQueue.isEmpty()){
                    chosenCell = aiTargetQueue.pollFirst();
                } else if (!aiHitList.isEmpty()){
                    aiEnqueueNeighbors(aiPrimaryHit[0], aiPrimaryHit[1]);
                    if (!aiTargetQueue.isEmpty()) chosenCell = aiTargetQueue.pollFirst();
                    else chosenCell = aiChooseHuntTarget();
                } else {
                    chosenCell = aiChooseHuntTarget();
                }

                int row = chosenCell[0], col = chosenCell[1];
                aiMarkFired(row, col);

                String resF = fire(col, row, 1, 2, false); // AI shoots P1
                if (resF.toLowerCase(Locale.ROOT).contains("hit")){
                    boolean present = false;
                    for (int[] hh : aiHitList) if (hh[0]==row && hh[1]==col) present = true;
                    if (!present) aiHitList.add(new int[]{row,col});
                    if (aiPrimaryHit == null) aiPrimaryHit = new int[]{row,col};
                    if (aiHitList.size() >= 2){
                        int[] a = aiHitList.get(0);
                        int[] b = aiHitList.get(aiHitList.size()-1);
                        char ori = aiDetermineOrientation(a,b);
                        if (ori != 'n'){
                            aiDirection = 'n';
                            aiLastHit = new int[]{row,col};
                            aiEnqueueNeighbors(row,col);
                        } else {
                            aiEnqueueNeighbors(row,col);
                        }
                    } else {
                        aiEnqueueNeighbors(row,col);
                    }
                    aiInHunt = false;
                }

                if (resF.toLowerCase(Locale.ROOT).contains("sunk") || resF.toLowerCase(Locale.ROOT).contains("you've sunk")){
                    aiHitList.clear();
                    aiTargetQueue.clear();
                    aiPrimaryHit = null;
                    aiLastHit = null;
                    aiDirection = 'n';
                    aiInHunt = true;
                }

                String shortRes = "Miss";
                if (resF.toLowerCase(Locale.ROOT).contains("hit")) shortRes = "Hit";
                if (resF.toLowerCase(Locale.ROOT).contains("sunk")) shortRes = "Sunk";
                String colLetter = "";
                switch(col+1){
                    case 1:
                        colLetter = "A";
                        break;
                    case 2:
                        colLetter = "B";
                        break;
                    case 3:
                        colLetter = "C";
                        break;
                    case 4:
                        colLetter = "D";
                        break;
                    case 5:
                        colLetter = "E";
                        break;
                    case 6:
                        colLetter = "F";
                        break;
                    case 7:
                        colLetter = "G";
                        break;
                    case 8:
                        colLetter = "H";
                        break;
                    case 9:
                        colLetter = "I";
                        break;
                }

                return "AI fired at " + colLetter + (row+1) + ": " + shortRes;
        }
    }

    // Public method AI uses to take its turn; returns minimal string describing action
    public String aiTakeTurn(){
        if (boatsRemainingP1 <= 0) return "AI did nothing. Player already defeated.";

        if (!aiHitList.isEmpty() || !aiTargetQueue.isEmpty()){
            aiInHunt = false;
        } else {
            aiInHunt = true;
        }

        String result = aiChooseActionAndExecute();

        if (boatsRemainingP1 <= 0) {
            result += "\nAll of Player 1's ships have been sunk. AI WINS!";
        }
        return result;
    }

    // END AI
}
