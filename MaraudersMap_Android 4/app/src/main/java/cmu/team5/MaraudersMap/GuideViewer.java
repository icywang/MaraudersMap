package cmu.team5.MaraudersMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.UUID;

import com.nimbledevices.indoorguide.GuideManager;
import com.nimbledevices.indoorguide.GuideManagerListener;
import com.nimbledevices.indoorguide.RoutingListener;
import com.nimbledevices.indoorguide.ZoneListener;
import com.nimbledevices.indoorguide.ui.FloorPlanViewPager;
import com.nimbledevices.indoorguide.ui.OnFloorChangeListener;

import com.nimbledevices.indoorguide.ui.POIMarkerView;

import org.json.JSONException;
import org.json.JSONObject;

public class GuideViewer extends Activity implements GuideManagerListener, LocationListener, ZoneListener, RoutingListener {
    protected GuideManager guideManager;

    protected final String TAG = GuideViewer.class.getName();

    protected FloorPlanViewPager floorPlans;
    private boolean useRoutedLocation = false;

    /**
     * Things to configure for testing
     */
    protected static final String myDefaultNDD = "2b01-492-henry-18.ndd";
    protected static final String myEddystonNID = "00000000000000000062";

    protected static final String myIBeaconUUID = "8492E75F-4FD6-469D-B132-043FE94921D8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_viewer);

        floorPlans = (FloorPlanViewPager) findViewById(R.id.nd_mapview);

        final Button upButton = (Button) findViewById(R.id.up_button);
        final Button downButton = (Button) findViewById(R.id.down_button);

        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floorPlans.floorUp();
            }
        });
        downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floorPlans.floorDown();
            }
        });

        floorPlans.setOnFloorChangeListener(new OnFloorChangeListener() {

            @Override
            public void onFloorChanged(int floorIndex, double altitude) {
                downButton.setEnabled(floorIndex > 0);
                upButton.setEnabled(floorIndex < floorPlans.getFloorCount() - 1);
            }
        });

        try {
            guideManager = GuideManager.getInstance(this);
            /*
             * On android, we're currently recommending you to NOT use the sensors due
             * to many reliability issues with many different phones. While sensors should
             * increase accuracy, we've seen that on Android this tends currently not to be
             * the case.
             */
            guideManager.setCompassAllowed(false);
            guideManager.setGyroscopeAllowed(false);
            guideManager.setAccelerometerAllowed(false);
            guideManager.addGuideManagerListener(this, this);

            showNDDInputDialog();
        } catch(RuntimeException exc) {
            exc.printStackTrace();
        }
        /* Install the default Exception handler to make debugging problems easier */
        final Thread.UncaughtExceptionHandler subclass = Thread.currentThread().getUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                Log.e(TAG, "Uncaught!", paramThrowable);
                subclass.uncaughtException(paramThread, paramThrowable);
                System.exit(-1);
            }
        });
    }

    private void showNDDInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Specify NDD file name");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        final GuideViewer app = this;

        builder.setPositiveButton("Load NDD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nddName = input.getText().toString();
                if (!nddName.endsWith(".ndd"))
                    nddName = nddName + ".ndd";
                File nddTarget = new File(app.getCacheDir(), nddName);
                guideManager.setNDD("https://s3-eu-west-1.amazonaws.com/ndd/" + nddName, nddTarget, false);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                String nddName = myDefaultNDD;
                File nddTarget = new File(app.getCacheDir(), nddName);
                guideManager.setNDD("https://s3-eu-west-1.amazonaws.com/ndd/" + nddName, nddTarget, false);
            }
        });

        builder.show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.guide_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onLocationChanged(final Location location) {
        if(useRoutedLocation == false && Double.isNaN(location.getLatitude()) == false) {
            //Switch to ui thread
            runOnUiThread(new Runnable() {
                public void run() {
                    floorPlans.setUserLocation(location);
                }
            });
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.i(TAG, s);
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.i(TAG, s);
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.i(TAG, s);
    }


    @Override
    public void onZoneEntered(long zoneId, final String zoneName) {
        Log.i(TAG, "Entered "+zoneName);
        runOnUiThread(new Runnable() {
            public void run() {
                Context context = getApplicationContext();
                CharSequence text = "" + zoneName + " entered";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });
    }

    @Override
    public void onZoneExited(long zoneId,final String zoneName) {
        Log.i(TAG, "Exited "+zoneName);
        runOnUiThread(new Runnable() {
            public void run() {
                Context context = getApplicationContext();
                CharSequence text = "" + zoneName + " exited";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });
    }

    @Override
    public void onNDDLoaded() {
        Log.i(TAG, "NDD Loaded");

        floorPlans.setDataSource(guideManager);

        guideManager.requestLocationUpdates(this, this);
        guideManager.requestZoneUpdates(this, this);
        guideManager.requestRoutingUpdates(this, this);

        /* Be sure to set the iBeacon UUID only *after* the NDD has loaded */
        if(myIBeaconUUID != null)
            guideManager.setIBeaconUUID(UUID.fromString(myIBeaconUUID));
        if(myEddystonNID != null)
            guideManager.setEddystoneNamespace(myEddystonNID);


        guideManager.startRouting("interview");

        useRoutedLocation = false;

        //guideManager.startRoutingBetween("Platform 3", "Platform 12");

        //guideManager.startRoutingBetween("Library", "Kitchen");


        for(JSONObject j: guideManager.getPOIs("WC") ) {
            /* Overriding background color like in iOS example */
            try {j.put("backgroundColor", "#ff8000"); } catch (JSONException e) { e.printStackTrace(); }
            floorPlans.addMarker(new POIMarkerView(floorPlans.getContext(), j));
        }
    }

    @Override
    public void onNDDLoadFailure(Throwable e) {
        Log.e(TAG, "Error loading NDD "+e);
    }

    @Override
    public void onError(Throwable e) {
        Log.e(TAG, "Error from GuideManager "+e);

    }

    @Override
    public void onLocationOnRouteChanged(final Location location) {
        if(useRoutedLocation && Double.isNaN(location.getLatitude()) == false) {
            //Switch to ui thread
            runOnUiThread(new Runnable() {
                public void run() {
                    floorPlans.setUserLocation(location);
                }
            });
        }
    }

    @Override
    public void onStatusOnRouteChanged(int pathDirection, int distanceToNextPoint, int distanceToNextStep, int distanceToGoal) {

    }

    @Override
    public void onRouteChange(final double[][] triplets) {
        useRoutedLocation = true;
        runOnUiThread(new Runnable() {
            public void run() {
                floorPlans.setRoute(triplets);


            }
        });
    }

        @Override
    public void onRouteCalculationFailure(int error, String msg) {
        Log.e(TAG, msg);
        useRoutedLocation = false;
        runOnUiThread(new Runnable() {
            public void run() {
                floorPlans.setRoute(null);
            }
        });
    }

}
