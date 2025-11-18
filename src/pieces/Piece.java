package pieces;

import data.Color;
import data.PieceType;

public abstract class Piece {
    public final Color color;
    public final PieceType pieceType;

    public Piece(Color color, PieceType pieceType){
        this.color=color;
        this.pieceType=pieceType;
    }

}
