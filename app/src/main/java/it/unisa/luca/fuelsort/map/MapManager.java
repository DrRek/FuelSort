package it.unisa.luca.fuelsort.map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import it.unisa.luca.fuelsort.gasstation.database.DatabaseManager;
import it.unisa.luca.fuelsort.gasstation.entity.Distributore;
import it.unisa.luca.fuelsort.route.entity.Route;
import it.unisa.luca.stradaalrisparmio.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Thsi class manage the map.
 * Created by Luca on 08/12/2017.
 */

public class MapManager implements OnMapReadyCallback {

    @SuppressWarnings("FieldCanBeLocal")
    private static float SCREEN_ZOOM_FOR_DATA = 13.5f;
    private static String GMAPS_DEFAULT_DIRECTION_URL = "https://www.google.com/maps/dir/?api=1&travelmode=driving&";

    private GoogleMap mMap;
    private Context activityContext;
    private boolean loadStationOnPosition;
    private DatabaseManager databaseManager;
    private DatabaseManager.SearchParams params;

    public MapManager(SupportMapFragment fragment, Context ctx) {
        fragment.getMapAsync(this);
        activityContext = ctx;

        ((Activity)ctx).findViewById(R.id.openOnGoogleMaps).setVisibility(View.GONE);
        ((Activity)ctx).findViewById(R.id.viewStation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onChangeStationScreenLoad();
            }
        });
    }

    public void setRoute(final Route r, final Distributore d){
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(BitmapCreator.getStartBitmap(activityContext))).title("Start").position(r.getStartLocation()));
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(BitmapCreator.getFinishBitmap(activityContext))).title("End").position(r.getEndLocation()));
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(BitmapCreator.getBitmap(activityContext, Color.BLUE, d.getBestPriceUsingSearchParams(), d.getBandiera()))).title(d.getId() + "").draggable(false).visible(true).alpha(0.95f).position(d.getPosizione()));
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(r.getLatLngBounds(), 100)); //100 is just some padding
        PolylineOptions plo = new PolylineOptions();
        plo.geodesic(true);
        plo.color(Color.BLUE);
        plo.width(10);
        for (int i = 0; i < r.getPoints().size(); i++) {
            plo.add(r.getPoints().get(i));
        }
        mMap.addPolyline(plo);

        Button openOnGoogleMaps = ((Activity)activityContext).findViewById(R.id.openOnGoogleMaps);
        openOnGoogleMaps.setVisibility(View.VISIBLE);
        openOnGoogleMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = GMAPS_DEFAULT_DIRECTION_URL+r.getParameters();
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
                activityContext.startActivity(intent);
            }
        });
    }

    public void onResume(){
        old=null; //Needed for new map position
        databaseManager = new DatabaseManager(activityContext);
        params = databaseManager.getSearchParams();
        loadStationOnPosition = false; //Temporaneamente
        removeAllStationFoundInScreen();
    }

    private void onChangeStationScreenLoad(){
        if(loadStationOnPosition){
            final Toast toast = Toast.makeText(activityContext, "Rimuovo i distributori nello schermo", Toast.LENGTH_SHORT);
            toast.show();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.cancel();
                }
            }, 350);
            removeAllStationFoundInScreen();
            loadStationOnPosition = false;
        } else {
            final Toast toast = Toast.makeText(activityContext, "Aggiungo i distributori nello schermo", Toast.LENGTH_SHORT);
            toast.show();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.cancel();
                }
            }, 150); //Il delay in questo caso è più corto perché il sistema sarà già in buona parte rallentato dalla ricerca di distributori

            resetLastBounds();
            loadStationOnPosition = true;
            setMarkersBasedOnPosition();
        }
    }

    private void removeAllStationFoundInScreen(){
        if (distributoriMarker != null) {
            Set<Marker> markers = distributoriMarker.keySet();
            for (Marker m : markers) {
                m.remove();
            }
        }
        resetLastBounds();
        distributoriMarker = new HashMap<>();
    }

    public void onDestroy(){
        SharedPreferences pref = activityContext.getSharedPreferences("it.unisa.luca.fuelsort.pref", MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.putLong("lat", Double.doubleToRawLongBits(mMap.getCameraPosition().target.latitude));
        edit.putLong("lng", Double.doubleToRawLongBits(mMap.getCameraPosition().target.longitude));
        edit.putFloat("zoom", mMap.getCameraPosition().zoom);
        edit.apply();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false); //disbale pointer button
        SharedPreferences pref = activityContext.getSharedPreferences("it.unisa.luca.fuelsort.pref", MODE_PRIVATE);

        if(!pref.contains("zoom")) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.769817, 14.7900013), 15.0f));
        }else{
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.longBitsToDouble(pref.getLong("lat", Double.doubleToRawLongBits(40.769817))),Double.longBitsToDouble(pref.getLong("lng", Double.doubleToRawLongBits(14.7900013)))), pref.getFloat("zoom", 15.0f)));
        }
        old = null;
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                setMarkersBasedOnPosition();
            }
        });
    }

    private void setMarkersBasedOnPosition() {
        if(loadStationOnPosition) {
            if (old != null) {
                old.cancel(false); //se mettessi true il thread potrebbe essere interrotto forzatamente e questo potrebbe comportare problemi.
            } else {
                resetLastBounds();
            }
            (old = new MapManager.LoadStationInScreen()).execute();
        }
    }

    private void resetLastBounds(){
        this.lastMinLat = null;
        this.lastMaxLat = null;
        this.lastMinLng = null;
        this.lastMaxLng = null;
    }


    private Double lastMinLat, lastMaxLat, lastMinLng, lastMaxLng;
    private LoadStationInScreen old;
    private volatile HashMap<Marker, Distributore> distributoriMarker;

    private class LoadStationInScreen extends AsyncTask<Void, Integer, ArrayList<Distributore>> {
        private Double minLat, maxLat, minLng, maxLng;
        private Double minLatC, maxLatC, minLngC, maxLngC;

        @Override
        protected void onPreExecute() {
            Log.d("Ricerca distributori", "Inizio a cercare distributori.");
            LatLngBounds view = mMap.getProjection().getVisibleRegion().latLngBounds;
            minLat = view.southwest.latitude;
            minLng = view.southwest.longitude;
            maxLat = view.northeast.latitude;
            maxLng = view.northeast.longitude;
            if (mMap.getCameraPosition().zoom<=SCREEN_ZOOM_FOR_DATA) {
                cancel(false);
                return;
            }//altrimenti cerca i distributori in zona

            if (view.northeast.latitude <= view.southwest.latitude) {
                minLat = view.northeast.latitude;
                maxLat = view.southwest.latitude;
            }
            if (view.northeast.longitude <= view.southwest.longitude) {
                minLat = view.northeast.longitude;
                maxLat = view.southwest.longitude;
            }

            if(lastMinLat==null) return; //Se non abbiamo distributori precedenti

            minLatC = Math.max(minLat, lastMinLat);
            maxLatC = Math.min(maxLat, lastMaxLat);
            minLngC = Math.max(minLng, lastMinLng);
            maxLngC = Math.min(maxLng, lastMaxLng);

            //Scorre tutti i marker già presenti, se non entrano nei bordi dello schermo li rimuove
            LatLng tempPosition;
            Marker temp;
            synchronized (LoadStationInScreen.class) {
                Iterator<Map.Entry<Marker, Distributore>> iter = distributoriMarker.entrySet().iterator();
                while (iter.hasNext()) {
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
            if (isCancelled()) {
                lastMinLat = minLatC;
                lastMaxLat = maxLatC;
                lastMinLng = minLngC;
                lastMaxLng = maxLngC;
                return null;
            }

            ArrayList<Distributore> nuovi = new ArrayList<>();
            if(lastMinLat==null){
                nuovi.addAll(databaseManager.getStationsInBound(minLat, maxLat, minLng, maxLng, params, false));
            }else {
                if (minLat < lastMinLat) {
                    nuovi.addAll(databaseManager.getStationsInBound(minLat, lastMinLat, minLng, maxLng, params, false));
                    if (isCancelled()) {
                        lastMinLat = minLat;
                        lastMaxLat = maxLatC;
                        lastMinLng = minLngC;
                        lastMaxLng = maxLngC;
                        return null;
                    }
                }
                if (maxLat > lastMaxLat) {
                    nuovi.addAll(databaseManager.getStationsInBound(lastMaxLat, maxLat, minLng, maxLng, params, false));
                    if (isCancelled()) {
                        lastMaxLat = maxLat;
                        lastMinLng = minLngC;
                        lastMaxLng = maxLngC;
                        return null;
                    }
                }
                if (minLng < lastMinLng) {
                    //Double neededMinLat = Math.max(minLat, lastMinLat);
                    // Double neededMaxLat = Math.min(maxLat, lastMaxLat);
                    nuovi.addAll(databaseManager.getStationsInBound(minLatC, maxLatC, minLng, lastMinLng, params, false));
                    if (isCancelled()) {
                        lastMinLng = minLng;
                        lastMaxLng = maxLngC;
                        return null;
                    }
                }
                if (maxLng > lastMaxLng) {
                    //Double neededMinLat = Math.max(minLat, lastMinLat);
                    //Double neededMaxLat = Math.min(maxLat, lastMaxLat);
                    nuovi.addAll(databaseManager.getStationsInBound(minLatC, maxLatC, lastMaxLng, maxLng, params, false));
                    if (isCancelled()) {
                        lastMaxLng = maxLng;
                        return null;
                    }
                }
            }
            return nuovi;
        }

        protected void onPostExecute(ArrayList<Distributore> nuovi) {
            synchronized (LoadStationInScreen.class) {
                Set<Marker> markers = distributoriMarker.keySet();
                for (Marker m : markers) {
                    m.remove();
                }
                nuovi.addAll(distributoriMarker.values());
                distributoriMarker = new HashMap<>();
                Collections.sort(nuovi, new Comparator<Distributore>() {
                    @Override
                    public int compare(Distributore distributore, Distributore t1) {
                        return (int)((t1.getBestPriceUsingSearchParams() - distributore.getBestPriceUsingSearchParams())*100000);
                    }
                });

                int numeroColoriDaUsare = nuovi.size();

                float angleRange = 120;
                float stepAngle = angleRange / numeroColoriDaUsare;

                float[] hsv = new float[3];
                hsv[1]=1;
                hsv[2]=1;
                for(int i=0; i<numeroColoriDaUsare; i++){
                    Distributore tempDist = nuovi.get(i);
                    hsv[0] = 0 + i*stepAngle;
                    Bitmap tempBitmap = BitmapCreator.getBitmap(activityContext, Color.HSVToColor(hsv), tempDist.setPriceByParams(params), tempDist.getBandiera());
                    Marker tempMark = mMap.addMarker(
                            new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(tempBitmap)).title(tempDist.getId() + "").draggable(false).visible(true).alpha(0.95f).position(tempDist.getPosizione())
                    );
                    distributoriMarker.put(tempMark, tempDist);
                }

                lastMinLat = minLat;
                lastMaxLat = maxLat;
                lastMinLng = minLng;
                lastMaxLng = maxLng;
                //loaderManager.remove("Cerco distributori nella zona");
            }
        }

        protected void onCancelled(ArrayList<Distributore> nuovi) {
            Log.d("Ricerca distributori", "Ricerca cancellata.");
            //loaderManager.remove("Cerco distributori nella zona");
        }
    }
}
