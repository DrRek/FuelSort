package it.drrek.fuelsort.control.map;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import it.drrek.fuelsort.entity.route.Region;
import it.drrek.fuelsort.entity.settings.SearchParams;
import it.drrek.fuelsort.entity.station.Distributore;
import it.drrek.fuelsort.entity.station.DistributoreAsResult;
import it.drrek.fuelsort.model.DistributoriManager;
import it.drrek.fuelsort.model.SearchParamsModel;
import it.drrek.fuelsort.support.BitmapCreator;
import it.drrek.fuelsort.entity.route.Route;
import it.drrek.fuelsort.R;
import it.drrek.fuelsort.view.DistributoreActivity;
import it.drrek.fuelsort.view.DistributoreAsResultFragment;
import it.drrek.fuelsort.view.DistributoreAsResultFragmentListener;
import it.drrek.fuelsort.view.MapsActivity;

import static android.content.Context.MODE_PRIVATE;

/**
 * Questa classe gestisce tutte le varie operazioni eseguibili sulla mappa.
 * Created by Luca on 08/12/2017.
 */

public class MapControl implements OnMapReadyCallback {

    /*
    Parte iniziale dell'url utilizzato per trovare un percorso dati partenza e arrivo.
     */
    private static String GMAPS_DEFAULT_DIRECTION_URL = "https://www.google.com/maps/dir/?api=1&travelmode=driving&";

    /*
    Activity che mostra la mappa in un fragment.
     */
    private Context activityContext;
    /*
    Mappa presente sullo schermo.
     */
    private GoogleMap mMap;

    /*
    Questa variabile statica definisce il minimo zoom della mappa che deve consentire
    la visualizzazione dei distributori trovati all'interno dello schermo.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private static float SCREEN_ZOOM_FOR_DATA = 13.5f;
    /*
    Varibile che definisce se si devono caricare i distributori nello schermo (se zoom>=SCreen_ZOOM_FOR_DATA)
     */
    private boolean loadStationOnPosition;

    /*
    Utilizzato per ottenere distributori.
     */
    private DistributoriManager distManager;
    /*
    Parametri personali dell'utente da utilizzare per le varie ricerche.
     */
    private SearchParams params;

    /*
    Listener collegato alle operazione effettuate da questa classe.
     */
    private MapControlListener listener;

    /*
    Contiene tutti i pin inseriti dall'utente.
     */
    private HashMap<LatLng, Marker> droppedPinHashMap;
    /*
    Continere tutti i distributori trovati da una ricerca;
     */
    private HashMap<Marker, Distributore> distributoreFoundAfterSerch;
    /*
    Se !=null corrisponde al marker della mia posizione.
     */
    private Marker myMarker;

    public MapControl(SupportMapFragment fragment, Context ctx) {
        fragment.getMapAsync(this);
        activityContext = ctx;

        ((Activity) ctx).findViewById(R.id.openOnGoogleMaps).setVisibility(View.GONE);
        ((Activity) ctx).findViewById(R.id.viewStation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onChangeStationScreenLoad();
            }
        });

        myMarker=null;
    }

    public void setListener(MapControlListener listener) {
        this.listener = listener;
    }

    /*
    Una volta trovata una strada qusto metodo si occupa di mostrarla all'utente.
     */
    public void setRoute(final Route r, final List<DistributoreAsResult> distributori) {
        removeAllStationFoundInScreen();
        mMap.clear();
        int coloreDistributori = ContextCompat.getColor(activityContext.getApplicationContext(), R.color.azzurro);
        distributoreFoundAfterSerch = new HashMap<>();
        for (Distributore d : distributori) {
            Marker m = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(BitmapCreator.getBitmap(activityContext, coloreDistributori, d.getBestPriceUsingSearchParams(), d.getBandiera()))).title(d.getId() + "").draggable(false).visible(true).alpha(0.95f).position(d.getPosizione()));
            distributoreFoundAfterSerch.put(m, d);
        }
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(BitmapCreator.getDefaultPin(activityContext))).title("Start").position(r.getStartLocation()));
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(BitmapCreator.getDefaultPin(activityContext))).title("End").position(r.getEndLocation()));
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(r.getLatLngBounds(), 100)); //100 is just some padding
        PolylineOptions plo = new PolylineOptions();
        plo.geodesic(true);
        plo.color(ContextCompat.getColor(activityContext, R.color.colorPrimary));
        plo.width(10);
        if (r.getRegions() == null) {
            for (Region re : r.getRegions()) {
                for (LatLng p : re.getPoints()) {
                    plo.add(p);
                }
            }
        } else {
            for (int i = 0; i < r.getPoints().size(); i++) {
                plo.add(r.getPoints().get(i));
            }
        }
        mMap.addPolyline(plo);

        Button openOnGoogleMaps = ((Activity) activityContext).findViewById(R.id.openOnGoogleMaps);
        openOnGoogleMaps.setVisibility(View.VISIBLE);
        openOnGoogleMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = GMAPS_DEFAULT_DIRECTION_URL + r.getParameters();
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
                activityContext.startActivity(intent);
            }
        });
    }

    public void onResume() {
        old = null; //Needed for new map position
        distManager = new DistributoriManager(activityContext);
        params = SearchParamsModel.getSearchParams(activityContext);
        setLoadStationOnPosition(true);
        removeAllStationFoundInScreen();
        if (mMap != null)
            setMarkersBasedOnPosition(); //Serve per reinizializzare i distributori all'interno della mappa quando si torna da un'activity
    }

    /*
    Metodo chiamato ogni bolta che ci si muove nella mappa.
     */
    private void onChangeStationScreenLoad() {
        if (loadStationOnPosition) {
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
            setLoadStationOnPosition(false);
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
            setLoadStationOnPosition(true);
            setMarkersBasedOnPosition();
        }
    }

    /*
    Rimuove tutte le stazioni di servizio trovate dalla ricerca nello schermo.
    Il marker di una stazione di servizio inserito come risultato di un percorso non viene rimosso.
     */
    private void removeAllStationFoundInScreen() {
        if (distributoriMarker != null) {
            Collection<Marker> markers = distributoriMarker.values();
            for (Marker m : markers) {
                m.remove();
            }
        }
        resetLastBounds();
        distributoriMarker = new HashMap<>();
    }

    public void onDestroy() {
        SharedPreferences pref = activityContext.getSharedPreferences("it.unisa.luca.fuelsort.pref", MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.putLong("lat", Double.doubleToRawLongBits(mMap.getCameraPosition().target.latitude));
        edit.putLong("lng", Double.doubleToRawLongBits(mMap.getCameraPosition().target.longitude));
        edit.putFloat("zoom", mMap.getCameraPosition().zoom);
        edit.apply();
    }

    /*
    Inizializzazione della mappa.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false); //disbale pointer button

        googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        activityContext, R.raw.default_map_style_json));


        SharedPreferences pref = activityContext.getSharedPreferences("it.unisa.luca.fuelsort.pref", MODE_PRIVATE);

        if (!pref.contains("zoom")) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.769817, 14.7900013), 10.0f));
        } else {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.longBitsToDouble(pref.getLong("lat", Double.doubleToRawLongBits(40.769817))), Double.longBitsToDouble(pref.getLong("lng", Double.doubleToRawLongBits(14.7900013)))), pref.getFloat("zoom", 15.0f)));
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
                if (droppedPinHashMap.containsValue(marker)) {
                    Projection projection = mMap.getProjection();
                    final LatLng markerPosition = marker.getPosition();
                    Point markerPoint = projection.toScreenLocation(markerPosition);

                    Point targetPoint = new Point(markerPoint.x, markerPoint.y - (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 110, activityContext.getResources().getDisplayMetrics()));
                    LatLng targetPosition = projection.fromScreenLocation(targetPoint);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetPosition, mMap.getCameraPosition().zoom), 500, new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            try {
                                LayoutInflater inflater = (LayoutInflater) activityContext
                                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                if (inflater != null) {
                                    View layout = inflater.inflate(R.layout.pin_layout,
                                            (ViewGroup) ((Activity) activityContext).findViewById(R.id.popup_element));
                                    final PopupWindow pw = new PopupWindow(layout, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 210, activityContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 175, activityContext.getResources().getDisplayMetrics()), true);

                                    LinearLayout ll = layout.findViewById(R.id.add_start);
                                    ll.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Log.d("DEBUG", "Adding LatLng to Start field on user request");
                                            TextView tv = ((Activity) activityContext).findViewById(R.id.from);
                                            tv.setText(markerPosition.latitude + "," + markerPosition.longitude);
                                            pw.dismiss();
                                        }
                                    });
                                    ll = layout.findViewById(R.id.add_end);
                                    ll.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Log.d("DEBUG", "Adding LatLng to End field on user request");
                                            TextView tv = ((Activity) activityContext).findViewById(R.id.to);
                                            tv.setText(markerPosition.latitude + "," + markerPosition.longitude);
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
                                } else {
                                    Log.e("MapControl", "L'inflater del layout è null, impossibile creare il popup.");
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
                } else if (distributoriMarker.containsValue(marker)) {
                    Distributore risultato = null;
                    for (Distributore d : distributoriMarker.keySet()) {
                        if (distributoriMarker.get(d).equals(marker)) {
                            risultato = d;
                            break;
                        }
                    }
                    openDistributoreActivity(risultato);
                } else if (distributoreFoundAfterSerch != null && distributoreFoundAfterSerch.containsKey(marker)) {
                    openDistributoreAsResultFragment(distributoreFoundAfterSerch.get(marker), true);
                } else {
                    return false;
                }
                return true;
            }
        });
    }

    /*
    Metodo chiamato per iniziare la ricerca dei distributori nello schermo
     */
    private void setMarkersBasedOnPosition() {
        if(loadStationOnPosition) {
            if (old != null) {
                old.cancel(false); //se mettessi true il thread potrebbe essere interrotto forzatamente e questo potrebbe comportare problemi.
            } else {
                resetLastBounds();
            }
            (old = new MapControl.LoadStationInScreen()).execute();
        }
    }

    /*
    Metodo utilizzato per gestire i casi limite (mappa appena aperta).
     */
    private void resetLastBounds(){
        this.lastMinLat = null;
        this.lastMaxLat = null;
        this.lastMinLng = null;
        this.lastMaxLng = null;
    }

    /*
    Contiene la posizione dello schermo nella mappa prima del movimento.
     */
    private Double lastMinLat, lastMaxLat, lastMinLng, lastMaxLng;
    /*
    Ultimo thread che sta eseguendo la ricerca. Se sto eseguendo una ricerca ma l'utente continua a muoversi la interrompo.
     */
    private LoadStationInScreen old;
    /*
    Contiene tutti i distributori trovati nello schermo.
     */
    private volatile HashMap<Distributore, Marker> distributoriMarker;

    private void setLoadStationOnPosition(boolean loadStationOnPosition) {
        this.loadStationOnPosition = loadStationOnPosition;
        if(loadStationOnPosition) {
            ((Activity) activityContext).findViewById(R.id.viewStation).setBackgroundResource(R.drawable.disable_disitributori);
        } else{
            ((Activity) activityContext).findViewById(R.id.viewStation).setBackgroundResource(R.drawable.enable_disitributori);
        }
    }

    /**
     * Classe utilizzata per la ricerca dei distributori nello schermo in un thread separato.
     */
    private class LoadStationInScreen extends AsyncTask<Void, Integer, ArrayList<Distributore>> {
        private Double minLat, maxLat, minLng, maxLng;
        private Double minLatC, maxLatC, minLngC, maxLngC;

        @Override
        protected void onPreExecute() {
            LatLngBounds view = mMap.getProjection().getVisibleRegion().latLngBounds;
            minLat = view.southwest.latitude;
            minLng = view.southwest.longitude;
            maxLat = view.northeast.latitude;
            maxLng = view.northeast.longitude;
            if (mMap.getCameraPosition().zoom<=SCREEN_ZOOM_FOR_DATA) {
                cancel(false);
                removeAllStationFoundInScreen();
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
                nuovi.addAll(distManager.getStationsInBound(minLat, maxLat, minLng, maxLng, params, false));
            }else {
                if (minLat < lastMinLat) {
                    nuovi.addAll(distManager.getStationsInBound(minLat, lastMinLat, minLng, maxLng, params, false));
                    if (isCancelled()) {
                        lastMinLat = minLat;
                        lastMaxLat = maxLatC;
                        lastMinLng = minLngC;
                        lastMaxLng = maxLngC;
                        return null;
                    }
                }
                if (maxLat > lastMaxLat) {
                    nuovi.addAll(distManager.getStationsInBound(lastMaxLat, maxLat, minLng, maxLng, params, false));
                    if (isCancelled()) {
                        lastMaxLat = maxLat;
                        lastMinLng = minLngC;
                        lastMaxLng = maxLngC;
                        return null;
                    }
                }
                if (minLng < lastMinLng) {
                    nuovi.addAll(distManager.getStationsInBound(minLatC, maxLatC, minLng, lastMinLng, params, false));
                    if (isCancelled()) {
                        lastMinLng = minLng;
                        lastMaxLng = maxLngC;
                        return null;
                    }
                }
                if (maxLng > lastMaxLng) {
                    nuovi.addAll(distManager.getStationsInBound(minLatC, maxLatC, lastMaxLng, maxLng, params, false));
                    if (isCancelled()) {
                        lastMaxLng = maxLng;
                        return null;
                    }
                }
            }
            return nuovi;
        }

        /**
         * Questa funzione viene eseguita sul main thread quando vengono trovati i distributori da aggiungenere nello schermo.
         * Qui vengono scelti i colori dei distributori da aggiungere sullo schermo, la tonalità si calcula in base al prezzo del distributore nell'insieme
         * la luminosità è in funzione della tonalità. f(x) = -0.00660982219578x^2 + 0.813008130081x + 75
         * @param nuovi
         */
        private static final double FUNZIONE_LUMINOSITA_A = -0.00660982219578;
        private static final double FUNZIONE_LUMINOSITA_B = 0.813008130081;
        private static final double FUNZIONE_LUMINOSITA_C = 75;
        protected void onPostExecute(ArrayList<Distributore> nuovi) {
            synchronized (LoadStationInScreen.class) {
                for(Distributore nuovo : nuovi){
                    Marker tempMark = mMap.addMarker(
                            new MarkerOptions().draggable(false).visible(true).alpha(0.95f).position(nuovo.getPosizione())
                    );
                    distributoriMarker.put(nuovo, tempMark);
                }
                nuovi.clear();
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
                    hsv[0]=123;
                    hsv[1]=1;
                    hsv[2]=0.75f;
                    Bitmap tempBitmap = BitmapCreator.getBitmap(activityContext, Color.HSVToColor(hsv), nuovi.get(0).setPriceByParams(params), nuovi.get(0).getBandiera());
                    tempMarker.setIcon(BitmapDescriptorFactory.fromBitmap(tempBitmap));

                }else if(distributoriSize>1) {
                    float min = nuovi.get(0).getBestPriceUsingSearchParams(), max = nuovi.get(distributoriSize - 1).getBestPriceUsingSearchParams();
                    float diff = max - min;
                    float[] hsv = new float[3];
                    hsv[1]=1;
                    for (int i = 0; i < distributoriSize; i++) {
                        Distributore tempDist = nuovi.get(i);
                        Marker tempMark = distributoriMarker.get(tempDist);
                        hsv[0] = (tempDist.getBestPriceUsingSearchParams() - min) * 123 / diff;
                        hsv[2] = (float)((FUNZIONE_LUMINOSITA_A*Math.pow(hsv[0], 2) + FUNZIONE_LUMINOSITA_B*hsv[0] + FUNZIONE_LUMINOSITA_C)/100);

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
        }
    }

    private void openDistributoreActivity(Distributore toOpen){
        Intent intent = new Intent(activityContext, DistributoreActivity.class);
        intent.putExtra("distributore", toOpen);
        activityContext.startActivity(intent);
        ((Activity)activityContext).overridePendingTransition(R.anim.explode_center, R.anim.explode_implode_no_anim);
    }

    private void openDistributoreAsResultFragment(final Distributore distributore, boolean isFirstFragment) {
        if (distributore != null) {
            final FragmentManager fm = ((Activity) activityContext).getFragmentManager();
            final FragmentTransaction ft = fm.beginTransaction();
            final DistributoreAsResultFragment fragment = new DistributoreAsResultFragment();
            fragment.setDistributore(distributore);
            fragment.setListener(new DistributoreAsResultFragmentListener() {
                @Override
                public void close() {
                    fm.popBackStack();
                }

                @Override
                public void info() {
                    openDistributoreActivity(fragment.getDistributore());
                }

                @Override
                public void next() {
                    openDistributoreAsResultFragment(((DistributoreAsResult)distributore).getNext(), false);
                }

                @Override
                public void prev() {
                    openDistributoreAsResultFragment(((DistributoreAsResult)distributore).getPrev(), false);
                }
            });
            if(!isFirstFragment) {
                fm.popBackStack();
            }
            ft.setCustomAnimations(R.animator.slide_in_bottom, R.animator.slide_out_bottom, R.animator.slide_in_bottom, R.animator.slide_out_bottom);
            ft.replace(R.id.fragment_distributore_as_result, fragment);
            ft.addToBackStack("distributore");
            ft.commit();
        } else {
            Log.e("MapControl", "Tentato di aprire la pagina di un distributore passando null");
        }
    }

    public void setMyPosition(double latitude, double longitude){
        if(myMarker == null){
            myMarker = mMap.addMarker(new MarkerOptions().draggable(false).visible(true).alpha(0.95f).position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.fromBitmap(BitmapCreator.getPositionPin(activityContext))));
        }else{
            myMarker.setPosition(new LatLng(latitude, longitude));
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), mMap.getCameraPosition().zoom), 500, null);
    }
}
