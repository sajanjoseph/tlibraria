package utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

import play.Play;

public class HashUtils {
	private static Logger logger = Logger.getLogger("tlibraria");
	private static final String HASH_ALGORITHM = Play.configuration.getProperty("MD_ALGORITHM");
	private static final int MD_RADIX = Integer.valueOf(Play.configuration.getProperty("MD_RADIX")).intValue();
	
	/*
	 * create a hash of the given plain string
	 * The plain string cannot be recovered from the hash
	 * Two plain strings may not have the same hash
	 */
	public static String makeHash(String plain) {
		String hash = null;
		try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            
            md.update(plain.getBytes());
            BigInteger hashint = new BigInteger(1, md.digest());
            hash = hashint.toString(MD_RADIX);
        } catch (NoSuchAlgorithmException nsae) {
            logger.error(nsae);
        }
		return hash;
	}

}
