package it.unisa.luca.fuelsort.gasstation.database;

/**
 * This listener interface will be used to inform the user of update status.
 * Created by Luca on 08/12/2017.
 */

public interface DatabaseUpdaterListener {
    void onStartStationUpdate();
    void onStartPriceUpdate();
    void onEndStationUpdate();
    void onEndPriceUpdate();
}

