package com.oceanview.reservation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;

/**
 * Seasonal Pricing Strategy - Strategy Pattern Implementation
 * Applies different pricing based on seasons:
 * - Peak Season (June-August): 30% surcharge
 * - Shoulder Season (April-May, September-October): 10% surcharge
 * - Off Season (November-March): Standard price
 * 
 */
public class SeasonalPricingStrategy implements PricingStrategy {
    
    private static final BigDecimal PEAK_SEASON_MULTIPLIER = new BigDecimal("1.30"); // 30% surcharge
    private static final BigDecimal SHOULDER_SEASON_MULTIPLIER = new BigDecimal("1.10"); // 10% surcharge
    private static final BigDecimal OFF_SEASON_MULTIPLIER = new BigDecimal("1.00"); // Standard
    
    @Override
    public BigDecimal calculatePrice(BigDecimal basePrice, Date checkInDate, 
                                    Date checkOutDate, String roomType) {
        
        LocalDate checkIn = checkInDate.toLocalDate();
        LocalDate checkOut = checkOutDate.toLocalDate();
        
        BigDecimal totalPrice = BigDecimal.ZERO;
        
        // Iterate through each night
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
    
    /**
     * Get season multiplier based on month
     * 
     * @param month Month (1-12)
     * @return Season multiplier
     */
    private BigDecimal getSeasonMultiplier(int month) {
        if (month >= 6 && month <= 8) {
            // Peak Season: June, July, August
            return PEAK_SEASON_MULTIPLIER;
        } else if ((month >= 4 && month <= 5) || (month >= 9 && month <= 10)) {
            // Shoulder Season: April, May, September, October
            return SHOULDER_SEASON_MULTIPLIER;
        } else {
            // Off Season: November - March
            return OFF_SEASON_MULTIPLIER;
        }
    }
}
