package it.unisa.luca.stradaalrisparmio.api.strada;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by luca on 22/10/17.
 */

public class Step {
    public final int EARTH_RADIUS = 6378000;
    public LatLng start, end;
    public LatLng SOBound, NEBound;
    public int distance;

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
}
