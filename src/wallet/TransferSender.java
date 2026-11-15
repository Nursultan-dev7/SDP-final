package wallet;

interface TransferSender {
    WalletNetwork.Receipt send(Transaction transaction, DemoAuthenticator.Credentials credentials);

    record NetworkSender(WalletNetwork network) implements TransferSender {
        @Override
        public WalletNetwork.Receipt send(Transaction transaction, DemoAuthenticator.Credentials credentials) {
            return network.broadcast(transaction);
        }
    }

    record AuthenticatedSender(TransferSender delegate, DemoAuthenticator authenticator)
            implements TransferSender {
        @Override
        public WalletNetwork.Receipt send(Transaction transaction,
                                          DemoAuthenticator.Credentials credentials) {
            authenticator.verify(transaction, credentials);
            return delegate.send(transaction, credentials);
        }
    }
}
