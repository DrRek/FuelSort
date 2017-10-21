package it.unisa.luca.stradaalrisparmio;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * Created by luca on 21/10/17.
 */

public class SettingsActivity extends FragmentActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_layout);
        SharedPreferences pref = getSharedPreferences("it.unisa.luca.stradaalrisparmio.pref", MODE_PRIVATE);
        String prefCarburante = pref.getString("carburante", "diesel");
        boolean prefSelf = pref.getBoolean("self", true);
        int prefKmxl = pref.getInt("kmxl", 20);

        if(prefCarburante.equalsIgnoreCase("benzina")) {
            RadioButton v = findViewById(R.id.carburante_benzina);
            v.setChecked(true);
        } else if(prefCarburante.equalsIgnoreCase("diesel")) {
            RadioButton v = findViewById(R.id.carburante_diesel);
            v.setChecked(true);
        } else if(prefCarburante.equalsIgnoreCase("gpl")) {
            RadioButton v = findViewById(R.id.carburante_gpl);
            v.setChecked(true);
        } else if(prefCarburante.equalsIgnoreCase("metano")) {
            RadioButton v = findViewById(R.id.carburante_metano);
            v.setChecked(true);
        }

        if(prefSelf){
            RadioButton v = findViewById(R.id.self_true);
            v.setChecked(true);
        }else {
            RadioButton v = findViewById(R.id.self_false);
            v.setChecked(true);
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
        edit.putString("carburante", ((RadioButton)findViewById(((RadioGroup)findViewById(R.id.carburante)).getCheckedRadioButtonId())).getText()+"");
        if((((RadioButton)findViewById(((RadioGroup)findViewById(R.id.self)).getCheckedRadioButtonId())).getText()+"").equalsIgnoreCase("si")){
            edit.putBoolean("self", true);
        } else{
            edit.putBoolean("self", false);
        }
        edit.putInt("kmxl", Integer.parseInt(((EditText)findViewById(R.id.kmxl)).getText()+""));
        edit.commit();
    }
}
