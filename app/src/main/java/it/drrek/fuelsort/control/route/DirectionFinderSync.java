package it.drrek.fuelsort.control.route;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.List;

import it.drrek.fuelsort.entity.route.Route;
import it.drrek.fuelsort.entity.station.Distributore;

/**
 * Utilizzata per una ricerca sincrona di un percorso.
 */
public class DirectionFinderSync extends DirectionFinder{
    String url;

    public DirectionFinderSync(String origin, String destination, List<Distributore> waypoints) {
        super(origin, destination, waypoints);
    }

    public List<Route> execute() throws UnsupportedEncodingException, JSONException {
        url = DIRECTION_URL_API + createUrl();

        return parseJSon(downloadRawData(url));
    }
}
