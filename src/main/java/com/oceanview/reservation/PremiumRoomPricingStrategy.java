package com.oceanview.reservation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.temporal.ChronoUnit;

public class PremiumRoomPricingStrategy implements PricingStrategy {

    private static final BigDecimal SUITE_MULTIPLIER = new BigDecimal("1.50");
    private static final BigDecimal PRESIDENTIAL_MULTIPLIER = new BigDecimal("2.00");
    private static final BigDecimal DELUXE_MULTIPLIER = new BigDecimal("1.25");
    private static final BigDecimal STANDARD_MULTIPLIER = BigDecimal.ONE;

    @Override
    public BigDecimal calculatePrice(BigDecimal basePrice, Date checkInDate,
                                    Date checkOutDate, String roomType) {

        long nights = ChronoUnit.DAYS.between(
            checkInDate.toLocalDate(),
            checkOutDate.toLocalDate()
        );

        BigDecimal multiplier = getRoomTypeMultiplier(roomType);
        BigDecimal subtotal = basePrice.multiply(BigDecimal.valueOf(nights));

        return subtotal.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getStrategyName() {
        return "PREMIUM_ROOM";
    }

    private BigDecimal getRoomTypeMultiplier(String roomType) {
        if (roomType == null) {
            return STANDARD_MULTIPLIER;
        }

        String lowerRoomType = roomType.toLowerCase();

        if (lowerRoomType.contains("presidential")) {
            return PRESIDENTIAL_MULTIPLIER;
        } else if (lowerRoomType.contains("suite")) {
            return SUITE_MULTIPLIER;
        } else if (lowerRoomType.contains("deluxe")) {
            return DELUXE_MULTIPLIER;
        } else {
            return STANDARD_MULTIPLIER;
        }
    }
}
