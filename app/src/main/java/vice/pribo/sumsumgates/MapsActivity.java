package vice.pribo.sumsumgates;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //Variables
    private GoogleMap myMap;
    private LatLng latLng;
    private Marker myMarker;
    private Circle myCircle;
    private Location lastKnownLocation;
    FusedLocationProviderClient client;
    private Boolean didEntered = false;
    private Boolean didDwelled = false;
    private Boolean didExited = false;
    GoogleApiClient googleApiClient = null;



    private static int UPDATE_INTERVAL = 2000;
    private static int FASTEST_INTERVAL = 2000;

    FloatingActionButton sumsumBtn;


    /**First Gate to test**/
    LatLng nofim_main_gate = new LatLng(32.149599, 35.108157);




    public static final int MY_PERMISSION_REQUEST_CODE = 27390;
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 300193;


    DatabaseReference ref;
    GeoFire geoFire;

    /*******************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        overridePendingTransition(R.anim.fade_in , R.anim.fade_out);

        ref = FirebaseDatabase.getInstance().getReference("My GeoLocation");
        geoFire = new GeoFire(ref);
        client = new FusedLocationProviderClient(this);

        sumsumBtn = findViewById(R.id.sumsumFab);
        sumsumBtn.setImageResource(R.drawable.sumsumiconwhite);
        sumsumBtn.setBackgroundColor(Color.argb(100, 0, 188, 212));
        sumsumBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeCall();
            }
        });
        SupportMapFragment mapFragment = new SupportMapFragment();

        getSupportFragmentManager().beginTransaction().
                replace(R.id.frame1, mapFragment).
                commit();

        client = new FusedLocationProviderClient(getApplicationContext());

        mapFragment.getMapAsync(this);
        checkCallPermission();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;

        setMyLocation(myMap);
        setupMap(myMap);

        /** Gate Data Test **/
//        LatLng nofim_main_gate = new LatLng(32.149599, 35.108157);
//        myMap.addCircle(new CircleOptions().center(nofim_main_gate)
//                .radius(250).strokeColor(Color.YELLOW).fillColor(Color.CYAN).strokeWidth(3.0f));


    }

    @Override
    protected void onResume() {
        super.onResume();
        getLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getLocationUpdates();
        setupMap(myMap);

    }


    private void sendNotification(String title, String content) {
        Notification.Builder builder = new Notification.Builder(this).
                setSmallIcon(R.drawable.sumsumicon)
                .setContentTitle(title)
                .setContentText(content);

        NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, MapsActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        manager.notify(new Random().nextInt(), notification);

    }

    private Intent getNotificationIntent() {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }



    private void showPendingNotification(){

        Intent callIntent = getNotificationIntent();
        callIntent.setAction("Open_Gate");

        Intent cancelIntent = getNotificationIntent();
        cancelIntent.setAction("Suspend_App");

        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this);
        nBuilder.setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(this, 0, getNotificationIntent(), PendingIntent.FLAG_UPDATE_CURRENT))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.sumsumicon)
                .setColorized(true)
                .addAction(new NotificationCompat.Action(
                        R.drawable.sumsumiconwhite,
                        "SumSum",
                        PendingIntent.getActivity(this, 0, callIntent, PendingIntent.FLAG_UPDATE_CURRENT)))
                .addAction(new NotificationCompat.Action(
                        R.drawable.sumsumicon,
                        "Later",
                        PendingIntent.getActivity(this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)))
                .setColor(Color.argb(100, 0, 188, 212))
                .setTicker("Still need me?")
                .setContentTitle("Need to open the gate?")
                .setContentText("SumSum or not.. :)");

        NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, nBuilder.build());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        processIntentAction(intent);
        super.onNewIntent(intent);
    }

    private void processIntentAction(Intent intent) {
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case "Open_Gate":
                    makeCall();
                    break;
                case "Suspend_App":
                    break;

            }
        }
    }

    private void setMyLocation(GoogleMap map) {
        if (checkLocationPermission()) {
            map.setMyLocationEnabled(true);
        }
    }

    private void setupMap(GoogleMap map) {


        myMap = map;

        checkLocationPermission();

        getLocationUpdates();

        if (lastKnownLocation == null) {
            latLng = new LatLng(32.15486, 35.10107);
        } else {
            latLng = new LatLng(lastKnownLocation.getAltitude(), lastKnownLocation.getLongitude());


        }


        myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

        /* The Marker of the Gate */

            final MarkerOptions customMarkerOptions = new MarkerOptions()
                    .position(nofim_main_gate).title("My Gate")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                    .draggable(true);

        if (myMarker == null) {
            myMarker = map.addMarker(customMarkerOptions);
        }

        //TODO: Change value of Radius to chosen by the user
        final CircleOptions circleOptions = new CircleOptions()
                .center(myMarker.getPosition())
                .radius(250)
                .strokeColor(Color.YELLOW)
                .fillColor(Color.argb(100, 0, 188, 212))
                .strokeWidth(8)
                .clickable(true);

        myCircle = myMap.addCircle(circleOptions);

        myMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {
                myCircle.setCenter(marker.getPosition());
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                Toast.makeText(MapsActivity.this, "Your Gate is ready!", Toast.LENGTH_SHORT).show();
            }
        });

        myMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (myMarker == null) {
                    myMarker = myMap.addMarker(customMarkerOptions.position(latLng));
                    myCircle = myMap.addCircle(circleOptions.center(myMarker.getPosition()));
                    myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                } else {
                    Toast.makeText(MapsActivity.this, "Gate already exists. Long click on map to clear", Toast.LENGTH_SHORT).show();
                }

            }
        });
        myMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                myMap.clear();
                myMarker = null;
                myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                Toast.makeText(MapsActivity.this, "Map Cleared. Click on to the map to create a gate", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void getLocationUpdates() {
        LocationRequest request = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        //request.setExpirationDuration(60 * 60 * 1000); //stop after one hour


        if (!checkLocationPermission()) return;
        if (client != null) {
            client.requestLocationUpdates(request, callback /*Callback*/, null /*Looper*/);
        }


    }

    /**Location Lilstener Methods**/

    LocationCallback callback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {


            lastKnownLocation = locationResult.getLastLocation();
            geoFire.setLocation("You", new GeoLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));

            String result = String.format(Locale.getDefault(), "(%e , %e) \n Speed: %e\n Time: %d\n",
                    lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), lastKnownLocation.getSpeed(), lastKnownLocation.getTime());

            if (myMarker != null) {
                LatLng newCoordinate = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newCoordinate, 16));

                /** GeoQuery Callbacks **/
                //Toast.makeText(MapsActivity.this, result, Toast.LENGTH_SHORT).show();

                //Make GeoQuery Based on the marker position

                GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(myMarker.getPosition().latitude, myMarker.getPosition().longitude), 0.25f);
                geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(String key, GeoLocation location) {
                        if (!didEntered) {
                            //sendNotification("SumSum", String.format("%s entered the Nofim area", key));
                            //Toast.makeText(MapsActivity.this, "Entered!", Toast.LENGTH_SHORT).show();
                            makeCall();
                            didEntered = true;
                            didDwelled = false;
                            didExited = false;
                        }
                    }

                    @Override
                    public void onKeyExited(String key) {
                        //sendNotification("SumSum", String.format("%s are no longer in the gate area. Bye :)", key));

                        if (!didExited){
                            Toast.makeText(MapsActivity.this, "Have a nice Ride!", Toast.LENGTH_SHORT).show();
                        }
                        didEntered = false;
                        didDwelled = false;
                        didExited = true;
                    }

                    @Override
                    public void onKeyMoved(String key, GeoLocation location) {
                        //sendNotification("SumSum", String.format("%s moved within the gate area", key));
                        //Toast.makeText(MapsActivity.this, "Still here?! !", Toast.LENGTH_SHORT).show();
                        if (!didDwelled) {
                            showPendingNotification();
                        }
                        didDwelled = true;
                        didEntered = true;
                    }

                    @Override
                    public void onGeoQueryReady() {

                    }

                    @Override
                    public void onGeoQueryError(DatabaseError error) {
                        Toast.makeText(MapsActivity.this, "GEOFENCE ERROR!", Toast.LENGTH_SHORT).show();
                    }
                });


            }

        }
    };

    private void makeCall() {
        checkCallPermission();
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:0543582460"));

        if (myMarker != null) {
            startActivity(callIntent);
        } else {
            Toast.makeText(MapsActivity.this, "Please Create a gate by clicking on the MAP", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissions();

            return false;
        } else {
            return true;
        }
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    private void checkCallPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1 );
        }
    }

    public void stopLocationUpdates(){
        client.removeLocationUpdates(callback);
    }

    public void startGeofenceMonitoring(){
        Geofence geofence = new Geofence.Builder()
                .setRequestId("My Geofence")
                .setCircularRegion(32.149599 , 35.108157 , 250)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setNotificationResponsiveness(1000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
                .addGeofence(geofence).build();

        Intent serviceIntent = new Intent(this , GeolocationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this , 0 , serviceIntent , PendingIntent.FLAG_CANCEL_CURRENT);

        if (!googleApiClient.isConnected() || (googleApiClient == null)) {
            Toast.makeText(this, "Google API is Not connected Yet", Toast.LENGTH_SHORT).show();

        } else {
            checkLocationPermission();
            LocationServices.GeofencingApi.addGeofences(googleApiClient, geofencingRequest , pendingIntent)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                Toast.makeText(MapsActivity.this, "GEOFENCE ADDED SECCESFFULY", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MapsActivity.this, "GEOFENCE Did Not add :(" , Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

    private void buildApiClient(){
        /*Creating GoogleApi Client for Location Services*/
        Toast.makeText(this, "Building Api.....", Toast.LENGTH_SHORT).show();
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Toast.makeText(MapsActivity.this, "GOOGLE API IS CONNECTED", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Toast.makeText(MapsActivity.this, "GOOGLE API SUSPENDED CONNECTION", Toast.LENGTH_SHORT).show();
                    }
                }).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(MapsActivity.this, "GOOGLE API CONNECTION FAILED" + connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).build();
    }


}
