package com.hoffnungland.jKarafDigestAuth.passthrough;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeneratePasswordDigest {
	
	private static final Logger logger = LoggerFactory.getLogger(GeneratePasswordDigest.class);

	public static String buildPasswordDigest(String password, String nonce, String nonceEncodingType, String created) throws IOException, NoSuchAlgorithmException{
		logger.trace("Called");
		String digestString = null;
		
		byte[] nonceBytes = null;
		if("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary".equals(nonceEncodingType)) {
			nonceBytes = java.util.Base64.getDecoder().decode(nonce.getBytes());
		} else {
			nonceBytes = nonce.getBytes();
		}
		
		byte[] createdBytes = created.getBytes("UTF-8");
		byte[] passwordBytes = password.getBytes("UTF-8");
		try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream();){

			outputStream.write(nonceBytes);
			outputStream.write(createdBytes);
			outputStream.write(passwordBytes);
			byte[] concatenatedBytes = outputStream.toByteArray();
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			digest.update(concatenatedBytes, 0, concatenatedBytes.length);
			byte[] digestBytes = digest.digest();
			digestString = java.util.Base64.getEncoder().encodeToString(digestBytes);
		}
		logger.trace("Done");
		return digestString;
	}
}
