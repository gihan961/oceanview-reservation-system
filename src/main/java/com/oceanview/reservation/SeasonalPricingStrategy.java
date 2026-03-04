package com.oceanview.reservation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;

public class SeasonalPricingStrategy implements PricingStrategy {

    private static final BigDecimal PEAK_SEASON_MULTIPLIER = new BigDecimal("1.30");
    private static final BigDecimal SHOULDER_SEASON_MULTIPLIER = new BigDecimal("1.10");
    private static final BigDecimal OFF_SEASON_MULTIPLIER = new BigDecimal("1.00");

    @Override
    public BigDecimal calculatePrice(BigDecimal basePrice, Date checkInDate,
                                    Date checkOutDate, String roomType) {

        LocalDate checkIn = checkInDate.toLocalDate();
        LocalDate checkOut = checkOutDate.toLocalDate();

        BigDecimal totalPrice = BigDecimal.ZERO;

        LocalDate currentDate = checkIn;
        while (currentDate.isBefore(checkOut)) {
            int month = currentDate.getMonthValue();
            BigDecimal multiplier = getSeasonMultiplier(month);

            totalPrice = totalPrice.add(basePrice.multiply(multiplier));
            currentDate = currentDate.plusDays(1);
        }

        return totalPrice.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getStrategyName() {
        return "SEASONAL";
    }

    private BigDecimal getSeasonMultiplier(int month) {
        if (month >= 6 && month <= 8) {

            return PEAK_SEASON_MULTIPLIER;
        } else if ((month >= 4 && month <= 5) || (month >= 9 && month <= 10)) {

            return SHOULDER_SEASON_MULTIPLIER;
        } else {

            return OFF_SEASON_MULTIPLIER;
        }
    }
}
