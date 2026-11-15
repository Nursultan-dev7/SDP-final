package wallet;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Consumer;

final class LocalChainAdapter implements WalletNetwork {
    private final LocalChainNode node;

    LocalChainAdapter(LocalChainNode node) {
        this.node = node;
    }

    @Override
    public BigDecimal confirmed(String address) {
        return BigDecimal.valueOf(node.confirmed(address), 2);
    }

    @Override
    public BigDecimal available(String address) {
        return BigDecimal.valueOf(node.spendable(address), 2);
    }

    @Override
    public void validate(Transaction transaction) {
        node.validate(toPayment(transaction));
    }

    @Override
    public Receipt broadcast(Transaction transaction) {
        return toReceipt(node.broadcast(toPayment(transaction)));
    }

    @Override
    public void onConfirmation(Consumer<Receipt> listener) {
        node.onIncluded(receipt -> listener.accept(toReceipt(receipt)));
    }

    @Override
    public BlockSummary createBlock() {
        LocalChainNode.Block block = node.mine();
        return new BlockSummary(block.height(), block.payments().size(), block.hash(), block.previousHash());
    }

    private static LocalChainNode.Payment toPayment(Transaction transaction) {
        try {
            return new LocalChainNode.Payment(transaction.id().toString(), transaction.sender(),
                    transaction.recipient(), transaction.amount().movePointRight(2).longValueExact(),
                    transaction.fee().movePointRight(2).longValueExact());
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Use an amount with at most two decimal places.");
        }
    }

    private static Receipt toReceipt(LocalChainNode.Receipt receipt) {
        Status status = switch (receipt.state()) {
            case LocalChainNode.IN_POOL -> Status.PENDING;
            case LocalChainNode.INCLUDED -> Status.CONFIRMED;
            default -> throw new IllegalStateException("Unknown node receipt state: " + receipt.state());
        };
        return new Receipt(UUID.fromString(receipt.reference()), status, receipt.height(), receipt.hash());
    }
}
