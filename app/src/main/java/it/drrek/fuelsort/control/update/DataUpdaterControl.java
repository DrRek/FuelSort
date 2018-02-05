package it.drrek.fuelsort.control.update;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.drrek.fuelsort.entity.exception.UnableToUpdateException;
import it.drrek.fuelsort.model.DataImporterManager;
import it.drrek.fuelsort.model.DatabaseCreator;
import it.drrek.fuelsort.model.DistributoriManager;
import it.drrek.fuelsort.model.PompeManager;


/**
 * Created by luca on 08/10/17.
 * Da continuare
 */

public class DataUpdaterControl extends Thread{
    private DatabaseCreator dbhelper;
    private DataUpdaterControlListener dataUpdaterControlListener;
    private String distributoriNewData, pompeNewData;
    private boolean forceUpdate;
    private Context activityContext;

    public DataUpdaterControl(Context ctx) {
        activityContext = ctx;
        dbhelper=new DatabaseCreator(ctx);
        forceUpdate = false;
    }

    public void setDataUpdaterControlListener(DataUpdaterControlListener dul){
        this.dataUpdaterControlListener = dul;
    }

    public void setForceUpdate(boolean force){
        this.forceUpdate=force;
    }

    @Override
    public void run() {
        Log.d("DataUpdaterControl", "Inizio l'aggiornamento dei dati. Aggiornamento forzato: "+forceUpdate);
        updateData();
    }

    private void updateData() {
        DataImporterManager browser = new DataImporterManager();
        DistributoriManager distManager = new DistributoriManager(activityContext);
        PompeManager pompManager = new PompeManager(activityContext);

        try {
            //In questo caso forzo il download dei dati
            if (forceUpdate) {
                forceUpdate = false;
                distributoriNewData = browser.retrieve(DataImporterManager.DISTRIBUTORI_PATH);
                pompeNewData = browser.retrieve(DataImporterManager.POMPE_PATH);
                if (dataUpdaterControlListener != null)
                    dataUpdaterControlListener.onStartStationUpdate();
                distManager.retrieveUpdatedDistributori(distributoriNewData);
                if (dataUpdaterControlListener != null)
                    dataUpdaterControlListener.onEndStationUpdate();
                if (dataUpdaterControlListener != null)
                    dataUpdaterControlListener.onStartPriceUpdate();
                pompManager.retrieveUpdatedPompe(pompeNewData);
                if (dataUpdaterControlListener != null)
                    dataUpdaterControlListener.onEndPriceUpdate();
                return;
            }

            Date thisMorning = null, distributoriDate = null, pompeDate = null;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.US);
                thisMorning = sdf1.parse(sdf.format(new Date()) + " 08:00");
                distributoriDate = thisMorning;
                pompeDate = thisMorning;
                Matcher m = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})").matcher(distManager.getDistributoriCurrentVersion());
                while (m.find()) {
                    distributoriDate = sdf.parse(m.group(1));
                }
                m = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})").matcher(pompManager.getPompeCurrentVersion());
                while (m.find()) {
                    pompeDate = sdf.parse(m.group(1));
                }
            } catch (ParseException e) {
                Log.e("DataUpdaterControl", "Errore cercando di controllare la necessità di aggiornamenti con la data odierna");
            }

            if (thisMorning == null || distributoriDate == null || thisMorning.after(distributoriDate)) {
                distributoriNewData = browser.retrieve(DataImporterManager.DISTRIBUTORI_PATH);
                if (isToUpdateDistributori(distManager)) {
                    if (dataUpdaterControlListener != null)
                        dataUpdaterControlListener.onStartStationUpdate();
                    distManager.retrieveUpdatedDistributori(distributoriNewData);
                    if (dataUpdaterControlListener != null)
                        dataUpdaterControlListener.onEndStationUpdate();
                }
            }
            if (thisMorning == null || pompeDate == null || thisMorning.after(pompeDate)) {
                pompeNewData = browser.retrieve(DataImporterManager.POMPE_PATH);
                if (isToUpdatePompe(pompManager)) {
                    if (dataUpdaterControlListener != null)
                        dataUpdaterControlListener.onStartPriceUpdate();
                    pompManager.retrieveUpdatedPompe(pompeNewData);
                    if (dataUpdaterControlListener != null)
                        dataUpdaterControlListener.onEndPriceUpdate();
                }
            }
        } catch (IOException e){
            dataUpdaterControlListener.exceptionUpdatingData(new UnableToUpdateException("Errore durante l'aggiornamento dei dati, le informazioni mostrate potrebbero non essere corrette. Assicurati di avere una connessione ad internet."));
        }
    }

    private boolean isToUpdateDistributori(DistributoriManager distManager){
        String distributoriLatestVersion = distributoriNewData.substring(0, distributoriNewData.indexOf("\n"));
        return !distManager.getDistributoriCurrentVersion().equalsIgnoreCase(distributoriLatestVersion);
    }

    private boolean isToUpdatePompe(PompeManager pompManager){
        String pompeLatestVersion = pompeNewData.substring(0, pompeNewData.indexOf("\n"));
        return !pompManager.getPompeCurrentVersion().equalsIgnoreCase(pompeLatestVersion);
    }








}
