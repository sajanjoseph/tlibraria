package functionaltests;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;

import models.Book;

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

}
