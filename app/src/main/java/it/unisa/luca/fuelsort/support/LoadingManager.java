package it.unisa.luca.fuelsort.support;

import android.app.Activity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import it.unisa.luca.stradaalrisparmio.R;

/**
 * Used to display information on background work to the user.
 * Created by Luca on 2017-10-10.
 */

public class LoadingManager {
    private static LoadingManager loader;

    public static LoadingManager getLoader(Activity activity){
        if(loader == null){
            synchronized (LoadingManager.class){
                if(loader == null){
                    loader = new LoadingManager(activity);
                }
            }
        }
        return loader;
    }

    private LoadingManager(Activity activity){
        this.activity = activity;
        acts = new ArrayList<>();
    }

    public synchronized void add(final String act){
        Log.d("Aggiungo", act);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(acts.isEmpty()){
                    activity.findViewById(R.id.LoadingLayout).setVisibility(LinearLayout.VISIBLE);
                }
                ((TextView) activity.findViewById(R.id.LoadingTextView)).setText(act);
            }
        });
        acts.add(0, act);
    }

    public synchronized void remove(String act){
        Log.d("Rimuovo", act);
        acts.remove(act);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(acts.isEmpty()){
                    activity.findViewById(R.id.LoadingLayout).setVisibility(LinearLayout.INVISIBLE);
                } else{
                    ((TextView) activity.findViewById(R.id.LoadingTextView)).setText(acts.get(0));
                }
            }
        });

    }

    private Activity activity;
    private ArrayList<String> acts;
}
