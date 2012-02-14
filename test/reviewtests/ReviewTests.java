package reviewtests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class ReviewTests extends UnitTest{

	@Before
	public void setUp() throws Exception {
		Fixtures.deleteDatabase();
	}

	@Test
	public void testCreateReviews() {
		
	}

}
