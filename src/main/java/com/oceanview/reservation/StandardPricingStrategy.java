package com.oceanview.reservation;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.temporal.ChronoUnit;

public class StandardPricingStrategy implements PricingStrategy {

    @Override
    public BigDecimal calculatePrice(BigDecimal basePrice, Date checkInDate,
                                    Date checkOutDate, String roomType) {

        long nights = ChronoUnit.DAYS.between(
            checkInDate.toLocalDate(),
            checkOutDate.toLocalDate()
        );

        return basePrice.multiply(BigDecimal.valueOf(nights));
    }

    @Override
    public String getStrategyName() {
        return "STANDARD";
    }
}
