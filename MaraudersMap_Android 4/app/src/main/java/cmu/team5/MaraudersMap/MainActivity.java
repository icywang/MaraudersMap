package cmu.team5.MaraudersMap;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import cmu.team5.MaraudersMap.ViewController.Navigation_INI;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * @author ximengw
 * Home activity, responsible for background Beacon detection / manual Beacon detection
 */
public class MainActivity extends Activity {

    private BeaconManager beaconManager;
  //  private Region region;

    private List<Beacon> beaconList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        beaconList = new ArrayList<>();

        // set up Beacon listener:
        beaconManager = new BeaconManager(this);

        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {

                beaconList = list;

                showNotification("Beacon detected!", "Please use the MaraudersMap to start indoor navigation!");

//               PendingIntent pending = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);


            }
            @Override
            public void onExitedRegion(Region region) {
                // could add an "exit" notification too if you want (-:
            }
        });

        // use full screen mode
        hideSystemUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // if already detected beacons, go to the MAP page:
        if(beaconList.size() > 0){
            Intent intent = new Intent(getApplicationContext(), Navigation_INI.class);

            intent.putExtra("destination", ""); // no destination for now

            startActivity(intent);
        }

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(new Region(
                        "monitored region",
                        UUID.fromString("8492E75F-4FD6-469D-B132-043FE94921D8"),
                        7182, 21252));
            }
        });

        /*
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
        */

    }


    // This snippet hides the system bars.
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    /**
     * For the button
     * @param view v
     */
    public void onGotoINI(View view){
        Intent intent = new Intent(this, Navigation_INI.class);
        intent.putExtra("destination", ""); // no destination for now
        startActivity(intent);
    }


    /**
     * Helper method for bringing up a notification with sound!
     * @param title msg
     * @param message msg
     */
    public void showNotification(String title, String message) {

        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[] { notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();

        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

}
