package it.unisa.luca.stradaalrisparmio;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unisa.luca.stradaalrisparmio.api.strada.DirectionFinder;
import it.unisa.luca.stradaalrisparmio.api.strada.DirectionFinderListener;
import it.unisa.luca.stradaalrisparmio.api.strada.Route;
import it.unisa.luca.stradaalrisparmio.stazioni.Distributore;
import it.unisa.luca.stradaalrisparmio.stazioni.database.DBmanager;
import it.unisa.luca.stradaalrisparmio.support.BitmapCreator;
import it.unisa.luca.stradaalrisparmio.support.LoadingShow;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static Double SCREEN_DIMENSION_FOR_DATA = 0.2;

    private GoogleMap mMap;
    private EditText to, from;
    private DBmanager manager;
    private LoadingShow loaderView;
    private Bitmap icon;
    private volatile HashMap<Marker, Distributore> distributoriMarker;
    Double lastMinLat, lastMaxLat, lastMinLng, lastMaxLng;
    LoadStationInScreen old;

    private String prefCarburante;
    private boolean prefSelf;
    private int prefKmxl;

    private DBmanager.SearchParams params;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        loaderView = LoadingShow.getLoader(this);
        loaderView.add("Starting app...");

        manager = new DBmanager(this);
        manager.start();

        //Roba delle mappe
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Roba dei campi d'input
        from = (EditText) findViewById(R.id.from);
        to = (EditText) findViewById(R.id.to);

        context = getApplicationContext();

        to.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
                            mMap.clear();
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

                                for(int i=0; i<r.steps.size()-1;i++){
                                    mMap.addMarker(new MarkerOptions().position(r.steps.get(i).end));
                                }
                                /*ArrayList<Distributore> vicini = manager.getZoneStation(r);
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
                                            if(minPrice>d.getLowestPrice(params)){
                                                economico = d;
                                            }
                                            mMap.addMarker(
                                                    new MarkerOptions().title(d.getLowestPrice(params)+"").draggable(false).visible(true).alpha(0.9f).position(d.getPosizione()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                            );
                                        }
                                    }
                                    mMap.addMarker(
                                            new MarkerOptions().title(economico.getLowestPrice(params)+"").draggable(false).visible(true).alpha(0.9f).position(economico.getPosizione()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                    );
                                }*/

                            }
                            loaderView.remove("Searching path");
                        }
                    }).execute();
                } catch (Exception e){
                    e.printStackTrace();
                }
                return false;
            }
        });
        loaderView.remove("Starting app...");
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences pref = getSharedPreferences("it.unisa.luca.stradaalrisparmio.pref", MODE_PRIVATE);
        prefCarburante = pref.getString("carburante", "diesel");
        prefSelf = pref.getBoolean("self", true);
        prefKmxl = pref.getInt("kmxl", 20);
        params = new DBmanager.SearchParams(prefCarburante, prefSelf, prefKmxl);

        old = null; //necessario per aggiornare bene lo schermo quando si ritorna da un'altra attività
        if(distributoriMarker!=null) {
            Set<Marker> markers = distributoriMarker.keySet();
            for (Marker m : markers) {
                m.remove();
            }
        }
        distributoriMarker = new HashMap<>();
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
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.769817, 14.7900013), 15.0f));
        old=null;
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
        if(old!=null){
            old.cancel(true);
        }else{
            //this.icon = BitmapCreator.getBitmap(context, Color.GRAY);
            //this.icon = resizeMapIcons("pomp_icon", 120, 120);
            this.lastMinLat=90.0;
            this.lastMaxLat=-90.0;
            this.lastMinLng=180.0;
            this.lastMaxLng=-180.0;
        }
        (old = new LoadStationInScreen()).execute();
    }

    public void onOpenSettings(View view){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Async task usato per la ricerca dei distributori all'interno dello schermo.
     * Se SCREEN_DIMENSION_FOR_DATA > delle dimensioni dello schermo non viene creato alcun thread
     */
    private class LoadStationInScreen extends AsyncTask<Void, Integer, ArrayList<Distributore>>{
        private Double minLat, maxLat, minLng, maxLng;
        private Double minLatC, maxLatC, minLngC, maxLngC;

        @Override
        protected void onPreExecute(){
            Log.d("Ricerca distributori", "Inizio a cercare distributori.");
            LatLngBounds view = mMap.getProjection().getVisibleRegion().latLngBounds;
            minLat = view.southwest.latitude; minLng = view.southwest.longitude; maxLat = view.northeast.latitude; maxLng = view.northeast.longitude;
            if(maxLat-minLat>SCREEN_DIMENSION_FOR_DATA && maxLng-minLng>SCREEN_DIMENSION_FOR_DATA){
                cancel(true);
                return;
            }
            if(view.northeast.latitude<=view.southwest.latitude){
                minLat = view.northeast.latitude;
                maxLat = view.southwest.latitude;
            }
            if(view.northeast.longitude<=view.southwest.longitude){
                minLat = view.northeast.longitude;
                maxLat = view.southwest.longitude;
            }
            minLatC=Math.max(minLat, lastMinLat);
            maxLatC=Math.min(maxLat, lastMaxLat);
            minLngC=Math.max(minLng, lastMinLng);
            maxLngC=Math.min(maxLng, lastMaxLng);

            //Scorre tutti i marker già presenti, se non entrano nei bordi dello schermo li rimuove
            LatLng tempPosition;
            Marker temp;
            if(isCancelled()) return;
            synchronized (LoadStationInScreen.class) {
                Iterator<Map.Entry<Marker, Distributore>> iter = distributoriMarker.entrySet().iterator();
                while(iter.hasNext()) {
                    temp = iter.next().getKey();
                    tempPosition = temp.getPosition();
                    if (tempPosition.latitude < minLatC || tempPosition.latitude > maxLatC || tempPosition.longitude < minLngC || tempPosition.longitude > maxLngC) {
                        temp.remove();
                        iter.remove();
                    }
                }
            }
        }

        /**
         * Uno schermo è composto da al più 5 rettangoli (in caso di zoom out), il rettangolo "Centrale" l'ho già calcolato dalla precedente ricerca,
         * Evito di ricalcolarlo ed eventualmente ricalcolo solo i rettangoli che rappresentano le aree nuove.
         */
        @Override
        protected synchronized ArrayList<Distributore> doInBackground(Void... voids) {
            if(isCancelled()) return null;
            ArrayList<Distributore> nuovi = new ArrayList<>();
            if(minLat<lastMinLat){
                nuovi.addAll(manager.getDistributoriInRange(minLat, lastMinLat, minLng, maxLng, params));
            }
            if(isCancelled()) return null;
            if(maxLat>lastMaxLat){
                nuovi.addAll(manager.getDistributoriInRange(lastMaxLat, maxLat, minLng, maxLng, params));
            }
            if(isCancelled()) return null;
            if(minLng<lastMinLng){
                Double neededMinLat = Math.max(minLat, lastMinLat);
                Double neededMaxLat = Math.min(maxLat, lastMaxLat);
                nuovi.addAll(manager.getDistributoriInRange(neededMinLat, neededMaxLat, minLng, lastMinLng, params));
            }
            if(isCancelled()) return null;
            if(maxLng>lastMaxLng){
                Double neededMinLat = Math.max(minLat, lastMinLat);
                Double neededMaxLat = Math.min(maxLat, lastMaxLat);
                nuovi.addAll(manager.getDistributoriInRange(neededMinLat, neededMaxLat, lastMaxLng, maxLng, params));
            }
            return nuovi;
        }

        protected void onPostExecute(ArrayList<Distributore> nuovi) {
            HashMap<Marker, Distributore> tempHashMap = new HashMap<>();
            Bitmap tempBitmap;
            for(Distributore dist : nuovi){
                tempBitmap = BitmapCreator.getBitmap(context, Color.GRAY, dist.getLowestPrice(params), dist.getBandiera());
                Marker temp = mMap.addMarker(
                        new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(tempBitmap)).title(dist.getId()+"").draggable(false).visible(true).alpha(0.95f).position(dist.getPosizione())
                );
                tempHashMap.put(temp, dist);
            }
            synchronized (LoadStationInScreen.class) {
                distributoriMarker.putAll(tempHashMap);
            }
            lastMinLat=minLat;
            lastMaxLat=maxLat;
            lastMinLng=minLng;
            lastMaxLng=maxLng;
            Log.d("Ricerca distributori", "Ricerca terminata con successo.");
        }

        protected void onCancelled(ArrayList<Distributore> nuovi){
            lastMinLat=minLat;
            lastMaxLat=maxLat;
            lastMinLng=minLng;
            lastMaxLng=maxLng;
            Log.d("Ricerca distributori", "Ricerca cancellata.");
        }
    }
}