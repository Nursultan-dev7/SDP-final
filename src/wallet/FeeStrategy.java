package wallet;

import java.math.BigDecimal;
import java.math.RoundingMode;

interface FeeStrategy {
    BigDecimal calculate(BigDecimal amount);

    final class Standard implements FeeStrategy {
        @Override
        public BigDecimal calculate(BigDecimal amount) {
            return new BigDecimal("1.00");
        }
    }

    final class Priority implements FeeStrategy {
        @Override
        public BigDecimal calculate(BigDecimal amount) {
            return amount.movePointLeft(1).setScale(2, RoundingMode.HALF_UP)
                    .max(new BigDecimal("2.00"));
        }
    }
}
