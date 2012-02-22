package controllers;

import models.BookOrder;
import models.BookShopUser;
import play.Play;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import utils.Status;

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
		String query = "select o from BookOrder o where o.status=:status and o.customer =:customer order by o.order_date DESC,o.id DESC";
		 //BookOrder lastorder = BookOrder.find("select o from BookOrder o where o.status='pending' and o.customer =? order by o.order_date DESC,o.id DESC",customer).first();
		BookOrder lastorder = BookOrder.find(query).bind("status",Status.PENDING.toString()).bind("customer", customer).first();
		if(lastorder!=null) {
			lastorder.refresh();//state refreshed
		}
		return lastorder;
	}
	
	public static void dummy() {
		System.out.println("controllers.Account.dummy()");
	}
	
	

}
