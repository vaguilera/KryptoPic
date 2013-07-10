package com.nocturns.cryptopic;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ActivityOptions extends ListActivity  {

	private static final String MODULE = "ActiviyOptions";
	public static final int DIALOG_SET_PASSWORD = 400;
	private int showPasswordDialog = 0;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
         
		String[] options = {"Change Password", "About KryptoPic"};
		
		Bundle b = getIntent().getExtras();
		if (b != null) this.showPasswordDialog = b.getInt("showpassword");

		//getListView().set(Color.rgb(230, 230, 230));
		//getListView().setBackgroundColor(Color.rgb(180, 180, 180));

		
		if (this.showPasswordDialog == 1)
		{
			Intent IAlertbox = new Intent(this,ActivityPasswordChange.class);
			IAlertbox.putExtra("text", R.string.DialogChangePassword_text);
			IAlertbox.putExtra("button", R.string.alert_button_close);
			startActivityForResult(IAlertbox,DIALOG_SET_PASSWORD);
		}
        
        // Binding resources Array to ListAdapter
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options));
	}
	
        @Override
        protected void onListItemClick(ListView l, View v, int position, long id) 
        {
        	switch (position) 
        	{
	        	case 0:
	        	{
	        		Intent IAlertbox = new Intent(this,ActivityPasswordChange.class);
					IAlertbox.putExtra("text", R.string.DialogChangePassword_text);
					IAlertbox.putExtra("button", R.string.alert_button_close);
					startActivityForResult(IAlertbox,DIALOG_SET_PASSWORD);
	        	} 
	        	break;
	        	
	        	case 1:
	        		//Context dialogContext = new ContextThemeWrapper(this, R.style.testing);
	        		//Dialog dialog = new AlertDialog.Builder(dialogContext).create();
	        		Intent IAlertbox = new Intent(this,ActivityAlertbox.class);
	    			IAlertbox.putExtra("text", R.string.about);
	    			IAlertbox.putExtra("button", R.string.alert_button_close);
	    			IAlertbox.putExtra("title", R.string.about_title);
	    			startActivity(IAlertbox);
					//new AlertDialog.Builder(dialogContext).setTitle("About").setMessage(getString(R.string.about)).setNeutralButton("Close", null).show();  
				break;
        	}
        }
        
        
        @Override
    	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
        { 
        	super.onActivityResult(requestCode, resultCode, data); 
        	switch(requestCode) {        	
	        	case DIALOG_SET_PASSWORD:        		
					if (resultCode == Activity.RESULT_OK)
					{
						String userpassword = data.getExtras().getString("password");
						byte[] InternalKey = null; 				
						try {
							InternalKey = CryptoModule.EncryptSecretKey(userpassword);
							PreferencesMgr.SavePreferences(this,"Password", InternalKey);
							new AlertDialog.Builder(this).setTitle("Password Change").setMessage("The password has been changed").setNeutralButton("Close", null).show();  
						} catch (Exception e) {
							Log.e(MODULE,"Error generating and crypting internal key");
							System.exit(0); // EMERGENCY EXIT!!!
						}						
					} else {
						new AlertDialog.Builder(this).setTitle("Password Change").setMessage("Password change canceled").setNeutralButton("Close", null).show();  
					}
	        		if (this.showPasswordDialog == 1) finish();

	        }
        }


}
