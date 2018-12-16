
package game.pieces;

import game.Board;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Bishop extends Piece {
	public static BufferedImage white;
	public static BufferedImage black;

	public Bishop(int row, int col, int color, Board b) {
		super(row, col, color, b);
	}

	@Override
	public boolean legal(int row, int col) {
		int rdiff = Math.abs(row - this.row);
		int cdiff = Math.abs(col - this.col);
		if (row < 0 || row > 7 || col < 0 || col > 7 || rdiff == 0 && cdiff == 0) {
			return false;
		}
		if (rdiff == cdiff && rdiff != 0) {
			int dx = (col - this.col) / cdiff; // x component of a unit vector in the direction the bishop moved
			int dy = (row - this.row) / rdiff; // y component of a unit vector in the direction the bishop moved
			for (int i = 1; i < rdiff; i++) {
				if (this.b.color(this.row + dy * i, this.col + dx * i) != EMPTY) {
					return false;
				}
			}
			return this.b.color(row, col) != this.color && !this.b.pinned(row, col, this);
		}
		return false;
	}

	@Override
	public boolean canMove() {
		return this.legal(this.row + 1, this.col + 1) || this.legal(this.row + 1, this.col - 1)
				|| this.legal(this.row - 1, this.col + 1) || this.legal(this.row - 1, this.col - 1);
	}

	@Override
	public BufferedImage drawBlack() {
		return black;
	}

	@Override
	public BufferedImage drawWhite() {
		return white;
	}

	// load in the images
	static {
		try {
			white = ImageIO.read(Bishop.class.getResource("imgs/wB.png"));
			black = ImageIO.read(Bishop.class.getResource("imgs/bB.png"));
		} catch (IOException i) {
			i.printStackTrace();
			System.exit(404);
		}
	}
}
