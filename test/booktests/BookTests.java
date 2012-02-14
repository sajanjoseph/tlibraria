package booktests;

import java.math.BigDecimal;
import java.util.Date;

import models.Author;
import models.Book;
import models.Category;
import models.Publisher;
import models.Review;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class BookTests extends UnitTest {
	@Before
	public void setUp() throws Exception {
		Fixtures.deleteDatabase();
	}

	@Test
	public void createAndFindBook() {
    	Fixtures.loadModels("data.yml");
    	Book book = Book.find("byIsbn", "978-0451160522").first();
    	assertNotNull(book);
    	assertEquals("The Gunslinger (The Dark Tower, Book 1)",book.name);
    }
	
	
	@Test(expected=javax.persistence.PersistenceException.class)
	public void isbnIsUniqueForBook() {
    	Fixtures.loadModels("data.yml");
    	Author author = new Author("impostor","chicago").save();
    	Publisher pub = new Publisher("GRA","grant","new york").save();
    	new Book("978-0451160522", "dummy", new Date(), new BigDecimal("2.50"),
    			"dummybook", author, pub).save();
    }
	
	@Test
    public void addReviews() {
    	Fixtures.loadModels("data.yml");
    	Book book1 = Book.find("byName", "The Gunslinger (The Dark Tower, Book 1)").first();
    	Book book2 = Book.find("byName", "Harry Potter and the Sorcerer's Stone (Book 1)").first();
    	assertNotNull(book1);
    	assertNotNull(book2);
    	
    	//create and add reviews
    	book1.addReview("bob","not a fast read");
    	//System.out.println("book1 reviews="+book1.reviews.size());
    	book1.addReview("smith","a promising start");
    	//System.out.println("book1 reviews="+book1.reviews.size());
    	//book1 has 2 reviews
    	assertEquals(2,book1.reviews.size());
    	
    	book2.addReview("bob", "entertains people of all ages");
    	
    	//book2 has 1 review
    	assertEquals(1,book2.reviews.size());
    	
    	//total 3 reviews
    	assertEquals(3,Review.count());
    	
    	
	}
	
	@Test
	public void testDeleteBookRemovesReviews() {
		Fixtures.loadModels("data.yml");
    	Book book1 = Book.find("byName", "The Gunslinger (The Dark Tower, Book 1)").first();
    	Book book2 = Book.find("byName", "Harry Potter and the Sorcerer's Stone (Book 1)").first();
    	assertNotNull(book1);
    	assertNotNull(book2);
    	
    	//create and add reviews
    	book1.addReview("bob","not a fast read");
    	//System.out.println("book1 reviews="+book1.reviews.size());
    	book1.addReview("smith","a promising start");
    	//System.out.println("book1 reviews="+book1.reviews.size());
    	//book1 has 2 reviews
    	assertEquals(2,book1.reviews.size());
    	
    	book2.addReview("bob", "entertains people of all ages");
		//deleting book1 deletes the 2 reviews
    	book1.delete();
    	//assertEquals(3,Book.count());
    	assertEquals(7,Book.count());
    	assertEquals(1,Review.count());
    	
    	//deleting book2 deletes the 1 review
    	book2.delete();
    	//assertEquals(2,Book.count());
    	assertEquals(6,Book.count());
    	assertEquals(0,Review.count());
		
	}
	
	@Test
	public void testBookCategories() {
		Fixtures.loadModels("data.yml");
    	Book hbook1 = Book.find("byName", "Harry Potter and the Sorcerer's Stone (Book 1)").first();
    	Book hbook2 = Book.find("byName", "Harry Potter and the Chamber of Secrets (Book 2)").first();
    	
    	Book bcbook1 = Book.find("byIsbn", "978-0312156961").first();
    	assertNotNull(hbook1);
    	assertNotNull(hbook2);
    	assertNotNull(bcbook1);
    	//3 categories
    	assertEquals(3,Category.count());
    	
    	hbook1.tagItWith("kids").tagItWith("harry potter").save();
    	hbook2.tagItWith("kids").tagItWith("harry potter").save();
    	assertEquals(2,Book.findTaggedWith("kids").size());
    	assertEquals(2,Book.findTaggedWith("harry potter").size());
    	
    	//2 categories created ,now total 5 categories
    	assertEquals(5,Category.count());
    	
    	//fiction category --> 8 books
    	assertEquals(8,Book.findTaggedWith("fiction").size());
    
    	//magic category --> 3 books
    	assertEquals(3,Book.findTaggedWith("magic").size());
    	
    	//history category --> 3 book
    	assertEquals(3,Book.findTaggedWith("history").size());
    	
    	//test the findTaggedWith(varargs)
    	assertEquals(3,Book.findTaggedWith("fiction","history").size());
    	assertEquals(0,Book.findTaggedWith("fiction","magic","history").size());
    	
	}

}
