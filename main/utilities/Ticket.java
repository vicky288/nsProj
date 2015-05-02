package main.utilities;

import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;



public class Ticket implements Serializable{
	private SealedObject sealedObject;
	private TreeMap<String, Object> unsealedMap;

	public SealedObject getSealedObject() {
		return sealedObject;
	}

	public void setSealedObject(SealedObject sealedObject) {
		this.sealedObject = sealedObject;
	}
	public TreeMap<String, Object> getUnsealedMap() {
		return unsealedMap;
	}
	public void setUnsealedMap(TreeMap<String, Object> unsealedMap) {
		this.unsealedMap = unsealedMap;
	}

	public static SealedObject sealObject(TreeMap<String, Object> unsealedMap, String key) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, IOException{
		System.out.println("Encrypting");
		final char[] password = key.toCharArray();
		final byte[] salt = "a9v5n38s".getBytes();

		// Create key
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec spec = new PBEKeySpec(password, salt, 1024, 128);
		SecretKey tmp = factory.generateSecret(spec);
		SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

		// Init ciphers
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, secret);

		// Encrypt packet
		SealedObject so = new SealedObject(unsealedMap, cipher);
		
		System.out.println("Befor encryption: "+unsealedMap.toString());
		System.out.println("After Encryption: "+so.toString());
		return so;
	}

	public static TreeMap<String, Object> unsealObject(SealedObject sealedObject, String key) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, IOException, ClassNotFoundException, BadPaddingException{
		System.out.println("Decrypting");
		final char[] password = key.toCharArray();
		final byte[] salt = "a9v5n38s".getBytes();

		// Create key
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec spec = new PBEKeySpec(password, salt, 1024, 128);
		SecretKey tmp = factory.generateSecret(spec);
		SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

		// Init ciphers
		Cipher dcipher = Cipher.getInstance("AES");
		dcipher.init(Cipher.DECRYPT_MODE, secret);

		// Decrypt packet
        TreeMap<String, Object> decryptedPacket = (TreeMap<String, Object>) sealedObject.getObject(dcipher);	
        
		System.out.println("Befor decryption: "+sealedObject.toString());
		System.out.println("After Decryption: "+decryptedPacket.toString());

		return decryptedPacket;
	}


}
