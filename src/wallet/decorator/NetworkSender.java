package wallet.decorator;

import wallet.adapter.WalletNetwork;
import wallet.builder.Transaction;

/** Concrete component: submits the transfer to the network. */
public record NetworkSender(WalletNetwork network) implements TransferSender {
    @Override
    public WalletNetwork.Receipt send(Transaction transaction, TransferAuthenticator.Credentials credentials) {
        return network.broadcast(transaction);
    }
}
