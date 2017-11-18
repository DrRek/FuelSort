package it.unisa.luca.stradaalrisparmio.api.strada;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

public class Route {
    public static final int SUGGESTED_REGION_SIZE = 4000; //in m
    public static final int BOUNDS_FOR_REGION = 500; //in m

    public Distance distance;
    public Duration duration;
    public String endAddress;
    public LatLng endLocation;
    public String startAddress;
    public LatLng startLocation, northeast, southwest;

    public List<LatLng> points;
    public List<Step> regions;

    public LatLng getCenter(){
        double  minLat=Math.min(southwest.latitude, northeast.latitude),
                minLng=Math.min(southwest.longitude, northeast.longitude),
                maxLat=Math.max(southwest.latitude, northeast.latitude),
                maxLng=Math.max(southwest.longitude, northeast.longitude);
        return new LatLng((maxLat-minLat)/2+minLat, (maxLng-minLng)/2+minLng);
    }

    public LatLngBounds getLatLngBounds(){
        return new LatLngBounds(southwest, northeast);
    }
}
