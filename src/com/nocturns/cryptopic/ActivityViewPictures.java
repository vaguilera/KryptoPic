package com.nocturns.cryptopic;

import java.io.File;
import java.io.FileInputStream;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

public class ActivityViewPictures extends Activity  {
	
	private static final int SELECT_PHOTO = 200;
	public static final int DIALOG_SET_PASSWORD = 400;
	public static final int DIALOG_PASSWORD = 600;
	private static final String MODULE = "Activity ViewPictures";
	private static Context context;
	private static String OriginalFile = null;
	private static String CurrentFile = null;
	private String SentFile = null;
	private BMPFactory BMPFile;
	private boolean CurrentFileCrypted = false;
	private Menu Mainmenu;
	
	private Thread background;
	//private Thread backgroundEncode;
	public static ProgressDialog Dialog;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_pictures);
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, SELECT_PHOTO);
		ActivityViewPictures.context = this;
		ActivityViewPictures.OriginalFile = null;
		ActivityViewPictures.Dialog = new ProgressDialog(ActivityViewPictures.context);

		background =new Thread(new Runnable() 
	    {
	    	@Override
	    	public void run() {
	    		ActivityViewPictures.CurrentFile = LoadPicture(ActivityViewPictures.OriginalFile);
	            handler.sendMessage(handler.obtainMessage());

	    	}
	    });
		
		/*
		backgroundEncode =new Thread(new Runnable() 
	    {
	    	@Override
	    	public void run() {
	    		ActivityViewPictures.CurrentFile = LoadPicture(ActivityViewPictures.OriginalFile);
	            handler.sendMessage(handler.obtainMessage());

	    	}
	    });
		*/

	}
	
	Handler handler = new Handler() 
	{
		  @Override
		  public void handleMessage(Message msg) {
	    		ActivityViewPictures.Dialog.dismiss();
	    		ShowImage();
		     }
	};	

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_view_pictures, menu);
		ActionBar actionBar = getActionBar();		
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowHomeEnabled(false); 	
		
		this.Mainmenu = menu;
		return true;
	}

	private void SetMenuStatus()
	{
		int option;
		
		if (this.CurrentFileCrypted) { 
			option = R.id.action_crypt;
		} else {
			option = R.id.action_decrypt;
		}	
		
		MenuItem item = Mainmenu.findItem(option);
		if (item != null) item.setVisible(false);			
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	     super.onActivityResult(requestCode, resultCode, data);
	      
	     switch(requestCode)
	     {
	     	case SELECT_PHOTO:
		    	if (resultCode == RESULT_OK && data != null) {
		            Uri selectedImage = data.getData();
		            String[] filePathColumn = { MediaStore.Images.Media.DATA };

		            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
		            cursor.moveToFirst();

		            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		            this.OriginalFile = cursor.getString(columnIndex);
		            cursor.close();
		    		ActivityViewPictures.Dialog.setCancelable(false);
		    		ActivityViewPictures.Dialog.setMessage("Loading image...");
		    		ActivityViewPictures.Dialog.show();     
		    		  
		            background.start();

		    	 } else {
		    		 finish();
		    	 }
	    	break;
	     	
	     	case DIALOG_SET_PASSWORD:
	     		if (resultCode == RESULT_OK) {
	     			CryptToSend(data.getExtras().getString("password"));
	     		}
	     	break;
	     	
	     	case DIALOG_PASSWORD:
	     		if (resultCode == RESULT_OK) {
	     			DecryptExternalPicture(data.getExtras().getString("password"));	     				    			
	     		} else {
	     			finish(); // Password wrong go back
	     		}
	     	break;
	    	
	     }
	     
	     
	}
	
	
	public void DecryptExternalPicture(String password)
	{		
		 this.CurrentFile = CryptoModule.DecryptExternalFile(this.OriginalFile, password);
	        if (this.CurrentFile != null) {
	        	ImageView imageView = (ImageView) findViewById(R.id.imageViewer);
	        	imageView.setImageBitmap(BitmapFactory.decodeFile(this.CurrentFile));
	        	CurrentFileCrypted = true;	        	
	        }
		
	}
	
	public void CryptToSend(String password)
	{
			
			BMPFactory bmp = new BMPFactory();			
			CryptoModule.SetTemporalKey(password);
						
			try {
    			bmp.newBMP(this.CurrentFile);
    			this.SentFile = this.CurrentFile.substring(1,this.CurrentFile.length()-3)+"bmp";
    			Intent shareIntent = new Intent(Intent.ACTION_SEND);
            	shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            	shareIntent.setType("image/*");
            	shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(this.SentFile)));	        	
            	startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share_with)));
            	
    		} catch (Exception e) {
				new AlertDialog.Builder(this).setTitle("Error").setMessage("Error creating encrypted file").setNeutralButton("Close", null).show();  

    			Log.d(MODULE,"Error creating BMP: " + e.toString());
    		}
			
        	
        	CryptoModule.RestoreKeys();
			
	}
	
	@Override
	public void onBackPressed() 
	{
	   	RemoveTemporalFile(this.CurrentFile);
	   	RemoveTemporalFile(this.SentFile);
	    super.onBackPressed();
	}
	
	@Override
	public void onStop () {
		RemoveTemporalFile(this.CurrentFile);
//		RemoveTemporalFile(this.SentFile);
		super.onStop(); 
	}
	
	///////////////////////////
	private void ShowImage() 
	{		
		
        ImageView imageView = (ImageView) findViewById(R.id.imageViewer);
        
        if (this.CurrentFile != null) {
        	try {
        		ExifInterface exif = new ExifInterface(this.CurrentFile);
            	int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            	int rotate = 0;
            	switch (orientation)
            	{
    	        	case 1: rotate = 0; break;
    	        	case 3: rotate = 180; break;
    	        	case 6: rotate = 90; break;
    	        	case 8: rotate = 270; break;
    	        	default: rotate = 0;
            	}
            	
            	Matrix matrix = new Matrix();
            	matrix.postRotate(rotate);
            	Bitmap bmpImage = BitmapFactory.decodeFile(this.CurrentFile);
            	Bitmap rotatedBitmap = Bitmap.createBitmap(bmpImage, 0, 0, bmpImage.getWidth(), bmpImage.getHeight(), matrix, true);
            	imageView.setImageBitmap(rotatedBitmap);

        	} catch (Exception e) {
        		Log.e(MODULE, "Error getting EXIF information: "+e.toString());
        		return;
        	}
        	
        } else {
        	Log.e(MODULE, "Error loading picture");
        }
        
        SetMenuStatus();
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_crypt:
	        	BMPFactory bmp = new BMPFactory();
	    		try {
	    			bmp.newBMP(this.CurrentFile);
		        	Toast.makeText(this, "File encrypted", Toast.LENGTH_LONG).show();
		        	finish();
	    		} catch (Exception e) {
	    			Toast.makeText(this, "Error creating encrypted file.\n", Toast.LENGTH_LONG).show();
	    			Log.d(MODULE,"Error creating BMP: " + e.toString());
	    		}
	    		
	            return true;
	        
	        case R.id.action_decrypt:
	        	File tempBMP = new File(this.CurrentFile);
	    		File newFile = new File(this.OriginalFile+".jpg");
	    		tempBMP.renameTo(newFile);
	    		sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
	    		Toast.makeText(this, "File decrypted succesfully.\n", Toast.LENGTH_LONG).show();
				finish();
				return true;
	        
	        case R.id.action_remove:
	        	DialogYesNo();
	            return true;
	  
	        case R.id.action_share:
	        	Intent IAlertbox = new Intent(this,ActivityPasswordChange.class);
				IAlertbox.putExtra("text", R.string.DialogChangePassword_text);
				startActivityForResult(IAlertbox,DIALOG_SET_PASSWORD);			

	            return true;

	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	
// *******************************************************	
	private String LoadPicture(String file) 
	{
		FileInputStream fis = null;
		String stfos = null;
				
		try {
			fis = new FileInputStream(file); 
		} catch (Exception e) {
			Log.d(MODULE,"Can't open file: "+e.toString());
			return null;
		}
		
				
		int code;
		
		//CryptoModule cr = new CryptoModule();
		
		BMPFile = new BMPFactory();
		try {
			code = BMPFile.CheckBMP(fis, CryptoModule.sha1key);						
		} catch (Exception e) {
			Log.d(MODULE,"Error Loading file: "+e.toString());
			new AlertDialog.Builder(this).setTitle("Error").setMessage("Error loading file").setNeutralButton("Close", null).show();  
			return null;
		} 
		
		
		switch (code)
		{
		case BMPFactory.FILE_NOT_CRYPTED: // Normal file
			//Toast.makeText(this, "This file isn't encrypted", Toast.LENGTH_LONG).show();
			CurrentFileCrypted = false;
			stfos = file;
			
			break;
		case BMPFactory.SHA1_ERROR_CHECK: //SHA1 signature error check
			Log.d(MODULE, "Password don't match  ");
			Intent IAlertbox = new Intent(this,ActivityPasswordPicture.class);
			IAlertbox.putExtra("PicturePass", BMPFile.sha1);
			startActivityForResult(IAlertbox,DIALOG_PASSWORD);			
			
			CurrentFileCrypted = false;
			stfos = null;
			break;
		case BMPFactory.SHA1_VALID: // crypted file			
			try {		
			//	new ActivityLoading(context,BMPFile, fis).execute("LALLA");
				stfos = BMPFile.Decrypt(fis);
				CurrentFileCrypted = true;
			} catch (Exception e) {
				new AlertDialog.Builder(this).setTitle("Error").setMessage("Error decrypting file").setNeutralButton("Close", null).show();  
				Log.d(MODULE, "Error decrypting file:  "+e.toString());
				RemoveTemporalFile(stfos);
				stfos = null;
			}
			break;
		}
		
		return stfos;
	}
	
	private boolean RemoveCurrentFile()
	{
		try {
    		File f = new File(this.CurrentFile);
    		f.delete();
    		return true;
    	} catch (Exception e) {
    		Log.d(MODULE,"Can't delete file: " + this.CurrentFile);
    		return false;
    	}
	}
	
	private void RemoveTemporalFile(String file)
	{
		if (!CurrentFileCrypted) return;
		
		try {
    		File f = new File(file);
    		f.delete();
    	} catch (Exception e) {
    		Log.d(MODULE,"Can't delete temporal file: " + file);
    	}
	}
	
	private void DialogYesNo() 
	{
		
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
		        	   if (RemoveCurrentFile())
		        		{
		        		   new AlertDialog.Builder(ActivityViewPictures.context).setTitle("KryptoPic").setMessage("File removed succesfully").setNeutralButton("Close", null).show();
		        		} else {
			        		   new AlertDialog.Builder(ActivityViewPictures.context).setTitle("KryptoPic").setMessage("Error removing file").setNeutralButton("Close", null).show();
		        		}
		            break;

		        case DialogInterface.BUTTON_NEGATIVE:
		            // OK OK. Nothing to see here. 
		            break;
		        }
		    }
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Picture will be removed. Are you sure?").setPositiveButton("Yes", dialogClickListener)
		    .setNegativeButton("No", dialogClickListener).show();
	}
	
}
