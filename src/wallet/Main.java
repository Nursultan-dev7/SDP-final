package wallet;

import java.awt.Dimension;
import java.time.Clock;
import java.util.Arrays;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import wallet.adapter.LocalChainAdapter;
import wallet.adapter.LocalChainNode;
import wallet.decorator.DemoAuthenticator;
import wallet.facade.WalletFacade;
import wallet.facade.WalletPanel;
import wallet.facade.WalletRegressionTest;
import wallet.facade.WalletSmokeTest;

/** The only class outside the six pattern packages: wires the app and runs all checks. */
public final class Main {
    private Main() {}

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "--test".equals(args[0]) && args.length <= 2) {
            System.setProperty("java.awt.headless", "true");
            WalletSmokeTest.main(Arrays.copyOfRange(args, 1, args.length));
            WalletRegressionTest.main(new String[0]);
            System.out.println("PASS: Main verified Builder, Strategy, Decorator, Adapter, Observer and Facade.");
            return;
        }
        if (args.length != 0) {
            throw new IllegalArgumentException("Usage: java -jar build/BlockchainWallet.jar [--test [screenshots]]");
        }
        SwingUtilities.invokeLater(() -> {
            WalletFacade wallet = new WalletFacade(
                    new LocalChainAdapter(new LocalChainNode(WalletFacade.SENDER, WalletFacade.RECIPIENT)),
                    new DemoAuthenticator(Clock.systemUTC()));
            JFrame window = new JFrame("Blockchain Wallet | Design Patterns Demo");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setContentPane(new WalletPanel(wallet));
            window.pack();
            window.setMinimumSize(new Dimension(900, 720));
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }
}
