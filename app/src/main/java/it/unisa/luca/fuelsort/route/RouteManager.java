package it.unisa.luca.fuelsort.route;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unisa.luca.fuelsort.gasstation.database.DatabaseManager;
import it.unisa.luca.fuelsort.gasstation.entity.Distributore;
import it.unisa.luca.fuelsort.route.api.DirectionFinder;
import it.unisa.luca.fuelsort.route.api.DirectionFinderListener;
import it.unisa.luca.fuelsort.route.entity.Route;
import it.unisa.luca.fuelsort.route.entity.Step;
import it.unisa.luca.stradaalrisparmio.R;

/**
 * Route manager is the class delegated to search for the best path.
 * Created by Luca on 09/12/2017.
 */

public class RouteManager {

    private Context activityContext;
    private RouteManagerListener routeManagerListener;
    private String from, to;

    public RouteManager(Context context) {
        activityContext = context;
    }

    public void findRoute() {
        EditText toET = ((Activity)activityContext).findViewById(R.id.to);
        EditText fromET = ((Activity)activityContext).findViewById(R.id.from);
        from = fromET.getText().toString();
        to = toET.getText().toString();

        try {
            new DirectionFinder(from, to, null, new DirectionFinderListener() {
                @Override
                public void onDirectionFinderStart() {}

                @Override
                public void onDirectionFinderSuccess(List<Route> routes) {
                    Log.d("DirectionFinderSuccess", "Success");
                    if (!routes.isEmpty()) {
                        Route r = routes.get(0);
                        new LoadStationForRoute().execute(r);
                    }
                }
            }).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setListener(RouteManagerListener rml){
        this.routeManagerListener = rml;
    }

    private List<Distributore> searchStationInRoute(Route r){
        List<Distributore> results = new ArrayList<>();

        abstract class ComputeSearchOnSingleRegion implements Runnable{
            private Step s;
            private List<Distributore> res;
            ComputeSearchOnSingleRegion(Step s){
                this.s=s;
            }
            void setResults(List<Distributore> res){this.res=res;}
            private List<Distributore> getResults(){
                return res;
            }
            Step getStep(){return s;}
        }
        final DatabaseManager databaseManager = new DatabaseManager(activityContext);
        final DatabaseManager.SearchParams searchParams = databaseManager.getSearchParams();

        Map<Thread, ComputeSearchOnSingleRegion> threads  = new HashMap<>();
        for (Step s : r.getRegions()){
            ComputeSearchOnSingleRegion runnableTemp = new ComputeSearchOnSingleRegion(s){
                @Override
                public void run() {
                    this.setResults(databaseManager.getStationsInBound(this.getStep().getSOBound().latitude,this.getStep().getNEBound().latitude,this.getStep().getSOBound().longitude,this.getStep().getNEBound().longitude, searchParams, true));
                }
            };
            Thread threadTemp = new Thread(runnableTemp);
            threadTemp.start();
            threads.put(threadTemp, runnableTemp);
        }

        for(Thread t : threads.keySet()){
            try{
                t.join();
                results.addAll(threads.get(t).getResults());
            } catch (Exception e){
                Log.d("Severe warning", "Compute search on a region did something wrong\n"+ Arrays.toString(e.getStackTrace()));
            }
        }

        Collections.sort(results, new Comparator<Distributore>() {
            @Override
            public int compare(Distributore fruit2, Distributore fruit1)
            {
                double f1Price = fruit1.getBestPriceUsingSearchParams(), f2Price = fruit2.getBestPriceUsingSearchParams();
                if(f1Price<f2Price) return 1;
                else if(f1Price==f2Price) return 0;
                return -1;
            }
        });

        return results;
    }

    private class LoadStationForRoute extends AsyncTask<Route, Integer, List<Distributore>> {
        @Override
        protected List<Distributore> doInBackground(Route... r) {
            return searchStationInRoute(r[0]);
        }

        @Override
        protected void onPostExecute(List<Distributore> results) {
            if (results != null) {
                final Distributore d = results.get(0);
                try {
                    new DirectionFinder(from, to, d.getPosizione(), new DirectionFinderListener() {
                        @Override
                        public void onDirectionFinderStart() {
                        }

                        @Override
                        public void onDirectionFinderSuccess(List<Route> routes) {
                            Log.d("DirectionFinderSuccess", "Success");
                            if (!routes.isEmpty()) {
                                Route r = routes.get(0);
                                if(routeManagerListener!=null)routeManagerListener.routeFound(r, d);
                            }
                        }
                    }).execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
