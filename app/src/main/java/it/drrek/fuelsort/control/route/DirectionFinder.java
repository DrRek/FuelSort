package it.drrek.fuelsort.control.route;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import it.drrek.fuelsort.entity.station.Distributore;

/**
 * Created by Luca on 03/02/2018.
 */

public abstract class DirectionFinder {
    protected static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    protected static final String GOOGLE_API_KEY = "AIzaSyD9FDqZVMF6hPeAO-Hk7YV0Slmx00yPojg";
    protected String origin;
    protected String destination;
    protected String waypoints;

    public DirectionFinder(String origin, String destination, LatLng waypoint) {
        this.origin = origin;
        this.destination = destination;
        if(waypoint!=null)
            this.waypoints = waypoint.latitude+","+waypoint.longitude;
        else
            this.waypoints=null;
    }

    public DirectionFinder(String origin, String destination, List<Distributore> distributori) {
        this.origin = origin;
        this.destination = destination;
        if(distributori.size() == 0){
            waypoints = null;
        } else {
            waypoints = "";
            for (int i = 0; i < distributori.size() - 1; i++) {
                waypoints += distributori.get(i).getLat() + "," + distributori.get(i).getLon() + "|";
            }
            waypoints += distributori.get(distributori.size() - 1).getLat() + "," + distributori.get(distributori.size() - 1).getLon();
        }

    }
}
