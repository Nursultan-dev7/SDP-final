package wallet.builder;

import java.math.BigDecimal;
import java.util.UUID;

/** Step-by-step transaction construction; domain invariants stay in Transaction. */
public final class TransactionBuilder {
    private final String sender;
    private String recipient;
    private BigDecimal amount;
    private BigDecimal fee;

    public TransactionBuilder(String sender) { this.sender = sender; }

    public TransactionBuilder recipient(String recipient) {
        this.recipient = recipient;
        return this;
    }

    public TransactionBuilder amount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public TransactionBuilder fee(BigDecimal fee) {
        this.fee = fee;
        return this;
    }

    public Transaction build() {
        return new Transaction(UUID.randomUUID(), sender, recipient, amount, fee);
    }
}
