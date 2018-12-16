
package game.pieces;

import game.Board;
import java.awt.image.BufferedImage;

public abstract class Piece {
	public int row;
	public int col;
	public int color;
	public static int EMPTY = 0;
	public static int WHITE = 1;
	public static int BLACK = 2;
	Board b;

	public Piece(int row, int col, int color, Board b) {
		this.row = row;
		this.col = col;
		this.color = color;
		this.b = b;
	}

	public abstract boolean legal(int var1, int var2);

	// returns whether the move was made
	public boolean move(int row, int col) {
		if (this.legal(row, col)) {
			this.b.move(this.row, this.col, row, col);
			return true;
		}
		return false;
	}

	public boolean attacked(int moverow, int movecol) {
		this.b.setPiece(this.row, this.col, null);
		Piece moveold = this.b.getPiece(moverow, movecol);
		this.b.setPiece(moverow, movecol, this);
		boolean o = false;
		for (Piece[] pieces : b.pieces()) {
			for (Piece p : pieces) {
				if (p == null || p.color == this.color || !p.legal(moverow, movecol))
					continue;
				o = true;
				break;
			}
			if (o)
				break;
		}
		this.b.setPiece(moverow, movecol, moveold);
		this.b.setPiece(this.row, this.col, this);
		return o;
	}

	// determines whether a piece has any legal moves
	public abstract boolean canMove();

	public abstract BufferedImage drawBlack();

	public abstract BufferedImage drawWhite();
}
