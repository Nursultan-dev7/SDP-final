package wallet;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;

final class WalletFacade {
    static final String SENDER = "demo-alice";
    static final String RECIPIENT = "demo-bob";

    record PreparedTransfer(Transaction transaction, String demoCode) {}
    record Balances(BigDecimal confirmed, BigDecimal available, BigDecimal recipient) {}

    private final WalletNetwork network;
    private final DemoAuthenticator authenticator;
    private final TransferSender sender;
    private final TransactionHistory history = new TransactionHistory();

    WalletFacade(Clock clock) {
        this(new LocalChainAdapter(new LocalChainNode(SENDER, RECIPIENT)), clock);
    }

    WalletFacade(WalletNetwork network, Clock clock) {
        this.network = network;
        authenticator = new DemoAuthenticator(clock);
        sender = new TransferSender.AuthenticatedSender(new TransferSender.NetworkSender(network), authenticator);
        network.onConfirmation(history::onConfirmation);
    }

    PreparedTransfer prepare(String recipient, String inputAmount, FeeStrategy strategy) {
        String amountText = inputAmount.strip().replace(',', '.');
        if (!amountText.matches("[0-9]{1,8}(\\.[0-9]{1,2})?")) {
            throw new IllegalArgumentException("Enter an amount like 10 or 10.50.");
        }
        BigDecimal amount = new BigDecimal(amountText).setScale(2);
        Transaction transaction = new Transaction.Builder(SENDER)
                .recipient(recipient.strip()).amount(amount).fee(strategy.calculate(amount)).build();
        network.validate(transaction);
        return new PreparedTransfer(transaction, authenticator.issue(transaction));
    }

    void cancel(PreparedTransfer transfer) {
        authenticator.cancel(transfer.transaction());
    }

    WalletNetwork.Receipt submit(PreparedTransfer transfer, String pin, String code) {
        Transaction transaction = transfer.transaction();
        WalletNetwork.Receipt receipt = sender.send(transaction, new DemoAuthenticator.Credentials(pin, code));
        history.recordSubmission(transaction, receipt);
        return receipt;
    }

    Balances balances() {
        return new Balances(network.confirmed(SENDER), network.available(SENDER), network.confirmed(RECIPIENT));
    }

    List<TransactionHistory.Entry> history() {
        return history.entries();
    }

    WalletNetwork.BlockSummary createBlock() {
        return network.createBlock();
    }
}
