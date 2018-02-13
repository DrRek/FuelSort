package it.drrek.fuelsort.control.route;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.List;

import it.drrek.fuelsort.entity.exception.NoDataForPathException;
import it.drrek.fuelsort.entity.route.Route;

public class DirectionFinderAsync extends DirectionFinder {
    private DirectionFinderListener listener;

    public DirectionFinderAsync(String origin, String destination, LatLng waypoint, DirectionFinderListener listener) {
        super(origin, destination, waypoint);
        this.listener = listener;
    }

    public void execute() throws UnsupportedEncodingException {
        if(listener!=null) listener.onDirectionFinderStart();
        String url = DIRECTION_URL_API + createUrl();
        new DownloadRawData().execute(url);
    }

    private class DownloadRawData extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            String link = params[0];
            return downloadRawData(link);
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                List<Route> result = parseJSon(res);
                if (result == null) {
                    if(listener != null) {
                        listener.directionFinderException(new NoDataForPathException("Errore cercando un percorso, controlla di avere una connessione ad internet disponibile!"));
                    }
                    return;
                }
                if(listener!=null)listener.onDirectionFinderSuccess(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
