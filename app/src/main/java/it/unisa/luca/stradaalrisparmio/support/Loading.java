package it.unisa.luca.stradaalrisparmio.support;

import android.app.Activity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import it.unisa.luca.stradaalrisparmio.R;

/**
 * Created by Luca on 2017-10-10.
 */

public class Loading {
    public static Loading loader;

    public static Loading getLoader(Activity activity){
        if(loader == null){
            synchronized (Loading.class){
                if(loader == null){
                    loader = new Loading(activity);
                }
            }
        }
        return loader;
    }

    public Loading(Activity activity){
        this.activity = activity;
        acts = new ArrayList<String>();
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
