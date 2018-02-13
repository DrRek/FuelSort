package it.drrek.fuelsort.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
import it.drrek.fuelsort.entity.exception.NoDataForPathException;
import it.drrek.fuelsort.entity.exception.UnableToUpdateException;

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
                Log.d("SettingsActivity","Backstack changed");
            }
        });

        SharedPreferences pref = getSharedPreferences("it.unisa.luca.stradaalrisparmio.pref", MODE_PRIVATE);
        String prefCarburante = pref.getString("carburante", "diesel");
        boolean prefSelf = pref.getBoolean("self", true);
        int capienzaSerbatoio = pref.getInt("capienzaSerbatoio", 20);
        int kmxl = pref.getInt("kmxl", 20);
        final int l100km = 100/kmxl;

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
                    public void exceptionUpdatingData(Exception e) {
                        onEndPriceUpdate();
                        onEndStationUpdate();
                        HandleExceptionAsListener(e);
                    }

                    @Override
                    public void onEndStationUpdate() {
                        end2=true;
                        checkForEnd();
                    }

                    private void checkForEnd(){
                        if(end1 && end2) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    pb.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                });
                dataUpdaterControl.start();
            }
        });

        EditText et = ((EditText)findViewById(R.id.capacita_serbatoio));
        et.getBackground().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null), PorterDuff.Mode.SRC_ATOP);
        et.setText(String.valueOf(capienzaSerbatoio), EditText.BufferType.EDITABLE);
        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    textView.clearFocus();
                    closeKeyboard();
                    return true;
                }
                return false;
            }
        });

        final EditText et1 = ((EditText)findViewById(R.id.kmxl));
        et1.getBackground().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null), PorterDuff.Mode.SRC_ATOP);
        et1.setText(String.valueOf(kmxl), EditText.BufferType.EDITABLE);

        final EditText et2 = ((EditText)findViewById(R.id.l100km));
        et2.getBackground().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null), PorterDuff.Mode.SRC_ATOP);
        et2.setText(String.valueOf(l100km), EditText.BufferType.EDITABLE);

        et1.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    et2.setText(String.valueOf(100/Integer.parseInt(((EditText)view).getText().toString())), EditText.BufferType.EDITABLE);
                    closeKeyboard();
                    return true;
                }
                return false;
            }
        });

        et2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    et1.setText(String.valueOf(100/Integer.parseInt(((EditText)view).getText().toString())), EditText.BufferType.EDITABLE);
                    closeKeyboard();
                    return true;
                }
                return false;
            }
        });
    }

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
        edit.putInt("capienzaSerbatoio", Integer.parseInt(((EditText)findViewById(R.id.capacita_serbatoio)).getText()+""));
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

    public void HandleExceptionAsListener(Exception e){
        if(e instanceof UnableToUpdateException){
            Log.e("SettingsActivity", "Impossibile aggiornare.");
            final AlertDialog.Builder builder = new AlertDialog.Builder((Context) this);
            builder.setMessage(e.getMessage())
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            // Create the AlertDialog object and return it
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Dialog d = builder.create();
                    d.show();
                }
            });
        }else {
            e.printStackTrace();
        }
    }

    public void closeKeyboard(){
        //Solo per chiudere la tastiera
        View et3 = getCurrentFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (et3 != null) {
            //se è un edit text lo riporta alle dimensioni normali
            et3.clearFocus();
            if (imm != null) {
                imm.hideSoftInputFromWindow(et3.getWindowToken(), 0);
            }
        }
    }
}
