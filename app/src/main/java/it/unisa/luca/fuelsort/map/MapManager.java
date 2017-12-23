package it.unisa.luca.fuelsort.map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import it.unisa.luca.fuelsort.gasstation.database.DatabaseManager;
import it.unisa.luca.fuelsort.gasstation.entity.Distributore;
import it.unisa.luca.fuelsort.route.entity.Route;
import it.unisa.luca.fuelsort.R;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
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

    private MapManagerListener listener;
    private HashMap<LatLng, Marker> droppedPinHashMap;

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

    public void setListener(MapManagerListener listener){
        this.listener=listener;
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
            Collection<Marker> markers = distributoriMarker.values();
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
        droppedPinHashMap = new HashMap<>();
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Marker temp = mMap.addMarker(new MarkerOptions().position(latLng).title("Your marker title").snippet("Your marker snippet")
                        .icon(BitmapDescriptorFactory.fromBitmap(BitmapCreator.getDefaultPin(activityContext))));
                droppedPinHashMap.put(latLng, temp);
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                if(droppedPinHashMap.containsValue(marker)) {
                    Projection projection = mMap.getProjection();
                    final LatLng markerPosition = marker.getPosition();
                    Point markerPoint = projection.toScreenLocation(markerPosition);

                    DisplayMetrics metrics = new DisplayMetrics();
                    ((Activity)activityContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
                    float logicalDensity = metrics.density;
                    //int px = (int) Math.ceil(dp * logicalDensity);
                    //int dp = (int) Math.floor(px / logicalDensity);

                    float px = ((Activity)activityContext).findViewById(android.R.id.content).getHeight();
                    int desired = (int)Math.floor(px*110/((int) Math.floor(px / logicalDensity)));

                    Point targetPoint = new Point(markerPoint.x, (int) Math.ceil(400*logicalDensity));//(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 110, activityContext.getResources().getDisplayMetrics()));
                    LatLng targetPosition = projection.fromScreenLocation(targetPoint);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetPosition, mMap.getCameraPosition().zoom), 500, new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            try {
                                LayoutInflater inflater = (LayoutInflater) activityContext
                                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                if(inflater != null) {
                                    View layout = inflater.inflate(R.layout.pin_layout,
                                            (ViewGroup) ((Activity) activityContext).findViewById(R.id.popup_element));
                                    final PopupWindow pw = new PopupWindow(layout, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 210, activityContext.getResources().getDisplayMetrics()), (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 175, activityContext.getResources().getDisplayMetrics()), true);

                                    LinearLayout ll = layout.findViewById(R.id.add_start);
                                    ll.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Log.d("DEBUG", "Adding LatLng to Start field on user request");
                                            TextView tv = ((Activity) activityContext).findViewById(R.id.from);
                                            tv.setText(markerPosition.latitude+","+markerPosition.longitude);
                                            pw.dismiss();
                                        }
                                    });
                                    ll = layout.findViewById(R.id.add_end);
                                    ll.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Log.d("DEBUG", "Adding LatLng to End field on user request");
                                            TextView tv = ((Activity) activityContext).findViewById(R.id.to);
                                            tv.setText(markerPosition.latitude+","+markerPosition.longitude);
                                            pw.dismiss();
                                        }
                                    });
                                    ll = layout.findViewById(R.id.remove_marker);
                                    ll.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Log.d("DEBUG", "Removing marker on user request");
                                            marker.remove();
                                            pw.dismiss();
                                        }
                                    });
                                    pw.showAtLocation(layout, Gravity.CENTER, 0, 0);
                                }else{
                                    Log.e("ERROR", "L'inflater del layout è null, impossibile creare il popup (MapManager)");
                                }
                                //To add listener
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
                }else if(distributoriMarker.containsValue(marker)){
                    Log.d("DEBUG", "FOR NOW NOTHING");
                }
                return true;
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
    private volatile HashMap<Distributore, Marker> distributoriMarker;

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
                listener.lowZoomWhileSearchingStationInScreen();
                return;
            }
            listener.startSearchingStationInScreen();

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
                Iterator<Map.Entry<Distributore, Marker>> iter = distributoriMarker.entrySet().iterator();
                while (iter.hasNext()) {
                    temp = iter.next().getValue();
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
                for(Distributore nuovo : nuovi){
                    Marker tempMark = mMap.addMarker(
                            new MarkerOptions().draggable(false).visible(true).alpha(0.95f).position(nuovo.getPosizione())
                    );
                    distributoriMarker.put(nuovo, tempMark);
                }
                nuovi.addAll(distributoriMarker.keySet());
                Collections.sort(nuovi, new Comparator<Distributore>() {
                    @Override
                    public int compare(Distributore distributore, Distributore t1) {
                        return (int)((t1.getBestPriceUsingSearchParams() - distributore.getBestPriceUsingSearchParams())*100000);
                    }
                });

                int distributoriSize = nuovi.size();

                if(distributoriSize==1){
                    Marker tempMarker = distributoriMarker.get(nuovi.get(0));
                    float[] hsv = new float[3];
                    hsv[0]=120;
                    hsv[1]=1;
                    hsv[2]=1;
                    Bitmap tempBitmap = BitmapCreator.getBitmap(activityContext, Color.HSVToColor(hsv), nuovi.get(0).setPriceByParams(params), nuovi.get(0).getBandiera());
                    tempMarker.setIcon(BitmapDescriptorFactory.fromBitmap(tempBitmap));

                }else if(distributoriSize>1) {
                    float min = nuovi.get(0).getBestPriceUsingSearchParams(), max = nuovi.get(distributoriSize - 1).getBestPriceUsingSearchParams();
                    float diff = max - min;
                    float[] hsv = new float[3];
                    hsv[1] = 1;
                    hsv[2] = 1;
                    for (int i = 0; i < distributoriSize; i++) {
                        Distributore tempDist = nuovi.get(i);
                        Marker tempMark = distributoriMarker.get(tempDist);
                        hsv[0] = (tempDist.getBestPriceUsingSearchParams() - min) * 120 / diff;
                        Bitmap tempBitmap = BitmapCreator.getBitmap(activityContext, Color.HSVToColor(hsv), tempDist.setPriceByParams(params), tempDist.getBandiera());
                        tempMark.setIcon(BitmapDescriptorFactory.fromBitmap(tempBitmap));
                    }
                }

                lastMinLat = minLat;
                lastMaxLat = maxLat;
                lastMinLng = minLng;
                lastMaxLng = maxLng;
                listener.endSearchingStationInScreen();
            }
        }

        protected void onCancelled(ArrayList<Distributore> nuovi) {
            Log.d("Ricerca distributori", "Ricerca cancellata.");
        }
    }
}
