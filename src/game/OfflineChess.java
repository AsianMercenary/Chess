
package game;

import game.Board;
import game.pieces.Piece;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;

public class OfflineChess extends JPanel {
	public OfflineChess() {
		GridBagLayout l = new GridBagLayout();
		this.setLayout(l);
		final OfflineChessBoard b = new OfflineChessBoard();
		JButton play = new JButton("Play");
		play.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				b.resetBoard();
				b.playAs(Piece.WHITE);
			}
		});
		JButton save = new JButton("Export");
		save.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				b.export();
			}
		});
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = 2;
		this.add((Component) play, c);
		c.gridx = 1;
		this.add((Component) save, c);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 8;
		c.gridheight = 8;
		this.add((Component) b, c);
	}

	class OfflineChessBoard extends Board {
		public OfflineChessBoard() {
			super(0, 0, 60);
		}

		@Override
		public void endMove() {
			this.asWhite = !this.asWhite;
			this.whiteToMove = !this.whiteToMove;
		}
	}

}
