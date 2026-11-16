package wallet.strategy;

import java.math.BigDecimal;

/** Interchangeable fee calculation algorithm. */
public interface FeeStrategy {
    BigDecimal calculate(BigDecimal amount);
}
