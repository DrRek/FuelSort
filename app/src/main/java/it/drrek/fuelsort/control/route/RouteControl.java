package it.drrek.fuelsort.control.route;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.drrek.fuelsort.model.DatabaseManager;
import it.drrek.fuelsort.entity.station.Distributore;
import it.drrek.fuelsort.entity.route.Region;
import it.drrek.fuelsort.entity.route.Route;
import it.unisa.luca.fuelsort.R;

/**
 * Route manager is the class delegated to search for the best path.
 * Created by Luca on 09/12/2017.
 */

public class RouteControl {

    private Context activityContext;
    private RouteControlListener routeControlListener;
    private String from, to;

    public RouteControl(Context context) {
        activityContext = context;
    }

    /**
     * Questo metodo viene eseguito quando si deve iniziare la ricerca di un percorso
     */
    public void findRoute() {
        EditText toET = ((Activity)activityContext).findViewById(R.id.to);
        EditText fromET = ((Activity)activityContext).findViewById(R.id.from);
        from = fromET.getText().toString();
        to = toET.getText().toString();

        /* Cerco il percorso normale.
         */
        try {
            new DirectionFinderAsync(from, to, null, new DirectionFinderListener() {
                @Override
                public void onDirectionFinderStart() {}

                /* Trovato il percorso normale cerco i distributori
                 */
                @Override
                public void onDirectionFinderSuccess(List<Route> routes) {
                    Log.d("DirectionFinderSuccess", "Success");
                    if (!routes.isEmpty()) {
                        Route r = routes.get(0);
                        new LoadStationForRoute().execute(r);
                        System.out.println("ed ora inizio a cercare un percorso nuovo");
                    }
                }
            }).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setListener(RouteControlListener rml){
        this.routeControlListener = rml;
    }

    private class LoadStationForRoute extends AsyncTask<Route, Integer, LoadStationForRoute.Result> {
        private Route defaultRoute;
        class Result
        {
            public Route strada;
            public Distributore distributore;

            Result(Route s, Distributore d){strada=s; distributore=d;}
        };

        @Override
        protected Result doInBackground(Route... r) {
            List<Distributore> results = new ArrayList<>();

            abstract class ComputeSearchOnSingleRegion implements Runnable{
                private Region s;
                private List<Distributore> res;
                ComputeSearchOnSingleRegion(Region s){
                    this.s=s;
                }
                void setResults(List<Distributore> res){this.res=res;}
                List<Distributore> getResults(){
                    return res;
                }
                Region getStep(){return s;}
            }
            final DatabaseManager databaseManager = new DatabaseManager(activityContext);
            final DatabaseManager.SearchParams searchParams = databaseManager.getSearchParams();

            Map<Thread, ComputeSearchOnSingleRegion> threads  = new HashMap<>();
            this.defaultRoute =r[0];
            for (Region s : this.defaultRoute.getRegions()){
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

            System.out.println("Results size" + results.size());
            while(results.size() != 0){
                Distributore d = results.get(0);
                try {
                    List<Route> resultList = new DirectionFinderSync(from, to, d.getPosizione()).execute();
                    Route result = resultList.get(0);
                    System.out.println("Analizzo il primo risultato: ");
                    System.out.println("Lunghezza nuovo: "+result.getDistance().getValue()+"m Lunghezza vecchio:"+ defaultRoute.getDistance().getValue()+"m");
                    System.out.println("Autostrade nuovo: "+result.getNumeroDiPagamenti()+"  Autostrade vecchio:"+ defaultRoute.getNumeroDiPagamenti()+" ");
                    if(     result.getDistance().getValue() - defaultRoute.getDistance().getValue() <= Route.DISTANZA_MASSIMA_AGGIUNTA_AL_PERCORSO &&
                            result.getNumeroDiPagamenti() <= defaultRoute.getNumeroDiPagamenti() &&
                            result.getDuration().getValue() - defaultRoute.getDuration().getValue() <= Route.TEMPO_MASSIMO_AGGIUNTO_AL_PERCORSO){
                        System.out.println("Risultato accettabile.");
                        return new Result(result, d);
                    } else{
                        System.out.println("Risultato NON accettabile, passo al prossimo. \n");
                        results.remove(d);
                    }
                } catch (UnsupportedEncodingException | JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Result r) {
            routeControlListener.routeFound(r.strada, r.distributore);
        }
    }
}
