package it.unisa.luca.fuelsort.route.entity;

import com.google.android.gms.maps.model.LatLng;

/**
 * Step will contain path steps.
 * Created by luca on 22/10/17.
 */

public class Step {
    @SuppressWarnings("FieldCanBeLocal")
    private final int EARTH_RADIUS = 6378000;
    private LatLng SOBound, NEBound;
    private int distance;
    private boolean isToll;

    public Step (LatLng start, LatLng end, int distance, boolean isToll){
        this.distance=distance;
        SOBound = start;
        NEBound = end;
    }

    public void merge(Step toMerge){
        this.distance += toMerge.getDistance();
        SOBound = new LatLng(Math.min(SOBound.latitude, toMerge.getSOBound().latitude), Math.min(SOBound.longitude, toMerge.getSOBound().longitude));
        NEBound = new LatLng(Math.max(NEBound.latitude, toMerge.getNEBound().latitude), Math.max(NEBound.longitude, toMerge.getNEBound().longitude));
    }

    public boolean isToll(){
        return isToll;
    }

    public void addMargin(int margin){
        SOBound = new LatLng(
                SOBound.latitude  - margin*180/(EARTH_RADIUS*Math.PI),
                SOBound.longitude - (margin*180/(EARTH_RADIUS*Math.PI))/ Math.cos(SOBound.latitude * Math.PI/180)
        );

        NEBound = new LatLng(
                NEBound.latitude  + margin*180/(EARTH_RADIUS*Math.PI),
                NEBound.longitude + (margin*180/(EARTH_RADIUS*Math.PI))/ Math.cos(NEBound.latitude * Math.PI/180)
        );
    }

    public LatLng getSOBound(){
        return SOBound;
    }

    public LatLng getNEBound(){
        return NEBound;
    }

    public int getDistance(){
        return distance;
    }
}
