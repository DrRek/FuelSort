package it.drrek.fuelsort.view;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

import it.drrek.fuelsort.R;
import it.drrek.fuelsort.control.update.DataUpdaterControl;
import it.drrek.fuelsort.control.update.DataUpdaterControlListener;

/**
 * I
 * Created by luca on 21/10/17.
 */

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Context ctx;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        ctx = this;

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);

        setSupportActionBar(myToolbar);
        if(getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Log.d("tset","test");
            }
        });

        SharedPreferences pref = getSharedPreferences("it.unisa.luca.stradaalrisparmio.pref", MODE_PRIVATE);
        String prefCarburante = pref.getString("carburante", "diesel");
        boolean prefSelf = pref.getBoolean("self", true);
        int prefKmxl = pref.getInt("kmxl", 20);

        Spinner spinner = (Spinner) findViewById(R.id.tipiCarburantiSpinner);
        //Set spinner color
        spinner.getBackground().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null), PorterDuff.Mode.SRC_ATOP);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.tipi_benzina_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        if(prefCarburante.equalsIgnoreCase("benzina")) {
            spinner.setSelection(0);
        } else if(prefCarburante.equalsIgnoreCase("diesel")) {
            spinner.setSelection(1);
        } else if(prefCarburante.equalsIgnoreCase("gpl")) {
            spinner.setSelection(2);
        } else if(prefCarburante.equalsIgnoreCase("metano")) {
            spinner.setSelection(3);
        }

        final CheckBox c = (CheckBox) findViewById(R.id.self);
        if(prefSelf){
            c.setChecked(true);
        }else {
            c.setChecked(false);
        }

        RelativeLayout tv = (RelativeLayout) findViewById(R.id.force_update_layout);
        tv.setClickable(true);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressBar pb = (ProgressBar) findViewById(R.id.force_update_progress);
                pb.setVisibility(View.VISIBLE);
                DataUpdaterControl dataUpdaterControl = new DataUpdaterControl(ctx);
                dataUpdaterControl.setForceUpdate(true);
                dataUpdaterControl.setDataUpdaterControlListener(new DataUpdaterControlListener(){
                    boolean end1 = false, end2 = false;
                    @Override
                    public void onStartPriceUpdate() {
                    }
                    @Override
                    public void onStartStationUpdate() {
                    }
                    @Override
                    public void onEndPriceUpdate() {
                        end1=true;
                        checkForEnd();
                    }
                    @Override
                    public void onEndStationUpdate() {
                        end2=true;
                        checkForEnd();
                    }

                    private void checkForEnd(){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pb.setVisibility(View.GONE);
                            }
                        });
                    }
                });
                dataUpdaterControl.start();
                /*try {
                    //dataUpdaterControl.join();

                } catch (InterruptedException e) {
                    Log.e("SettingsActivity", "Errore facendo il join del thread per aggiornare il database");
                    e.printStackTrace();
                }*/
            }
        });

        EditText et = ((EditText)findViewById(R.id.kmxl));
        //Set spinner color
        et.getBackground().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null), PorterDuff.Mode.SRC_ATOP);
        et.setText(String.valueOf(prefKmxl), EditText.BufferType.EDITABLE);
    }

    /**
     * Futuro Luca mi dispiace ma ero troppo pigro per scrivere questa funzione idiota in maniera più leggibile.
     * Tanto lo so che ti piacciono le parentesi.
     */
    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences pref = getSharedPreferences("it.unisa.luca.stradaalrisparmio.pref", MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        if(((CheckBox)findViewById(R.id.self)).isChecked()){
            edit.putBoolean("self", true);
        } else{
            edit.putBoolean("self", false);
        }
        edit.putInt("kmxl", Integer.parseInt(((EditText)findViewById(R.id.kmxl)).getText()+""));
        edit.apply();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        SharedPreferences pref = getSharedPreferences("it.unisa.luca.stradaalrisparmio.pref", MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        Log.e("test", i+"");
        if(i==0){
            edit.putString("carburante", "benzina");
        }else if(i==1){
            edit.putString("carburante", "diesel");
        }else if(i==2){
            edit.putString("carburante", "gpl");
        }else if(i==3){
            edit.putString("carburante", "metano");
        }
        edit.apply();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}
}
