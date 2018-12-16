
import game.OfflineChess;
import game.OnlineChess;
import java.awt.Component;
import java.awt.Container;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class Main
extends JFrame {
    public Main() {
        super("Chess");
    }

    public static void main(String[] args) {
        Main f = new Main();
        JTabbedPane p = new JTabbedPane();
        OfflineChess c = new OfflineChess();
        p.addTab("Offline Chess", null, c, "Play on one computer by yourself");
        OnlineChess o = new OnlineChess();
        p.addTab("Online Chess", null, o, "Play with another via the internet");
        f.getContentPane().add(p);
        f.setDefaultCloseOperation(3);
        f.setVisible(true);
        f.setResizable(false);
        f.pack();
    }
}

