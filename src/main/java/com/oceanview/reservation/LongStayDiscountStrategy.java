package com.oceanview.reservation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.temporal.ChronoUnit;

public class LongStayDiscountStrategy implements PricingStrategy {

    private static final BigDecimal WEEK_DISCOUNT = new BigDecimal("0.95");
    private static final BigDecimal TWO_WEEK_DISCOUNT = new BigDecimal("0.90");
    private static final BigDecimal THREE_WEEK_DISCOUNT = new BigDecimal("0.85");

    @Override
    public BigDecimal calculatePrice(BigDecimal basePrice, Date checkInDate,
                                    Date checkOutDate, String roomType) {

        long nights = ChronoUnit.DAYS.between(
            checkInDate.toLocalDate(),
            checkOutDate.toLocalDate()
        );

        BigDecimal subtotal = basePrice.multiply(BigDecimal.valueOf(nights));
        BigDecimal discount = getDiscountMultiplier(nights);

        return subtotal.multiply(discount).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getStrategyName() {
        return "LONG_STAY";
    }

    private BigDecimal getDiscountMultiplier(long nights) {
        if (nights >= 21) {
            return THREE_WEEK_DISCOUNT;
        } else if (nights >= 14) {
            return TWO_WEEK_DISCOUNT;
        } else if (nights >= 7) {
            return WEEK_DISCOUNT;
        } else {
            return BigDecimal.ONE;
        }
    }
}
