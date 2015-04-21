package cn.iipc.id21432044;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.*;
//import com.marakana.android.yamba.*;
import com.marakana.android.yamba.clientlib.*;

public class StatusActivity extends Activity implements OnClickListener,TextWatcher {
	private static String TAG = "Active button click";
	private EditText editStatus;
	private Button buttonTweet;
	private TextView textCount;
	private TextView pkgName;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        editStatus = (EditText) findViewById(R.id.tweetContent);
        buttonTweet = (Button) findViewById(R.id.buttonTweet);
        textCount = (TextView) findViewById(R.id.textCount);
        pkgName = (TextView) findViewById(R.id.pkgName);
        
        editStatus.addTextChangedListener(this);
        buttonTweet.setOnClickListener(this);
        pkgName.setText(pkgName.getText().toString()+this.getPackageName().toString());
    }

    @Override
    public void onClick(View view) {
    	String status = "Tsinglee£º"+editStatus.getText().toString();
    	Log.d(TAG,"Onclick with status:"+status);
    	new PostTask().execute(status);
    }
    
    private final class PostTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(StatusActivity.this); //
			String username = prefs.getString("username", ""); 
			String password = prefs.getString("password", "");
			// Check that user name and password are not empty.
			// If empty, Toast a message to set login info and bounce
			// to SettingActivity.
			if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) { 
				startActivity(new Intent(StatusActivity.this, SettingsActivity.class));
				if (TextUtils.isEmpty(username)) return "Your user name is empty, Please update";
				if (TextUtils.isEmpty(password)) return "Your password is empty, Please update";
			} else {
				YambaClient yambaCloud = new YambaClient(username, password);
				try {
					yambaCloud.postStatus(params[0]);
					return "Successfully posted";
				} catch (YambaClientException e){
					e.printStackTrace();
					return "Failed to post to yama service";
				}
			}
			return "Nothing happen";
		}
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Toast.makeText(StatusActivity.this, result, Toast.LENGTH_LONG).show();
			if (result.startsWith("Successfully")) editStatus.setText("");
		}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.status, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
    	 int id = item.getItemId();
    	 if (id == R.id.action_close) {
	         finish();
	         return true;
    	 }
         return super.onOptionsItemSelected(item);
    }

	@Override
	public void afterTextChanged(Editable arg0) {
		int count = 140 - editStatus.length();
		textCount.setText(Integer.toString(count));

		textCount.setTextColor(Color.GREEN);
		if (count <= 10) textCount.setTextColor(Color.YELLOW); 
		if (count <= 0) textCount.setTextColor(Color.RED); 
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}
}
