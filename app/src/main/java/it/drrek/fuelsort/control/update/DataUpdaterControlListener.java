package it.drrek.fuelsort.control.update;

/**
 * This listener interface will be used to inform the user of update status.
 * Created by Luca on 08/12/2017.
 */

public interface DataUpdaterControlListener {
    void onStartStationUpdate();
    void onStartPriceUpdate();
    void onEndStationUpdate();
    void onEndPriceUpdate();
}

