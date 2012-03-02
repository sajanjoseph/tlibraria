package controllers;

import java.util.Date;
import java.util.List;

import models.BookOrder;
import models.BookShopUser;
import play.Play;
import play.data.validation.Required;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import utils.Status;


@With(Secure.class)
@Check("admin")
public class Admin extends Controller {
	
	@Before
	static void setConnectedUser() {
		renderArgs.put("shopLogoText", Play.configuration.getProperty("bookshop.logotext"));
        if(Security.isConnected()) {
            BookShopUser user = BookShopUser.find("byEmail", Security.connected()).first();
            if(user.isAdmin) {
            	renderArgs.put("user", user.fullName);
            }else {
            	Application.index();
            }
        }
	}

	public static void index() {
		System.out.println("Admin::index()");
		render();
	}
	
	public static void customerOrders() {
		List<BookOrder> customerOrders = BookOrder.findAll();
		render(customerOrders);
	}
	
	
	public static void showEditBookOrderForm(Long id) {
		System.out.println("showEditBookOrderForm()::id="+id);
		BookOrder bookOrder = null;
		if(id!=null) {
			bookOrder = BookOrder.findById(id);
			System.out.println("editBookOrderForm()::got order of="+id);
		}
		
		render("Admin/bookordereditform.html",bookOrder);
	}
	
	public static void editBookOrder(Long id,@Required String status) {
		System.out.println("editBookOrder()::you selected status="+status); 
		boolean statuschanged = false;
		Status newStatus = null;
		BookOrder order = BookOrder.findById(id);
		System.out.println("editBookOrder()::got order:"+order.id+" of status:"+order.status);
		if(status!=null && status.trim().length()>0 && !order.status.equals(Status.DELIVERED)) {
			try {
				//try to create new status 
				newStatus = Status.valueOf(status);
			}catch(IllegalArgumentException iae) {
				System.out.println("editBookOrder()::invalid Order Status"+status+"="+status.length());
				validation.addError("orderstatus", "Enter a valid Order Status");
				params.flash();
				validation.keep();
				showEditBookOrderForm(id);
			}
			
			
			
			if(!order.status.equals(status)) {
				statuschanged = true;
			}
			Date now = new Date();
			if(statuschanged) {
				//parse string to enum and set new status
				order.status=newStatus;
				order.orderDate = now;
				order.save();
				//send email to client
				//EmailUtils.sendEmail(order.customer, order,statuschanged);
				System.out.println("editBookOrder()::new orderstatus set");
			}
			Admin.customerOrders();
			
		}else {
			System.out.println("editBookOrder()::nothing");
			showEditBookOrderForm(id);
		}
		
	}
	
	public static void deleteBookOrder(Long id) {
		BookOrder order = BookOrder.findById(id);
		System.out.println("deleteBookOrder()::got order:"+order.getId()+" of status:"+order.status);
		order.delete();
		System.out.println("deleteBookOrder()::deleted order");
		Admin.customerOrders();
	}
	

}
