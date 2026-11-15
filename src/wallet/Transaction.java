package wallet;

import java.math.BigDecimal;
import java.util.UUID;

record Transaction(UUID id, String sender, String recipient, BigDecimal amount, BigDecimal fee) {
    BigDecimal total() {
        return amount.add(fee);
    }

    static final class Builder {
        private final String sender;
        private String recipient;
        private BigDecimal amount;
        private BigDecimal fee;

        Builder(String sender) {
            this.sender = sender;
        }

        Builder recipient(String recipient) {
            this.recipient = recipient;
            return this;
        }

        Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        Builder fee(BigDecimal fee) {
            this.fee = fee;
            return this;
        }

        Transaction build() {
            if (sender.isBlank() || recipient == null || recipient.isBlank() || sender.equals(recipient)) {
                throw new IllegalArgumentException("Choose a different recipient address.");
            }
            if (amount == null || amount.signum() <= 0 || fee == null || fee.signum() < 0) {
                throw new IllegalArgumentException("Enter a positive amount and a valid fee.");
            }
            return new Transaction(UUID.randomUUID(), sender, recipient, amount, fee);
        }
    }
}
