package com.dcits.app.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ThreeDesUtils {

	private static final Log LOG = LogFactory.getLog(ThreeDesUtils.class);
	private static final String Algorithm = "DESede";

	public static byte[] encryptMode(byte[] key, byte[] src) {
		SecretKey secretKey = new SecretKeySpec(key, Algorithm);
		try {
			Cipher cipher = Cipher.getInstance(Algorithm);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return cipher.doFinal(src);
		} catch (NoSuchAlgorithmException e) {
			LOG.error(e);
		} catch (NoSuchPaddingException e) {
			LOG.error(e);
		} catch (InvalidKeyException e) {
			LOG.error(e);
		} catch (IllegalBlockSizeException e) {
			LOG.error(e);
		} catch (BadPaddingException e) {
			LOG.error(e);
		}
		return null;
	}

	public static byte[] decryptMode(byte[] key, byte[] src) {
		SecretKey secretKey = new SecretKeySpec(key, Algorithm);
		try {
			Cipher cipher = Cipher.getInstance(Algorithm);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			return cipher.doFinal(src);
		} catch (NoSuchAlgorithmException e) {
			LOG.error(e);
		} catch (NoSuchPaddingException e) {
			LOG.error(e);
		} catch (InvalidKeyException e) {
			LOG.error(e);
		} catch (IllegalBlockSizeException e) {
			LOG.error(e);
		} catch (BadPaddingException e) {
			LOG.error(e);
		}
		return null;
	}

}