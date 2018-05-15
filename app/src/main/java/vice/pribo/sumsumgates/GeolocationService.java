package vice.pribo.sumsumgates;

import android.Manifest;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import java.util.List;
import java.util.Locale;

public class GeolocationService extends IntentService {

    FusedLocationProviderClient client;
    Location location;
    private static int UPDATE_INTERVAL = 2000;
    private static int FASTEST_INTERVAL = 2000;


    private static final String TAG = "GeolocationService";
    private static final String ACTION_TAG = "update_location";

    public GeolocationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        getLocationUpdates();
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()){
            Log.d("VICE_DEBUG" , "INTENT HAS AN ERROR !!! :(");
        } else {
            int transition = geofencingEvent.getGeofenceTransition();
            List<Geofence> geofences = geofencingEvent.getTriggeringGeofences();
            Geofence geofence = geofences.get(0);
            String requestid = geofence.getRequestId();

            if (transition == Geofence.GEOFENCE_TRANSITION_ENTER){
                Log.d("VICE" , "GEO ENTERED JUST NOW");
            } else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT){
                Log.d("VICE" , "GEO EXITED JUST NOW");
            } else if (transition == Geofence.GEOFENCE_TRANSITION_DWELL){
                Log.d("VICE" , "GEO DWELLED JUST NOW");
            }
        }
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        client = new FusedLocationProviderClient(getApplicationContext());

        if (intent != null){
            getLocationUpdates();

            if (intent.getAction() == ACTION_TAG){
                Toast.makeText(this, "Service has STARTED!", Toast.LENGTH_SHORT).show();
            }
        }

        return START_STICKY;
    }

    private void getLocationUpdates() {
        LocationRequest request = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if (!checkLocationPermission()) return;
        if (client != null) {
            client.requestLocationUpdates(request, callback, null);
        }

    }

    LocationCallback callback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            location = locationResult.getLastLocation();

            String result = String.format(Locale.getDefault() , "New Coordinates: %e , %e \n *********************"
                    , location.getLatitude() , location.getLongitude());

            Log.d("VICE DEBUG: " , result);
        }
    };

        private boolean checkLocationPermission() {
            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            } else {
                return true;
            }
        }

}
