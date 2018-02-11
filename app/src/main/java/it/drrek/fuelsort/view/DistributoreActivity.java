package it.drrek.fuelsort.view;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import it.drrek.fuelsort.R;
import it.drrek.fuelsort.entity.station.Distributore;
import it.drrek.fuelsort.entity.station.Pompa;
import it.drrek.fuelsort.support.BitmapCreator;

/**
 * Created by Luca on 05/02/2018.
 */
public class DistributoreActivity extends AppCompatActivity {
    Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.distributore_layout);

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

        Intent i = getIntent();
        Distributore toUse = (Distributore) i.getSerializableExtra("distributore");

        System.out.println(toUse.getBandiera());
        System.out.println(toUse.getBestPriceUsingSearchParams());
        System.out.println(toUse.getComune());
        System.out.println(toUse.getGestore());
        System.out.println(toUse.getId());
        System.out.println(toUse.getIndirizzo());
        System.out.println(toUse.getLat());
        System.out.println(toUse.getLon());
        System.out.println(toUse.getNome());
        System.out.println(toUse.getProvincia());
        System.out.println(toUse.getTipoImpianto());

        for(Pompa p : toUse.getPompe()){
            System.out.println("Pompa:");
            System.out.println(p.getCarburante());
            System.out.println(p.getId());
            System.out.println(p.getLatestUpdate());
            System.out.println(p.getPrezzo());
            System.out.println(p.isSelf());
            System.out.println(p.toString());
        }

        ImageView bandieraIv = (ImageView) findViewById(R.id.bandiera_img);
        bandieraIv.setImageBitmap(BitmapCreator.getBitmapBandiera(this, toUse.getBandiera()));

        TextView bandieraTv = (TextView) findViewById(R.id.bandiera);
        bandieraTv.setText(toUse.getBandiera());

        TextView locationTv = (TextView) findViewById(R.id.location);
        locationTv.setText(toUse.getComune()+" ("+toUse.getProvincia()+")");

        TableLayout tblPompe = (TableLayout) findViewById(R.id.table_pompe);
        for(Pompa p : toUse.getPompe()){

            TableRow toInflate = (TableRow) View.inflate(this, R.layout.pomp_layout, null);

            ImageView img = (ImageView) toInflate.findViewById(R.id.image);
            img.setImageBitmap(BitmapCreator.getBitmapTipoCarburante(this, p.getCarburante()));
            TextView carburante = (TextView) toInflate.findViewById(R.id.carburante);
            carburante.setText(p.getCarburante().trim());
            TextView prezzo = (TextView) toInflate.findViewById(R.id.prezzo);
            prezzo.setText(p.getPrezzo()+"€");
            TextView self = (TextView) toInflate.findViewById(R.id.self);
            if(p.isSelf()){
                self.setText("Self-service");
            }else{
                self.setText("            ");
            }
            tblPompe.addView(toInflate);
        }

        TextView proprietarioTv = (TextView) findViewById(R.id.proprietario);
        proprietarioTv.setText("ID:"+toUse.getId()+" Nome: " + toUse.getNome());

        TextView coordinateTv = (TextView) findViewById(R.id.coordinate);
        coordinateTv.setText("Coordinate: " + toUse.getLat()+", "+toUse.getLon());
     }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.explode_implode_no_anim, R.anim.implode_center);
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
}
