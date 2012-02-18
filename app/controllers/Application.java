package controllers;

import play.*;
import play.cache.Cache;
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
        System.out.println("books="+books.size());
        render(category,book, books);
    }
    
    public static void search(Long id,String keyword) {
    	
    	render();
    }
    
    public static void addReview(Long bookId,@Required String author,@Required String content) {
    	
    	render();
    }

}
