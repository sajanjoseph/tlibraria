package carttests;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Set;
import java.util.TreeSet;

import models.Book;
import models.BookOrder;
import models.BookShopUser;
import models.CartItem;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class BookOrderTests extends UnitTest {

	@Before
	public void setUp() throws Exception {
		Fixtures.deleteDatabase();
	}

	@Test
    public void testAddItemsToCart() {
		Fixtures.loadModels("data.yml");
		BookOrder cart = new BookOrder();
		
		Book book1 = Book.find("byIsbn", "978-0451160522").first();
    	Book book2 = Book.find("byIsbn", "978-0451210876").first();
    	Book book3 = Book.find("byIsbn", "978-0590353403").first();
    	
    	CartItem citem1 = new CartItem(book1,2);
    	CartItem citem2 = new CartItem(book2,1);
    	CartItem citem3 = new CartItem(book3,2);
    	
    	assertEquals(0,cart.cartItemsCount());
    	cart.addItem(citem1);
    	assertEquals(1,cart.cartItemsCount());
    	cart.addItem(citem2);
    	assertEquals(2,cart.cartItemsCount());
    	cart.addItem(citem3);
    	assertEquals(3,cart.cartItemsCount());
	}
	
	@Test
    public void testAddCartItemsWithSameBookFails() {
    	Fixtures.loadModels("data.yml");
    	
    	BookOrder cart = new BookOrder();
    	Book book1 = Book.find("byIsbn", "978-0451160522").first();
    	Book book2 = Book.find("byIsbn", "978-0451160522").first();
    	assertEquals(book1,book2);
    	CartItem citem1 = new CartItem(book1,2);
    	CartItem citem2 = new CartItem(book2,3);
    	assertEquals(citem1,citem2);
    	
    	boolean added = cart.addItem(citem1);
    	assertTrue(added);
    	
    	added = cart.addItem(citem2);
    	assertFalse(added);
    	
    	assertEquals(1,cart.cartItemsCount());
    }
	
	@Test
    public void testRemoveItemFromCart() {
    	Fixtures.loadModels("data.yml");
    	BookOrder cart = new BookOrder();
    	Book book1 = Book.find("byIsbn", "978-0451160522").first();
    	Book book2 = Book.find("byIsbn", "978-0451210876").first();
    	Book book3 = Book.find("byIsbn", "978-0590353403").first();
    	Book book4 = Book.find("byIsbn", "978-0439064873").first();
    	Book book5 = Book.find("byIsbn", "978-0439136365").first();
    	
    	CartItem citem1 = new CartItem(book1,2);
    	CartItem citem2 = new CartItem(book2,1);
    	CartItem citem3 = new CartItem(book3,2);
    	CartItem citem4 = new CartItem(book4,3);
    	CartItem citem5= new CartItem(book5,2);
    	
    	assertEquals(0,cart.cartItemsCount());
    	cart.addItem(citem1);
    	cart.addItem(citem2);
    	cart.addItem(citem3);
    	cart.addItem(citem4);
    	cart.addItem(citem5);
    	assertEquals(5,cart.cartItemsCount());
    	
    	//remove 2 items
    	cart.removeItem("978-0590353403");
    	cart.removeItem("978-0439136365");
    	assertEquals(3,cart.cartItemsCount());
	}
	
	 @Test
    public void testTotalPriceOfItemsInCart() {
    	Fixtures.loadModels("data.yml");
    	BookOrder cart = new BookOrder();
    	Book book1 = Book.find("byIsbn", "978-0451160522").first();
    	Book book2 = Book.find("byIsbn", "978-0451210876").first();
    	Book book3 = Book.find("byIsbn", "978-0590353403").first();
    	Book book4 = Book.find("byIsbn", "978-0439064873").first();
    	Book book5 = Book.find("byIsbn", "978-0439136365").first();
    	
    	CartItem citem1 = new CartItem(book1,2);
    	CartItem citem2 = new CartItem(book2,1);
    	CartItem citem3 = new CartItem(book3,2);
    	CartItem citem4 = new CartItem(book4,3);
    	CartItem citem5= new CartItem(book5,2);
    	
    	assertEquals(0,cart.cartItemsCount());
    	cart.addItem(citem1);
    	cart.addItem(citem2);
    	cart.addItem(citem3);
    	cart.addItem(citem4);
    	cart.addItem(citem5);
    	assertEquals(5,cart.cartItemsCount());
    	assertEquals(new BigDecimal("65.44"),cart.getTotalPrice());
    	
    	cart.removeItem(citem5);
    	assertEquals(4,cart.cartItemsCount());
    	assertEquals(new BigDecimal("51.48"),cart.getTotalPrice());
    	
    }
	 
	 @Test
	 public void testRetrieveShoppingCartOfCustomer() {
		 Fixtures.loadModels("data.yml");
		 BookShopUser den = BookShopUser.find("byEmail", "denny@gmail.com").first();
		 assertNotNull(den);
		 
		 Book book1 = Book.find("byIsbn","978-0439136365").first();
	     assertNotNull(book1);
	     
	     Book book2 = Book.find("byIsbn","978-0451210876").first();
	     Book book3 = Book.find("byIsbn","978-0590353403").first();
	     assertNotNull(book2);
	     assertNotNull(book3);
	     
	    //add 3 copies of book1
	    CartItem citem1 = new CartItem(book1,3);
	    
	    //add 2 copies of book2
    	CartItem citem2 = new CartItem(book2,2);
    	
    	//add 1 copy of book3
    	CartItem citem3 = new CartItem(book3,1);
    	
    	BookOrder cart = new BookOrder(den);
    	cart.addItem(citem1);    	
    	cart.addItem(citem2);
    	cart.addItem(citem3);
    	cart.save();
    	
    	BookOrder denCart =BookOrder.find("byCustomer", den).first();
    	assertNotNull(denCart);
    	
    	Set<CartItem> denCartItems = denCart.cartItems;
    	assertEquals(3,denCartItems.size());
    	
    	//assert if the sets contain same cart items
    	Set<CartItem> expectedCartItems = new TreeSet<CartItem>();
    	expectedCartItems.add(citem1);
    	expectedCartItems.add(citem2);
    	expectedCartItems.add(citem3);
    	assertEquals(expectedCartItems,denCartItems);
    	
    	//check each cartitem..the following code will work
    	//since ordering is based on ISBN string
    	
    	CartItem[] citems = denCartItems.toArray(new CartItem[0]);
    	CartItem denCartItem0 = citems[0];
    	CartItem denCartItem1 = citems[1];
    	CartItem denCartItem2 = citems[2];
    	
    	assertEquals(3,denCartItem0.quantity);
    	assertEquals("978-0439136365",denCartItem0.book.isbn);
    	assertEquals(2,denCartItem1.quantity);
    	assertEquals("978-0451210876",denCartItem1.book.isbn);
    	assertEquals(1,denCartItem2.quantity);
    	assertEquals("978-0590353403",denCartItem2.book.isbn);
	 }
	 
	 @Test
	 public void testCartItemsDeletedWhenCartIsDeleted() {
		 Fixtures.loadModels("data.yml");
		 BookShopUser den = BookShopUser.find("byEmail", "denny@gmail.com").first();
		 assertNotNull(den);
		 
		 Book book1 = Book.find("byIsbn","978-0439136365").first();
	     assertNotNull(book1);
	     
	     Book book2 = Book.find("byIsbn","978-0451210876").first();
	     assertNotNull(book2);
	     
	   //add 3 copies of book1
	    CartItem citem1 = new CartItem(book1,3);
	    
	    //add 2 copies of book2
    	CartItem citem2 = new CartItem(book2,2);
    	
    	//add cartItemsToCart
    	BookOrder cart = new BookOrder(den);
    	cart.addItem(citem1);    	
    	cart.addItem(citem2);
    	cart.save();
    	assertEquals(1,BookOrder.findAll().size());
    	assertEquals(2,cart.cartItemsCount());
    	
    	assertEquals(2,CartItem.findAll().size());
    	
    	//deleting the cart should delete the corresponding cartitems
    	cart.delete();
    	assertEquals(0,BookOrder.findAll().size());
    	assertEquals(0,CartItem.findAll().size());
    	
	 }

}
