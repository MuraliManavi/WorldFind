package com.murali.worldfind.utils

import com.murali.worldfind.data.mock.CountryData
import java.util.Locale

object CurrencyConverter {
    
    /**
     * Converts a local price to INR based on the country's exchange rate.
     * Rate: 1 INR = X local units
     */
    fun convertToINR(localPrice: Double, countryCode: String): Double {
        val country = CountryData.countries.find { it.countryCode == countryCode }
            ?: return localPrice
        
        return if (country.exchangeRateFromINR != 0.0) {
            localPrice / country.exchangeRateFromINR
        } else {
            localPrice
        }
    }

    /**
     * Converts an INR price to local currency based on the country's exchange rate.
     */
    fun convertFromINR(inrPrice: Double, countryCode: String): Double {
        val country = CountryData.countries.find { it.countryCode == countryCode }
            ?: return inrPrice
        
        return inrPrice * country.exchangeRateFromINR
    }

    /**
     * Formats a price with currency symbol.
     */
    fun formatPrice(price: Double, currencySymbol: String): String {
        return "$currencySymbol${String.format(Locale.US, "%.0f", price)}"
    }

    /**
     * Formats INR price.
     */
    fun formatINR(price: Double): String {
        return "₹${String.format(Locale.US, "%.0f", price)}"
    }
}
