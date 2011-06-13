package com.rob.wrek;

import java.io.IOException;
import java.net.URL;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Index extends Activity {
    MediaPlayer mp;
	WrekMeta meta;
    public final String HD_2 = "http://streaming.wrek.org:8000/wrek_HD-2";
    public final String live = "http://streaming.wrek.org:8000/wrek_live-128kb";
    public final String mono = "http://streaming.wrek.org:8000/wrek_live-24kb-mono";
    private Handler mHandler = new Handler();
    NotificationManager  mNotificationManager;
    Notification notification;
    CharSequence contentTitle = "Now Playing";
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        setupPlayer(live);
        bindControls();
        spinner();
		setup_notification();
		mHandler.postDelayed(mUpdateTimeTask, 5000);
    }
    
    private void refresh(){
    	TextView title = (TextView)findViewById(R.id.title);
    	TextView artist = (TextView)findViewById(R.id.artist);
    	try {
			meta.refreshMeta();
			title.setText(meta.getTitle());
			artist.setText(meta.getArtist());
			Intent notificationIntent = new Intent(this, Index.class);
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
			CharSequence contentText = meta.getTitle()+" - "+meta.getArtist();
			notification.setLatestEventInfo(getApplicationContext(), contentTitle, contentText, contentIntent);
			if(mp.isPlaying()){
				mNotificationManager.notify(1, notification);
			}
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "Connection Error", Toast.LENGTH_LONG);
		}
    }
    
    private void setupPlayer(String url){
        mp = new MediaPlayer();
        try {
        	URL new_url = new URL(url);
        	meta = new WrekMeta(new_url);
			mp.setDataSource(url);
			mp.prepareAsync();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(this, "Connection Error", Toast.LENGTH_LONG);
		}
    }

    private void bindControls(){
    	Button play = (Button) findViewById(R.id.play_button);
		play.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(!mp.isPlaying()){
					mp.start();
					mNotificationManager.notify(1, notification);
				}
			}
		});
		Button pause = (Button) findViewById(R.id.pause_button);
		pause.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mp.pause();
				mNotificationManager.cancelAll();
			}
		});
    }
    
    private void spinner(){
    	Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource( this, R.array.stream_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
    }
    
    public class MyOnItemSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
          mp.pause();
          String station = parent.getItemAtPosition(pos).toString();
          Toast.makeText(parent.getContext(), "Switched to " + station, Toast.LENGTH_LONG).show();
          setupPlayer("http://streaming.wrek.org:8000/"+station);
          mp.start();
          refresh();
        }

        public void onNothingSelected(AdapterView<?> parent) {
          // Do nothing.
        }
    }
    
    private Runnable mUpdateTimeTask = new Runnable() {
    	   public void run() {
    		   if(mp.isPlaying()){
    			   refresh();
    		   }
    	       mHandler.postDelayed(this, 10000);
    	   }
    	};
    
    public void setup_notification(){
    	String ns = Context.NOTIFICATION_SERVICE;
    	mNotificationManager = (NotificationManager) getSystemService(ns);
    	int icon = R.drawable.wrek_icon;
    	CharSequence tickerText = "Now Playing";
    	long when = System.currentTimeMillis();

    	notification = new Notification(icon, tickerText, when);
    	notification.flags |= Notification.FLAG_ONGOING_EVENT;
    	
    	Context context = getApplicationContext();
    	CharSequence contentText = "";
		try {
			contentText = meta.getTitle()+" - "+meta.getArtist();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	Intent notificationIntent = new Intent(this, Index.class);
    	notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

    	notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
    }
}