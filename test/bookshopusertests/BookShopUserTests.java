package bookshopusertests;

import static org.junit.Assert.*;

import models.BookShopUser;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class BookShopUserTests extends UnitTest {

	@Before
	public void setUp() throws Exception {
		Fixtures.deleteDatabase();
	}

	@Test
	public void testConnectAsUser() {
		Fixtures.loadModels("data.yml");
		System.out.println("connecting");
		assertNotNull(BookShopUser.connect("admin@bookshop.com","bookshopadmin"));
		System.out.println("admin ok");
		assertNotNull(BookShopUser.connect("jimmy@gmail.com","jimmy"));
		System.out.println("jimmy ok");
		assertNotNull(BookShopUser.connect("denny@gmail.com","denny"));
		System.out.println("denny ok");
		
	}

}
