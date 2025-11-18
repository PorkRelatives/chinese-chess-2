package pieces;

import data.Color;
import data.PieceType;

public class GeneralPiece extends Piece{

    public GeneralPiece(Color color) {
        super(color, PieceType.GENERAL);
    }

    private boolean isInPalace(int row, int col) {
        if (col < 3 || col > 5) return false;
        // Red Palace: Rows 7, 8, 9
        if (color == Color.RED && row >= 7 && row <= 9) return true;
        // Black Palace: Rows 0, 1, 2
        if (color == Color.BLACK && row >= 0 && row <= 2) return true;
        return false;
    }

    public List

}
