package wallet.decorator;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;

import wallet.builder.Transaction;

public final class DemoAuthenticator implements TransferAuthenticator {
    public static final String DEMO_PIN = "2468";
    private static final int MAX_ATTEMPTS = 3;

    private record Challenge(Transaction transaction, String code, Instant expiresAt) {}

    private final Clock clock;
    private final SecureRandom random = new SecureRandom();
    private Challenge active;
    private int attempts;

    public DemoAuthenticator(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String issue(Transaction transaction) {
        String code = String.format(Locale.ROOT, "%06d", random.nextInt(1_000_000));
        active = new Challenge(transaction, code, clock.instant().plusSeconds(120));
        attempts = 0;
        return code;
    }

    @Override
    public void verify(Transaction transaction, Credentials credentials) {
        if (active == null || !active.transaction().equals(transaction)) {
            throw new IllegalArgumentException("Review the transfer again to get a new code.");
        }
        if (!clock.instant().isBefore(active.expiresAt())) {
            active = null;
            throw new IllegalArgumentException("Code expired. Cancel and review the transfer again.");
        }
        if (!DEMO_PIN.equals(credentials.pin()) || !active.code().equals(credentials.code())) {
            attempts++;
            if (attempts == MAX_ATTEMPTS) {
                active = null;
                throw new IllegalArgumentException("Too many attempts. Cancel and review the transfer again.");
            }
            throw new IllegalArgumentException("Incorrect PIN or code. Try again.");
        }
        active = null;
    }

    @Override
    public void cancel(Transaction transaction) {
        if (active != null && active.transaction().equals(transaction)) {
            active = null;
        }
    }
}
