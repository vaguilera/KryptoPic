package com.nocturns.cryptopic;

import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ActivityPasswordPicture extends Activity   {

	private static final String MODULE = "ActiviyPasswordPicture";
	private static final int MAX_ERRORS = 3;
	private static final int RES_CLOSE = 0;
	private static final int RES_OK = 1;
	
	private byte[] PicturePass = null;
	private int num_errors;
	private TextView errorText=null;

	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		
		setContentView(R.layout.dialog_password);
		Bundle b = getIntent().getExtras();
		this.PicturePass = b.getByteArray("PicturePass");
		this.setTitle(R.string.DialogPassword_title);
		this.errorText = (TextView) findViewById(R.id.textError);
		this.errorText.setVisibility(View.INVISIBLE);
		this.num_errors = 0;
		
		Button close_button = (Button) findViewById(R.id.dialog_close);
		Button ok_button = (Button) findViewById(R.id.dialog_ok);
		EditText edit = (EditText) findViewById(R.id.EditPassword);
		TextView Text = (TextView) findViewById(R.id.alert_text);
		Text.setText(R.string.DialogPassword2_text);
		
		edit.requestFocus();
		
	
		ok_button.setOnClickListener(new OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	EditText edit = (EditText) findViewById(R.id.EditPassword);
		    	String pass = edit.getText().toString(); 
		    	if ( !pass.equals("")) {
		    		if (CheckPassword(pass)) CloseFinish(RES_OK, edit.getText().toString());
		    	}
		    }
		});
		
		// Close activity
		close_button.setOnClickListener(new OnClickListener() {
		    @Override
		    public void onClick(View v) {
			    CloseFinish(RES_CLOSE, null);
		    }
		});

	
	
	}
	
	
	private boolean CheckPassword(String pass)
	{
   		byte[] PBKDF = CryptoModule.GetPBKDF(pass);
   		byte[] SHA1 = CryptoModule.sha1(PBKDF);
   		
   		if (Arrays.equals(SHA1,this.PicturePass)) return true; // Correct Password 
  		
		this.errorText.setVisibility(View.VISIBLE);
		this.num_errors++;
		if (this.num_errors >= MAX_ERRORS) CloseFinish(RES_CLOSE, null);
		return false;
		
		
	}
	
	private void CloseFinish(int status, String pass) {
		Intent resultIntent = new Intent();
		int res;
		
		if (status == RES_CLOSE )
		 res = Activity.RESULT_CANCELED;
		else
			res = Activity.RESULT_OK;
		
		if (pass != null) resultIntent.putExtra("password", pass );
		setResult(res, resultIntent);	    	
        
		finish();

	}

	
}
