package it.drrek.fuelsort.control.route;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.drrek.fuelsort.entity.exception.NoPathFoundException;
import it.drrek.fuelsort.entity.settings.SearchParams;
import it.drrek.fuelsort.entity.station.Distributore;
import it.drrek.fuelsort.entity.route.Region;
import it.drrek.fuelsort.entity.route.Route;
import it.drrek.fuelsort.R;
import it.drrek.fuelsort.entity.station.DistributoreAsResult;
import it.drrek.fuelsort.model.DistributoriManager;
import it.drrek.fuelsort.model.SearchParamsModel;

import static android.content.Context.MODE_PRIVATE;

/**
 * Classe che si occupa della ricerca dei percorsi
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
        if(routeControlListener!=null){
            routeControlListener.startRouteSearch();
        }
        EditText toET = ((Activity)activityContext).findViewById(R.id.to);
        EditText fromET = ((Activity)activityContext).findViewById(R.id.from);
        from = fromET.getText().toString();
        to = toET.getText().toString();

        /* Cerco il percorso normale.
         */
        try {
            new DirectionFinderAsync(from, to, null, new DirectionFinderListener() {
                @Override
                public void onDirectionFinderStart() {
                    if(routeControlListener!=null){
                        routeControlListener.sendMessage("Inizio a cercare il percorso di base.");
                    }
                }

                /* Trovato il percorso normale cerco i distributori
                 */
                @Override
                public void onDirectionFinderSuccess(List<Route> routes) {
                    if (!routes.isEmpty()) {
                        Route r = routes.get(0);
                        new LoadStationForRoute().execute(r);
                        return;
                    }
                    if(routeControlListener!=null){
                        routeControlListener.exceptionSearchingForRoute(new NoPathFoundException("Errore nella ricerca del percorso di base. Prova a scegliere un punto di partenza e di destinazione migliori."));
                    }
                }

                @Override
                public void directionFinderException(Exception e) {
                    routeControlListener.exceptionSearchingForRoute(e);
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
        int capienzaSerbatoio;
        int kmxl;
        int autonomiaInMetri;
        int distributoriNecessari;
        int distanzaPercorso;
        List<Distributore> distributoriTrovati;
        final Map<Distributore, Integer> distributoriTrovatiConDistanza = new HashMap<>();
        List<Distributore> distributoriTrovatiAllaFine;
        Route bestRouteTillNow = null;
        DistributoriBounds[] gruppi;
        double[][] memoized;
        class Result {
            Route strada;
            List<DistributoreAsResult> distributori;

            Result(Route s, List<DistributoreAsResult> d){strada=s; distributori=d;}
        }
        class DistributoriBounds{
            private int start, end;
            private DistributoriBounds(int start, int end){
                this.start = start;
                this.end = end;
            }
        }

        @Override
        protected Result doInBackground(final Route... r) {

            if(routeControlListener!=null){
                routeControlListener.sendMessage("Ricerca di tutti i possibili distibutori in zona.");
            }

            abstract class ComputeSearchOnSingleRegion implements Runnable{
                private Map<Distributore, Integer> risultati;
                private Region s;
                ComputeSearchOnSingleRegion(Region s){
                    this.s=s;
                }
                Region getStep(){return s;}
                private Map<Distributore, Integer> getRisultati() {
                    return risultati;
                }
                void setRisultati(Map<Distributore, Integer> risultati) {
                    this.risultati = risultati;
                }
            }

            final DistributoriManager distManager = new DistributoriManager(activityContext);
            final SearchParams searchParams = SearchParamsModel.getSearchParams(activityContext);

            final Map<Thread, ComputeSearchOnSingleRegion> threads  = new HashMap<>();
            this.defaultRoute = r[0];
            for (final Region s : this.defaultRoute.getRegions()){
                ComputeSearchOnSingleRegion runnableTemp = new ComputeSearchOnSingleRegion(s){
                    @Override
                    public void run() {
                        List<Distributore> trovatiInRegion = distManager.getStationsInBound(this.getStep().getSOBound().latitude, this.getStep().getNEBound().latitude, this.getStep().getSOBound().longitude, this.getStep().getNEBound().longitude, searchParams, true, s.isToll());

                        Map<Distributore, Integer> risultati = new HashMap<>();
                        List<LatLng> puntiInRegion = s.getPoints();
                        int lunghezzaPerOra = s.getDistanceFromStart();
                        for (int i = 0; i < puntiInRegion.size() - 1; i++) {

                            final int R = 6371000; // Radius of the earth
                            double lat1 = puntiInRegion.get(i).latitude;
                            double lon1 = puntiInRegion.get(i).longitude;
                            double lat2 = puntiInRegion.get(i + 1).latitude;
                            double lon2 = puntiInRegion.get(i + 1).longitude;
                            lat1=Math.toRadians(lat1); lat2=Math.toRadians(lat2);
                            lon1=Math.toRadians(lon1); lon2=Math.toRadians(lon2);

                            double distanza12 = Math.acos( Math.sin(lat1)*Math.sin(lat2) + Math.cos(lat1)*Math.cos(lat2)*Math.cos(lon2-lon1) ) * R;

                            lunghezzaPerOra += distanza12;

                            for (Distributore distributoreCorrente : trovatiInRegion) {

                                double risultato;

                                double lat3 = distributoreCorrente.getLat();
                                double lon3 = distributoreCorrente.getLon();

                                lat3=Math.toRadians(lat3);
                                lon3=Math.toRadians(lon3);

                                double bear12 = Math.atan2( Math.sin(lon2-lon1)*Math.cos(lat2), Math.cos(lat1)*Math.sin(lat2) - Math.sin(lat1)*Math.cos(lat2)*Math.cos(lon2-lon1));
                                double bear13 = Math.atan2( Math.sin(lon3-lon1)*Math.cos(lat3), Math.cos(lat1)*Math.sin(lat3) - Math.sin(lat1)*Math.cos(lat3)*Math.cos(lon3-lon1));

                                double dis13 = Math.acos( Math.sin(lat1)*Math.sin(lat3) + Math.cos(lat1)*Math.cos(lat3)*Math.cos(lon3-lon1) ) * R;

                                if (Math.abs(bear13-bear12)>(Math.PI/2)){
                                    risultato=dis13;
                                } else {
                                    double dxt = Math.asin( Math.sin(dis13/R)* Math.sin(bear13 - bear12) ) * R;

                                    double dis14 = Math.acos( Math.cos(dis13/R) / Math.cos(dxt/R));

                                    if(dis14>distanza12){
                                        risultato = Math.acos( Math.sin(lat2)*Math.sin(lat3) + Math.cos(lat2)*Math.cos(lat3)*Math.cos(lon3-lon2) ) * R;
                                    } else{
                                        risultato = Math.abs(dxt);
                                    }
                                }

                                if (risultato < 25) { //TODO:RENDILA UNA VARIABILE STAICA E TROVA IL VALORE GIUSTO
                                    int distanzaDistributoreDaStart = (int) (lunghezzaPerOra + risultato);
                                    if (risultati.containsKey(distributoreCorrente)) {
                                        if (risultati.get(distributoreCorrente) > distanzaDistributoreDaStart) {
                                            risultati.put(distributoreCorrente, distanzaDistributoreDaStart);
                                        }
                                    } else {
                                        risultati.put(distributoreCorrente, distanzaDistributoreDaStart);
                                    }
                                }
                            }
                        }
                        setRisultati(risultati);
                    }
                };
                Thread threadTemp = new Thread(runnableTemp);
                threadTemp.start();
                threads.put(threadTemp, runnableTemp);
            }

            for(Thread t : threads.keySet()){
                try{
                    t.join();
                    distributoriTrovatiConDistanza.putAll(threads.get(t).getRisultati());
                } catch (Exception e){
                    Log.d("Severe warning", "Compute search on a region did something wrong\n"+ Arrays.toString(e.getStackTrace()));
                }
            }

            distributoriTrovati = new ArrayList<>(distributoriTrovatiConDistanza.keySet());

            SharedPreferences pref = activityContext.getSharedPreferences("it.unisa.luca.stradaalrisparmio.pref", MODE_PRIVATE);
            capienzaSerbatoio = pref.getInt("capienzaSerbatoio", 20);
            kmxl = pref.getInt("kmxl", 20);
            autonomiaInMetri = capienzaSerbatoio*kmxl*1000;

            distanzaPercorso = Math.max(defaultRoute.getDistance().getValue(), distributoriTrovatiConDistanza.get(distributoriTrovati.get(distributoriTrovati.size()-1)));
            distributoriNecessari = (int) Math.ceil(distanzaPercorso / (double) autonomiaInMetri);

            if(routeControlListener!=null) {
                if (distributoriNecessari == 1){
                    routeControlListener.sendMessage("Ricerca del miglior distributore nel percorso.");
                } else{
                    routeControlListener.sendMessage("Ricerca dei migliori "+distributoriNecessari+" distributori per poter raggiungere la destinazione.");
                }
            }

            boolean routeOk, stationStatus;
            do {
                stationStatus = findSetOfStation();
                routeOk  = false;
                if(stationStatus) {
                    routeOk = searchRouteBasedOnStationSet();
                }
                if(!routeOk){
                    if(routeControlListener!=null) {
                        routeControlListener.sendMessage("Uno dei distributori selezionati non era adatto, continuo la ricerca.");
                    }
                }
            } while(!routeOk);

            List<DistributoreAsResult> risultatiConInformazioni = new ArrayList<>();
            List<Integer> distanzePezziDiPercorso = bestRouteTillNow.getLegsDistance();
            for(int i=0; i<distanzePezziDiPercorso.size()-1; i++){
                int distanza = distanzePezziDiPercorso.get(i+1);
                Distributore di = distributoriTrovatiAllaFine.get(i);
                DistributoreAsResult oneResult = new DistributoreAsResult(di);

                oneResult.setKmPerProssimoDistributore(distanza/1000.0);
                oneResult.setLitriPerProssimoDistributore(distanza/(1000.0*kmxl));
                oneResult.setCostoBenzinaNecessaria((int)Math.ceil((distanza*di.getBestPriceUsingSearchParams())/(1000.0*kmxl)));
                if(i!=0)
                    oneResult.setPrev(risultatiConInformazioni.get(i-1));
                risultatiConInformazioni.add(oneResult);
            }

            return new Result(bestRouteTillNow, risultatiConInformazioni);
        }

        private boolean searchRouteBasedOnStationSet() {
            try {
                List<Route> resultList = new DirectionFinderSync(from, to, distributoriTrovatiAllaFine).execute();
                Route result = resultList.get(0);
                Log.d("RouteControl", "Analizzo il primo risultato: ");
                Log.d("RouteControl", "Lunghezza nuovo: " + result.getDistance().getValue() + "m Lunghezza vecchio:" + defaultRoute.getDistance().getValue() + "m");
                Log.d("RouteControl", "Autostrade nuovo: " + result.getNumeroDiPagamenti() + "  Autostrade vecchio:" + defaultRoute.getNumeroDiPagamenti() + " ");
                Log.d("RouteControl", "Durata nuovo: " + result.getDuration().getValue() + "m Durata vecchio:" + defaultRoute.getDuration().getValue() + "m");


                bestRouteTillNow = resultList.get(0);//TODO: CONTROLLA CHE LA STRADA VADA BENE ALTRIMENTI CAMBIA

                int distanzaTotale = 0;
                List<Integer> newLegsDistance = bestRouteTillNow.getLegsDistance();
                for(int indice = 0; indice<newLegsDistance.size(); indice++){
                    distanzaTotale += newLegsDistance.get(indice);
                    if(distributoriTrovatiConDistanza.get(distributoriTrovati.get(indice)) >= distanzaTotale + Route.DISTANZA_MASSIMA_AGGIUNTA_AL_PERCORSO){
                        Distributore d = distributoriTrovati.get(indice);
                        Log.d("RouteControl", "La strada non va bene per la distanza aggiunta al percorso. Rimuovo il distributore "+ d.getId() +" dai risultati e rieffettuo la ricerca");
                        distributoriTrovatiConDistanza.remove(d);
                        distributoriTrovati.remove(indice);
                        return false;
                    }
                }

                if (    result.getNumeroDiPagamenti() <= defaultRoute.getNumeroDiPagamenti() &&
                        result.getDuration().getValue() - defaultRoute.getDuration().getValue() <= Route.TEMPO_MASSIMO_AGGIUNTO_AL_PERCORSO*distributoriNecessari) {
                }{
                    Log.d("RouteControl", "La strada non va bene per il numero di autostrade o per la durata");
                    //TODO: Gestire meglio questo caso
                    return true;
                }
            } catch (UnsupportedEncodingException | JSONException e) {
                e.printStackTrace();
            }
            return true;
        }

        private boolean findSetOfStation() {
            if(distributoriTrovati == null || distributoriTrovati.size()==0){
                //TODO: Avvisa qualcuno
                return false;
            }

            Collections.sort(distributoriTrovati, new Comparator<Distributore>() {
                @Override
                public int compare(Distributore fruit1, Distributore fruit2)
                {
                    if(distributoriTrovatiConDistanza.get(fruit1)>distributoriTrovatiConDistanza.get(fruit2)){
                        return 1;
                    }
                    return -1;
                }
            });



            Log.d("RouteControl", "Distanza percorso: " + distanzaPercorso);
            Log.d("RouteControl", "Massima autonomia: " + autonomiaInMetri);
            Log.d("RouteControl", "Distributori necessari: " + distributoriNecessari);
            Log.d("RouteControl", "Distributori presenti: " + distributoriTrovati.size());

            /**
             * Definisco i gruppi, la soluzione finale conterà un distributore da ogni gruppo, se un distributore fa parte di più gruppi potrà partecipare una sola volta al risultato finale
             */
            gruppi = new DistributoriBounds[distributoriNecessari+1];
            for(int i = 0; i<distributoriTrovati.size(); i++){
                int lunghezza = distributoriTrovatiConDistanza.get(distributoriTrovati.get(i));

                int gruppoCorrente = 0;
                while (gruppoCorrente < distributoriNecessari) {
                    if(lunghezza > (distanzaPercorso - (autonomiaInMetri * (distributoriNecessari-gruppoCorrente))) && lunghezza < autonomiaInMetri*(gruppoCorrente+1)){
                        if(gruppi[gruppoCorrente] == null){
                            gruppi[gruppoCorrente] = new DistributoriBounds(i, i);
                        } else {
                            gruppi[gruppoCorrente].end = i;
                        }
                    }
                    gruppoCorrente++;
                }
            }
            gruppi[distributoriNecessari] = new DistributoriBounds(distributoriTrovati.size(), distributoriTrovati.size());

            /** Inizializzo array multidimensionale per contenere i risultati.*/
            memoized = new double[distributoriTrovati.size()][distributoriNecessari];
            for(int y = 0; y<distributoriNecessari; y++) {
                for (int i = 0; i < distributoriTrovati.size(); i++) {
                    memoized[i][y] = 99;
                }
            }

            opt(0, distributoriNecessari);
            distributoriTrovatiAllaFine = new ArrayList<>();
            findSolution(0, distributoriNecessari);

            if(distributoriTrovatiAllaFine == null || distributoriTrovatiAllaFine.size()==0){
                //TODO: Avvisa qualcuno
                return false;
            }
            return true;
        }

        private double opt(int indice, int distributoriMancanti){
            if(indice == distributoriTrovati.size()){
                if(distributoriMancanti == 0){
                    return 0;
                }else{
                    return 99;
                }
            }

            if(memoized[indice][distributoriMancanti-1] != 99)
                return memoized[indice][distributoriMancanti-1];

            Distributore d = distributoriTrovati.get(indice);

            if (gruppi[distributoriNecessari-(distributoriMancanti)].start > indice || indice > gruppi[distributoriNecessari-(distributoriMancanti)].end) {
                memoized[indice][distributoriMancanti-1] = 400;
                return memoized[indice][distributoriMancanti-1];
            }

            memoized[indice][distributoriMancanti-1] = 400;
            int distanzaIndiceDaStart = distributoriTrovatiConDistanza.get(d);
            for(int i=gruppi[distributoriNecessari-(distributoriMancanti-1)].start; i<=gruppi[distributoriNecessari-(distributoriMancanti-1)].end; i++){
                int distanzaTraIDue;
                if(i==distributoriTrovati.size())   distanzaTraIDue = distanzaPercorso - distanzaIndiceDaStart;
                else                                distanzaTraIDue = Math.abs(distributoriTrovatiConDistanza.get(distributoriTrovati.get(i)) - distanzaIndiceDaStart);
                if(distanzaTraIDue <= autonomiaInMetri ) {
                    int distanzaDaPagare;
                    if(distributoriMancanti == distributoriNecessari && i<distributoriTrovati.size())   distanzaDaPagare = distributoriTrovatiConDistanza.get(distributoriTrovati.get(i));
                    else if(distributoriMancanti == distributoriNecessari)                              distanzaDaPagare = distanzaPercorso;
                    else                                                                                distanzaDaPagare = distanzaTraIDue;

                    double temp = ((distanzaDaPagare*d.getBestPriceUsingSearchParams())/(1000*kmxl)) + opt(i, distributoriMancanti - 1);
                    memoized[indice][distributoriMancanti - 1] = Math.min(memoized[indice][distributoriMancanti - 1], temp);
                }
            }

            if(indice+1<=gruppi[distributoriNecessari-(distributoriMancanti)].end){
                opt(indice+1, distributoriMancanti);
            }
            return memoized[indice][distributoriMancanti - 1];
        }

        private void findSolution(int lastChoosenDistance, int distributoriMancanti) {
            int nextGroupBestIndex = gruppi[distributoriNecessari - (distributoriMancanti)].start;
            for (int i = gruppi[distributoriNecessari - (distributoriMancanti)].start; i <= gruppi[distributoriNecessari - (distributoriMancanti)].end; i++) {
                if (distributoriTrovatiConDistanza.get(distributoriTrovati.get(i)) - lastChoosenDistance <= autonomiaInMetri) {
                    if (memoized[nextGroupBestIndex][distributoriMancanti - 1] > memoized[i][distributoriMancanti - 1]) {
                        nextGroupBestIndex = i;
                    }
                }
            }
            distributoriTrovatiAllaFine.add(distributoriTrovati.get(nextGroupBestIndex));
            if (distributoriMancanti > 1) {
                findSolution(distributoriTrovatiConDistanza.get(distributoriTrovati.get(nextGroupBestIndex)), distributoriMancanti - 1);
            }
        }

        @Override
        protected void onPostExecute(Result r) {
            if(routeControlListener != null) {
                if(r.strada != null && r.distributori != null && r.distributori.size() > 0) {
                    routeControlListener.routeFound(r.strada, r.distributori);
                } else{
                    routeControlListener.exceptionSearchingForRoute(new NoPathFoundException("La ricerca di una strada con i distributori trovati non ha avuto successo"));
                }
            }
        }
    }
}
