package com.nocturns.cryptopic;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;

import android.os.Environment;
import android.util.Log;

public class BMPFactory {
	private static final String MODULE = "BMPFactory";
	public static final int FILE_NOT_CRYPTED = 0;
	public static final int SHA1_ERROR_CHECK = 1;
	public static final int SHA1_VALID = 2;
	
	
	byte[] DataHeader;
	byte VERSION = 1;
	String BMPFile;
	byte[] sha1;

			
	// Creates a crypted BMP from a specified original file
	public void newBMP(String stfile)  throws Exception {
		
		DataHeader = new byte[80];
		FileInputStream fis = null;
		FileOutputStream fos = null;
		
		BMPFile = stfile + ".bmp";		
		File file = new File(stfile);
		int size = (int)Math.sqrt(file.length()/3)-1;
				
		Log.d(MODULE,"size: " + file.length());
		
		
		CryptoModule cr = new CryptoModule();
		
		try {
			fis = new FileInputStream(stfile);
			fos = new FileOutputStream(BMPFile);
		} catch (Exception e) {
			Log.d(MODULE,"ERROR Opening files: " + e.toString());
			return;
		}
		
		try { 
			saveFileHeader();
			saveInfoHeader(size,size);
			saveCryptHeader(cr);
			fos.write(DataHeader); 
			cr.Encrypt(fis,fos);
		} catch (Exception e) {
			Log.e(MODULE,"ERROR Encripting process: " + e.toString());
			return;
		} finally {
			fis.close();
			fos.close();
		}			

		try {
		file.delete();
		File tempBMP = new File(BMPFile);
		File newFile = new File(stfile.substring(1,stfile.length()-3)+"bmp");
		tempBMP.renameTo(newFile);
		Log.d(MODULE,"size: " + newFile.length());
		} catch (Exception e) {
			Log.d(MODULE,"Error renaming file");
		}
				
		
	}
	
	public int CheckBMP(FileInputStream fis, byte[] sha1User) throws Exception
	{
		if (fis == null) {
			throw new NullPointerException("No FileInputStream provided");
		}
		if (sha1User == null) {
			return SHA1_ERROR_CHECK; // If no md5User provided for some reason, act like checksum error
		}
		
		DataHeader= new byte[80];
		byte[] stub = {'k','r', 'y', 'p','t', VERSION };
		byte[] stubfile;
		
		
		fis.read(DataHeader);
		stubfile = Arrays.copyOfRange(DataHeader, 54, 60);
				
		if (!Arrays.equals(stub, stubfile)) {
			this.sha1 = null;
			return FILE_NOT_CRYPTED; 
		}
		this.sha1 = Arrays.copyOfRange(DataHeader, 60, 80);
		
		if (!Arrays.equals(this.sha1, sha1User)) {			
			return SHA1_ERROR_CHECK; // MD5 doesn't fit
		}
				
		
		return SHA1_VALID;
	}
	
	public String Decrypt(FileInputStream fis) throws Exception
	{
		CryptoModule cr = new CryptoModule();
		FileOutputStream fos = null;
		String stfos = getTempFile();
		
		try {			
			fos = new FileOutputStream(stfos);
		} catch (Exception e) {
			Log.d(MODULE,"ERROR Opening temporal file: " + e.toString());
			return null;
		}
		
		cr.Decrypt(fis,fos);
		fis.close();
		fos.close();
		return stfos;
	}
	
	private void saveFileHeader() { 
		byte[] a; 
		DataHeader[0]=66;  
		DataHeader[1]=77; 
		a= intToFourBytes(DataHeader.length); 
		DataHeader[5]=a[0]; 
		DataHeader[4]=a[1]; 
		DataHeader[3]=a[2]; 
		DataHeader[2]=a[3]; //DataHeader offset 
		DataHeader[10]=0x36; 
	}
	
	private void saveInfoHeader(int height, int width) { 
		DataHeader[14]=0x28; 
		byte[]a=intToFourBytes(width); 
		DataHeader[22]=a[3]; 
		DataHeader[23]=a[2]; 
		DataHeader[24]=a[1]; 
		DataHeader[25]=a[0]; 
		a=intToFourBytes(height); 
		DataHeader[18]=a[3]; 
		DataHeader[19]=a[2]; 
		DataHeader[20]=a[1]; 
		DataHeader[21]=a[0]; 
		DataHeader[26]=1; 
		DataHeader[28]=0x18; 
	}
	
	private void saveCryptHeader(CryptoModule cr) {
		DataHeader[54]='k';
		DataHeader[55]='r';
		DataHeader[56]='y';
		DataHeader[57]='p';
		DataHeader[58]='t';
		DataHeader[59]=VERSION;
		for (int i=0;i<cr.sha1key.length;i++)
		{
			DataHeader[60+i] = cr.sha1key[i];
		}
				
	}
	
	private byte[] intToFourBytes(int x) { 
		byte [] array = new byte[4]; 
		array[3]=(byte) x; 
		array[2]=(byte) (x>>8); 
		array[1]=(byte) (x>>16); 
		array[0]=(byte) (x>>24); 
		return array; 
		} 

	// --------------------------------------------------------------------------------------------    
    public static String getTempFile(){
    	
    	File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "KryptoPicture");
    	
    	if (! mediaStorageDir.exists()){
    		if (! mediaStorageDir.mkdirs()){
    			Log.d(MODULE, "failed to create directory");
		        return null;
    		}
    	}
    	
        return mediaStorageDir.getPath() + File.separator + "temp.jpg"; 
    }	
	
	
}
