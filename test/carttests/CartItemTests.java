package carttests;

import static org.junit.Assert.*;

import models.Book;
import models.CartItem;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class CartItemTests extends UnitTest {

	@Before
	public void setUp() throws Exception {
		Fixtures.deleteDatabase();
	}

	@Test
	public void testCartItemPrice() {
		Fixtures.loadModels("data.yml");
		Book book1 = Book.find("byIsbn", "978-0451160522").first();
		CartItem cartItem1 = new CartItem(book1,4);
		System.out.println("price="+cartItem1.getCartItemPrice());
		assertEquals(12.0,cartItem1.getCartItemPrice().doubleValue(),0.0001);
	}

}
