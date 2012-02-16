package controllers;

import play.*;
import play.data.validation.Required;
import play.mvc.*;

import java.util.*;

import models.*;

public class Application extends Controller {
	
	@Before
	public static void addDefaults() {
		renderArgs.put("shopLogoText",Play.configuration.getProperty("bookshop.logotext"));
	}

    public static void index() {
    	Book book = null;
    	List<Book> books = Book.find("order by PublishDate asc").fetch();
    	if(books.size()>0) {
    		book = books.get(0);
    	}
    	render(books,book);
    }
    
    public static void listTagged(String category) {
    	//render list of books of a category
    	
    	render();
    }
    
    public static void search(Long id,String keyword) {
    	
    	render();
    }
    
    public static void addReview(Long bookId,@Required String author,@Required String content) {
    	
    	render();
    }

}
