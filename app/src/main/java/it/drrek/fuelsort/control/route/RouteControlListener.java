package it.drrek.fuelsort.control.route;

import java.util.List;

import it.drrek.fuelsort.entity.station.Distributore;
import it.drrek.fuelsort.entity.route.Route;

/**
 * Used to undestand when a route is found.
 * Created by Luca on 09/12/2017.
 */

public interface RouteControlListener {
    void routeFound(Route r, List<Distributore> d);

    void exceptionSearchingForRoute(Exception e);
}
