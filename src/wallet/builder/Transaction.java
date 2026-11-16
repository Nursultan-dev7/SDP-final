package wallet.builder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

public record Transaction(UUID id, String sender, String recipient, BigDecimal amount, BigDecimal fee) {
    // The canonical constructor protects every creation path, including the Builder.
    public Transaction {
        if (id == null) {
            throw new IllegalArgumentException("A transaction ID is required.");
        }
        if (sender == null || recipient == null || sender.isBlank() || recipient.isBlank()) {
            throw new IllegalArgumentException("Choose a different recipient address.");
        }
        sender = sender.strip();
        recipient = recipient.strip();
        if (sender.equals(recipient)) {
            throw new IllegalArgumentException("Choose a different recipient address.");
        }
        if (amount == null || amount.signum() <= 0 || fee == null || fee.signum() < 0) {
            throw new IllegalArgumentException("Enter a positive amount and a valid fee.");
        }
        try {
            amount = amount.setScale(2, RoundingMode.UNNECESSARY);
            fee = fee.setScale(2, RoundingMode.UNNECESSARY);
            amount.add(fee).movePointRight(2).longValueExact();
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Amount and fee must fit in minor units with at most two decimal places.",
                    exception);
        }
    }

    public BigDecimal total() {
        return amount.add(fee);
    }
}
