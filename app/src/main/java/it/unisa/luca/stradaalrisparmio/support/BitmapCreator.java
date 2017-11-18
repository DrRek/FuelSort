package it.unisa.luca.stradaalrisparmio.support;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;


/**
 * Created by luca on 22/10/17.
 */

public class BitmapCreator {

    public static String DEFAULT_ICON = "pomp_icon";
    public static String ESSO_ICON = "esso";
    public static String IP_ICON = "ip";
    public static String OTHER_ICON = "interrogative";
    public static String TOTALERG_ICON = "totalerg";
    public static String Q8_ICON = "q8";
    public static String BIANCHE_ICON = "bianche";
    public static String REPSOL_ICON = "repsol";
    public static String TAMOIL_ICON = "tamoil";
    public static String ENERGAS_ICON = "energas";
    public static int SIZE = 160;
    public static int BANDIERA_SIZE = 65;

    public static Bitmap getBitmap(Context context, int color, Float value, String bandiera){
        Bitmap.Config config = android.graphics.Bitmap.Config.ARGB_8888;

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),context.getResources().getIdentifier(DEFAULT_ICON, "drawable", context.getPackageName()));
        bitmap = Bitmap.createScaledBitmap(bitmap, SIZE, SIZE, false).copy(config, true);

        int [] allpixels = new int [SIZE*SIZE];
        bitmap.getPixels(allpixels, 0, SIZE, 0, 0, SIZE, SIZE);
        for(int i = 0; i < allpixels.length; i++)
        {
            if(allpixels[i] != 0 && allpixels[i]!=-1){
                allpixels[i] = color;
            }
        }
        bitmap.setPixels(allpixels,0,SIZE,0, 0, SIZE,SIZE);

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(0, 0, 0));
        paint.setTextSize(30);
        Rect bounds = new Rect();
        paint.getTextBounds(value+"", 0, (value+"").length(), bounds);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(value+"", ((SIZE - bounds.width())/2)-2, SIZE/4, paint);

        if(bandiera!=null){
            Bitmap bandieraBitmap;
            String toCheck = bandiera.toLowerCase();
            if(toCheck.contains("esso")){
                bandieraBitmap = BitmapFactory.decodeResource(context.getResources(),context.getResources().getIdentifier(ESSO_ICON, "drawable", context.getPackageName()));
            } else if(toCheck.contains("q8")){
                bandieraBitmap = BitmapFactory.decodeResource(context.getResources(),context.getResources().getIdentifier(Q8_ICON, "drawable", context.getPackageName()));
            } else if(toCheck.contains("bianche")){
                bandieraBitmap = BitmapFactory.decodeResource(context.getResources(),context.getResources().getIdentifier(BIANCHE_ICON, "drawable", context.getPackageName()));
            } else if(toCheck.contains("total erg")){
                bandieraBitmap = BitmapFactory.decodeResource(context.getResources(),context.getResources().getIdentifier(TOTALERG_ICON, "drawable", context.getPackageName()));
            } else if(toCheck.contains("energas")){
                bandieraBitmap = BitmapFactory.decodeResource(context.getResources(),context.getResources().getIdentifier(ENERGAS_ICON, "drawable", context.getPackageName()));
            } else if(toCheck.contains("tamoil")){
                bandieraBitmap = BitmapFactory.decodeResource(context.getResources(),context.getResources().getIdentifier(TAMOIL_ICON, "drawable", context.getPackageName()));
            } else if(toCheck.contains("repsol")){
                bandieraBitmap = BitmapFactory.decodeResource(context.getResources(),context.getResources().getIdentifier(REPSOL_ICON, "drawable", context.getPackageName()));
            }   else if(toCheck.contains("ip")){
                bandieraBitmap = BitmapFactory.decodeResource(context.getResources(),context.getResources().getIdentifier(IP_ICON, "drawable", context.getPackageName()));
            } else {
                bandieraBitmap = BitmapFactory.decodeResource(context.getResources(), context.getResources().getIdentifier(OTHER_ICON, "drawable", context.getPackageName()));
            }
            int x=bandieraBitmap.getWidth(), y=bandieraBitmap.getHeight();
            if(x>y){
                y = BANDIERA_SIZE * y / x;
                x = BANDIERA_SIZE;
            }
            else{
                x = BANDIERA_SIZE * x / y;
                y = BANDIERA_SIZE;
            }
            bandieraBitmap = Bitmap.createScaledBitmap(bandieraBitmap, x, y, false).copy(config, true);
            canvas.drawBitmap(bandieraBitmap, (SIZE-x)/2, (SIZE-y)*3/5, null);
        }

        return bitmap;
    }

    public static Bitmap getStartBitmap(Context context){
        Bitmap.Config config = android.graphics.Bitmap.Config.ARGB_8888;

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),context.getResources().getIdentifier("start", "drawable", context.getPackageName()));
        return Bitmap.createScaledBitmap(bitmap, SIZE, SIZE, false).copy(config, true);
    }
    public static Bitmap getFinishBitmap(Context context){
        Bitmap.Config config = android.graphics.Bitmap.Config.ARGB_8888;

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),context.getResources().getIdentifier("finish", "drawable", context.getPackageName()));
        return Bitmap.createScaledBitmap(bitmap, SIZE, SIZE, false).copy(config, true);
    }
}
