package wallet.observer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import wallet.adapter.WalletNetwork;
import wallet.builder.Transaction;

public final class TransactionHistory {
    public record Entry(Transaction transaction, WalletNetwork.Receipt receipt) {}

    private final Map<UUID, Entry> entries = new LinkedHashMap<>();

    public void recordSubmission(Transaction transaction, WalletNetwork.Receipt receipt) {
        entries.put(transaction.id(), new Entry(transaction, receipt));
    }

    // Observer callback. Only confirmations for the active wallet appear in its history.
    public void onConfirmation(WalletNetwork.Receipt receipt) {
        entries.computeIfPresent(receipt.transactionId(),
                (id, entry) -> new Entry(entry.transaction(), receipt));
    }

    public List<Entry> entries() {
        return List.copyOf(entries.values());
    }
}
