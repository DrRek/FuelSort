package it.unisa.luca.fuelsort.route.entity;

import com.google.android.gms.maps.model.LatLng;

/**
 * Step will contain path steps.
 * Created by luca on 22/10/17.
 */

public class Step {
    @SuppressWarnings("FieldCanBeLocal")
    private final int EARTH_RADIUS = 6378000;
    private LatLng start, end;
    private LatLng SOBound, NEBound;
    private int distance;

    public Step (LatLng start, LatLng end, int distance){
        this.start=start;
        this.end=end;
        this.distance=distance;
        SOBound = new LatLng(Math.min(start.latitude, end.latitude), Math.min(start.longitude, end.longitude));
        NEBound = new LatLng(Math.max(start.latitude, end.latitude), Math.max(start.longitude, end.longitude));
    }

    public void setNewEnd(LatLng end, int distance){
        this.end = end;
        this.distance = this.distance+distance;
        SOBound = new LatLng(Math.min(SOBound.latitude, end.latitude), Math.min(SOBound.longitude, end.longitude));
        NEBound = new LatLng(Math.max(NEBound.latitude, end.latitude), Math.max(NEBound.longitude, end.longitude));
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

    public LatLng getStart() {
        return start;
    }

    public void setStart(LatLng start) {
        this.start = start;
    }

    public LatLng getEnd() {
        return end;
    }

    public void setEnd(LatLng end) {
        this.end = end;
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
