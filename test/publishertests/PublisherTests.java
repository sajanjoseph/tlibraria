package publishertests;

import static org.junit.Assert.*;

import models.Publisher;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class PublisherTests extends UnitTest  {

	@Before
	public void setUp() throws Exception {
		Fixtures.deleteDatabase();
	}

	@Test
	public void testCreateAndFindPublisher() {
		new Publisher("AMU","amazon","chicago").save();
		assertNotNull(Publisher.getPublisherByCode("AMU"));
		assertNull(Publisher.getPublisherByCode("AMZ"));
	}
	
	 @Test
    public void testCreateAndFindPublishers() {
    	new Publisher("AMU","amazon","chicago").save();
    	new Publisher("AMB","amazon","london").save();
    	assertEquals(2,Publisher.getPublishersByName("amazon").size());
    }
	
	@Test(expected=javax.persistence.PersistenceException.class)
	public void publisherCodeIsUnique() {
		new Publisher("GRA","grant","new york").save();
		new Publisher("GRA","grantone","new york").save();
	}
	
	

}
