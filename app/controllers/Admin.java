package controllers;

import java.util.Date;
import java.util.List;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import models.Book;
import models.BookOrder;
import models.BookShopUser;
import models.CartItem;
import utils.Status;
import utils.Triplet;

import play.Play;
import play.data.validation.Required;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;




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
	
	public static void allPendingOrders() {
		List<BookOrder> pendingOrders = BookOrder.find("select o from BookOrder o where o.status=?  order by o.orderDate DESC,o.id DESC",Status.PENDING).fetch();
		render(pendingOrders);
	}
	
	public static void allConfirmedOrders() {
		List<BookOrder> confirmedOrders = BookOrder.find("select o from BookOrder o where o.status=?  order by o.orderDate DESC,o.id DESC",Status.CONFIRMED).fetch();
		render(confirmedOrders);
	}
	
	public static void allApprovedOrders() {
		List<BookOrder> approvedOrders = BookOrder.find("select o from BookOrder o where o.status=?  order by o.orderDate DESC,o.id DESC",Status.APPROVED).fetch();
		render(approvedOrders);
	}
	
	public static void allDeliveredOrders() {
		List<BookOrder> deliveredOrders = BookOrder.find("select o from BookOrder o where o.status=?  order by o.orderDate DESC,o.id DESC",Status.DELIVERED).fetch();
		render(deliveredOrders);
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
		System.out.println("Admin::deleteBookOrder()::got order:"+order.getId()+" of status:"+order.status);
		order.delete();
		System.out.println("Admin::deleteBookOrder()::deleted order");
		Admin.customerOrders();
	}
	
	public static void bookSalesData() {
		render();
	}
	
	public static void allBooksSold() {
		List<BookOrder> orders = BookOrder.find("select o from BookOrder o where o.status=?  order by o.orderDate DESC,o.id DESC",Status.DELIVERED).fetch();
		Triplet<BigDecimal,BigDecimal, HashMap<Book, Integer>> triplet = createBookQuantityMap(orders);
		HashMap<Book, Integer> book_QuantityMap = triplet.getThird();
		BigDecimal totalPriceOfBooks = triplet.getFirst();
		BigDecimal totalAmountFromSales = triplet.getSecond();
		System.out.println("allBooksSold()::calling renderPdf on generateReports");
		//generateReport("allBooksSold",book_QuantityMap,totalPriceOfBooks,totalAmountFromSales);
		String title = "all Books Sold";
		//String currencySymbol = play.libs.I18N.getCurrencySymbol(play.i18n.Messages.get("currencycode"));
		//System.out.println("currencycode="+play.i18n.Messages.get("currencycode"));
		//System.out.println("currencySymbol="+currencySymbol);
		render("Admin/generateReport.html",title,book_QuantityMap,totalPriceOfBooks,totalAmountFromSales);
		//play.modules.pdf.PDF.renderPDF("Admin/generateReport.html",title,book_QuantityMap,totalPriceOfBooks,totalAmountFromSales);
		
	}
	
	private static Triplet<BigDecimal,BigDecimal, HashMap<Book, Integer>> createBookQuantityMap(List<BookOrder> orders) {
		HashMap<Book,Integer> book_QuantityMap = new HashMap<Book,Integer>();
		BigDecimal totalBookPrice = BigDecimal.ZERO;
		BigDecimal totalAmountFromSales = BigDecimal.ZERO;
		for(BookOrder order: orders) {
			totalAmountFromSales = totalAmountFromSales.add(order.getTotalPricePlusTaxAndShipping());
			totalBookPrice = totalBookPrice.add(order.getTotalPrice());
			Set<CartItem> cartitems = order.cartItems;
			for(CartItem item: cartitems) {
				Book bk = item.book;
				int qty = item.quantity;
				if(book_QuantityMap.containsKey(bk)) {
					Integer oldqty =  book_QuantityMap.get(bk);
					
					book_QuantityMap.put(bk, oldqty+qty);
				}else {
					book_QuantityMap.put(bk, qty);
				}
			}
		}
		Triplet<BigDecimal,BigDecimal, HashMap<Book, Integer>> triplet = new Triplet<BigDecimal,BigDecimal, HashMap<Book, Integer>>(totalBookPrice,totalAmountFromSales,book_QuantityMap);
		return triplet;
	}
	

}
