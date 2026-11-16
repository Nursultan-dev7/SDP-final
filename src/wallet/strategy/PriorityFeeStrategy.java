package wallet.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PriorityFeeStrategy implements FeeStrategy {
    @Override
    public BigDecimal calculate(BigDecimal amount) {
        return amount.movePointLeft(1).setScale(2, RoundingMode.HALF_UP)
                .max(new BigDecimal("2.00"));
    }
}
