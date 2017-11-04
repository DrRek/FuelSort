package it.unisa.luca.stradaalrisparmio.api.strada;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class Route {
    public static final int SUGGESTED_REGION_SIZE = 2000; //in km
    public static final int BOUNDS_FOR_REGION = 1000; //in km

    public Distance distance;
    public Duration duration;
    public String endAddress;
    public LatLng endLocation;
    public String startAddress;
    public LatLng startLocation;

    public List<LatLng> points;
    public List<Step> regions;

}
