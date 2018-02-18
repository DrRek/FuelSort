package it.drrek.fuelsort.control.route;

import java.util.List;

import it.drrek.fuelsort.entity.route.Route;
import it.drrek.fuelsort.entity.station.DistributoreAsResult;

/**
 * Used to undestand when a route is found.
 * Created by Luca on 09/12/2017.
 */

public interface RouteControlListener {
    void startRouteSearch();
    void routeFound(Route r, List<DistributoreAsResult> d);
    void sendMessage(String message);
    void exceptionSearchingForRoute(Exception e);
}
