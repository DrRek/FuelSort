package it.unisa.luca.fuelsort.route.api;

import java.util.List;

import it.unisa.luca.fuelsort.route.entity.Route;

public interface DirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);
}
