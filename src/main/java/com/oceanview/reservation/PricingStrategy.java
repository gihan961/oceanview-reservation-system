package com.oceanview.reservation;

import java.math.BigDecimal;
import java.sql.Date;

/**
 * Pricing Strategy Interface - Strategy Pattern
 * Defines the contract for room pricing strategies
 * 
 */
public interface PricingStrategy {
    
    /**
     * Calculate price based on strategy
     * 
     * @param basePrice Base price per night
     * @param checkInDate Check-in date
     * @param checkOutDate Check-out date
     * @param roomType Room type
     * @return Calculated price
     */
    BigDecimal calculatePrice(BigDecimal basePrice, Date checkInDate, 
                             Date checkOutDate, String roomType);
    
    /**
     * Get strategy name
     * 
     * @return Strategy name
     */
    String getStrategyName();
}
