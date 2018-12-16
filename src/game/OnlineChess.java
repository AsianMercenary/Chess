package game;

import game.Board;
import game.pieces.Bishop;
import game.pieces.Knight;
import game.pieces.Piece;
import game.pieces.Queen;
import game.pieces.Rook;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.text.MaskFormatter;

public class OnlineChess extends JPanel {
	public OnlineChess() {
		GridBagLayout l = new GridBagLayout();
		this.setLayout(l);
		final OnlineChessBoard b = new OnlineChessBoard();
		JFormattedTextField ip = null;
		final JButton play = new JButton("Connect");
		try {
			ip = new JFormattedTextField(new MaskFormatter("###.###.###.###:#####"));
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(1);
		}
		ip.setPreferredSize(new Dimension(150, 25));
		final JFormattedTextField t = ip;
		play.addActionListener(new ActionListener() {
			boolean asPlay = true;
			volatile Socket sock;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (this.asPlay) {
					String[] s = t.getText().split(":");
					int port = Integer.parseInt(s[1]);
					try {
						this.sock = new Socket(s[0], port);
						this.sock.setKeepAlive(true);
						PrintWriter out = new PrintWriter(this.sock.getOutputStream(), true);
						BufferedReader in = new BufferedReader(new InputStreamReader(this.sock.getInputStream()));
						b.resetBoard();
						int r = (int) (2.0 * Math.random()); // decide who plays first
						out.println(r);
						if (r == 1) {
							b.playAs(Piece.WHITE);
						} else {
							b.playAs(Piece.BLACK);
						}
						b.start(this.sock, in, out);
						play.setText("Disconnect");
						this.asPlay = false;
					} catch (IOException io) {
						JOptionPane.showMessageDialog(null, "Connection Timed Out", "Connection Error", 2);
					}
				} else {
					try {
						this.sock.close();
						this.asPlay = true;
					} catch (IOException s) {
						// empty catch block
					}
					play.setText("Connect");
				}
			}
		});
		JButton save = new JButton("Export");
		save.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				b.export();
			}
		});
		JFormattedTextField p = null;
		try {
			p = new JFormattedTextField(new MaskFormatter("#####"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		final JFormattedTextField t1 = p;
		final JButton host = new JButton("Host");
		p.setPreferredSize(new Dimension(75, 25));
		host.addActionListener(new ActionListener() {
			boolean hosting = false;
			volatile ServerSocket serverSocket;
			volatile Socket clientSocket;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!this.hosting) {
					int port = Integer.parseInt(t1.getText());
					host.setText("Disconnect");
					this.hosting = true;
					Thread t = new Thread(new Runnable() {

						@Override
						public void run() {
							try {
								serverSocket = new ServerSocket(port);
								clientSocket = serverSocket.accept();
								clientSocket.setKeepAlive(true);
								PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
								BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
								b.resetBoard();
								if (Integer.parseInt(in.readLine()) == 0) {
									b.playAs(Piece.WHITE);
								} else {
									b.playAs(Piece.BLACK);
								}
								b.start(clientSocket, in, out);
							} catch (IOException out) {
								// empty catch block
							}
						}
						
					});
					t.start();
				} else {
					this.hosting = false;
					try {
						this.serverSocket.close();
					} catch (IOException port) {
						// empty catch block
					}
					host.setText("Host");
				}
			}
		});
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.fill = 2;
		this.add((Component) ip, c);
		c.gridx = 2;
		c.gridwidth = 1;
		this.add((Component) play, c);
		c.gridx = 3;
		this.add((Component) save, c);
		c.gridx = 4;
		c.gridwidth = 2;
		this.add((Component) p, c);
		c.gridx = 6;
		this.add((Component) host, c);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 8;
		c.gridheight = 8;
		this.add((Component) b, c);
	}

	class OnlineChessBoard extends Board {
		Socket s;
		BufferedReader in;
		PrintWriter out;

		public OnlineChessBoard() {
			super(0, 0, 60);
		}

		public void start(Socket s, BufferedReader in, PrintWriter out) {
			this.in = in;
			this.out = out;
			this.s = s;
			if (!this.asWhite) {
				this.listen();
			}
		}

		public void listen() {
			if (this.s.isClosed()) {
				JOptionPane.showMessageDialog(null, "Connection Lost", "Connection Error", 2);
				return;
			}
			OnlineChessBoard b = this;
			Thread t = new Thread(() -> {
				try {
					String m = this.in.readLine();
					char special = m.charAt(m.length() - 1);
					Integer i = Integer.parseInt(m.substring(0, m.length() - 1));
					// decode the move
					int cd = i & 7;
					i = i >> 3;
					int rd = i & 7;
					i = i >> 3;
					int c = i & 7;
					i = i >> 3;
					int r = i & 7;
					i = i >> 3;
					boolean replace = false;
					if (special != Board.MOVE && special != Board.LEFT_CASTLE && special != Board.RIGHT_CASTLE) {
						this.togglePrompt();
						replace = true;
					}
					Piece p = this.pieces()[r][c];
					p.move(rd, cd);
					if (replace) {
						Object n = null;
						switch (special) {
							case Board.KNIGHT_PROMOTION:
								p = new Knight(rd, cd, this.getPiece((int) rd, (int) cd).color, b);
								break;
							case Board.BISHOP_PROMOTION:
								p = new Bishop(rd, cd, this.getPiece((int) rd, (int) cd).color, b);
								break;
							case Board.ROOK_PROMOTION: 
								p = new Rook(rd, cd, this.getPiece((int) rd, (int) cd).color, b);
								break;
							case Board.QUEEN_PROMOTION: 
								p = new Queen(rd, cd, this.getPiece((int) rd, (int) cd).color, b);
								break;
						}
						this.setPiece(rd, cd, p);
						this.setLastSpecial(special);
						this.running = !this.gameOver(this.numberOfMoves() - 1);
					}
					this.whiteToMove = !this.whiteToMove;
				} catch (IOException io) {
					JOptionPane.showMessageDialog(null, "Connection Lost", "Connection Error", 2);
				}
			});
			t.start();
		}

		@Override
		public void endMove() {
			int[] m = this.getLastMove();
			// encode the move
			int msg = m[0];
			msg <<= 3;
			msg |= m[1];
			msg <<= 3;
			msg |= m[2];
			msg <<= 3;
			this.out.println("" + (msg |= m[3]) + "" + this.getLastSpecial());
			this.whiteToMove = !this.whiteToMove;
			this.listen();
		}
	}

}
