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
		System.out.println("address1="+address1);
		
		Address address2 = new Address();
		address2.addressLine1 = "#1401,Brigade Millennium";
		address2.state = "Karnataka";
		address2.country = "India";
		System.out.println("address2="+address2);
		
		assertEquals(address1,address2);
		
	}

}
