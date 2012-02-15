package utilstests;

import static org.junit.Assert.*;

import org.junit.Test;

import play.test.UnitTest;

import utils.HashUtils;

public class HashTests extends UnitTest{
	
	

	@Test
	public void testHash() {
		//assumes sha1 with radix 16
		String name1 = "libraria";
		String hash1 = "4ce989d01c3f83a724587fb15e1929168a1cc5d4";
		
		assertEquals(hash1,HashUtils.makeHash(name1));
	}

}
