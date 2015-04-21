package cn.iipc.id21432044;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity {
	private TextView pkgName;
	private TextView tweetsList;
	private Cursor cursor;
	private static String TAG = "ReadDatabase";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		pkgName = (TextView) findViewById(R.id.pkgNameMain);
		pkgName.setText(pkgName.getText().toString()+this.getPackageName().toString());
		
		tweetsList = (TextView) findViewById(R.id.TweetsList);
		DbHelper dbHelper = new DbHelper(this);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		cursor = db.rawQuery(String.format("Select * from %s order by %s desc", StatusContract.TABLE, StatusContract.Column.CREATED_AT), null);
		String s = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
		while (cursor.moveToNext()){
			Date created_at = new Date(cursor.getLong(cursor.getColumnIndex(StatusContract.Column.CREATED_AT)));
			s+=String.format("%s·¢±íÓÚ%s\n%s\n\n", 
					cursor.getString(cursor.getColumnIndex(StatusContract.Column.USER)),
					sdf.format(created_at),
					cursor.getString(cursor.getColumnIndex(StatusContract.Column.MESSAGE)));
		}
		tweetsList.setText(s);
		Log.d(TAG, s);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
    	 switch (item.getItemId()) {
         case R.id.action_settings:
         startActivity(new Intent(this, SettingsActivity.class)); 
         return true; 
         case R.id.action_tweet:
         startActivity(new Intent("cn.iipc.id21432044.tweet"));
         return true;
         case R.id.action_start:
         startService(new Intent(this, UpdateService.class)); //
         return true;
 	     case R.id.action_stop:
         stopService(new Intent(this, UpdateService.class)); //
         return true;
         default:
         return super.onOptionsItemSelected(item);
         }
    }
}
