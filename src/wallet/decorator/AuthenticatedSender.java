package wallet.decorator;

import wallet.adapter.WalletNetwork;
import wallet.builder.Transaction;

/** Decorator: authorizes the transfer before invoking the wrapped component. */
public record AuthenticatedSender(TransferSender delegate, TransferAuthenticator authenticator)
        implements TransferSender {
    @Override
    public WalletNetwork.Receipt send(Transaction transaction,
                                      TransferAuthenticator.Credentials credentials) {
        authenticator.verify(transaction, credentials);
        return delegate.send(transaction, credentials);
    }
}
