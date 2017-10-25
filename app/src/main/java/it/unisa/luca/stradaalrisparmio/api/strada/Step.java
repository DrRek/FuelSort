package it.unisa.luca.stradaalrisparmio.api.strada;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by luca on 22/10/17.
 */

public class Step {
    public LatLng start, end;
    public Distance distance;

    public Step (LatLng start, LatLng end, Distance distance){
        this.start=start;
        this.end=end;
        this.distance=distance;
    }
}
