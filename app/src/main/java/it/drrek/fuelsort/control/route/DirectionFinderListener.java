package it.drrek.fuelsort.control.route;

import java.util.List;

import it.drrek.fuelsort.entity.route.Route;

/*Listener della ricerca asincrona*/
public interface DirectionFinderListener {
    /*Inizio della ricerca di un percorso*/
    void onDirectionFinderStart();
    /*Fine della ricerca di un percorso*/
    void onDirectionFinderSuccess(List<Route> route);
    /*Eccezione sollevata dalla ricerca di un percorso*/
    void directionFinderException(Exception e);
}
