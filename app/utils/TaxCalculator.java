package utils;

import java.math.BigDecimal;
import java.util.Set;

import models.CartItem;

public class TaxCalculator {
	public static BigDecimal getTax(Set<CartItem> cartItems) {
		return new BigDecimal(1.7);
	}

}
