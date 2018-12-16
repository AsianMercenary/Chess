
package game.pieces;

import game.Board;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class King extends Piece {
	private boolean moved = false;
	private boolean lcastling = false;
	private boolean rcastling = false;
	public static BufferedImage white;
	public static BufferedImage black;

	public King(int row, int col, int color, Board b) {
		super(row, col, color, b);
	}

	@Override
	public boolean legal(int row, int col) {
		int rdiff = Math.abs(row - this.row);
		int cdiff = Math.abs(col - this.col);
		if (row < 0 || row > 7 || col < 0 || col > 7 || rdiff == 0 && cdiff == 0) {
			return false;
		}
		if (!(rdiff > 1 || cdiff > 1 || rdiff == 0 && cdiff == 0 || this.attacked(row, col))) {
			return this.b.color(row, col) != this.color;
		}

		// castling code
		if (!this.moved && rdiff == 0 && cdiff == 2 && !this.checked()) {
			if (col > this.col) {
				this.rcastling = false;
				Piece p = this.b.getPiece(row, 7);
				if (p != null && p instanceof Rook && !((Rook) p).moved) {
					for (int i = 1; i < 3; i++) {
						if (this.b.color(row, this.col + i) == EMPTY && !this.attacked(row, this.col + i))
							continue;
						return false;
					}
					this.rcastling = true;
					return !this.attacked(row, col);
				}
			} else {
				this.lcastling = false;
				Piece p = this.b.getPiece(row, 0);
				if (p != null && p instanceof Rook && !((Rook) p).moved) {
					for (int i = 1; i < 3; i++) {
						if (this.b.color(row, this.col - i) == EMPTY && !this.attacked(row, this.col - i))
							continue;
						return false;
					}
					if (this.b.color(row, 1) == EMPTY) {
						this.lcastling = true;
						return !this.attacked(row, col);
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean canMove() {
		return this.legal(this.row - 1, this.col - 1) || this.legal(this.row, this.col - 1)
				|| this.legal(this.row + 1, this.col - 1) || this.legal(this.row - 1, this.col)
				|| this.legal(this.row + 1, this.col) || this.legal(this.row + 1, this.col - 1)
				|| this.legal(this.row + 1, this.col) || this.legal(this.row + 1, this.col + 1);
	}

	public boolean checked() {
		return this.attacked(this.row, this.col);
	}

	@Override
	public boolean move(int row, int col) {
		if (this.legal(row, col)) {
			if (this.rcastling) {
				Rook r = (Rook) this.b.getPiece(row, 7);
				this.b.setPiece(row, 7, null);
				r.col = 5;
				this.b.setPiece(row, 5, r);
				this.rcastling = false;
				this.b.specialMove(this.row, this.col, row, col, 'R');
			} else if (this.lcastling) {
				Rook r = (Rook) this.b.getPiece(row, 0);
				this.b.setPiece(row, 0, null);
				r.col = 3;
				this.b.setPiece(row, 3, r);
				this.lcastling = false;
				this.b.specialMove(this.row, this.col, row, col, 'L');
			} else {
				this.b.move(this.row, this.col, row, col);
			}
			this.moved = true;
			return true;
		}
		return false;
	}

	@Override
	public BufferedImage drawBlack() {
		return black;
	}

	@Override
	public BufferedImage drawWhite() {
		return white;
	}

	static {
		try {
			white = ImageIO.read(King.class.getResource("imgs/wK.png"));
			black = ImageIO.read(King.class.getResource("imgs/bK.png"));
		} catch (IOException i) {
			i.printStackTrace();
			System.exit(404);
		}
	}
}
