package com.oceanview.reservation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Weekend Pricing Strategy - Strategy Pattern Implementation
 * Applies 20% surcharge for weekend nights (Friday and Saturday)
 * 
 */
public class WeekendPricingStrategy implements PricingStrategy {
    
    private static final BigDecimal WEEKEND_SURCHARGE = new BigDecimal("1.20"); // 20% surcharge
    
    @Override
    public BigDecimal calculatePrice(BigDecimal basePrice, Date checkInDate, 
                                    Date checkOutDate, String roomType) {
        
        LocalDate checkIn = checkInDate.toLocalDate();
        LocalDate checkOut = checkOutDate.toLocalDate();
        
        BigDecimal totalPrice = BigDecimal.ZERO;
        
        // Iterate through each night
        LocalDate currentDate = checkIn;
        while (currentDate.isBefore(checkOut)) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            
            // Apply surcharge for Friday and Saturday nights
            if (dayOfWeek == DayOfWeek.FRIDAY || dayOfWeek == DayOfWeek.SATURDAY) {
                totalPrice = totalPrice.add(basePrice.multiply(WEEKEND_SURCHARGE));
            } else {
                totalPrice = totalPrice.add(basePrice);
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        return totalPrice.setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    public String getStrategyName() {
        return "WEEKEND";
    }
}
