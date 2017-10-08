package it.unisa.luca.stradaalrisparmio;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import it.unisa.luca.stradaalrisparmio.stazioni.database.DBmanager;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText to, from;
    private DBmanager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        manager = new DBmanager(this);
        manager.start();

        //Roba delle mappe
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Roba dei campi d'input
        from = (EditText) findViewById(R.id.from);
        to = (EditText) findViewById(R.id.to);

        to.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                Toast.makeText(getApplicationContext(), "Inizio la ricerca!", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng unisa = new LatLng(40.769817,14.7900013);
        //mMap.addMarker(new MarkerOptions().position(unisa).title("UniSa"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(unisa));
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                LatLngBounds view = mMap.getProjection().getVisibleRegion().latLngBounds;
                Double minLat = view.southwest.latitude, minLng = view.southwest.longitude, maxLat = view.northeast.latitude, maxLng = view.northeast.longitude;
                if(view.northeast.latitude<=view.southwest.latitude){
                    minLat = view.northeast.latitude;
                    maxLat = view.southwest.latitude;
                }
                if(view.northeast.longitude<=view.southwest.longitude){
                    minLat = view.northeast.longitude;
                    maxLat = view.southwest.longitude;
                }
                if(maxLat-minLat<=0.1 && maxLng-minLng<=0.1) {
                    manager.setPin(mMap, minLat, maxLat, minLng, maxLng);
                }
                else{
                    mMap.clear();
                }

                Log.d("test","NE:" + view.northeast.latitude + " " + view.northeast.longitude + " SO:" + view.southwest.latitude + " " + view.southwest.longitude);
            }
        });
    }
}
