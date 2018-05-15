package vice.pribo.sumsumgates;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class TransitionsService extends IntentService {

    private static final String TAG = "TransitionsService";
    private static final String ACTION_TAG = "update_location";


    public TransitionsService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Intent locationUpdateIntent = new Intent(this , GeolocationService.class);
        locationUpdateIntent.setAction(ACTION_TAG);
        startService(locationUpdateIntent);

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(locationUpdateIntent);
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER){
            Toast.makeText(this, "ENTERED GEO ! ! ! ", Toast.LENGTH_SHORT).show();
        }
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL){
            Toast.makeText(this, "DWELLED ! ! ! ", Toast.LENGTH_SHORT).show();
        }
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT){
            Toast.makeText(this, "EXITED ! ! ! ", Toast.LENGTH_SHORT).show();
        }

    }
}
