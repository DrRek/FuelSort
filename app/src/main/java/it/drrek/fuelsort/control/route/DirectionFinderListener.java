package it.drrek.fuelsort.control.route;

import java.util.List;

import it.drrek.fuelsort.entity.route.Route;

public interface DirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);
    void directionFinderException(Exception e);
}
