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
