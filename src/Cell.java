public class Cell {
    private int intRow;
    private int intCol;
    private char chStatus;

    public Cell(int col, int row, char c){
        // X-coord is the column, Y-coord is the row.
        this.intCol = col;
        this.intRow = row;
        this.chStatus = c;
    }

    public char get_status(){
        return this.chStatus;
    }

    public int getIntRow(){
        return this.intRow;
    }

    public void setIntRow(int r){
        this.intRow = r;
    }

    public int getIntCol(){
        return this.intCol;
    }

    public void setIntCol(int c){
        this.intCol = c;
    }

    public void setStatus(char c){
        if ((c == '-' || c == 'B') || ((c == 'H') || c == 'M') || c == 'b'){
            // '-' means the cell has not been guessed, and there is not a boat present.
            // 'B' means the cell has not been guessed, and there is a boat present.
            // 'H' means the cell has been guessed, and there is a boat present.
            // 'M' means the cell has been guessed, and there is not a boat present.
            // 'b' means the cell has not been guessed, a boat is present and a drone has scanned it.
            this.chStatus = c;
        }
        else{
            System.out.println("ERROR: Invalid input for cell status.");
        }
    }
}