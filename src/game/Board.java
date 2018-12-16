
package game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.Timer;

import game.pieces.Bishop;
import game.pieces.King;
import game.pieces.Knight;
import game.pieces.Pawn;
import game.pieces.Piece;
import game.pieces.Queen;
import game.pieces.Rook;

public abstract class Board extends JPanel implements MouseListener, MouseMotionListener {
	private static final int MAXIMUM_AMOUNT_OF_MOVES = 1000;

	// where in the JPanel the board is drawn
	public int x;
	public int y;
	public int size; // length of each square
	protected boolean running;

	// represents board; null = empty square
	private Piece[][] pieces;

	protected boolean asWhite; // if true, the board will be oriented as if one is playing as white
	protected boolean whiteToMove;

	// for recording and exporting games
	private int[][] moves; // array of vectors with a size of 4,
							// where moves[i] = (row where the ith moved piece was,
							// col where the ith moved piece was
							// row where the ith moved piece was moved to,
							// col where the ith moved piece was moved to)
	private boolean[] checked; // if checked[i] == true, the ith move led to a check
	private boolean[] capture; // if capture[i] == true, the ith move led to a capture
	private char[] special; // if special[i] == true, the ith move was a special move
	private Piece[] pieceMoved; // records what piece was moved
	private int numberOfMoves; // records the number of moves
	private boolean prompt;
	private char end; // records how the game ended
	public King white;
	public King black;

	// constants for recording games
	public static final char MOVE = 'd';
	public static final char KNIGHT_PROMOTION = 'h';
	public static final char BISHOP_PROMOTION = 'b';
	public static final char ROOK_PROMOTION = 'r';
	public static final char QUEEN_PROMOTION = 'q';
	public static final char LEFT_CASTLE = 'L';
	public static final char RIGHT_CASTLE = 'R';
	public static final char WHITE_MATE = 'M';
	public static final char BLACK_MATE = 'm';
	public static final char DRAW = 'D';
	private static final Color WHITE_SQUARE = new Color(222, 184, 135);
	private static final Color BROWN_SQUARE = new Color(139, 69, 19);
	private static final Color HIGHLIGHTED_SQUARE = new Color(102, 102, 255);

	// for promotion menu
	private static final Color SELECT_PANE = new Color(0.8f, 0.8f, 0.8f, 0.5f);
	private static final Color THROUGH_PANE = new Color(0.9f, 0.9f, 0.9f, 0.7f);
	// for end screen
	private static final Font BIGFONT = new Font("Times New Roman", 1, 36);

	private Piece selectedPiece = null; // piece selected
	private int mx; // x coord for mouse click
	private int my; // y coord for mouse click

	public Board(int x, int y, int size) {
		this.x = x;
		this.y = y;
		this.size = size;
		this.end = ' ';
		this.running = false;
		this.prompt = false;
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.setPreferredSize(new Dimension(this.size * 8, this.size * 8));
		this.repaint();
	}

	// sets orientation of the board
	public void playAs(int color) {
		this.asWhite = color == Piece.WHITE;
	}

	// returns grid of pieces
	public Piece[][] pieces() {
		return this.pieces;
	}

	// returns the piece at that location
	public Piece getPiece(int row, int col) {
		return this.pieces[row][col];
	}

	// sets the piece at that location
	public void setPiece(int row, int col, Piece p) {
		this.pieces[row][col] = p;
	}

	// adds a piece to the board
	public void addPiece(Piece p) {
		this.pieces[p.row][p.col] = p;
	}

	// returns the last move
	public int[] getLastMove() {
		return this.moves[this.numberOfMoves - 1];
	}

	// returns the last piece moved
	public Piece getLastPiece() {
		return this.pieceMoved[this.numberOfMoves - 1];
	}

	// returns the number of moves
	public int numberOfMoves() {
		return this.numberOfMoves;
	}

	// returns a character characterizing the move last played
	public char getLastSpecial() {
		return this.special[this.numberOfMoves - 1];
	}

	// characterizes a move as a special move denoted by a constant
	public void setLastSpecial(char c) {
		this.special[this.numberOfMoves - 1] = c;
	}

	// returns how the game ended
	public char getEnd() {
		return this.end;
	}

	// returns the color of a piece at a specified location
	public int color(int row, int col) {
		return this.pieces[row][col] == null ? Piece.EMPTY : this.pieces[row][col].color;
	}

	// marks a piece as captured
	public void markAsCapture() {
		this.capture[this.numberOfMoves] = true;
	}

	// toggles the promotion prompt
	public void togglePrompt() {
		this.prompt = !this.prompt;
	}

	// when the move ends, what should happen?
	public abstract void endMove();

	// is a piece pinned
	public boolean pinned(int row, int col, Piece p) {
		this.pieces[p.row][p.col] = null;
		Piece temp = this.pieces[row][col];
		this.pieces[row][col] = p;
		if (p.color == Piece.WHITE) {
			if (this.white.checked()) {
				this.pieces[p.row][p.col] = p;
				this.pieces[row][col] = temp;
				return true;
			}
		} else if (p.color == Piece.BLACK && this.black.checked()) {
			this.pieces[p.row][p.col] = p;
			this.pieces[row][col] = temp;
			return true;
		}
		this.pieces[p.row][p.col] = p;
		this.pieces[row][col] = temp;
		return false;
	}

	// plays a special move
	public void specialMove(int row, int col, int destr, int destc, char c) {
		this.move(row, col, destr, destc);
		this.setLastSpecial(c);
	}

	// determines whether there are any legal moves
	public boolean moreMoves(int color) {
		for (Piece[] pieces : this.pieces) {
			for (Piece p : pieces) {
				if (p != null && p.color == color && p.canMove()) {
					return true;
				}
			}
		}
		return false;
	}

	// moves a piece from one destination to another
	public void move(int row, int col, int destr, int destc) {
		this.pieces[row][col].row = destr;
		this.pieces[row][col].col = destc;
		this.pieceMoved[this.numberOfMoves] = this.pieces[row][col];
		if (this.pieces[destr][destc] != null) {
			this.capture[this.numberOfMoves] = true;
		}
		this.pieces[destr][destc] = this.pieces[row][col];
		this.pieces[row][col] = null;
		this.moves[this.numberOfMoves] = new int[] { row, col, destr, destc };
		this.running = !this.gameOver(this.numberOfMoves);
		this.numberOfMoves++;
	}

	// determines whether a game is over
	public boolean gameOver(int move) {
		if (!this.whiteToMove) {
			if (this.white.checked()) {
				this.checked[move] = true;
			}
			if (!this.moreMoves(Piece.WHITE)) {
				this.end = this.checked[move] ? BLACK_MATE : DRAW;
				return true;
			}
		} else {
			if (this.black.checked()) {
				this.checked[move] = true;
			}
			if (!this.moreMoves(Piece.BLACK)) {
				this.end = this.checked[move] ? WHITE_MATE : DRAW;
				return true;
			}
		}
		return false;
	}

	// resets the board
	public void resetBoard() {
		this.whiteToMove = true;
		this.pieces = new Piece[8][8];

		// fill in white pieces
		this.pieces[0][0] = new Rook(0, 0, Piece.WHITE, this);
		this.pieces[0][7] = new Rook(0, 7, Piece.WHITE, this);
		this.pieces[0][1] = new Knight(0, 1, Piece.WHITE, this);
		this.pieces[0][6] = new Knight(0, 6, Piece.WHITE, this);
		this.pieces[0][2] = new Bishop(0, 2, Piece.WHITE, this);
		this.pieces[0][5] = new Bishop(0, 5, Piece.WHITE, this);
		this.pieces[0][3] = new Queen(0, 3, Piece.WHITE, this);
		this.white = new King(0, 4, Piece.WHITE, this);
		this.pieces[0][4] = this.white;
		for (int i = 0; i < 8; ++i) {
			this.pieces[1][i] = new Pawn(1, i, Piece.WHITE, this);
		}

		// fill in black pieces
		this.pieces[7][0] = new Rook(7, 0, Piece.BLACK, this);
		this.pieces[7][7] = new Rook(7, 7, Piece.BLACK, this);
		this.pieces[7][1] = new Knight(7, 1, Piece.BLACK, this);
		this.pieces[7][6] = new Knight(7, 6, Piece.BLACK, this);
		this.pieces[7][2] = new Bishop(7, 2, Piece.BLACK, this);
		this.pieces[7][5] = new Bishop(7, 5, Piece.BLACK, this);
		this.pieces[7][3] = new Queen(7, 3, Piece.BLACK, this);
		this.black = new King(7, 4, Piece.BLACK, this);
		this.pieces[7][4] = this.black;
		for (int i = 0; i < 8; ++i) {
			this.pieces[6][i] = new Pawn(6, i, Piece.BLACK, this);
		}

		this.whiteToMove = true;
		this.end = ' ';
		this.moves = new int[MAXIMUM_AMOUNT_OF_MOVES][4];
		this.checked = new boolean[MAXIMUM_AMOUNT_OF_MOVES];
		this.capture = new boolean[MAXIMUM_AMOUNT_OF_MOVES];
		this.special = new char[MAXIMUM_AMOUNT_OF_MOVES];
		Arrays.fill(this.special, 'd');
		this.pieceMoved = new Piece[MAXIMUM_AMOUNT_OF_MOVES];
		this.numberOfMoves = 0;
		this.prompt = false;
		this.running = true;

		Timer timer = new Timer(25, (e) -> this.repaint());
		timer.start(); // repaints board every 25 ms
	}

	// exports the game
	public void export() {
		Board b = this;
		JFileChooser fileChooser = new JFileChooser("Save Game");
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		int s = fileChooser.showOpenDialog(b);
		if (s == 0) {
			try {
				try (PrintWriter w = new PrintWriter(fileChooser.getSelectedFile());) {
					for (int i = 0; i < this.numberOfMoves; i++) {
						if (i % 2 == 0) {
							w.print("" + (i / 2 + 1) + ".");
						}
						w.print("\t");
						switch (this.special[i]) {
						case 'L':
							w.print("O-O-O");
							break;
						case 'R':
							w.print("O-O");
							break;
						default:
							if (this.pieceMoved[i] instanceof King) {
								w.print("K");
							} else if (this.pieceMoved[i] instanceof Rook) {
								w.print("R");
							} else if (this.pieceMoved[i] instanceof Knight) {
								w.print("N");
							} else if (this.pieceMoved[i] instanceof Queen) {
								w.print("Q");
							} else if (this.pieceMoved[i] instanceof Bishop) {
								w.print("B");
							}
							w.print("" + (char) ('a' + this.moves[i][1]) + "" + (this.moves[i][0] + 1));
							if (this.capture[i]) {
								w.print("x");
							} else {
								w.print("-");
							}
							w.print("" + (char) ('a' + this.moves[i][3]) + "" + (this.moves[i][2] + 1));
							switch (this.special[i]) {
							case KNIGHT_PROMOTION:
								w.print("K");
								break;
							case BISHOP_PROMOTION:
								w.print("B");
								break;
							case ROOK_PROMOTION:
								w.print("R");
								break;
							case QUEEN_PROMOTION:
								w.print("Q");
								break;
							}
							break;
						}
						if (this.checked[i]) {
							if (i == this.numberOfMoves - 1 && this.end != DRAW && this.end != ' ') {
								w.print("#");
							} else {
								w.print("+");
							}
						}
						if (i % 2 != 1) {
							continue;
						}
						w.println();
					}
				}
			} catch (IOException w) {
				// empty catch block
			}
		}
	}

	// draws the board
	private void drawBoard(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		for (int i = 0; i < 64; i++) { // 64 is the number of squares on a traditional chess board
			int x = i % 8;
			int y = this.asWhite ? i / 8 : (63 - i) / 8;
			if ((i - y) % 2 == 0) {
				g.setColor(WHITE_SQUARE);
			} else {
				g.setColor(BROWN_SQUARE);
			}
			int sx = this.x + x * this.size;
			int sy = this.y + y * this.size;
			g.fillRect(sx, sy, this.size, this.size);
		}
	}

	// draws the pieces
	private void drawPieces(Graphics g) {
		for (Piece[] pieces : this.pieces) {
			for (Piece p : pieces) {
				if (p != null) {
					int py;
					int px;
					BufferedImage piece = p.color == Piece.WHITE ? p.drawWhite() : p.drawBlack();
					if (!this.asWhite) {
						px = this.x + (7 - p.col) * this.size;
						py = this.y + p.row * this.size;
					} else {
						px = this.x + p.col * this.size;
						py = this.y + (7 - p.row) * this.size;
					}
					g.drawImage(piece, px + 5, py + 5, null);
				}
			}
		}
		if (this.selectedPiece != null) {
			if (this.selectedPiece.color == Piece.WHITE) {
				g.drawImage(this.selectedPiece.drawWhite(), this.mx - 25, this.my - 25, null);
			} else {
				g.drawImage(this.selectedPiece.drawBlack(), this.mx - 25, this.my - 25, null);
			}
		}
	}

	// draws the overlay for promoting a pawn
	private void drawPromotionPrompt(Graphics g) {
		g.setColor(THROUGH_PANE);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		if (this.asWhite) {
			g.drawImage(Knight.white, this.size - this.size / 2, this.size * 4 - 45, null);
			g.drawImage(Bishop.white, this.size * 3 - this.size / 2, this.size * 4 - 45, null);
			g.drawImage(Rook.white, this.size * 5 - this.size / 2, this.size * 4 - 45, null);
			g.drawImage(Queen.white, this.size * 7 - this.size / 2, this.size * 4 - 45, null);
		} else {
			g.drawImage(Knight.black, this.size - this.size / 2, this.size * 4 - 45, null);
			g.drawImage(Bishop.black, this.size * 3 - this.size / 2, this.size * 4 - 45, null);
			g.drawImage(Rook.black, this.size * 5 - this.size / 2, this.size * 4 - 45, null);
			g.drawImage(Queen.black, this.size * 7 - this.size / 2, this.size * 4 - 45, null);
		}
		g.setColor(SELECT_PANE);
		g.fillRect(this.mx / (this.size * 2) * this.size * 2, 0, this.size * 2, this.size * 8);
	}

	// draws the overlay for the end screen
	private void drawEndScreen(Graphics g) {
		g.setColor(THROUGH_PANE);
		g.setFont(BIGFONT);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		FontMetrics metrics = g.getFontMetrics(BIGFONT);
		String endmsg = "";
		switch (this.end) {
		case WHITE_MATE:
			endmsg = "White to mate";
			break;
		case BLACK_MATE:
			endmsg = "Black to mate";
			break;
		case DRAW:
			endmsg = "Draw";
			break;
		}
		g.setColor(Color.BLACK);
		g.drawString(endmsg, this.size * 4 - metrics.stringWidth(endmsg) / 2, this.size * 4);
	}

	// paints the board
	@Override
	public void paint(Graphics g) {
		this.drawBoard(g);
		if (numberOfMoves > 0) {
			int[] move = this.getLastMove();
			g.setColor(HIGHLIGHTED_SQUARE);
			if (this.asWhite) {
				g.fillRect(move[1] * size, (7 - move[0]) * size, size, size);
				g.fillRect(move[3] * size, (7 - move[2]) * size, size, size);
			} else {
				g.fillRect((7 - move[1]) * size, move[0] * size, size, size);
				g.fillRect((7 - move[3]) * size, move[2] * size, size, size);
			}
		}
		if (this.running) {
			this.drawPieces(g);
			if (this.prompt) {
				this.drawPromotionPrompt(g);
			}
		} else if (this.end != ' ') {
			this.drawPieces(g);
			this.drawEndScreen(g);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (!this.prompt) {
			this.mx = e.getX();
			this.my = e.getY();
			if (this.selectedPiece == null) {
				int sx = (e.getX() - this.x) / this.size;
				int sy = (e.getY() - this.y) / this.size;
				if (this.asWhite) {
					sy = 7 - sy;
				} else {
					sx = 7 - sx;
				}
				if (sx >= 0 && sx <= 7 && sy >= 0 && sy <= 7 && this.pieces != null) {
					this.selectedPiece = this.pieces[sy][sx];
					this.pieces[sy][sx] = null;
				}
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (this.prompt) {
			Piece p;
			int selection = e.getX() / (2 * this.size);
			Piece last = this.getLastPiece();
			switch (selection) {
			case 0: {
				p = new Knight(last.row, last.col, last.color, this);
				this.setLastSpecial(KNIGHT_PROMOTION);
				break;
			}
			case 1: {
				p = new Bishop(last.row, last.col, last.color, this);
				this.setLastSpecial(BISHOP_PROMOTION);
				break;
			}
			case 2: {
				p = new Rook(last.row, last.col, last.color, this);
				this.setLastSpecial(ROOK_PROMOTION);
				break;
			}
			case 3: {
				p = new Queen(last.row, last.col, last.color, this);
				this.setLastSpecial(QUEEN_PROMOTION);
				break;
			}
			default: {
				return;
			}
			}
			this.addPiece(p);
			this.running = !this.gameOver(this.numberOfMoves - 1);
			this.togglePrompt();
			this.endMove();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (this.selectedPiece != null) {
			this.pieces[this.selectedPiece.row][this.selectedPiece.col] = this.selectedPiece;
			if (!this.prompt && this.whiteToMove == this.asWhite) {
				int sx = (e.getX() - this.x) / this.size;
				int sy = (e.getY() - this.y) / this.size;
				if (this.asWhite) {
					sy = 7 - sy;
				} else {
					sx = 7 - sx;
				}
				if (sx >= 0 && sx <= 7 && sy >= 0 && sy <= 7
						&& this.selectedPiece.color == Piece.WHITE == this.whiteToMove
						&& this.selectedPiece.move(sy, sx) && !this.prompt) {
					this.endMove();
				}
			}
			this.selectedPiece = null;
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (this.prompt) {
			this.mx = e.getX();
			this.my = e.getY();
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (!this.prompt) {
			this.mx = e.getX();
			this.my = e.getY();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}
