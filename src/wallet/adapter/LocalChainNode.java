package wallet.adapter;


import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/** In-memory ledger with its own message format and integer transaction states. */
public final class LocalChainNode {
    private static final Logger LOG = Logger.getLogger(LocalChainNode.class.getName());
    public static final int IN_POOL = 0;
    public static final int INCLUDED = 1;

    public record Payment(String reference, String from, String to, long value, long fee) {}
    public record Receipt(String reference, int state, int height, String hash) {}
    public record Block(int height, String previousHash, List<Payment> payments, String hash) {}

    private final Map<String, Long> balances = new HashMap<>();
    private final Map<String, Payment> pool = new LinkedHashMap<>();
    private final Map<String, Receipt> receipts = new HashMap<>();
    private final List<Block> blocks = new ArrayList<>();
    private final List<Consumer<Receipt>> listeners = new ArrayList<>();

    public LocalChainNode(String fundedAddress, String recipientAddress) {
        Payment allocation = new Payment("genesis", "mint", fundedAddress, 10_000, 0);
        balances.put(fundedAddress, allocation.value());
        balances.put(recipientAddress, 0L);
        blocks.add(createBlock(0, "0".repeat(64), List.of(allocation)));
    }

    public long confirmed(String address) {
        return balances.getOrDefault(address, 0L);
    }

    public long spendable(String address) {
        long reserved = pool.values().stream()
                .filter(payment -> payment.from().equals(address))
                .mapToLong(payment -> payment.value() + payment.fee())
                .sum();
        return confirmed(address) - reserved;
    }

    public void validate(Payment payment) {
        if (!balances.containsKey(payment.from()) || !balances.containsKey(payment.to())
                || payment.from().equals(payment.to())) {
            throw new IllegalArgumentException("Use the recipient address shown in the demo guide.");
        }
        if (payment.value() <= 0 || payment.fee() < 0) {
            throw new IllegalArgumentException("Payment amount or fee is invalid.");
        }
        if (BigDecimal.valueOf(payment.value()).add(BigDecimal.valueOf(payment.fee()))
                .compareTo(BigDecimal.valueOf(spendable(payment.from()))) > 0) {
            throw new IllegalArgumentException("Insufficient available balance for amount plus fee.");
        }
    }

    public Receipt broadcast(Payment payment) {
        if (receipts.containsKey(payment.reference())) {
            throw new IllegalArgumentException("This transfer has already been submitted.");
        }
        validate(payment);
        pool.put(payment.reference(), payment);
        Receipt receipt = new Receipt(payment.reference(), IN_POOL, 0, "");
        receipts.put(payment.reference(), receipt);
        return receipt;
    }

    public void onIncluded(Consumer<Receipt> listener) {
        listeners.add(listener);
    }

    public Block mine() {
        if (pool.isEmpty()) {
            throw new IllegalArgumentException("Submit a transfer before creating a block.");
        }
        // The simulated producer orders transfers by fee, then by arrival order.
        List<Payment> payments = pool.values().stream()
                .sorted(Comparator.comparingLong(Payment::fee).reversed())
                .toList();
        Block block = createBlock(blocks.size(), blocks.get(blocks.size() - 1).hash(), payments);
        List<Receipt> confirmations = new ArrayList<>();
        for (Payment payment : payments) {
            balances.merge(payment.from(), -(payment.value() + payment.fee()), Long::sum);
            balances.merge(payment.to(), payment.value(), Long::sum);
            Receipt receipt = new Receipt(payment.reference(), INCLUDED, block.height(), block.hash());
            receipts.put(payment.reference(), receipt);
            confirmations.add(receipt);
        }
        blocks.add(block);
        pool.clear();
        // Commit the ledger first. One subscriber cannot prevent delivery to the others.
        List<Consumer<Receipt>> subscribers = List.copyOf(listeners);
        for (Receipt receipt : confirmations) {
            for (Consumer<Receipt> listener : subscribers) {
                try {
                    listener.accept(receipt);
                } catch (RuntimeException exception) {
                    LOG.log(Level.WARNING, "Confirmation observer failed for " + receipt.reference(), exception);
                }
            }
        }
        return block;
    }

    private static Block createBlock(int height, String previousHash, List<Payment> payments) {
        StringJoiner contents = new StringJoiner("|").add(Integer.toString(height)).add(previousHash);
        for (Payment payment : payments) {
            contents.add(payment.reference()).add(payment.from()).add(payment.to())
                    .add(Long.toString(payment.value())).add(Long.toString(payment.fee()));
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(contents.toString().getBytes(StandardCharsets.UTF_8));
            return new Block(height, previousHash, List.copyOf(payments), HexFormat.of().formatHex(digest));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
        }
    }
}
