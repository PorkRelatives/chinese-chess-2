package Core;

import data.Color;
import pieces.GeneralPiece;
import pieces.Piece;

public class Board {
    public static final int ROWS =10;
    public static final int COLS = 9;
    private final Piece[][] grid=new Piece[ROWS][COLS];
    private Color currentTurn=Color.RED;

    public Board(){
        initializeBoard();
    }

    private void initializeBoard(){
        grid[0][4] = new GeneralPiece(Color.BLACK);
        grid[9][4] = new GeneralPiece(Color.RED);
    }
}
