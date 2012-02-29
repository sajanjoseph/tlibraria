package controllers;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Address;
import models.Book;
import models.BookOrder;
import models.BookShopUser;
import models.CartItem;
import models.Payment;
import paymentgateway.PaymentProcessor;
import play.Play;
import play.data.validation.Required;
import play.data.validation.MinSize;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Router;
import play.mvc.With;
import utils.OrderUtils;
import utils.PaymentUtils;
import utils.Status;

import validation.CCNumber;

@With(Secure.class)
@Check("customer")
public class Account extends Controller {
	
	@Before
	public static void addDefaults() {
		renderArgs.put("shopLogoText",Play.configuration.getProperty("bookshop.logotext"));
	}
	
	@Before
	static void storeCurrentUrl() {
		System.out.println("Account::storeCurrentUrl()");
		if("GET".equals(request.method)) {
			System.out.println("Account::storeCurrentUrl():request.url="+request.url);
			flash.put("url", request.url);
		}
	}
	
	@Before
	static void storeLoggedInUserAndCart() {
		if(Security.isConnected()) {
			BookShopUser user = BookShopUser.find("byEmail", Security.connected()).first();
			renderArgs.put("customer", user);
			BookOrder lastorder = getPendingOrder(user);
			if(lastorder == null) {
            	lastorder = new BookOrder(user);
            	lastorder.save();
            	System.out.println("Account:storeUserIfLoggedIn()::created a new order:"+lastorder.id);
            }else {
            	System.out.println("Account:storeUserIfLoggedIn()::existing pendOrder:"+lastorder.id +" retrieved from db");
            }
            renderArgs.put("cart", lastorder);
            System.out.println("Account:storeUserIfLoggedIn()::cart with items:"+lastorder.cartItems.size()+" put as lastorder");
		}
	}
	
	private static BookOrder getPendingOrder(BookShopUser customer) {
		String query = "select o from BookOrder o where o.status=:status and o.customer =:customer order by o.orderDate DESC,o.id DESC";
		 //BookOrder lastorder = BookOrder.find("select o from BookOrder o where o.status='pending' and o.customer =? order by o.order_date DESC,o.id DESC",customer).first();
		BookOrder lastorder = BookOrder.find(query).bind("status",Status.PENDING).bind("customer", customer).first();
		if(lastorder!=null) {
			lastorder.refresh();//state refreshed
		}
		System.out.println("controllers.Account.getPendingOrder():lastorder="+lastorder);
		return lastorder;
	}
	
	public static void addItemToCart(Long bookId,Long cartId,String quantity) {
		System.out.println("Account::addItemToCart()::bookId="+bookId+" cartId="+cartId+"quantity="+quantity);
		Book book = Book.findById(bookId);
		BookOrder order = BookOrder.findById(cartId);
		int qty = Integer.parseInt(quantity);
		CartItem cartItem = new CartItem(book,qty);
		order.addItem(cartItem, qty);
		order.save();
		System.out.println("Account::addItemToCart()::order saved");
		
		gotoLastView();
	}
	
	public static void removeItemFromCart(Long cartId,Long cartItemId) {
		System.out.println("Account::remove():cartId=" +cartId+" cartItemId="+cartItemId);
		
		CartItem cartItem = CartItem.findById(cartItemId);
		BookOrder cart = BookOrder.findById(cartId);
		
		System.out.println("Account::remove():initially cartitems="+cart.cartItemsCount());
		//if cart has this cartitem remove it
		if(cart.cartItems.contains(cartItem)) {
			cart.removeItem(cartItem);
			cart.save();
			System.out.println("Account::remove():cart saved cartitems="+cart.cartItemsCount());
			
		}
		gotoLastView();
	}
	
	public static void showCheckoutForm(Long customerId) {
		BookShopUser customer = BookShopUser.findById(customerId);
		BookOrder cart = getPendingOrder(customer);
		if (cart.cartItemsCount()==0) {
			System.out.println("Account::showCheckoutForm():cart empty ..going to Application.index()");
			Application.index();
		}else {
			System.out.println("Account::showCheckoutForm():render with cart="+cart.getId()+" customer="+customer.getId());
			//nextpage after checkout
			Map map = new HashMap();
			map.put("customerId", customer.getId());
			String paymentpage = Router.reverse("Account.showPaymentForm",map).url;
			System.out.println("Account::showCheckoutForm():paymentpage="+paymentpage);
			Address latestAddress = Address.find("bookshopuser = ? order by dateOfSubmit desc", customer).first();
			System.out.println("Account::showCheckoutForm():latestAddress="+latestAddress);
			render(cart,customer,paymentpage,latestAddress);
		}
	}
	
	public static void editCartItem(Long cartId,String quantity,Long cartItemId) {
		System.out.println("Account::editCartItem():cartId="+cartId +"cartItemId="+cartItemId +"quantity="+quantity);
		CartItem cartItem = CartItem.findById(cartItemId);
		System.out.println("Account::editCartItem():got cartItem="+cartItem.book.name+" "+cartItem.quantity);
		BookOrder cart = BookOrder.findById(cartId);
		int qty = Integer.parseInt(quantity);
		cartItem.quantity=qty;
		cart.save();
		gotoLastView();
	}
	
	public static void setCustomerAddressDetails(String nexturl,Long customerId,@Required(message="validation.addressline1") String addressline1,String addressline2,String city,@Required(message="validation.state")String state,String pincode,String phonenumber,@Required(message="validation.country") String country) {
		BookShopUser customer = BookShopUser.findById(customerId);
		BookOrder cart = getPendingOrder(customer);
		
		if(validation.hasErrors()) {
			validation.keep();
			Account.showCheckoutForm(customerId);
		}
		//if this address exists retrieve it else create new
		addressline1 = addressline1.trim();
		addressline2 = addressline2.trim();
		city = city.trim();
		state = state.trim();
		pincode = pincode.trim();
		phonenumber = phonenumber.trim();
		
		Address address = findOrCreateAddress(customer,addressline1,addressline2,city,state,pincode,phonenumber,country);
		//if customer addresses dont contain address
		
		if ((customer.addresses.size()==0) || !(customer.addresses.contains(address)) ) {
			customer.addresses.add(address);
			customer.save();
			System.out.println("Account:setCustomerAddressDetails()::customer.saved");
		}
		
		if(nexturl!=null && nexturl.length()!=0) {
			System.out.println("Account:setCustomerAddressDetails()::redirect to:"+nexturl);
			redirect(nexturl);//
		}else {
			System.out.println("Account:setCustomerAddressDetails()::gotoLastView:");
			gotoLastView();
		}
	}
	
	private static Address findOrCreateAddress(BookShopUser customer,String addressline1,	String addressline2, String city, String state, String pincode,String phonenumber, String country) {
		/*System.out.println("findOrCreateAddress():: addressline1="+addressline1+addressline1.length());
		System.out.println("findOrCreateAddress():: addressline2="+addressline2);
		System.out.println("findOrCreateAddress():: city="+city);
		System.out.println("findOrCreateAddress():: state="+state+state.length());
		System.out.println("findOrCreateAddress():: pincode="+pincode);
		System.out.println("findOrCreateAddress():: phonenumber="+phonenumber);
		System.out.println("findOrCreateAddress():: country="+country);*/
		
		
		String query = "select distinct a from  Address a where a.bookshopuser=:bookshopuser and a.addressLine1=:addressline1 and a.addressLine2=:addressline2 and a.city=:city and a.state=:state and a.pincode=:pincode and a.phoneNumber=:phonenumber and a.country=:country";
		Address address = Address.find(query).bind("bookshopuser", customer).bind("addressline1", addressline1).bind("addressline2", addressline2).bind("city", city).bind("state", state).bind("pincode", pincode).bind("phonenumber", phonenumber).bind("country", country).first();
		if(address == null) {//create a new address
			address = new Address();
			System.out.println("Account::findOrCreateAddress() creating new address");
			address.addressLine1=addressline1;
			address.addressLine2=addressline2;
			address.city=city;
			address.state=state;
			address.phoneNumber=phonenumber;
			address.pincode=pincode;
			address.country=country;
			address.bookshopuser = customer;//?
			address.save();
		}else {
			System.out.println("Account::findOrCreateAddress() got old address");
			address.dateOfSubmit = new Date();//??
			address.save();
		}
		return address;
	}
	
	public static void showPaymentForm(Long customerId) {
		System.out.println("showPaymentForm():: customerid="+customerId);
		BookShopUser customer = BookShopUser.findById(customerId);
		System.out.println("showPaymentForm():: customer has ="+customer.payments.size()+" payments");
		BookOrder cart = getPendingOrder(customer);
		if(cart.cartItems.size()==0) {//if cart empty go to Home page
			System.out.println("showPaymentForm():: calling Application.index()");
			Application.index();
		}
		Map map = new HashMap();
		map.put("customerId", customerId);
		String orderconfirmpage = Router.reverse("Account.showOrderConfirmPage",map).url;
		render(customer,orderconfirmpage);
		
	}
	
	public static void addNewPaymentDetails(Long customerId,@Required(message="validation.ccname") String newCCName,@Required @CCNumber(value=14,message="validation.ccnum_minsz") String newCreditCardNumber,@Required(message="validation.ccmonth") String newCCExpMonth,@Required(message="validation.ccyear") String newCCExpYear,@Required(message="validation.cctype") String newCCType){
		System.out.println("addNewPaymentDetails():: customerid="+customerId);
		System.out.println("addNewPaymentDetails():: newCCName="+newCCName+"="+newCCName.length());
		System.out.println("addNewPaymentDetails():: newCreditCardNumber="+newCreditCardNumber+"="+newCreditCardNumber.length());
		System.out.println("addNewPaymentDetails():: newCCExpMonth="+newCCExpMonth);
		System.out.println("addNewPaymentDetails():: newCCExpYear="+newCCExpYear);
		System.out.println("addNewPaymentDetails():: newCCType="+newCCType);
		
		if(validation.hasErrors()) {
			System.out.println("addNewPaymentDetails():: validation errors!");
			params.flash();
			validation.keep();
			System.out.println("calling showPaymentForm("+customerId+")");
			Account.showPaymentForm(customerId);
		}
		BookShopUser customer = BookShopUser.findById(customerId);
		BookOrder cart = getPendingOrder(customer);
		if(cart.cartItems.size()==0) {
			Application.index();
		}
		PaymentProcessor.addToPaymentProcessor(customer,newCCName,newCreditCardNumber,newCCExpMonth,newCCExpYear,newCCType);
		String maskedCCNum = PaymentUtils.maskCCNumber(newCreditCardNumber);
		System.out.println("addNewPaymentDetails():: maskedCCNum:"+maskedCCNum);
		Payment payment = findOrCreatePayment(customer,newCCName,maskedCCNum, newCCExpMonth,newCCExpYear, newCCType);
		System.out.println("addNewPaymentDetails():: foundOr created payment:"+payment);
		System.out.println("addNewPaymentDetails()::new payment method");
		customer.payments.add(payment);
		customer.save();
		System.out.println("addNewPaymentDetails()::customer  has:"+customer.payments.size()+" payments");
		
		showPaymentForm(customerId);
	}
	
	
	public static void setPaymentDetails(Long customerId,String nexturl,@Required Long paymentId) {
		System.out.println("setPaymentDetails():: nexturl="+nexturl);
		if(validation.hasErrors()) {
			System.out.println("setPaymentDetails():: validation errors!");
			validation.keep();
			Account.showPaymentForm(customerId);
		}
		BookShopUser customer = BookShopUser.findById(customerId);
		Payment selected = Payment.findById(paymentId);
		customer.currentPayment = selected;
		customer.save();
		System.out.println("setPaymentDetails():: cust currentpayment="+customer.currentPayment.id);
		
		BookOrder cart = getPendingOrder(customer);
		cart.paymentMethod = selected;
		cart.save();
		System.out.println("setPaymentDetails():: cart.paymentMethod="+cart.paymentMethod.id);
		redirect(nexturl);
	}
	
	public static void showOrderConfirmPage(String nexturl,Long customerId) {
		//WHY DO we need the nexturl????
		System.out.println("showOrderConfirmPage("+customerId+"nexturl="+nexturl+")");
		BookShopUser customer = BookShopUser.findById(customerId);
		//if no items in cart go back to index
		BookOrder cart = getPendingOrder(customer);
		if(cart.cartItems.size()==0) {
			Application.index();
		}
		if((customer.payments.size()==0) || (customer.currentPayment == null)){
			System.out.println("showOrderConfirmPage():: empty payments or no selectedpayment");
			showPaymentForm(customerId);
		}
		Address latestAddress = Address.find("bookshopuser = ? order by dateOfSubmit desc", customer).first();
		
		
		Map map = new HashMap();
		map.put("customerId", customer.id);
		String orderconfirmpage = Router.reverse("Account.showOrderConfirmPage",map).url;
		System.out.println("Account::showOrderConfirmPage():confirmpage="+orderconfirmpage);
		System.out.println("Account::showOrderConfirmPage():render()");
		render(customer,latestAddress);
	}
	
	public static void confirmOrder(Long customerId) {
		System.out.println("Account::confirmOrder():"+customerId);
		BookShopUser customer = BookShopUser.findById(customerId);
		BookOrder cart = getPendingOrder(customer);
		//cart.orderDate = new Date();//??
		cart.status=Status.CONFIRMED;
		cart.orderNumber =  OrderUtils.createOrderNumberString(customer.email,cart.orderDate);
		cart.save();
		int estimatedWeeks=Integer.parseInt(Play.configuration.getProperty("WEEKS_TO_DELIVER"));
		int estimatedDays=Integer.parseInt(Play.configuration.getProperty("DAYS_TO_DELIVER"));
		
		Date estimatedDeliveryDate = OrderUtils.incrementDate(cart.orderDate,estimatedWeeks, estimatedDays);
		//EmailUtils.sendEmail(customer,cart,false);
		render("Account/thankYou.html",customer,cart,estimatedDeliveryDate);
	}
	
	/*
	 * customer should be able to cancel a confirmed order
	 */
	public static void deleteBookOrder(Long id) {
		BookOrder order = BookOrder.findById(id);
		order.delete();
		System.out.println("Account::deleteBookOrder():order="+order+" deleted");
		Application.index();
	}
	
	private static Payment findOrCreatePayment(BookShopUser customer,String name,String creditCardNumber,String month, String year, String cctype) {
		String query = "select distinct p from  Payment p where p.bookshopuser=:bookshopuser and p.name=:name and p.creditCardNumber=:creditCardNumber and p.month=:month and p.year=:year and p.cctype=:cctype";
		Payment payment = Payment.find(query).bind("bookshopuser", customer).bind("name", name).bind("creditCardNumber",creditCardNumber).bind("month", month).bind("year",year).bind("cctype",cctype).first();
		if(payment==null) {
			payment = new Payment(name,creditCardNumber,month,year,cctype);
			payment.bookshopuser = customer;
			payment.save();
			System.out.println("findOrCreatePayment():: created new Payment");
		}
		return payment;
	}
	
	private static void gotoLastView() {
		String url = flash.get("url");
		if(url!=null) {
			System.out.println("Account::gotoLastView()::url not null");
			redirect(url);
		}else {
			System.out.println("Account::gotoLastView()::url null  ..go to /");
			Application.index();
		}
	}
	
	
	

}
