package com.oceanview.reservation;

import java.math.BigDecimal;
import java.sql.Date;

public interface PricingStrategy {

    BigDecimal calculatePrice(BigDecimal basePrice, Date checkInDate,
                             Date checkOutDate, String roomType);

    String getStrategyName();
}
