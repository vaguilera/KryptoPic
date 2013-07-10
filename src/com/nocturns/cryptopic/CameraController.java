package com.nocturns.cryptopic;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

public class CameraController {
	private static final String MODULE = "CameraController";
	private Uri CurrentPicUri;
	private String CurrentPicFile;
	private Activity ParentActivity;
	
	public CameraController(Activity ac) {
		this.ParentActivity = ac;
	}
	
	public void ActivateCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		this.CurrentPicUri = getOutputMediaFile(); // create a file to save the image
		intent.putExtra(MediaStore.EXTRA_OUTPUT, this.CurrentPicUri); // set the image file name    		
		// Start the image capture Intent
		this.ParentActivity.startActivityForResult(intent, ActivityMain.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);		
	}
	
	public void ProcessResult(int resultCode, Intent data) {
		Log.d("MyCameraApp", "ActivityResult");
		
        if(resultCode == Activity.RESULT_OK){
        	BMPFactory bmp = new BMPFactory();
    		try {
        	bmp.newBMP(CurrentPicFile);
    		} catch (Exception e) {
    			Toast.makeText(ParentActivity, "Error creating crypted file.\n", Toast.LENGTH_LONG).show();
    			Log.e(MODULE,"Error creating BMP: " + e.toString());
    		}
        
        } 
	}
	
	// --------------------------------------------------------------------------------------------    
    private Uri getOutputMediaFile(){
    	
    	File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "KryptoPicture");
    	
    	if (! mediaStorageDir.exists()){
    		if (! mediaStorageDir.mkdirs()){
    			Log.d(MODULE, "failed to create directory");
		            return null;
		        }
    	    }
    	String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        this.CurrentPicFile = mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg"; 
        mediaFile = new File(this.CurrentPicFile);
        Log.d(MODULE, mediaFile.getAbsolutePath());
        return Uri.fromFile(mediaFile);
    }
}
