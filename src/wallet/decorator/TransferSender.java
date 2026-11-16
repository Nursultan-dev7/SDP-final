package wallet.decorator;

import wallet.adapter.WalletNetwork;
import wallet.builder.Transaction;

/** Component contract shared by the concrete sender and authorization decorator. */
public interface TransferSender {
    WalletNetwork.Receipt send(Transaction transaction, TransferAuthenticator.Credentials credentials);
}
