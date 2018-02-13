package it.drrek.fuelsort.model;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import it.drrek.fuelsort.entity.settings.SearchParams;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Luca on 05/02/2018.
 */

public class SearchParamsModel {

    public static SearchParams getSearchParams(Context activityContext) {
        SharedPreferences pref = activityContext.getSharedPreferences("it.unisa.luca.stradaalrisparmio.pref", MODE_PRIVATE);
        String prefCarburante = pref.getString("carburante", "diesel");
        boolean prefSelf = pref.getBoolean("self", true);
        return new SearchParams(prefCarburante, prefSelf);
    }

}
