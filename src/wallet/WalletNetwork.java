package wallet;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Consumer;

interface WalletNetwork {
    enum Status {
        PENDING("Pending"), CONFIRMED("Confirmed");

        private final String label;

        Status(String label) {
            this.label = label;
        }

        String label() {
            return label;
        }
    }

    record Receipt(UUID transactionId, Status status, int blockHeight, String blockHash) {}
    record BlockSummary(int height, int transactionCount, String hash, String previousHash) {}

    BigDecimal confirmed(String address);
    BigDecimal available(String address);
    void validate(Transaction transaction);
    Receipt broadcast(Transaction transaction);
    void onConfirmation(Consumer<Receipt> listener);
    BlockSummary createBlock();
}
