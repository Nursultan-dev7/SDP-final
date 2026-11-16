package wallet.facade;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import wallet.adapter.WalletNetwork;
import wallet.builder.Transaction;
import wallet.builder.TransactionBuilder;
import wallet.decorator.AuthenticatedSender;
import wallet.decorator.NetworkSender;
import wallet.decorator.TransferAuthenticator;
import wallet.decorator.TransferSender;
import wallet.observer.TransactionHistory;
import wallet.strategy.FeeStrategy;

public final class WalletFacade {
    public static final String SENDER = "demo-alice";
    public static final String RECIPIENT = "demo-bob";

    public record PreparedTransfer(Transaction transaction, String demoCode) {}
    public record Balances(BigDecimal confirmed, BigDecimal available, BigDecimal recipient) {}

    private final WalletNetwork network;
    private final TransferAuthenticator authenticator;
    private final TransferSender sender;
    private final TransactionHistory history = new TransactionHistory();

    public WalletFacade(WalletNetwork network, TransferAuthenticator authenticator) {
        this.network = Objects.requireNonNull(network);
        this.authenticator = Objects.requireNonNull(authenticator);
        sender = new AuthenticatedSender(new NetworkSender(network), authenticator);
        network.onConfirmation(history::onConfirmation);
    }

    public PreparedTransfer prepare(String recipient, String inputAmount, FeeStrategy strategy) {
        if (recipient == null || inputAmount == null || strategy == null) {
            throw new IllegalArgumentException("Recipient, amount and fee strategy are required.");
        }
        String amountText = inputAmount.strip().replace(',', '.');
        if (!amountText.matches("[0-9]{1,8}(\\.[0-9]{1,2})?")) {
            throw new IllegalArgumentException("Enter an amount like 10 or 10.50.");
        }
        BigDecimal amount = new BigDecimal(amountText).setScale(2);
        Transaction transaction = new TransactionBuilder(SENDER)
                .recipient(recipient.strip()).amount(amount).fee(strategy.calculate(amount)).build();
        network.validate(transaction);
        return new PreparedTransfer(transaction, authenticator.issue(transaction));
    }

    public void cancel(PreparedTransfer transfer) {
        authenticator.cancel(transfer.transaction());
    }

    public WalletNetwork.Receipt submit(PreparedTransfer transfer, String pin, String code) {
        Transaction transaction = transfer.transaction();
        WalletNetwork.Receipt receipt = sender.send(transaction, new TransferAuthenticator.Credentials(pin, code));
        history.recordSubmission(transaction, receipt);
        return receipt;
    }

    public Balances balances() {
        return new Balances(network.confirmed(SENDER), network.available(SENDER), network.confirmed(RECIPIENT));
    }

    public List<TransactionHistory.Entry> history() {
        return history.entries();
    }

    public WalletNetwork.BlockSummary createBlock() {
        return network.createBlock();
    }
}
