package it.unisa.luca.stradaalrisparmio.api.strada;

import com.google.android.gms.maps.model.LatLng;

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
    public LatLng startLocation;

    public List<LatLng> points;
    public List<Step> regions;

}
