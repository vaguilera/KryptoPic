package com.nocturns.cryptopic;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;

public class ActivityMain extends Activity implements OnTouchListener  {

	private static final String MODULE = "ActiviyMain";
	
	public static final int DIALOG_FIRST_RUN = 200;
	public static final int DIALOG_FIRST_RUN_CANCEL = 300;
	public static final int DIALOG_SET_PASSWORD = 400;
	public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 500;
	public static final int DIALOG_PASSWORD = 600;
	
	private CameraController CameraCtrl;
	public static Context context;
	private byte[] LoadedPass;
	public static ProgressDialog Dialog;
	private Thread background;

	public static int resultCode;
	public static Intent data;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		ActivityMain.context = this;
		setContentView(R.layout.activity_main);
		CameraCtrl = new CameraController(this);
		
		View cameraButton = findViewById(R.id.captureImage);
		cameraButton.setOnTouchListener(this);
		View ViewPicturesButton = findViewById(R.id.ViewImages);
		ViewPicturesButton.setOnTouchListener(this);
		View OptionsButton = findViewById(R.id.Options);
		OptionsButton.setOnTouchListener(this);
		
		// Uncomment this line to clear preferences and test the app from a clean status
		//PreferencesMgr.ClearPreferences(this);
		LoadedPass = PreferencesMgr.LoadPreferences(this);

		if (LoadedPass == null)
		{
			Intent IAlertbox = new Intent(this,ActivityAlertbox.class);
			IAlertbox.putExtra("text", R.string.alert_new_user);
			IAlertbox.putExtra("button", R.string.alert_button_next);
			IAlertbox.putExtra("title", R.string.alert_title_new_user);
			startActivityForResult(IAlertbox,DIALOG_FIRST_RUN);			
		} else
		{
			Intent IAlertbox = new Intent(this,ActivityPassword.class);
			IAlertbox.putExtra("InternalPass", LoadedPass);
			startActivityForResult(IAlertbox,DIALOG_PASSWORD);			
		}
	

	}

	Handler handler = new Handler() 
	{
		  @Override
		  public void handleMessage(Message msg) {
			  ActivityMain.Dialog.dismiss();
			  new AlertDialog.Builder(ActivityMain.context).setTitle("KryptoPic").setMessage("File encrypted succesfully").setNeutralButton("Close", null).show();
		     }
	};	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);		
		ActionBar actionBar = getActionBar();		
		actionBar.setDisplayShowHomeEnabled(false); 	

		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Usually we do a switch to check menu item. In this case we just have one button
		Intent it = new Intent(this,ActivityOptions.class);
		startActivity(it);
		
		return true;
	}
		
	public boolean onTouch(View v, MotionEvent me) {
		
		ImageButton button = (ImageButton) v;
		boolean status = false;
		
		if (me.getAction() == MotionEvent.ACTION_DOWN) {
			button.setColorFilter(Color.argb(150, 155, 155, 155));

		} else if (me.getAction() == MotionEvent.ACTION_UP) {
			button.setColorFilter(Color.argb(0, 155, 155, 155)); // or null
			status = true;
		}

		switch (v.getId()) {
	    	case R.id.captureImage:
	    		if (status) CameraCtrl.ActivateCamera();
	    		return true;

	    	case R.id.ViewImages:
	    		if (status) {
		    		Intent it = new Intent(this,ActivityViewPictures.class);
		    		startActivity(it);
	    		}
	    		return true;
	    	
	    	case R.id.Options:
	    		if (status) {
	    			Intent it = new Intent(this,ActivityOptions.class);
	    			it.putExtra("showpassword", 1);
	    			startActivity(it);
	    		}
	    		return true;
    	}
		
		return false;
	}
 
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
		super.onActivityResult(requestCode, resultCode, data); 
		
		
		background =new Thread(new Runnable() 
	    {
	    	@Override
	    	public void run() {
				CameraCtrl.ProcessResult(ActivityMain.resultCode, ActivityMain.data); 
				sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory())));	
	            handler.sendMessage(handler.obtainMessage());

	    	}
	    });
		
		switch(requestCode) {
			case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
				if (resultCode == Activity.RESULT_OK) {
					ActivityMain.Dialog = new ProgressDialog(ActivityMain.context);
					ActivityMain.resultCode = resultCode;
					ActivityMain.data = data;
					ActivityMain.Dialog.setCancelable(false);
					ActivityMain.Dialog.setMessage("Encrypting image...");
					ActivityMain.Dialog.show();     
		    		  
		            background.start();

					  
				}
				break;
			
			case DIALOG_PASSWORD:	
				if (resultCode != Activity.RESULT_OK)
				{
				// Si cancela la entrada de password se presupone que quiere salir.
					finish(); 
				}
				break;
				
			case DIALOG_FIRST_RUN:
				if (resultCode == Activity.RESULT_OK)
				{
					Intent IAlertbox = new Intent(this,ActivityPasswordChange.class);
					IAlertbox.putExtra("text", R.string.DialogSetPassword_text);
					//IAlertbox.putExtra("button", R.string.alert_button_close);
					startActivityForResult(IAlertbox,DIALOG_SET_PASSWORD);
					
				} else {
					Intent IAlertbox = new Intent(this,ActivityAlertbox.class);
					IAlertbox.putExtra("text", R.string.alert_first_cancel);
					IAlertbox.putExtra("button", R.string.alert_button_close);
					IAlertbox.putExtra("title", R.string.alert_title_first_cancel);
					startActivityForResult(IAlertbox,DIALOG_FIRST_RUN_CANCEL);	
				}				
				break;
				
			case DIALOG_FIRST_RUN_CANCEL:
				finish();
				break;
			
			case DIALOG_SET_PASSWORD:
				if (resultCode == Activity.RESULT_OK)
				{
					String userpassword = data.getExtras().getString("password");
					byte[] InternalKey = null; 				
					try {
						InternalKey = CryptoModule.GenerateSecretKey(userpassword);
						PreferencesMgr.SavePreferences(context,"Password", InternalKey);
					} catch (Exception e) {
						Log.e(MODULE,"Error generating and crypting internal key");
						System.exit(0); // EMERGENCY EXIT!!!
					}						
					
				} else {
					Intent IAlertbox = new Intent(this,ActivityAlertbox.class);
					IAlertbox.putExtra("text", R.string.alert_first_cancel);
					IAlertbox.putExtra("button", R.string.alert_button_close);
					IAlertbox.putExtra("title", R.string.alert_title_first_cancel);
					startActivityForResult(IAlertbox,DIALOG_FIRST_RUN_CANCEL);	
				}				
				break;
		}
	}
	 
}
