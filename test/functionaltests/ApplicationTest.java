package functionaltests;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.mvc.Http;
import play.mvc.Http.Request;
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
	
	/*//@Test
	public void testAddValidAddressWorks() {
		Fixtures.loadModels("data.yml");
		//no addresses
		assertTrue(Address.findAll().size()==0);
		BookOrder dennyCart = loginAndShop("denny@gmail.com","denny");
		//dennyCart.refresh();
		assertFalse(dennyCart.cartItems.size()==0);
		System.out.println("dennycart has="+dennyCart.cartItems.size());
		//Map<String,String> addressParams = createValidAddressParams();
		Request request = newRequest();
		request.url = "/books/address/"+dennyCart.customer.getId().toString();
		request.method = "POST";
		request.params.put("addressline1", "karukail");
		request.params.put("addressline2", "");
		request.params.put("city", "");
		request.params.put("pincode", "");
		request.params.put("phonenumber", "");
		request.params.put("state", "kerala");
		request.params.put("country", "IN");
		Response response1 = makeRequest(request);
		System.out.println("testAddValidAddressWorks()::response1="+response1.status);
    	//Response response = POST("/books/address/"+dennyCart.customer.getId().toString(),addressParams);
		//Response response = postNewAddress("/books/address/"+dennyCart.customer.id,addressParams);
    	//assertTrue(Address.findAll().size()==1);
	}*/
	
	
	//@Test
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
	
	@Test
	public void testShowOrderConfirmPageWithoutPaymentSelected() {
		//THIS SHOULD ALSO DISPLAY PROPER ERROR MESSAGE (now it is 'PaymentId required')
		
		Fixtures.loadModels("data.yml");
		BookOrder dennyCart = loginAndShop("denny@gmail.com","denny");
		BookShopUser denny = BookShopUser.find("byEmail","denny@gmail.com").first();
		String showorderconfirmurl = "/books/orderconfirm/"+denny.id;
		Response response = GET(showorderconfirmurl);
		assertLocationRedirect("/books/payment/"+denny.id,response);
	}
	
	//@Test
	public void testShowOrderConfirmPageWithEmptyCart() {
		Fixtures.loadModels("data.yml");
		Response response = loginAsUser("denny@gmail.com","denny");
		assertLocationRedirect("/",response);
		BookShopUser denny = BookShopUser.find("byEmail","denny@gmail.com").first();
		String showorderconfirmurl = "/books/orderconfirm/"+denny.id;
		Response showresponse = GET(showorderconfirmurl);
		assertLocationRedirect("/",showresponse);
	}
	
	private BookOrder loginAndShop(String email,String pass) {
		BookShopUser user = BookShopUser.find("byEmail", email).first();
		assertNotNull(user);
		//login
		Response loginResponse = loginAsUser(email,pass);
		BookOrder userCart = new BookOrder(user);
		userCart.save();
		addItemsToCart(userCart.id);
		
		
		//userCart.refresh();
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
	
	@Test
	public void testRegisterNewUSer() {
		Fixtures.loadModels("data.yml");
		Map<String,String> regusermap= new HashMap<String,String>();
		regusermap.put("email", "newuser@gmail.com");
		regusermap.put("password", "secret");
		regusermap.put("passwordconfirm", "secret");
		regusermap.put("fullname", "newuser");
		
		List<BookShopUser> users = BookShopUser.findAll();
		assertEquals(3,users.size());
		assertEquals(3,BookShopUser.count());
		POST("/register",regusermap);
		users = BookShopUser.findAll();
		assertEquals(4,users.size());
		assertEquals(4,BookShopUser.count());
		
	}
	public static void restartTx() {
        JPAPlugin.closeTx(false);
        JPAPlugin.startTx(false);
	}

	public static void clearHibernateSession() {
        JPA.em().flush();
        JPA.em().clear();
	} 
	
	
	/*@Test
	public void testTransactions() {
		Fixtures.loadModels("data.yml");
		BookShopUser user = BookShopUser.find("byEmail", "denny@gmail.com").first();
		assertNotNull(user);
		//login
		Map<String,String> loginUserParams = new HashMap<String,String>();
    	loginUserParams.put("username","denny@gmail.com");
    	loginUserParams.put("password", "denny");
    	Response loginResponse = POST("/login",loginUserParams);
    	
    	//new req for adding to cart
    	BookOrder cart = new BookOrder(user);
    	cart.save();
    	Book book = Book.find("byIsbn","978-0451160522").first();
    	String addtocarturl = "/books/addtocart/"+book.id.toString();
    	assertNotNull(book);
    	assertEquals(0,cart.cartItemsCount());
    	System.out.println("cart has:"+cart.cartItems.size());
    	
    	Request addToCartRequest = newRequest();
        addToCartRequest.cookies = loginResponse.cookies;
        addToCartRequest.url = addtocarturl;
        addToCartRequest.method = "POST";
        addToCartRequest.params.put("cartId", cart.id.toString());
        addToCartRequest.params.put("quantity", "2");
        Response response = makeRequest(addToCartRequest);
    	assertEquals(1,cart.cartItemsCount());
		System.out.println("cart has:"+cart.cartItems.size());
	}*/
}
