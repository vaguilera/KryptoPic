package com.nocturns.cryptopic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ActivityPassword extends Activity   {

	private static final String MODULE = "ActiviyPassword";
	private static final int MAX_ERRORS = 3;
	private static final int RES_CLOSE = 0;
	private static final int RES_OK = 1;
	
	private byte[] InternalPass = null;
	private int num_errors;
	private TextView errorText=null;

	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		
		setContentView(R.layout.dialog_password);
		Bundle b = getIntent().getExtras();
		this.InternalPass = b.getByteArray("InternalPass");
		this.setTitle(R.string.DialogPassword_title);
		this.errorText = (TextView) findViewById(R.id.textError);
		this.errorText.setVisibility(View.INVISIBLE);
		this.num_errors = 0;
		
		Button close_button = (Button) findViewById(R.id.dialog_close);
		Button ok_button = (Button) findViewById(R.id.dialog_ok);
		EditText edit = (EditText) findViewById(R.id.EditPassword);
		edit.requestFocus();
		
	
		ok_button.setOnClickListener(new OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	EditText edit = (EditText) findViewById(R.id.EditPassword);		    	
		    	if (CheckPassword(edit.getText().toString())) CloseFinish(RES_OK);
		    }
		});
		
		// Close activity
		close_button.setOnClickListener(new OnClickListener() {
		    @Override
		    public void onClick(View v) {
			    CloseFinish(RES_CLOSE);
		    }
		});

	
	
	}
	
	
	private boolean CheckPassword(String pass)
	{
		try {
    		CryptoModule.DecryptInternalKey(pass, InternalPass);
    	} catch (Exception e) {  
    		Log.d(MODULE,e.toString());
    		this.errorText.setVisibility(View.VISIBLE);
    		this.num_errors++;
    		if (this.num_errors >= MAX_ERRORS) CloseFinish(RES_CLOSE);
    		return false;
    	}
		
		return true;
		
	}
	
	private void CloseFinish(int status) {
		Intent resultIntent = new Intent();
		int res;
		
		if (status == RES_CLOSE )
		 res = Activity.RESULT_CANCELED;
		else
			res = Activity.RESULT_OK;
		
		setResult(res, resultIntent);	    	
        
		finish();

	}

	
}
