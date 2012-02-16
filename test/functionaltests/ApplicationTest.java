package functionaltests;

import org.junit.Before;
import org.junit.Test;

import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;

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
	
	

}
