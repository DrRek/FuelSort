package it.drrek.fuelsort.view;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import it.drrek.fuelsort.R;
import it.drrek.fuelsort.entity.station.Distributore;
import it.drrek.fuelsort.entity.station.DistributoreAsResult;

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.GONE;

/**
 * Created by Luca on 17/02/2018.
 */

public class DistributoreAsResultFragment extends Fragment {
    private DistributoreAsResult distributore;
    private DistributoreAsResultFragmentListener listener;

    public DistributoreAsResultFragment(){}

    public void setDistributore(Distributore d){
        this.distributore = (DistributoreAsResult) d;
    }

    public void setListener(DistributoreAsResultFragmentListener listener){
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_distribure_as_result, container, false);

        ViewTreeObserver vto = v.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                v.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                ImageView contatore = v.findViewById(R.id.contatore_img);
                ImageView lancetta = v.findViewById(R.id.lancetta);

                lancetta.setX(contatore.getRight()-(contatore.getWidth()/2) - lancetta.getWidth());
                lancetta.setY(contatore.getY()-20+contatore.getBottom()-lancetta.getHeight()/2);
                lancetta.invalidate();

                FrameLayout background = v.findViewById(R.id.contatore);

                background.getLayoutParams().height = background.getHeight() + (lancetta.getHeight()/2);
                background.requestLayout();
                
                ImageView palla_lancetta = new ImageView(getActivity());
                palla_lancetta.setImageResource(R.drawable.lancetta_palla);
                palla_lancetta.setLayoutParams(new FrameLayout.LayoutParams(lancetta.getHeight(),lancetta.getHeight()));
                palla_lancetta.setX(lancetta.getX()+lancetta.getWidth()-(lancetta.getHeight()/2));
                palla_lancetta.setY(lancetta.getY()+lancetta.getHeight()/2 -(lancetta.getHeight()/2));

                //background.addView(palla_lancetta, 100);
                background.addView(palla_lancetta);

                int capienzaSerbatoio = getActivity().getSharedPreferences("it.unisa.luca.stradaalrisparmio.pref", MODE_PRIVATE).getInt("capienzaSerbatoio", 20);

                float angolo = (float) distributore.getLitriPerProssimoDistributore() * 180f / capienzaSerbatoio;

                RotateAnimation anim = new RotateAnimation(0f, angolo, lancetta.getX()+lancetta.getWidth(), lancetta.getY()+(lancetta.getHeight()/2));
                anim.setInterpolator(new LinearInterpolator());
                anim.setDuration(1000);
                anim.setFillAfter(true);

                lancetta.startAnimation(anim);

            }
        });

        NumberFormat formatter = new DecimalFormat("#0.00");
        ((TextView)v.findViewById(R.id.km_per_prossimo_distributore)).setText(String.valueOf(formatter.format(distributore.getKmPerProssimoDistributore())));
        ((TextView)v.findViewById(R.id.lt_per_prossimo_distributore)).setText(String.valueOf(formatter.format(distributore.getLitriPerProssimoDistributore())));
        ((TextView)v.findViewById(R.id.tv_denaro_necessario)).setText(String.valueOf("Assicurati di avere almeno " + formatter.format(distributore.getCostoBenzinaNecessaria()) +"€ di benzina per poter raggiungere la prossima destinazione."));


        v.findViewById(R.id.chiudi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener!=null)
                    listener.close();
            }
        });

        v.findViewById(R.id.left_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        v.findViewById(R.id.right_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        v.findViewById(R.id.more_info_distributore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener!=null)
                    listener.info();
            }
        });

        TextView next = v.findViewById(R.id.right_arrow);
        if(distributore.getNext() != null){
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.next();
                }
            });
        }else{
            next.setVisibility(TextView.GONE);
        }

        TextView prev = v.findViewById(R.id.left_arrow);
        if(distributore.getPrev() != null){
            prev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.prev();
                }
            });
        }else{
            prev.setVisibility(TextView.GONE);
        }

        return v;
    }

    public DistributoreAsResult getDistributore() {
        return distributore;
    }
}
