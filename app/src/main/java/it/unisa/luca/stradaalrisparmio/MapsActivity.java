package it.unisa.luca.stradaalrisparmio;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.unisa.luca.stradaalrisparmio.api.strada.DirectionFinder;
import it.unisa.luca.stradaalrisparmio.api.strada.DirectionFinderListener;
import it.unisa.luca.stradaalrisparmio.api.strada.Route;
import it.unisa.luca.stradaalrisparmio.stazioni.Distributore;
import it.unisa.luca.stradaalrisparmio.stazioni.database.DBmanager;
import it.unisa.luca.stradaalrisparmio.stazioni.database.DBmanagerListener;
import it.unisa.luca.stradaalrisparmio.support.Loading;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText to, from;
    private DBmanager manager;
    private Loading loaderView;
    private Bitmap icon;
    private ArrayList<Marker> station;
    private HashMap<Marker, Distributore> distributoriMarker;
    Double lastMinLat, lastMaxLat, lastMinLng, lastMaxLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        loaderView = Loading.getLoader(this);
        loaderView.add("Starting app...");

        manager = new DBmanager(this);
        manager.start();

        //Roba delle mappe
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        station = new ArrayList<>();
        distributoriMarker = new HashMap<>();

        //Roba dei campi d'input
        from = (EditText) findViewById(R.id.from);
        to = (EditText) findViewById(R.id.to);

        /*to.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                Toast.makeText(getApplicationContext(), "Inizio la ricerca!", Toast.LENGTH_SHORT).show();
                try {
                    new DirectionFinder(from.getText().toString(), to.getText().toString(), new DirectionFinderListener() {
                        @Override
                        public void onDirectionFinderStart() {
                            loaderView.add("Searching path");
                        }

                        @Override
                        public void onDirectionFinderSuccess(List<Route> routes) {
                            Log.d("DirectionFinderSuccess", "Success");
                            if (!routes.isEmpty()) {
                                Route r = routes.get(0);
                                mMap.addMarker(new MarkerOptions().title("Start").position(r.startLocation));
                                mMap.addMarker(new MarkerOptions().title("End").position(r.endLocation));
                                PolylineOptions plo = new PolylineOptions();
                                plo.geodesic(true);
                                plo.color(Color.BLUE);
                                plo.width(10);
                                for (int i = 0; i < r.points.size(); i++) {
                                    plo.add(r.points.get(i));
                                }
                                mMap.addPolyline(plo);
                                ArrayList<Distributore> vicini = manager.getZoneStation(r);
                                Log.d("Test", "test1");
                                Distributore economico = null;
                                Float minPrice = 10f;
                                for(Distributore d : vicini){
                                    Log.d("Test", "test1");
                                    Location a = new Location("");//provider name is unnecessary
                                    a.setLatitude(d.getLat());//your coords of course
                                    a.setLongitude(d.getLon());
                                    for(int i=0; i<r.points.size()-1; i++){
                                        Location b = new Location("");//provider name is unnecessary
                                        b.setLatitude(r.points.get(i).latitude);//your coords of course
                                        b.setLongitude(r.points.get(i).longitude);
                                        Location c = new Location("");//provider name is unnecessary
                                        c.setLatitude(r.points.get(i+1).latitude);//your coords of course
                                        c.setLongitude(r.points.get(i+1).longitude);
                                        if(a.distanceTo(b) * a.distanceTo(c) / b.distanceTo(c) > 1000){
                                            //vicini.remove(d);
                                        }else{
                                            if(minPrice>d.getDieselLowestPrice()){
                                                economico = d;
                                            }
                                            station.add(mMap.addMarker(
                                                    new MarkerOptions().title(d.getDieselLowestPrice()+"").draggable(false).visible(true).alpha(0.9f).position(d.getPosizione()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                            ));
                                        }
                                    }
                                    station.add(mMap.addMarker(
                                            new MarkerOptions().title(economico.getDieselLowestPrice()+"").draggable(false).visible(true).alpha(0.9f).position(economico.getPosizione()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                    ));
                                }

                            }
                            loaderView.remove("Searching path");
                        }
                    }).execute();
                } catch (Exception e){
                    e.printStackTrace();
                }
                return false;
            }
        });*/
        loaderView.remove("Starting app...");
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
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.769817, 14.7900013), 5.0f));
        this.icon = resizeMapIcons("pomp_icon", 120, 120);
        lastMinLat=90.0;
        lastMaxLat=-90.0;
        lastMinLng=180.0;
        lastMaxLng=-180.0;
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                setMarkersBasedOnPosition();
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                setInfoWindowBasedOnStation(distributoriMarker.get(marker));
                return false;
            }
        });
    }

    void setInfoWindowBasedOnStation(Distributore d){
        Log.d("Distributore info", d.toString());
    }

    void setMarkersBasedOnPosition(){
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


        //Elimino tutti quelli che sono usciti dallo schermo
        Double minLatC=Math.max(minLat, lastMinLat),
                maxLatC=Math.min(maxLat, lastMaxLat),
                minLngC=Math.max(minLng, lastMinLng),
                maxLngC=Math.min(maxLng, lastMaxLng);
        LatLng tempPosition;
        for(int i=0; i<station.size(); i++){
            tempPosition = station.get(i).getPosition();
            if(tempPosition.latitude<minLatC || tempPosition.latitude>maxLatC || tempPosition.longitude<minLngC || tempPosition.longitude>maxLngC){
                station.get(i).remove();
                distributoriMarker.remove(station.get(i));
                station.remove(i);
                i--;
            }
        }
        for(Marker m : station){
            tempPosition = m.getPosition();
            if(tempPosition.latitude<minLatC || tempPosition.latitude>maxLatC || tempPosition.longitude<minLngC || tempPosition.longitude>maxLngC){
                m.remove();
                distributoriMarker.remove(m);
            }
        }

        //Controllo se devo aggiungere altri marker
        ArrayList<Distributore> nuovi = new ArrayList<>();
        if(maxLat-minLat<=0.2 && maxLng-minLng<=0.2) {
            if(minLat<lastMinLat){
                nuovi.addAll(manager.getDistributoriInRange(minLat, lastMinLat, minLng, maxLng));
            }
            if(maxLat>lastMaxLat){
                nuovi.addAll(manager.getDistributoriInRange(lastMaxLat, maxLat, minLng, maxLng));
            }
            if(minLng<lastMinLng){
                Double neededMinLat = Math.max(minLat, lastMinLat);
                Double neededMaxLat = Math.min(maxLat, lastMaxLat);
                nuovi.addAll(manager.getDistributoriInRange(neededMinLat, neededMaxLat, minLng, lastMinLng));
            }
            if(maxLng>lastMaxLng){
                Double neededMinLat = Math.max(minLat, lastMinLat);
                Double neededMaxLat = Math.min(maxLat, lastMaxLat);
                nuovi.addAll(manager.getDistributoriInRange(neededMinLat, neededMaxLat, lastMaxLng, maxLng));
            }
            for(Distributore dist : nuovi){
                Marker temp = mMap.addMarker(
                        new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(this.icon)).title(dist.getId()+"").draggable(false).visible(true).alpha(0.95f).position(dist.getPosizione())
                );
                distributoriMarker.put(temp, dist);
                station.add(temp);
            }
        }

        lastMinLat=minLat;
        lastMaxLat=maxLat;
        lastMinLng=minLng;
        lastMaxLng=maxLng;
    }

    public Bitmap resizeMapIcons(String iconName, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }
}
