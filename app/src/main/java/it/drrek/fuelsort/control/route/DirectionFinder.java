package it.drrek.fuelsort.control.route;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Luca on 03/02/2018.
 */

public abstract class DirectionFinder {
    protected static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    protected static final String GOOGLE_API_KEY = "AIzaSyD9FDqZVMF6hPeAO-Hk7YV0Slmx00yPojg";
    protected String origin;
    protected String destination;
    protected String waypoint;

    public DirectionFinder(String origin, String destination, LatLng waypoint) {
        this.origin = origin;
        this.destination = destination;
        if(waypoint!=null)
            this.waypoint = waypoint.latitude+","+waypoint.longitude;
        else
            this.waypoint=null;
    }
}
