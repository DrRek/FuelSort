package it.unisa.luca.stradaalrisparmio;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * I
 * Created by luca on 21/10/17.
 */

public class SettingsActivity extends FragmentActivity implements AdapterView.OnItemSelectedListener {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        SharedPreferences pref = getSharedPreferences("it.unisa.luca.stradaalrisparmio.pref", MODE_PRIVATE);
        String prefCarburante = pref.getString("carburante", "diesel");
        boolean prefSelf = pref.getBoolean("self", true);
        int prefKmxl = pref.getInt("kmxl", 20);

        Spinner spinner = findViewById(R.id.tipiCarburantiSpinner);
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

        CheckBox v = findViewById(R.id.self);
        if(prefSelf){
            v.setChecked(true);
        }else {
            v.setChecked(false);
        }

        ((EditText)findViewById(R.id.kmxl)).setText(prefKmxl+"", EditText.BufferType.EDITABLE);
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
        edit.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
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
        edit.commit();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}
}
