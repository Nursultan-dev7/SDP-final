package wallet.facade;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import wallet.adapter.LocalChainAdapter;
import wallet.adapter.LocalChainNode;
import wallet.adapter.WalletNetwork;
import wallet.builder.Transaction;
import wallet.decorator.DemoAuthenticator;
import wallet.observer.TransactionHistory;
import wallet.strategy.FeeStrategy;
import wallet.strategy.PriorityFeeStrategy;
import wallet.strategy.StandardFeeStrategy;

public final class WalletSmokeTest {
    private WalletSmokeTest() {}

    public static void main(String[] args) throws Exception {
        transferLifecycle();
        Path screenshots = args.length == 0 ? null : Files.createDirectories(Path.of(args[0]));
        SwingUtilities.invokeAndWait(() -> exerciseInterface(screenshots));
        System.out.println("PASS: wallet lifecycle, validation, authentication, linked blocks and Swing interface.");
    }

    private static void transferLifecycle() {
        LocalChainNode node = new LocalChainNode(WalletFacade.SENDER, WalletFacade.RECIPIENT);
        WalletFacade wallet = new WalletFacade(new LocalChainAdapter(node), new DemoAuthenticator(Clock.systemUTC()));
        FeeStrategy standard = new StandardFeeStrategy();
        FeeStrategy priority = new PriorityFeeStrategy();
        expect("100.00", wallet.balances().available().toPlainString());
        reject("positive amount", () -> wallet.prepare(WalletFacade.RECIPIENT, "0", standard));
        reject("Enter an amount", () -> wallet.prepare(WalletFacade.RECIPIENT, "1.001", standard));
        reject("different recipient", () -> wallet.prepare(WalletFacade.SENDER, "10", standard));
        reject("recipient address", () -> wallet.prepare("unknown", "10", standard));
        reject("Insufficient", () -> wallet.prepare(WalletFacade.RECIPIENT, "100", standard));
        reject("Submit a transfer", wallet::createBlock);

        WalletFacade.PreparedTransfer canceled = wallet.prepare(WalletFacade.RECIPIENT, "10", standard);
        wallet.cancel(canceled);
        reject("new code", () -> submit(wallet, canceled));

        WalletFacade.PreparedTransfer locked = wallet.prepare(WalletFacade.RECIPIENT, "10", standard);
        for (int attempt = 0; attempt < 2; attempt++) {
            reject("Incorrect PIN", () -> wallet.submit(locked, "0000", locked.demoCode()));
        }
        reject("Too many attempts", () -> wallet.submit(locked, "0000", locked.demoCode()));
        reject("new code", () -> submit(wallet, locked));
        expect(0, wallet.history().size());
        expect("100.00", wallet.balances().available().toPlainString());

        WalletFacade.PreparedTransfer first = wallet.prepare(WalletFacade.RECIPIENT, "10", standard);
        expect("1.00", first.transaction().fee().toPlainString());
        reject("Incorrect PIN or code", () -> wallet.submit(first, DemoAuthenticator.DEMO_PIN, "wrong"));
        expect(WalletNetwork.Status.PENDING, submit(wallet, first).status());
        reject("new code", () -> submit(wallet, first));
        expect("89.00", wallet.balances().available().toPlainString());
        expect("100.00", wallet.balances().confirmed().toPlainString());
        expect("0.00", wallet.balances().recipient().toPlainString());
        reject("Insufficient", () -> wallet.prepare(WalletFacade.RECIPIENT, "89", standard));

        WalletFacade.PreparedTransfer second = wallet.prepare(WalletFacade.RECIPIENT, "30,25", priority);
        expect("3.03", second.transaction().fee().toPlainString());
        submit(wallet, second);
        expect(2, wallet.history().size());
        WalletNetwork.BlockSummary block = wallet.createBlock();
        expect(1, block.height());
        expect(2, block.transactionCount());
        expect(64, block.hash().length());
        for (TransactionHistory.Entry entry : wallet.history()) {
            expect(WalletNetwork.Status.CONFIRMED, entry.receipt().status());
            expect(block.hash(), entry.receipt().blockHash());
        }
        expect("55.72", wallet.balances().available().toPlainString());
        expect("55.72", wallet.balances().confirmed().toPlainString());
        expect("40.25", wallet.balances().recipient().toPlainString());

        WalletFacade.PreparedTransfer third = wallet.prepare(WalletFacade.RECIPIENT, "1", priority);
        expect("2.00", third.transaction().fee().toPlainString());
        submit(wallet, third);
        WalletNetwork.BlockSummary nextBlock = wallet.createBlock();
        expect(2, nextBlock.height());
        expect(block.hash(), nextBlock.previousHash());
    }

    private static WalletNetwork.Receipt submit(WalletFacade wallet, WalletFacade.PreparedTransfer transfer) {
        return wallet.submit(transfer, DemoAuthenticator.DEMO_PIN, transfer.demoCode());
    }

    private static void exerciseInterface(Path screenshots) {
        WalletPanel panel = new WalletPanel(new WalletFacade(
                new LocalChainAdapter(new LocalChainNode(WalletFacade.SENDER, WalletFacade.RECIPIENT)),
                new DemoAuthenticator(Clock.systemUTC())));
        panel.setSize(panel.getPreferredSize());
        JTable history = (JTable) component(panel, "history");
        JButton mine = (JButton) component(panel, "create-block");
        expect(false, mine.isEnabled());
        ((JTextField) component(panel, "amount")).setText("30.00");
        ((JComboBox<?>) component(panel, "fee")).setSelectedIndex(1);
        click(panel, "review");
        expect("To demo-bob  |  Amount 30.00 DCO  |  Fee 3.00 DCO  |  Total 33.00 DCO",
                ((JLabel) component(panel, "quote")).getText());
        capture(panel, screenshots, "01-review.png");
        String challenge = ((JLabel) component(panel, "demo-code")).getText().split(" ")[3];
        ((JTextField) component(panel, "pin")).setText(DemoAuthenticator.DEMO_PIN);
        ((JTextField) component(panel, "code")).setText("wrong");
        click(panel, "send");
        expect(0, history.getRowCount());
        expect("Incorrect PIN or code. Try again.", ((JLabel) component(panel, "notice")).getText());
        ((JTextField) component(panel, "code")).setText(challenge);
        click(panel, "send");
        expect(1, history.getRowCount());
        expect("Pending", history.getValueAt(0, 4));
        expect("67.00 DCO", ((JLabel) component(panel, "available")).getText());
        capture(panel, screenshots, "02-pending.png");
        mine.doClick(0);
        expect("Confirmed", history.getValueAt(0, 4));
        expect("#1", history.getValueAt(0, 5));
        expect("30.00 DCO", ((JLabel) component(panel, "recipient-balance")).getText());
        expect(false, mine.isEnabled());
        capture(panel, screenshots, "03-confirmed.png");
    }

    private static void click(Container parent, String name) {
        ((JButton) component(parent, name)).doClick(0);
    }

    private static Component component(Container parent, String name) {
        for (Component child : parent.getComponents()) {
            if (name.equals(child.getName())) {
                return child;
            }
            if (child instanceof Container container) {
                Component result = component(container, name);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private static void capture(WalletPanel panel, Path directory, String name) {
        if (directory == null) {
            return;
        }
        layoutTree(panel);
        BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        panel.printAll(graphics);
        graphics.dispose();
        try {
            ImageIO.write(image, "png", directory.resolve(name).toFile());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save the application screenshot.", exception);
        }
    }

    private static void layoutTree(Container container) {
        container.invalidate();
        container.doLayout();
        for (Component child : container.getComponents()) {
            if (child instanceof Container childContainer) {
                layoutTree(childContainer);
            }
        }
    }

    private static void expect(Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected " + expected + ", got " + actual);
        }
    }

    private static void reject(String expectedMessage, Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException exception) {
            if (exception.getMessage().contains(expectedMessage)) {
                return;
            }
            throw new AssertionError("Unexpected validation message: " + exception.getMessage());
        }
        throw new AssertionError("Expected rejection containing: " + expectedMessage);
    }
}
