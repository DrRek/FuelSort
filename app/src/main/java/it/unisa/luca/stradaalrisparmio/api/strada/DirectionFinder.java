package it.unisa.luca.stradaalrisparmio.api.strada;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import it.unisa.luca.stradaalrisparmio.R;

public class DirectionFinder {
    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyD9FDqZVMF6hPeAO-Hk7YV0Slmx00yPojg";
    private DirectionFinderListener listener;
    private String origin;
    private String destination;
    private String waypoint;

    public DirectionFinder(String origin, String destination, LatLng waypoint, DirectionFinderListener listener) {
        this.listener = listener;
        this.origin = origin;
        this.destination = destination;
        if(waypoint!=null)
            this.waypoint = waypoint.latitude+","+waypoint.longitude;
        else
            this.waypoint=null;
    }

    public void execute() throws UnsupportedEncodingException {
        listener.onDirectionFinderStart();
        new DownloadRawData().execute(createUrl());
    }

    private String createUrl() throws UnsupportedEncodingException {
        String urlOrigin = URLEncoder.encode(origin, "utf-8");
        String urlDestination = URLEncoder.encode(destination, "utf-8");
        if(waypoint==null)
            return DIRECTION_URL_API + "origin=" + urlOrigin + "&destination=" + urlDestination + "&key=" + GOOGLE_API_KEY;
        else
            return DIRECTION_URL_API + "origin=" + urlOrigin + "&destination=" + urlDestination + "&waypoints=" + waypoint + "&key=" + GOOGLE_API_KEY;
    }

    private class DownloadRawData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String link = params[0];
            try {
                URL url = new URL(link);
                InputStream is = url.openConnection().getInputStream();
                StringBuffer buffer = new StringBuffer();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    buffer.append(line + "\n");
                }
                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                parseJSon(res);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseJSon(String data) throws JSONException {
        if (data == null)
            return;
        List<Route> routes = new ArrayList<Route>();
        JSONObject jsonData = new JSONObject(data);
        JSONArray jsonRoutes = jsonData.getJSONArray("routes");
        for (int i = 0; i < jsonRoutes.length(); i++) {
            JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
            Route route = new Route();

            JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
            JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
            JSONObject jsonLegStart = jsonLegs.getJSONObject(0);
            JSONObject jsonLegEnd = jsonLegs.getJSONObject(jsonLegs.length()-1);

            JSONObject jsonBounds = jsonRoute.getJSONObject("bounds");
            JSONObject jsonNE = jsonBounds.getJSONObject("northeast");
            JSONObject jsonSO = jsonBounds.getJSONObject("southwest");

            JSONObject jsonEndLocation = jsonLegEnd.getJSONObject("end_location");
            JSONObject jsonStartLocation = jsonLegStart.getJSONObject("start_location");

            Distance distance = new Distance("No string data for path with waypoint, just waypoint", 0);
            Duration duration = new Duration("No string data for path with waypoint, just waypoint", 0);
            for (int y=0; i<jsonLegs.length();i++){
                JSONObject jsO = (JSONObject) jsonLegs.get(y);
                JSONObject jsODuration = jsO.getJSONObject("duration");
                JSONObject jsODistance = jsO.getJSONObject("distance");
                duration.value = duration.value + jsODuration.getInt("value");
                distance.value = distance.value + jsODistance.getInt("value");
            }
            route.distance = distance;
            route.duration = duration;

            route.northeast = new LatLng(jsonNE.getDouble("lat"), jsonNE.getDouble("lng"));
            route.southwest = new LatLng(jsonSO.getDouble("lat"), jsonSO.getDouble("lng"));
            route.endAddress = jsonLegEnd.getString("end_address");
            route.startAddress = jsonLegStart.getString("start_address");
            route.startLocation = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
            route.endLocation = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));
            route.points = decodePolyLine(overview_polylineJson.getString("points"));
            route.regions = calculateRegions(route);

            routes.add(route);
        }

        listener.onDirectionFinderSuccess(routes);
    }

    private List calculateRegions(Route r){
        List<Step> regions = new ArrayList<>();
        int distance;
        LatLng prec, succ;
        Step lastReg = null;
        for(int i=1; i<r.points.size(); i++){
            float [] dist = new float[1];
            prec = r.points.get(i-1);
            succ = r.points.get(i);
            Location.distanceBetween(prec.latitude, prec.longitude, succ.latitude, succ.longitude, dist);
            distance = (int)dist[0];
            if(lastReg!=null && lastReg.distance+distance>Route.SUGGESTED_REGION_SIZE){
                lastReg.addMargin(Route.BOUNDS_FOR_REGION);
            }
            if(lastReg==null || lastReg.distance+distance>Route.SUGGESTED_REGION_SIZE){//Max kilometer
                regions.add(new Step(prec, succ, distance));
                lastReg = regions.get(regions.size()-1);
            } else {
                lastReg.setNewEnd(succ, distance);
            }
        }
        regions.get(regions.size()-1).addMargin(Route.BOUNDS_FOR_REGION);
        return regions;
    }

    private List<Step> decodeSteps(JSONArray steps) throws JSONException {
        List<Step> trovati = new ArrayList<>();
        for(int i=0; i<steps.length(); i++){
            JSONObject step = steps.getJSONObject(i);
            JSONObject start = step.getJSONObject("start_location");
            JSONObject end = step.getJSONObject("end_location");
            JSONObject distance = step.getJSONObject("distance");
            trovati.add(
                    new Step(
                        new LatLng(start.getDouble("lat"), start.getDouble("lng")),
                        new LatLng(end.getDouble("lat"), end.getDouble("lng")),
                            distance.getInt("value")
                    )
            );
        }
        return trovati;
    }

    private List<LatLng> decodePolyLine(final String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }
}
