package it.senato.areatesti.ebook.scriba.misc;


import it.senato.areatesti.ebook.scriba.Context;
import org.apache.commons.codec.binary.Hex;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

/**
 * Utilities for security stuffs
 */
public class SecUtils {
    /**
     * Transforms a byte sequence in an hex string
     */
    public static String getHex(byte[] raw) {
        return Hex.encodeHexString(raw);
    }

    /**
     * Gets a random byte sequences: it uses SecureRandom
     */
    public static byte[] getRandomBytes(int numBytes) {
        SecureRandom sr = null;
        try {
            // XXX: bug: on JRE in Linux the SecureRandom has performance problems
            // http://stackoverflow.com/questions/137212/how-to-solve-performance-problem-with-java-securerandom
            sr = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            Context.getInstance().getLogger().error("The secure random provider doesn't exist! This is an unrecoverab");
            System.exit(1);
        }

        sr.setSeed(sr.generateSeed(numBytes));
        byte[] bytes = new byte[numBytes];
        sr.nextBytes(bytes);
        return bytes;
    }

    /**
     * Gets a Seed
     */
    public static String getSeedAsString() {
        return Long.toString(new Date().getTime());
    }

}
