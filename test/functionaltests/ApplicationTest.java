package functionaltests;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;

import models.Address;
import models.Book;
import models.BookOrder;
import models.BookShopUser;
import models.CartItem;
import models.Payment;

public class ApplicationTest extends FunctionalTest {
	@Before
    public void setup() {
		Fixtures.deleteDatabase();
	}
	
	@Test
	public void testThatIndexPageWorks() {
        Response response = GET("/");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(play.Play.defaultWebEncoding, response);
    }
	
	@Test
	public void testListTagged() {
		Fixtures.loadModels("data.yml");
		//for a given category,the correct number of books are listed
		Response response = GET("/books/category/history");
		assertNotNull(renderArgs("books"));
		List<Book> books = (List<Book>) renderArgs("books");
		assertEquals(3,books.size());
	}
	
	@Test
	public void testSearchNormal() {
		Fixtures.loadModels("data.yml");
		Response response = GET("/books/magic");
		assertNotNull(renderArgs("books"));
		List<Book> books = (List<Book>) renderArgs("books");
		assertEquals(4,books.size());
	}
	
	@Test
	public void testSearchUppercase() {
		Fixtures.loadModels("data.yml");
		Response response = GET("/books/HOGWARTS");
		assertNotNull(renderArgs("books"));
		List<Book>books = (List<Book>) renderArgs("books");
		assertEquals(3,books.size());
	}
	
	@Test
	public void testSearchMixedcase() {
		Fixtures.loadModels("data.yml");
		Response response = GET("/books/HOGwarts");
		assertNotNull(renderArgs("books"));
		List<Book>books = (List<Book>) renderArgs("books");
		assertEquals(3,books.size());
	}
	
	/*
	 * Admin can do everything in the shop
	 * But should he be able to buy?
	 * At present he is forbidden from buying stuff
	 * This needs more thinking
	 */
	@Test
	public void testAdminCannotAddItemsToCart() {
		Fixtures.loadModels("data.yml");
		Response response  = loginAsUser("admin@bookshop.com","bookshopadmin");
		assertStatus(302,response);//redirect
		
		BookShopUser user = BookShopUser.find("byEmail", "admin@bookshop.com").first();
		BookOrder userCart = new BookOrder(user);
		userCart.save();
		assertEquals(0,userCart.cartItemsCount());
		//response  = addItemsToCart(userCart);
		response  = addItemsToCart(userCart.id);
		
		assertStatus(403,response);//permissions on the server do not allow user to add items.
		assertEquals(0,userCart.cartItemsCount());
		
	}
	
	@Test
	public void testCustomerCanAddToCart() {
		Fixtures.loadModels("data.yml");
		Response response = loginAsUser("denny@gmail.com","denny");
		assertStatus(302,response);
		BookShopUser user = BookShopUser.find("byEmail", "denny@gmail.com").first();
		BookOrder userCart = new BookOrder(user);
		userCart.save();
		assertEquals(0,userCart.cartItemsCount());
		//response  = addItemsToCart(userCart);
		response  = addItemsToCart(userCart.id);
		assertStatus(302,response);
		assertEquals(1,userCart.cartItemsCount());
		
	}
	/*
	 * find out how I can avoid repeating the above chunk
	 */
	/*@Test
	public void testCustomerCanRemoveItemFromCart() {
		
		Fixtures.loadModels("data.yml");
		Response response = loginAsUser("denny@gmail.com","denny");
		assertStatus(302,response);
		BookShopUser user = BookShopUser.find("byEmail", "denny@gmail.com").first();
		BookOrder userCart = new BookOrder(user);
		userCart.save();
		assertEquals(0,userCart.cartItemsCount());
		response  = addItemsToCart(userCart.id);
		assertStatus(302,response);
		assertEquals(1,userCart.cartItemsCount());
		
		CartItem cartItem = (CartItem) CartItem.findAll().get(0);
		System.out.println("testCustomerCanRemoveItemFromCart():cartItem="+cartItem.id);
		
		
		response = removeCartItemFromCart(userCart.id,cartItem.id);
		
		
		assertStatus(302,response);
		assertEquals(0,userCart.cartItemsCount());
	}*/
	
	/*@Test
	public void testCustomerCanRemoveItemFromCart() {
		
		Fixtures.loadModels("data.yml");
		Response response = loginAsUser("denny@gmail.com","denny");
		assertStatus(302,response);
		BookShopUser user = BookShopUser.find("byEmail", "denny@gmail.com").first();
		BookOrder userCart = new BookOrder(user);
		userCart.save();
		assertEquals(0,userCart.cartItemsCount());
		Book book = Book.find("byIsbn","978-0451160522").first();
    	assertNotNull(book);
    	
		CartItem cartItem = new CartItem(book,1);
		userCart.addItem(cartItem);
		userCart.save();

		assertStatus(302,response);
		userCart.refresh();
		assertEquals(1,userCart.cartItemsCount());
		
		
		System.out.println("usercart="+userCart.cartItemsCount());
		Map<String,String> removecartItemParams = new HashMap<String,String>();
		removecartItemParams.put("cartItemId", cartItem.id.toString());
		String removecartItemurl = "/books/removefromcart/"+userCart.id.toString();
		response = POST(removecartItemurl,removecartItemParams);
		assertStatus(302,response);
		//restartTx();
		//userCart.refresh();
		
		System.out.println("usercart="+userCart.cartItemsCount());
		assertEquals(0,userCart.cartItemsCount());
	}*/
	@Test
	public void testLoginNeededToAddItemToCart() {
    	Fixtures.loadModels("data.yml");
    	BookShopUser newguy = new BookShopUser("newguy@home.com","secretpass","NewGuy");
    	newguy.save();
    	assertNotNull(newguy);
    	BookOrder newguyCart = new BookOrder(newguy);
    	newguyCart.save();
    	Long cartId = newguyCart.getId();
    	assertTrue(newguyCart.cartItems.size()==0);
    	
    	Response response = addItemsToCart(newguyCart.id);
    	assertTrue(newguyCart.cartItems.size()==0);
    	assertLocationRedirect("/login",response);
    }
	
	@Test
	public void testLoginNeededToRemoveCartItem() {
    	Fixtures.loadModels("data.yml");
    	BookShopUser newguy = new BookShopUser("newguy@home.com","secretpass","NewGuy");
    	newguy.save();
    	assertNotNull(newguy);
    	BookOrder newguyCart = new BookOrder(newguy);
    	newguyCart.save();
    	Long cartId = newguyCart.getId();
    	
    	Response response = removeCartItemFromCart(newguyCart.id,new Long(20));//dummy item
    	assertLocationRedirect("/login",response);
    }
	@Test
	public void testLoginNeededToCheckout() {
    	//typing checkout url should take user to a login page
    	BookShopUser newguy = new BookShopUser("newguy@home.com","secretpass","NewGuy");
    	newguy.save();
    	Long custId = newguy.getId();
    	Response response = GET("/books/checkout/"+custId);
    	assertLocationRedirect("/login",response);
    }
	@Test
    public void checkoutWithEmptyCartShowsIndexPage() {
    	Fixtures.loadModels("data.yml");
    	BookShopUser denny = BookShopUser.find("byEmail", "denny@gmail.com").first();
    	assertNotNull(denny);
    	Response response = loginAsUser("denny@gmail.com","denny");
    	System.out.println("resp="+response.getHeader("Location"));
    	assertHeaderEquals("Location", "/", response);
    	//login ok, now checkout
    	response = GET("/books/checkout/"+denny.getId() );
    	assertHeaderEquals("Location", "/", response);
    }
    
    @Test
    public void checkoutWithFilledCartShowsCheckoutPage() {
    	Fixtures.loadModels("data.yml");
    	BookShopUser denny = BookShopUser.find("byEmail", "denny@gmail.com").first();
    	assertNotNull(denny);
    	Response response = loginAsUser("denny@gmail.com","denny");
    	System.out.println("resp location after login:="+response.getHeader("Location"));
    	assertHeaderEquals("Location", "/", response);
    	BookOrder dennyCart = new BookOrder(denny);
    	dennyCart.save();
    	assertNotNull(dennyCart);
    	Long cartId = dennyCart.getId();
    	assertTrue(dennyCart.cartItems.size()==0);
    	response = addItemsToCart(dennyCart.id);
    	System.out.println("resp location after addtocart="+response.getHeader("Location"));
    	assertHeaderEquals("Location", "/", response);
    	response = GET("/books/checkout/"+denny.getId().toString() );
    	assertNotNull(response);
    	assertIsOk(response);
    }
	
	@Test
	public void testLoginNeededToEditCartItem() {
    	//typing editcartitem url should take user to a login page
		Fixtures.loadModels("data.yml");
    	BookShopUser newguy = new BookShopUser("newguy@home.com","secretpass","NewGuy");
    	newguy.save();
    	Long custId = newguy.getId();
    	Map<String,String> editcartItemParams = new HashMap<String,String>();
    	editcartItemParams.put("quantity", "5");
    	editcartItemParams.put("cartItemId", "7");
    	String editcartItemUrl = "/books/editcartitem/"+custId;
    	Response response = POST(editcartItemUrl,editcartItemParams);
    	assertLocationRedirect("/login",response);
    }
	
	@Test
	public void setAddressWithoutLogin() {
		Fixtures.loadModels("data.yml");
		BookShopUser denny = BookShopUser.find("byEmail", "denny@gmail.com").first();
		Response response = POST("/books/address/"+denny.getId().toString());
		assertLocationRedirect("/login",response);
	}
	
	private Map<String,String> createValidAddressParams(){
		Map<String,String> addressParams = new HashMap<String,String>();
		addressParams.put("addressline1", "karukail");
		addressParams.put("addressline2", "");
		addressParams.put("city", "");
		addressParams.put("pincode", "");
		addressParams.put("phonenumber", "");
		
    	addressParams.put("state", "kerala");
    	addressParams.put("country", "IN");
    	return addressParams;
	}
	
	private Map<String,String> createInValidAddressParams(){
		Map<String,String> addressParams = new HashMap<String,String>();
		addressParams.put("addressline2", "kanhangad");
    	addressParams.put("phonenumber", "918972222232");
    	//required field country omitted
    	addressParams.put("addressline2", "");
		addressParams.put("city", "");
		addressParams.put("pincode", "");
		addressParams.put("state", "");
    	addressParams.put("country", "");
    	return addressParams;
	}
	
	@Test
	public void setAddressOmittingRequiredField() {
		Fixtures.loadModels("data.yml");
		//no addresses
		assertTrue(Address.findAll().size()==0);
		BookShopUser denny = BookShopUser.find("byEmail", "denny@gmail.com").first();
		//login
		Response loginResponse = loginAsUser("denny@gmail.com","denny");
		//shop
		BookOrder dennyCart = new BookOrder(denny);
    	dennyCart.save();
    	assertNotNull(dennyCart);
    	addItemsToCart(dennyCart.id);
    	dennyCart.refresh();
    	System.out.println("CARTITEMS="+dennyCart.cartItems.size());
    	assertFalse(dennyCart.cartItems.size()==0);
    	
    	Map<String,String> addressParams = createInValidAddressParams();
    	Response response = POST("/books/address/"+denny.getId().toString(),addressParams);
    	assertTrue(Address.findAll().size()==0);
	}
	
	@Test
	public void testAddValidAddressWorks() {
		Fixtures.loadModels("data.yml");
		//no addresses
		assertTrue(Address.findAll().size()==0);
		BookOrder dennyCart = loginAndShop("denny@gmail.com","denny");
		dennyCart.refresh();
		assertFalse(dennyCart.cartItems.size()==0);
		Map<String,String> addressParams = createValidAddressParams();
    	Response response = POST("/books/address/"+dennyCart.customer.getId().toString(),addressParams);
    	assertTrue(Address.findAll().size()==1);
	}
	
	@Test
	public void testTwoUsersCanHaveSameAddressFieldValues() {
		//denny gives addressline1=x,addressline2=y,state1=a,country1=b
		//jimmy gives addressline1=x,addressline2=y,state1=a,country1=b
		//2 address records be created in db
		
		Fixtures.loadModels("data.yml");
		BookOrder dennyCart = loginAndShop("denny@gmail.com","denny");
		assertFalse(dennyCart.cartItems.size()==0);
		Map<String,String> dennyAddressParams = createValidAddressParams();
		Response response = POST("/books/address/"+dennyCart.customer.getId().toString(),dennyAddressParams);
		assertTrue(Address.findAll().size()==1);
		
		GET("/books/logout");
		BookOrder jimmyCart = loginAndShop("jimmy@gmail.com","jimmy");
		assertFalse(jimmyCart.cartItems.size()==0);
		Map<String,String> jimmyAddressParams = createValidAddressParams();
		Response response1 = POST("/books/address/"+jimmyCart.customer.getId().toString(),jimmyAddressParams);
		assertTrue(Address.findAll().size()==2);
	}
	
	@Test
	public void paymentPageWithoutLogin() {
		Fixtures.loadModels("data.yml");
		BookShopUser denny = BookShopUser.find("byEmail", "denny@gmail.com").first();
		Response response = GET("/books/payment/"+denny.getId().toString());
		assertLocationRedirect("/login",response);
	}
	
	@Test
	public void paymentPageWithoutShopping() {
		Fixtures.loadModels("data.yml");
		Response response = loginAsUser("denny@gmail.com","denny");
		BookShopUser denny = BookShopUser.find("byEmail", "denny@gmail.com").first();
		Response response1 = GET("/books/payment/"+denny.getId().toString());
		assertLocationRedirect("/",response1);
	}
	
	private Map<String, String> createValidPaymentParams() {
		Map<String,String> paymentParams = new HashMap<String,String>();
    	paymentParams.put("newCreditCardNumber", "11111122222222");
    	paymentParams.put("newCCExpMonth", "03");
    	paymentParams.put("newCCExpYear", "2013");
    	paymentParams.put("newCCType", "Visa");
    	paymentParams.put("newCCName", "dennyj");
		return paymentParams;
	}
	
	private Map<String, String> createInValidPaymentParams() {
		Map<String,String> paymentParams = new HashMap<String,String>();
    	paymentParams.put("newCreditCardNumber", "111111");
    	paymentParams.put("newCCExpMonth", "03");
    	paymentParams.put("newCCExpYear", "2013");
    	paymentParams.put("newCCType", "Visa");
    	paymentParams.put("newCCName", "dennyj");
		return paymentParams;
	}
	
	@Test
	public void testAddPaymentMethodWithoutShopping() {
    	Fixtures.loadModels("data.yml");
    	//user adds an invalid payment method
    	List<Payment> payments = Payment.findAll();
    	assertEquals(3,payments.size());
    	//String invalidCC = "11223344";
    	Response response = loginAsUser("denny@gmail.com","denny");
    	assertLocationRedirect("/",response);
    	BookShopUser denny = BookShopUser.find("byEmail","denny@gmail.com").first();
    	assertNotNull(denny);
    	Map<String, String> paymentParams = createValidPaymentParams();
    	
    	Response payment_response = POST("/books/payment/add/"+denny.getId().toString(),paymentParams);
    	assertLocationRedirect("/",payment_response);
    	assertEquals(3, Payment.findAll().size());
    }
	
	private BookOrder loginAndShop(String email,String pass) {
		BookShopUser user = BookShopUser.find("byEmail", email).first();
		assertNotNull(user);
		//login
		Response loginResponse = loginAsUser(email,pass);
		BookOrder userCart = new BookOrder(user);
		userCart.save();
		addItemsToCart(userCart.id);
		userCart.refresh();
		return userCart;
	}
	
	
	
	private void assertLocationRedirect(String location,Response response) {
    	assertHeaderEquals("Location",location,response);
    }
	
	private Response removeCartItemFromCart(Long cartId,Long cartItemId) {
		BookOrder cart = BookOrder.findById(cartId);
		Map<String,String> removecartItemParams = new HashMap<String,String>();
		removecartItemParams.put("cartItemId", cartItemId.toString());
		String removecartItemurl = "/books/removefromcart/"+cart.id.toString();
		Response response = POST(removecartItemurl,removecartItemParams);
		
		cart.refresh();
		return response;
	}
	
	private Response loginAsUser(String email,String password) {
		Map<String,String> loginUserParams = new HashMap<String,String>();
    	loginUserParams.put("username", email);
    	loginUserParams.put("password", password);
    	Response response = POST("/login",loginUserParams); // valid login
		return response;
	}
	
	private Response addItemsToCart(Long cartId) {
		BookOrder cart = BookOrder.findById(cartId);
    	Book book = Book.find("byIsbn","978-0451160522").first();
    	assertNotNull(book);
    	System.out.println("TEST::BEFORE POST:addItemsToCart id="+cart.id+" has="+cart.cartItems.size());
    	Map<String,String> addtocartParams = new HashMap<String,String>();
    	addtocartParams.put("cartId", cart.id.toString());
    	addtocartParams.put("quantity", "2");
    	String addtocarturl = "/books/addtocart/"+book.id.toString();
    	Response response = POST(addtocarturl,addtocartParams);
    	
    	cart.refresh();//? bring cart uptodate by refreshing state 
    	System.out.println("TEST::AFTER POST:addItemsToCart id="+cart.id+" has="+cart.cartItems.size());
    	return response;
    }
	public static void restartTx() {
        JPAPlugin.closeTx(false);
        JPAPlugin.startTx(false);
	}

	public static void clearHibernateSession() {
        JPA.em().flush();
        JPA.em().clear();
	} 
	
}
