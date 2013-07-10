package com.nocturns.cryptopic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ActivityPasswordChange extends Activity   {

	private int alert_text;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		
		setContentView(R.layout.dialog_change_password);
		Bundle b = getIntent().getExtras();
		this.alert_text = b.getInt("text");
		this.setTitle(R.string.DialogChangePassword_title);
		
		Button ok_button = (Button) findViewById(R.id.alert_OK);
		Button close_button = (Button) findViewById(R.id.alert_close);
		TextView errorText = (TextView) findViewById(R.id.textError);
		errorText.setVisibility(View.INVISIBLE);
		
	
		ok_button.setOnClickListener(new OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	EditText edit1 = (EditText) findViewById(R.id.EditPassword1);
		    	EditText edit2 = (EditText) findViewById(R.id.EditPassword2);
		    	
		    	if (edit1.getText().toString().equals(edit2.getText().toString()) && (!edit1.getText().toString().equals("")))
		    	{
			    	Intent resultIntent = new Intent();
			    	resultIntent.putExtra("password", edit1.getText().toString() );
			    	setResult(Activity.RESULT_OK, resultIntent);	    	
			        finish();		    		
		    	} else {
		    		TextView errorText = (TextView) findViewById(R.id.textError);
		    		errorText.setVisibility(View.VISIBLE);    		
		    	}	    	
		    }
		});

		close_button.setOnClickListener(new OnClickListener() {
		    @Override
		    public void onClick(View v) {
			    	Intent resultIntent = new Intent();
			    	setResult(Activity.RESULT_CANCELED, resultIntent);	    	
			        finish();
		    }
		});
		
		TextView alertText = (TextView) findViewById(R.id.alert_text);
		alertText.setText(this.alert_text);
	}

	
}
