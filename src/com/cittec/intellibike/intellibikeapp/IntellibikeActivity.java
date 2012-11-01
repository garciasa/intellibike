package com.cittec.intellibike.intellibikeapp;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.cittec.intellibike.R;
import com.cittec.intellibike.R.layout;
import com.cittec.intellibike.R.menu;
import com.cittec.intellibike.interfaces.Constants;
import com.cittec.intellibike.interfaces.GPSCallback;
import com.cittec.intellibike.managers.GPSManager;

import android.os.Bundle;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.widget.ImageView;
import android.widget.TextView;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;


public class IntellibikeActivity extends IOIOActivity implements GPSCallback{
	GPSManager gpsManager = null;
	private double speed = 0.0;
	private int speedUnitIndex = Constants.INDEX_KM;
	private AbsoluteSizeSpan sizeSpanLarge = null;
    private AbsoluteSizeSpan sizeSpanSmall = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        gpsManager = new GPSManager();
        gpsManager.startListening(getApplicationContext());
        gpsManager.setGPSCallback(this);
        
        
        //Custom Fonts
        //TODO: fonts in settings and in a function
        TextView tv =(TextView)findViewById(R.id.speed);
        Typeface font = Typeface.createFromAsset(getAssets(), "digit.ttf");
        tv.setTypeface(font);
        
        tv =(TextView)findViewById(R.id.vots);
        tv.setTypeface(font);
        
        tv =(TextView)findViewById(R.id.amps);
        tv.setTypeface(font);
        
        tv =(TextView)findViewById(R.id.porcentageBatt);
        tv.setTypeface(font);
        
        tv =(TextView)findViewById(R.id.watts);
        tv.setTypeface(font);
        
        tv =(TextView)findViewById(R.id.speedmeter);
        tv.setTypeface(font);
        
        tv =(TextView)findViewById(R.id.chronometer);
        tv.setTypeface(font);
        
        //Battery
        ImageView v = (ImageView)findViewById(R.id.battery);
        v.setImageLevel(100);
        
        //Screen always on at this moment 20%
        //TODO:
        //- brightness in settings
        //- Take a look PowerManager SCREEN_DIM_WAKE_ON
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.flags |= LayoutParams.FLAG_KEEP_SCREEN_ON;
        params.screenBrightness = 0.10f;
        getWindow().setAttributes(params);
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
  
    public void onGPSUpdate(Location location){
    	Log.d(Constants.TAG, "onGPSUpdate");
    	location.getLatitude();
        location.getLongitude();
        speed = location.getSpeed();
        
    	String speedString = "" + roundDecimal(convertSpeed(speed),1);
    	
    	
    	TextView tv = ((TextView)findViewById(R.id.speed));
    	tv.setText(speedString);
    }
    
    
    public void onGPSStatusChange(){
    	Log.d(Constants.TAG, "onGPSStatusChange");
    	
    }
    
    
    @Override
    protected void onDestroy() {
    	Log.d(Constants.TAG, "onDestroy");
    	gpsManager.stopListening();
        gpsManager.setGPSCallback(null);
            
        gpsManager = null;
            
        super.onDestroy();
    }
    
    @Override
    protected void onPause(){
    	Log.d(Constants.TAG, "onPause");
    	super.onPause();
    	gpsManager.stopListening();    	
    	
    }
    
    @Override
    protected void onStart(){
    	Log.d(Constants.TAG, "onStart");
    	super.onStart();
    	gpsManager.startListening(getApplicationContext());    		
    }
    
    @Override
    protected void onResume(){
    	Log.d(Constants.TAG, "onResume");
    	super.onResume();
    	gpsManager.startListening(getApplicationContext());
    }
    
    private double convertSpeed(double speed){
    	return ((speed * Constants.HOUR_MULTIPLIER) * Constants.UNIT_MULTIPLIERS[speedUnitIndex]);
    	
    }
    
    private double roundDecimal(double value, final int decimalPlace){
    	BigDecimal bd = new BigDecimal(value);
        
        bd = bd.setScale(decimalPlace, RoundingMode.HALF_UP);
        value = bd.doubleValue();
        
        return value;
   
    }
    
    private void showSettingsAlert(){
    	AlertDialog.Builder alertDialog = new AlertDialog.Builder(getApplicationContext());
    	 
        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");
 
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
 
        // On pressing Settings button
        alertDialog.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                getApplicationContext().startActivity(intent);
            }
        });
 
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
 
        // Showing Alert Message
        alertDialog.show();
    }
    

	/**
	 * This is the thread on which all the IOIO activity happens. It will be run
	 * every time the application is resumed and aborted when it is paused. The
	 * method setup() will be called right after a connection with the IOIO has
	 * been established (which might happen several times!). Then, loop() will
	 * be called repetitively until the IOIO gets disconnected.
	 */
	class Looper extends BaseIOIOLooper {
		
		private AnalogInput  _tempInput;

		/**
		 * Called every time a connection with IOIO has been established.
		 * Typically used to open pins.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#setup()
		 */
		@Override
		protected void setup() throws ConnectionLostException {
			_tempInput = ioio_.openAnalogInput(Constants.TMP36_PIN);
		}

		/**
		 * Called repetitively while the IOIO is connected.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
		 */
		@Override
		public void loop() throws ConnectionLostException {
			try {
				final float voltage = _tempInput.getVoltage();
				float raw = ((voltage * 1024) - 500) / 10;
				int celsius = Math.round(raw);
				setTextTemperature(celsius);				
				Thread.sleep(10);} 
			catch (InterruptedException e) {
				ioio_.disconnect();
			}
		}
	}
	
	
	/**
	 * A method to create our IOIO thread.
	 * 
	 * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
	 */
	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}
	
	private void setTextTemperature (final int temp){
		runOnUiThread(new Runnable(){
			@Override
			public void run(){
				TextView tv = ((TextView)findViewById(R.id.speedmeter));
				tv.setText(Integer.toString(temp) + " ¼C");
			}
		});
		
	}

}
