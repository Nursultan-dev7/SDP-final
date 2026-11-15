package wallet;

import java.awt.Dimension;
import java.time.Clock;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public final class WalletApp {
    private WalletApp() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Blockchain Wallet | Design Patterns Demo");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setContentPane(new WalletPanel(new WalletFacade(Clock.systemUTC())));
            window.pack();
            window.setMinimumSize(new Dimension(900, 720));
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }
}
