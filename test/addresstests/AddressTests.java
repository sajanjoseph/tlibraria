package addresstests;

import static org.junit.Assert.*;

import models.Address;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class AddressTests extends UnitTest {

	@Before
	public void setUp() throws Exception {
		Fixtures.deleteDatabase();
	}

	@Test
	public void testAddressEquality() {
		Address address1 = new Address();
		address1.addressLine1 = "#1401,Brigade Millennium";
		address1.state = "Karnataka";
		address1.country = "India";
		
		Address address2 = new Address();
		address2.addressLine1 = "#1401,Brigade Millennium";
		address2.state = "Karnataka";
		address2.country = "India";
		
		Address address3 = new Address();
		address3.addressLine1 = "#1401,Brigade Millennium";
		address3.addressLine2 = "j.p nagar,bangalore";
		address3.state = "Karnataka";
		address3.country = "India";
		
		Address address4 = new Address();
		address4.addressLine1 = "#1401,Brigade Millennium,j.p nagar,bangalore";
		address4.state = "Karnataka";
		address4.country = "India";
		
		assertTrue(address1.equals(address2));
		assertFalse(address1.equals(address3));
		assertFalse(address3.equals(address4));//need to  refactor 
	}
	
	

}
