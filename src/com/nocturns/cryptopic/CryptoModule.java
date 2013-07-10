package com.nocturns.cryptopic;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;
import java.security.MessageDigest;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;


public class CryptoModule {

	public static SecretKey InternalKey;	
	public static byte[] sha1key;		
	public static SecretKey TemporalKey;	
	public static byte[] Temporalsha1key;		

	public static byte[] salt = "fH3d6_S2".getBytes();
	
	
	private static final String MODULE = "CryptoModule";
	
		
	public static byte[] GenerateSecretKey(String password) throws Exception
	{
		KeyGenerator key;
		
		key = KeyGenerator.getInstance("AES");
		key.init(128); // AES Size
		InternalKey = key.generateKey();
			
		byte [] CurrentKey = CryptoModule.GetPBKDF(password);
		
		CryptoModule.InternalKey = new SecretKeySpec(CurrentKey,0,CurrentKey.length, "AES");
		CryptoModule.sha1key = CryptoModule.sha1(InternalKey.getEncoded());
		
		return EncryptSecretKey(password);
				
	}

	// Stupid one-line-function just to skip GenerateSecretKey step in case you only want to Reencrypt the password
	public static byte[] EncryptSecretKey(String password) throws Exception
	{

		return CryptInternalKey(GetPBKDF(password));
		
	}

	public static String DecryptExternalFile(String file, String password)
	{
		FileInputStream fis = null;
		String stfos = null;
				
		try {
			fis = new FileInputStream(file);
			fis.skip(80); // Skip header
		} catch (Exception e) {
			Log.d(MODULE,"Error reading file: "+e.toString());
			return null;
		}
		
		CryptoModule cr = new CryptoModule();
		FileOutputStream fos = null;
		stfos = BMPFactory.getTempFile();
		
		try {			
			fos = new FileOutputStream(stfos);
		} catch (Exception e) {
			Log.d(MODULE,"ERROR Opening temporal file: " + e.toString());
			return null;
		}
		
		CryptoModule.SetTemporalKey(password);
		try {
		cr.Decrypt(fis,fos);
		fis.close();
		fos.close();
		} catch (Exception e) {
			Log.d(MODULE,"ERROR decrypting file: " + e.toString());
			CryptoModule.RestoreKeys();
			return null;
		}
		CryptoModule.RestoreKeys();
		return stfos;
		
		
	}
	
	public static void SetTemporalKey(String newkey )
	{
		CryptoModule.TemporalKey = CryptoModule.InternalKey;
		CryptoModule.Temporalsha1key = CryptoModule.sha1key;
		
		byte [] CurrentKey = CryptoModule.GetPBKDF(newkey);
		
		CryptoModule.InternalKey = new SecretKeySpec(CurrentKey,0,CurrentKey.length, "AES");
		CryptoModule.sha1key = CryptoModule.sha1(InternalKey.getEncoded());
				
	}
	
	public static void RestoreKeys() 
	{
		CryptoModule.InternalKey = CryptoModule.TemporalKey;
		CryptoModule.sha1key = CryptoModule.Temporalsha1key;
		
		CryptoModule.TemporalKey = null;
		CryptoModule.Temporalsha1key = null;
	}
	
	// Encriptar la internal key con el password del usuario
	public static byte[] CryptInternalKey(byte[] Key) throws Exception
	{		
		SecretKeySpec CryptKey = new SecretKeySpec(Key, "AES");
		Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding"); 
		c.init(Cipher.ENCRYPT_MODE, CryptKey);
		byte[] encrypted = c.doFinal(InternalKey.getEncoded());
		
		return encrypted;
	
		
	}

	// DesEncriptar la internal key con el password del usuario
	public static void DecryptInternalKey(String UserKey, byte[] InternalCrypted) throws Exception
	{				
		byte[] Key = GetPBKDF(UserKey);
		SecretKeySpec CryptKey = new SecretKeySpec(Key, "AES");
		Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding"); 
		c.init(Cipher.DECRYPT_MODE, CryptKey);
		byte[] decrypted = c.doFinal(InternalCrypted);
		
		CryptoModule.InternalKey = new SecretKeySpec(decrypted,0,decrypted.length, "AES");
		CryptoModule.sha1key = CryptoModule.sha1(InternalKey.getEncoded());
	
		
	}

	
	public void Encrypt(FileInputStream input, FileOutputStream output) throws Exception
	{
		SecretKeySpec sInternalKey = new SecretKeySpec(InternalKey.getEncoded(), "AES");
		Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding"); 
		c.init(Cipher.ENCRYPT_MODE, sInternalKey); 
		
		try	{
			crypt(input,output,c);
	    	Log.d(MODULE,"File crypted...");  		
		} catch (Exception e) {
			Log.e(MODULE,"Error crypting: "+e.toString());
		} 
		
	}

	public void Decrypt(FileInputStream input, FileOutputStream output) throws Exception
	{
		SecretKeySpec sInternalKey;
		sInternalKey = new SecretKeySpec(InternalKey.getEncoded(), "AES");
		Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding"); 
		c.init(Cipher.DECRYPT_MODE, sInternalKey); 
		
		crypt(input,output,c);
    	Log.d(MODULE,"File Decrypted...");  		
		
	}
	
	 private void crypt(FileInputStream in, FileOutputStream out, Cipher cipher) throws Exception
	 {
		int blockSize = cipher.getBlockSize();
		int outputSize = cipher.getOutputSize(blockSize);
		byte[] inBytes = new byte[blockSize];
		byte[] outBytes = new byte[outputSize];
		int inLength = 0;
		boolean more = true;
		
		while (more) 
		{
			inLength = in.read(inBytes);
			if (inLength == blockSize) 
			{
				int outLength = cipher.update(inBytes, 0, blockSize, outBytes);
				out.write(outBytes, 0, outLength);
			} else {
				more = false;
			}
		}
		if (inLength > 0) {
			outBytes = cipher.doFinal(inBytes, 0, inLength);
		} else {
			outBytes = cipher.doFinal();
		}
			out.write(outBytes);
	 }
	 
	 

	
	public static byte[] sha1(byte[] data) 
	{
	    try {
	        // Create MD5 Hash
	        MessageDigest digest = java.security.MessageDigest.getInstance("SHA-1");
	        digest.update(data);
	        byte messageDigest[] = digest.digest();
	
	      
	        // Create Hex String
	        StringBuffer hexString = new StringBuffer();
	        for (int i=0; i<messageDigest.length; i++) 
	        {
	            hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
	        }
	        Log.d(MODULE,hexString.toString());
	      
	        return messageDigest;
	
	    } catch (Exception e) {
	    	Log.d(MODULE,"Error in SHA1 Func: "+e.toString());
	    }	    
	    return null;
	}
	
	public static byte[] GetPBKDF(String password)
	{
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			KeySpec keyspec = new PBEKeySpec(password.toCharArray(), salt, 1000, 128);
			Key key = factory.generateSecret(keyspec);
			return key.getEncoded();
		} catch (Exception e) {
			Log.d(MODULE,"Error in GetPBKDF Func: "+e.toString());
		}
		
		return null;
		
	}
	
	
}
