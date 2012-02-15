package paymenttests;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

import models.Payment;


public class PaymentTests extends UnitTest {

	@Before
	public void setUp() throws Exception {
		Fixtures.deleteDatabase();
	}
	
	@Test
	public void testRetrievePaymentByName(){
		Fixtures.loadModels("data.yml");
    	List<Payment> payments = Payment.findByName("denny");
    	assertEquals(1,payments.size());
    	assertEquals("4111111111111111",payments.get(0).creditCardNumber);
    	//Payment cc1 = payments.get(0);
    	
	}
	
	@Test(expected=javax.persistence.PersistenceException.class)
	public void testUniqueCard(){
    	Payment cc1 = new Payment("denny","4222211111111111","03","2012","Visa").save();
    	
    	Payment cc2 = new Payment("dennyj","4222211111111111","04","2011","Visa").save();
    	
	}

}
