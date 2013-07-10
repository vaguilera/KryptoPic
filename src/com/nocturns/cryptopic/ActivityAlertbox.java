package com.nocturns.cryptopic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ActivityAlertbox extends Activity   {

	private int alert_text;
	private int alert_button_text;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alertbox);
		Bundle b = getIntent().getExtras();
		this.alert_text = b.getInt("text");
		this.alert_button_text = b.getInt("button");
		this.setTitle(b.getInt("title"));
		
		Button close_button = (Button) findViewById(R.id.alert_close);
		close_button.setText(this.alert_button_text);
	
		close_button.setOnClickListener(new OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	Intent resultIntent = new Intent();
		    	setResult(Activity.RESULT_OK, resultIntent);
		    	finish();
		    }
		});
		
		TextView alertText = (TextView) findViewById(R.id.alert_text);
		alertText.setText(this.alert_text);
	}

	
}
