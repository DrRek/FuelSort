package it.drrek.fuelsort.control.map;

/**
 * Created by Luca on 09/12/2017.
 */

public interface MapControlListener {
    /*Chiamata quando si inizia a cercare le stazioni nello schermo*/
    void startSearchingStationInScreen();
    /*Chiamata quando si termina di cercare le stazioni nello schermo*/
    void endSearchingStationInScreen();
    /*Chiamata quando lo stato dello zoom non consente la ricerca nello schermo*/
    void lowZoomWhileSearchingStationInScreen();
}
