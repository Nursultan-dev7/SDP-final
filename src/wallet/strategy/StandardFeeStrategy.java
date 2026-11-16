package wallet.strategy;

import java.math.BigDecimal;

public final class StandardFeeStrategy implements FeeStrategy {
    @Override
    public BigDecimal calculate(BigDecimal amount) {
        return new BigDecimal("1.00");
    }
}
