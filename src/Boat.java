import java.util.Locale;

public class Boat {
    private int size;
    private int health;
    private String type;
    private int xpos;
    private int ypos;
    private char orientation;
    private int ID;
    private String[] coords; // coords[] is a 1D array of strings in the format x,y. coords[] stores the coordinates of every cell of each boat.

    public Boat(String t, int x, int y, char o, int Ident) {
        /*
        - Destroyers are 2 cells long.
        - Cruisers are 3 cells long. Submarines are used as powers instead of ships in this game.
        - Battleships are 4 cells long.
        - Carriers are 5 cells long.
        All ships have as much health as their cells.
         */

        Boolean bValid = true;
        t = t.toLowerCase(Locale.ROOT);
        this.type = t;
        if (t.equals("destroyer")){
            this.size = 2;
        }
        else{
            if (t.equals("cruiser")){
                this.size = 3;
            }
            else{
                if (t.equals("battleship")){
                    this.size = 4;
                }
                else{
                    if (t.equals("carrier")){
                        this.size = 5;

                    }
                    else{
                        bValid = false;
                        System.out.println("ERROR: Invalid input for boat type.");
                    }
                }
            }
        }
        if (bValid){
            this.coords = new String[this.size];
            this.type = t;
            this.health = this.size;
            this.xpos = x;
            this.ypos = y;
            this.ID = Ident;
            o = Character.toLowerCase(o);
            if (o == 'v' || o == 'h'){ // vertical or horizontal
                this.orientation = o;
            }
            else {
                System.out.println("ERROR: Invalid input for boat orientation.");
            }

            // Set up the coordinate array for the new boat
            this.coords[0] = (Integer.toString(x) + "," + Integer.toString(y));
            for (int i = 0; i < this.size; i++){
                if (this.orientation == 'v'){
                    this.coords[i] = (Integer.toString(x) + "," + Integer.toString(y+i));
                }
                else{ //this.orientation == 'h'
                    this.coords[i] = (Integer.toString(x+i) + "," + Integer.toString(y));
                }
            }
        }
    }

    public int getHealth(){
        return this.health;
    }

    public void setHealth(int h){
        if (h <= this.size){ // the ship size is the maximum health
            this.health = h;
        }
        else{
            System.out.println("ERROR: Input for setHealth exceeds maximum ship health allowed for " + this.type + "s.");
        }
    }

    public int getSize(){
        return this.size;
    }

    public boolean containsCoordinates(int x, int y){
        // Tests if a boat exists on specified coordinates.
        // Since the coordinates in coords[] are set up like (x,y), we split each string element into two string
        // sub-elements and check if they match the coordinates we are looking for.
        String temp[] = new String[2];
        String xInpString = Integer.toString(x);
        String yInpString = Integer.toString(y);
        boolean bMatchFound = false;
        for (int i = 0; i < this.coords.length; i++){
            temp = this.coords[i].split(",");
            if (xInpString.equals(temp[0]) && yInpString.equals(temp[1])){
                bMatchFound = true;
            }
        }
        return bMatchFound;
    }

    public char getOrientation(){
        return this.orientation;
    }

    public int getXpos(){
        return this.xpos;
    }

    public void setXpos(int x){
        this.xpos = x;
    }

    public int getYpos(){
        return this.ypos;
    }

    public void setYpos(int y){
        this.ypos = y;
    }

    public String getType(){
        return this.type;
    }
}