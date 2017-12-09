package it.unisa.luca.fuelsort.route;

import it.unisa.luca.fuelsort.gasstation.entity.Distributore;
import it.unisa.luca.fuelsort.route.entity.Route;

/**
 * Used to undestand when a route is found.
 * Created by Luca on 09/12/2017.
 */

public interface RouteManagerListener {
    void routeFound(Route r, Distributore d);
}
