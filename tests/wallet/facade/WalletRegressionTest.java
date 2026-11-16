package wallet.facade;

import wallet.adapter.LocalChainAdapter;
import wallet.adapter.LocalChainNode;
import wallet.adapter.WalletNetwork;
import wallet.builder.Transaction;
import wallet.builder.TransactionBuilder;
import wallet.decorator.DemoAuthenticator;
import wallet.observer.TransactionHistory;
import wallet.strategy.FeeStrategy;
import wallet.strategy.StandardFeeStrategy;
import wallet.strategy.PriorityFeeStrategy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/** Regression checks for domain boundaries and failures in the confirmation flow. */
public final class WalletRegressionTest {
    private WalletRegressionTest() {}

    public static void main(String[] args) {
        transactionInvariants();
        authenticationBoundaries();
        nodeEventsAndFeeOrder();
        System.out.println("PASS: domain invariants, expiry, superseded reviews, replay, fee order and observer isolation.");
    }

    private static void transactionInvariants() {
        reject(() -> transaction(null, "demo-bob", "1", "1"));
        reject(() -> transaction("demo-alice", null, "1", "1"));
        reject(() -> transaction("demo-alice", "demo-alice", "1", "1"));
        reject(() -> transaction("demo-alice", "demo-bob", "0", "1"));
        reject(() -> transaction("demo-alice", "demo-bob", "1", "-1"));
        reject(() -> transaction("demo-alice", "demo-bob", "1.001", "1"));
        reject(() -> transaction("demo-alice", "demo-bob", "1", "0.001"));
        reject(() -> transaction("demo-alice", "demo-bob", "92233720368547758.07", "0.01"));
        reject(() -> new Transaction(null, "demo-alice", "demo-bob", BigDecimal.ONE, BigDecimal.ONE));
        reject(() -> new TransactionBuilder(null).recipient("demo-bob")
                .amount(BigDecimal.ONE).fee(BigDecimal.ONE).build());
        Transaction valid = transaction(" demo-alice ", " demo-bob ", "1.000", "0");
        expect("demo-alice", valid.sender());
        expect("1.00", valid.amount().toPlainString());
        expect("0.00", valid.fee().toPlainString());
    }

    private static Transaction transaction(String from, String to, String amount, String fee) {
        return new Transaction(UUID.randomUUID(), from, to, new BigDecimal(amount), new BigDecimal(fee));
    }

    private static void authenticationBoundaries() {
        MutableClock clock = new MutableClock();
        WalletFacade wallet = new WalletFacade(new LocalChainAdapter(new LocalChainNode(WalletFacade.SENDER, WalletFacade.RECIPIENT)), new DemoAuthenticator(clock));
        FeeStrategy fees = new StandardFeeStrategy();
        WalletFacade.PreparedTransfer expired = wallet.prepare(WalletFacade.RECIPIENT, "10", fees);
        clock.advance(120);
        reject(() -> submit(wallet, expired));
        expect("100.00", wallet.balances().available().toPlainString());
        expect(0, wallet.history().size());

        WalletFacade.PreparedTransfer old = wallet.prepare(WalletFacade.RECIPIENT, "10", fees);
        WalletFacade.PreparedTransfer current = wallet.prepare(WalletFacade.RECIPIENT, "20", fees);
        wallet.cancel(old); // Canceling a stale review must not invalidate the current one.
        reject(() -> submit(wallet, old));
        clock.advance(119);
        expect(WalletNetwork.Status.PENDING, submit(wallet, current).status());
        reject(() -> submit(wallet, current));
        expect("79.00", wallet.balances().available().toPlainString());
        expect(1, wallet.history().size());
    }

    private static WalletNetwork.Receipt submit(WalletFacade wallet, WalletFacade.PreparedTransfer transfer) {
        return wallet.submit(transfer, DemoAuthenticator.DEMO_PIN, transfer.demoCode());
    }

    private static void nodeEventsAndFeeOrder() {
        LocalChainNode node = new LocalChainNode(WalletFacade.SENDER, WalletFacade.RECIPIENT);
        // A broken unrelated observer must not hide already committed confirmations from the wallet.
        node.onIncluded(receipt -> { throw new IllegalStateException("Simulated observer failure"); });
        WalletFacade wallet = new WalletFacade(new LocalChainAdapter(node), new DemoAuthenticator(Clock.systemUTC()));
        WalletFacade.PreparedTransfer first = wallet.prepare(WalletFacade.RECIPIENT, "10", new StandardFeeStrategy());
        submit(wallet, first);
        WalletFacade.PreparedTransfer second = wallet.prepare(WalletFacade.RECIPIENT, "20", new PriorityFeeStrategy());
        submit(wallet, second);
        List<String> observed = new ArrayList<>();
        node.onIncluded(receipt -> observed.add(receipt.reference()));
        Logger logger = Logger.getLogger(LocalChainNode.class.getName());
        boolean parentHandlers = logger.getUseParentHandlers();
        logger.setUseParentHandlers(false);
        try {
            wallet.createBlock();
        } finally {
            logger.setUseParentHandlers(parentHandlers);
        }
        expect(List.of(second.transaction().id().toString(), first.transaction().id().toString()), observed);
        for (TransactionHistory.Entry entry : wallet.history()) {
            expect(WalletNetwork.Status.CONFIRMED, entry.receipt().status());
        }
        expect("67.00", wallet.balances().confirmed().toPlainString());
        expect("30.00", wallet.balances().recipient().toPlainString());
        reject(() -> node.broadcast(new LocalChainNode.Payment(first.transaction().id().toString(),
                WalletFacade.SENDER, WalletFacade.RECIPIENT, 1000, 100)));
        expect("67.00", wallet.balances().confirmed().toPlainString());
    }

    private static void reject(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Expected an IllegalArgumentException");
    }

    private static void expect(Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected " + expected + ", got " + actual);
        }
    }

    private static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-01-01T00:00:00Z");

        void advance(long seconds) { now = now.plusSeconds(seconds); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return Clock.fixed(now, zone); }
        @Override public Instant instant() { return now; }
    }
}
