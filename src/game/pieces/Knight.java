
package game.pieces;

import game.Board;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Knight extends Piece {
	public static BufferedImage white;
	public static BufferedImage black;

	public Knight(int row, int col, int color, Board b) {
		super(row, col, color, b);
	}

	@Override
	public boolean legal(int row, int col) {
		int rdiff = Math.abs(row - this.row);
		int cdiff = Math.abs(col - this.col);
		if (row < 0 || row > 7 || col < 0 || col > 7 || rdiff == 0 && cdiff == 0) {
			return false;
		}
		if (this.b.color(row, col) != this.color && (rdiff == 2 && cdiff == 1 || rdiff == 1 && cdiff == 2)) {
			return !this.b.pinned(row, col, this);
		}
		return false;
	}

	@Override
	public boolean canMove() {
		return this.legal(this.row + 2, this.col + 1) || this.legal(this.row + 2, this.col - 1)
				|| this.legal(this.row - 2, this.col + 1) || this.legal(this.row - 2, this.col - 1)
				|| this.legal(this.row + 1, this.col + 2) || this.legal(this.row - 1, this.col + 2)
				|| this.legal(this.row + 1, this.col - 2) || this.legal(this.row - 1, this.col - 2);
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
			white = ImageIO.read(Knight.class.getResource("imgs/wH.png"));
			black = ImageIO.read(Knight.class.getResource("imgs/bH.png"));
		} catch (IOException i) {
			i.printStackTrace();
			System.exit(404);
		}
	}
}
