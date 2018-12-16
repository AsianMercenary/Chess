
package game.pieces;

import game.Board;
import game.pieces.Piece;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Queen extends Piece {
	public static BufferedImage white;
	public static BufferedImage black;

	public Queen(int row, int col, int color, Board b) {
		super(row, col, color, b);
	}

	@Override
	public boolean legal(int row, int col) {
		int rdiff = Math.abs(row - this.row);
		int cdiff = Math.abs(col - this.col);
		if (row < 0 || row > 7 || col < 0 || col > 7 || rdiff == 0 && cdiff == 0) {
			return false;
		}
		// moving vertically
		if (cdiff == 0) {
			if (rdiff == 0) {
				return false;
			}
			if (this.row < row) {
				for (int i = 1; i < rdiff; i++) {
					if (this.b.color(this.row + i, col) == EMPTY)
						continue;
					return false;
				}
			} else {
				for (int i = 1; i < rdiff; i++) {
					if (this.b.color(this.row - i, col) == EMPTY)
						continue;
					return false;
				}
			}
			return this.b.color(row, col) != this.color && !this.b.pinned(row, col, this);
		}
		// moving horizontally
		if (rdiff == 0) {
			if (cdiff == 0) {
				return false;
			}
			if (this.col < col) {
				for (int i = 1; i < cdiff; i++) {
					if (this.b.color(row, this.col + i) == EMPTY)
						continue;
					return false;
				}
			} else {
				for (int i = 1; i < cdiff; i++) {
					if (this.b.color(row, this.col - i) == EMPTY)
						continue;
					return false;
				}
			}
			return this.b.color(row, col) != this.color && !this.b.pinned(row, col, this);
		}
		// moving diagonally
		if (rdiff == cdiff && rdiff != 0) {
			int dx = (col - this.col) / cdiff;
			int dy = (row - this.row) / rdiff;
			for (int i = 1; i < rdiff; ++i) {
				if (this.b.color(this.row + dy * i, this.col + dx * i) == EMPTY)
					continue;
				return false;
			}
			return this.b.color(row, col) != this.color && !this.b.pinned(row, col, this);
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
			white = ImageIO.read(Queen.class.getResource("imgs/wQ.png"));
			black = ImageIO.read(Queen.class.getResource("imgs/bQ.png"));
		} catch (IOException i) {
			i.printStackTrace();
			System.exit(404);
		}
	}
}
