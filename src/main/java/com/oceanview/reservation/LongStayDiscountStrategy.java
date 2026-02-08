package com.oceanview.reservation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.temporal.ChronoUnit;

/**
 * Long Stay Discount Strategy - Strategy Pattern Implementation
 * Applies progressive discounts based on length of stay:
 * - 7-13 nights: 5% discount
 * - 14-20 nights: 10% discount
 * - 21+ nights: 15% discount
 * 
 */
public class LongStayDiscountStrategy implements PricingStrategy {
    
    private static final BigDecimal WEEK_DISCOUNT = new BigDecimal("0.95"); // 5% discount
    private static final BigDecimal TWO_WEEK_DISCOUNT = new BigDecimal("0.90"); // 10% discount
    private static final BigDecimal THREE_WEEK_DISCOUNT = new BigDecimal("0.85"); // 15% discount
    
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
    
    /**
     * Get discount multiplier based on number of nights
     * 
     * @param nights Number of nights
     * @return Discount multiplier
     */
    private BigDecimal getDiscountMultiplier(long nights) {
        if (nights >= 21) {
            return THREE_WEEK_DISCOUNT; // 15% discount
        } else if (nights >= 14) {
            return TWO_WEEK_DISCOUNT; // 10% discount
        } else if (nights >= 7) {
            return WEEK_DISCOUNT; // 5% discount
        } else {
            return BigDecimal.ONE; // No discount
        }
    }
}
