package bookshopusertests;

import static org.junit.Assert.*;

import models.BookShopUser;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.HashUtils;

public class BookShopUserTests extends UnitTest {

	@Before
	public void setUp() throws Exception {
		Fixtures.deleteDatabase();
	}

	@Test
	public void testConnectAsUser() {
		Fixtures.loadModels("data.yml");
		String hashforadmin = HashUtils.makeHash("bookshopadmin");
    	String hashfordenny = HashUtils.makeHash("denny");
    	String hashforjimmy = HashUtils.makeHash("jimmy");
    	
		assertNotNull(BookShopUser.connect("admin@bookshop.com",hashforadmin));
		assertNotNull(BookShopUser.connect("jimmy@gmail.com",hashforjimmy));
		assertNotNull(BookShopUser.connect("denny@gmail.com",hashfordenny));
		
	}

}
