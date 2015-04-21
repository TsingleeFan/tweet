package cn.iipc.id21432044;

import java.util.List;
import com.marakana.android.yamba.clientlib.YambaClient;
import com.marakana.android.yamba.clientlib.YambaClient.Status;
import com.marakana.android.yamba.clientlib.YambaClientException;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;


public class UpdateService extends Service implements OnSharedPreferenceChangeListener {
	private static final String TAG = "UpdaterService";
	static long DELAY = 60000;
	static String username;
	static String password;
	private boolean runFlag = false;
	private Updater updater;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		DELAY = Long.parseLong(prefs.getString("interval", "60")) * 1000; 
		username = prefs.getString("username", ""); 
		password = prefs.getString("password", "");
		prefs.registerOnSharedPreferenceChangeListener(this);
		if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) { 
			if (TextUtils.isEmpty(username)) username = "student";
			if (TextUtils.isEmpty(password)) password = "password";
		}
		this.updater = new Updater();
		Log.d(TAG, "onCreate");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		this.runFlag = false;
		this.updater.interrupt();
		this.updater = null;
		Log.d(TAG, "onDestroy");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if (!runFlag){
			this.runFlag = true;
			this.updater.start();
		}
		Log.d(TAG, "onStart");
		return Service.START_STICKY;
	}

	private class Updater extends Thread {

		@Override
		public void run() {
			while (runFlag) {
				Log.d(TAG, "Running in background thread, " + DELAY / 1000);
				DbHelper dbHelper = new DbHelper(UpdateService.this);
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				int count = 0;
				try {
					long rowId = 0;
					ContentValues values = new ContentValues();
					YambaClient cloud = new YambaClient(username, password);
					List<Status> timeline = cloud.getTimeline(5);
					for (Status status : timeline) {
						values.clear();
						values.put(StatusContract.Column.ID, status.getId());
						values.put(StatusContract.Column.USER, status.getUser());
						values.put(StatusContract.Column.MESSAGE, status.getMessage());
						values.put(StatusContract.Column.CREATED_AT, status.getCreatedAt().getTime());
						rowId = db.insertWithOnConflict(StatusContract.TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
						if (rowId != -1) {
							Log.d(TAG, String.format("%s: %s", status.getUser(),status.getMessage()));
							count++;
						}
					}
					Thread.sleep(DELAY);
				} catch (YambaClientException e) {
					Log.e(TAG, "Failed to fetch the timeline.");
					e.printStackTrace();
				} catch(InterruptedException e){
					Log.e(TAG, "Interrupted.");
					runFlag = false;
				} finally {
					db.close();
					Log.d(TAG, String.format("Stored: %d messages", count));
				}
			}//while
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		Log.d(TAG, key + ":" + prefs.getString(key, ""));
		if (key.equals("interval"))
			DELAY = Long.parseLong(prefs.getString("interval", "60")) * 1000;
		if (key.equals("username"))
			username = prefs.getString("username", "");
		if (key.equals("password"))
			password = prefs.getString("password", "");
	}
	
}
