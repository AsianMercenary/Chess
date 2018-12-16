
package game.pieces;

import game.Board;
import game.pieces.Piece;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Pawn extends Piece {
	private boolean enPassanting;
	public static BufferedImage white;
	public static BufferedImage black;

	public Pawn(int row, int col, int color, Board b) {
		super(row, col, color, b);
	}

	@Override
	public boolean legal(int row, int col) {
		int rdiff = Math.abs(row - this.row);
		int cdiff = Math.abs(col - this.col);
		if (row < 0 || row > 7 || col < 0 || col > 7 || rdiff == 0 && cdiff == 0) {
			return false;
		}
		if (this.color == Piece.WHITE) {
			if (cdiff == 0) { // moving forward
				if (this.b.color(row, col) == Piece.EMPTY
						&& (row == this.row + 1 || this.row == 1 && row == 3 && this.b.color(2, col) == Piece.EMPTY)) {
					return !this.b.pinned(row, col, this);
				}
			} else if (row == this.row + 1 && cdiff == 1) { // capturing
				int[] m = this.b.getLastMove();
				Piece p = this.b.getLastPiece();
				if (this.b.color(row, col) == Piece.BLACK) {
					return !this.b.pinned(row, col, this);
				}
				// en passant
				if (this.b.color(this.row, col) == Piece.BLACK && m[0] == 6 && m[1] == col && m[2] == 4 && m[3] == col
						&& p instanceof Pawn) {
					this.enPassanting = true;
					return !this.b.pinned(row, col, this);
				}
			}
		} else if (cdiff == 0) { // moving forward
			if (this.b.color(row, col) == Piece.EMPTY
					&& (row == this.row - 1 || this.row == 6 && row == 4 && this.b.color(5, col) == Piece.EMPTY)) {
				return !this.b.pinned(row, col, this);
			}
		} else if (row == this.row - 1 && cdiff == 1) { // capturing
			int[] m = this.b.getLastMove();
			Piece p = this.b.getLastPiece();
			if (this.b.color(row, col) == Piece.WHITE) {
				return !this.b.pinned(row, col, this);
			}
			// en passant
			if (this.b.color(this.row, col) == Piece.WHITE && m[0] == 1 && m[1] == col && m[2] == 3 && m[3] == col
					&& p instanceof Pawn) {
				this.enPassanting = true;
				return !this.b.pinned(row, col, this);
			}
		}
		return false;
	}

	@Override
	public boolean canMove() {
		if (this.color == WHITE) {
			return this.legal(this.row + 1, this.col) || this.legal(this.row + 1, this.col + 1)
					|| this.legal(this.row + 1, this.col - 1);
		}
		return this.legal(this.row - 1, this.col) || this.legal(this.row - 1, this.col + 1)
				|| this.legal(this.row - 1, this.col - 1);
	}

	@Override
	public boolean move(int row, int col) {
		if (this.legal(row, col) && !this.b.pinned(row, col, this)) {
			if (this.enPassanting) {
				this.b.move(this.row, this.col, row, col);
				if (this.color == WHITE) {
					this.b.setPiece(row - 1, col, null);
				} else {
					this.b.setPiece(row + 1, col, null);
				}
				this.b.markAsCapture();
				this.enPassanting = false;
				return true;
			}
			// promoting
			if (row == 0 || row == 7) {
				this.b.togglePrompt();
			}
			this.b.move(this.row, this.col, row, col);
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
			white = ImageIO.read(Pawn.class.getResource("imgs/wP.png"));
			black = ImageIO.read(Pawn.class.getResource("imgs/bP.png"));
		} catch (IOException i) {
			i.printStackTrace();
			System.exit(404);
		}
	}
}
