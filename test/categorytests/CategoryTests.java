package categorytests;

import static org.junit.Assert.*;

import models.Category;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class CategoryTests extends UnitTest {

	@Before
	public void setUp() throws Exception {
		Fixtures.deleteDatabase();
	}

	@Test
	public void testCreateCategory() {
		Fixtures.loadModels("data.yml");
		Category javacat = Category.findOrCreateByName("java");
		assertNotNull(javacat);
		assertEquals("java",javacat.name);
	}
	
	

}
