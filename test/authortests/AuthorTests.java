package authortests;

import static org.junit.Assert.*;

import java.util.List;

import models.Author;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class AuthorTests extends UnitTest{

	@Before
	public void setUp() throws Exception {
		Fixtures.deleteDatabase();
	}

	@Test
	public void testCreateAndFindAuthor() {
		new Author("stephen king","castle rock,maine").save();
		Author author = Author.getAuthor("stephen king","castle rock,maine");
		assertNotNull(author);
    	assertEquals("stephen king",author.name);
	}
	
	@Test
    public void createAndFindAuthors() {
    	new Author("stephen king","castle rock,maine").save();
    	new Author("stephen king","bedford,massachusets").save();
    	List<Author> authors = Author.getAuthorsByName("stephen king");
    	assertEquals(2,authors.size());
    }

}
