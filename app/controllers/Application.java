package controllers;

import play.*;
import play.cache.Cache;
import play.data.validation.Email;
import play.data.validation.Required;
import play.mvc.*;
import utils.HashUtils;
import utils.Status;

import java.util.*;

import models.*;

public class Application extends Controller {
	
	@Before
	public static void addDefaults() {
		renderArgs.put("shopLogoText",Play.configuration.getProperty("bookshop.logotext"));
	}
	
	@Before
	static void storeLoggedInUserAndCart() {
		if(Security.isConnected()) {
            BookShopUser user = BookShopUser.find("byEmail", Security.connected()).first();
            renderArgs.put("customer", user);
            System.out.println("Account:storeUserIfLoggedIn()::"+user.email+" put as customer");
            BookOrder lastorder = null;
            
            if (user.isCustomer) {
            	lastorder = getPendingOrder(user);
            
	            if(lastorder == null) {
	            	lastorder = new BookOrder(user);
	            	lastorder.save();
	            	System.out.println("Account:storeUserIfLoggedIn()::created a new order:"+lastorder.id);
	            }else {
	            	System.out.println("Account:storeUserIfLoggedIn()::existing pendOrder:"+lastorder.id +" retrieved from db");
	            }
	            renderArgs.put("cart", lastorder);
	            System.out.println("Application:storeUserIfLoggedIn()::cart with items:"+lastorder.cartItems.size()+" put as lastorder");
            }
		}
	}
	
	public static void register(@Required @Email String email,@Required  String password,@Required String passwordconfirm,@Required String fullname) {
		System.out.println("Application::register:email="+email);
		System.out.println("Application::register:password="+password);
		System.out.println("Application::register:passwordconfirm="+passwordconfirm);
		System.out.println("Application::register:fullName="+fullname);
		if(validation.hasErrors()) {
			System.out.println("Application::validation error");
			for (play.data.validation.Error err: validation.errors()) {
				System.out.println(err);
			}
			
			//System.out.println("Application::validation:email="+validation.error("email"));
			//System.out.println("Application::validation:pass="+validation.error("password"));
			//System.out.println("Application::validation:passwordconfirm="+validation.error("passwordconfirm"));
			
			validation.keep();
			showRegistrationForm();
		}
		if(!(password.trim().equals(passwordconfirm.trim()))  ){
			System.out.println("Application::passwords don't match");
			validation.addError("passwordconfirm","passwords donot match");
			
			System.out.println("Application::password equality:passwordconfirm="+validation.error("passwordconfirm"));
			validation.keep();
			showRegistrationForm();
		}else if(BookShopUser.find("byEmail", email).first() !=null){
			System.out.println("Application:register()::that email already taken");
			validation.addError("email","Email already registered with us");
			validation.keep();
			showRegistrationForm();
		}
		
		else{
			System.out.println("Application::can register");
			String hashpass= HashUtils.makeHash(password);
			BookShopUser newUser = new BookShopUser(email,hashpass,fullname);
			newUser.isCustomer=true;
			newUser.save();
			index();
		}
	}
	
	public static void showRegistrationForm() {
		render();
		
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

    public static void index() {
    	System.out.println("Application.index()");
    	Book book = null;
    	List<Book> books = Book.find("order by PublishDate desc").fetch();
    	if(books.size()>0) {
    		book = books.get(0);
    	}
    	render(books,book);
    }
    /*
     * for a given bookId show the book in full mode,
     * and show all books listing
     */
    public static void details(Long bookId) {
    	List<Book> books = Book.find("order by PublishDate desc").fetch();
    	Book book = Book.findById(bookId);
    	render(books,book);
    }
    
    /*
     * render list of books of a given category
     */
    public static void listTagged(String category) {
    	List<Book> books =null;
   		books= Book.findTaggedWith(category);
    	
    	Book book = null;
        if (books!=null && books.size()>0) {
        	book = books.get(0);
        }
        render(category,book, books);
    }
    
    public static void search(String keyword) {
    	Book book = null;
    	String trimmedKeyword =null;
    	List<Book> books = null;
    	if(keyword!=null && keyword.length()>0) {
    		trimmedKeyword = keyword.trim().toLowerCase();
        	String pattern = "%"+trimmedKeyword+"%";
        	String query="select b from Book b where (lower(name) like :pattern or lower(description) like :pattern) order by b.publishDate desc";
        	
        	books = Book.find(query).bind("pattern", pattern).fetch();
    		
    	}else {
    		books = Book.find("order by publishDate desc").fetch();
    	}
    	if(books !=null && books.size()>0) {
    		book = books.get(0);
    	}
    	render(books,book,trimmedKeyword);
    }
    
    public static void addReview(Long bookId,@Required String author,@Required String content) {
    	Book book = Book.findById(bookId);
    	if(validation.hasErrors()) {
    		params.flash();//the parameters are added to flash to display in the textfields
    		validation.keep();//keep the errors collection for the next request
    		//render("Application/details.html",book,books);
    		details(bookId);
    	}
    	book.addReview(author, content);
    	
    	flash.success("Thanks for your review, %s!", author);//success msg put in flash
    	System.out.println("add review success");
    	//render("Application/details.html",book,books);
    	details(bookId);
    }

}
