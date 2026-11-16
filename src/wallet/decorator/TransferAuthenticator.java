package wallet.decorator;

import wallet.builder.Transaction;

/** Authorization contract; the demo challenge can be replaced independently of the sender. */
public interface TransferAuthenticator {
    record Credentials(String pin, String code) {}

    String issue(Transaction transaction);
    void verify(Transaction transaction, Credentials credentials);
    void cancel(Transaction transaction);
}
