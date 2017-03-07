package model;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.widget.ImageView;

import com.fortunekenya.m_payslips.R;

import java.io.InputStream;
import java.net.URL;

import activity.Dashboard;

/**
 * Created by Enock on 9/28/2016.
 */
public class Globals {
    public final String ROOT_URL = "http://192.168.1.2:8012/M-Payslips";
    //public final String ROOT_URL = "http://192.168.42.164:8012/M-Payslips";
    private Resources mResources;
    private Bitmap mBitmap;
    private Context context;
    public Globals(Context context)
    {
        this.context = context;
    }




    public void MyalertDialog(String msg)
    {

        AlertDialog.Builder builder =  new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Response")
                .setMessage(msg)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok,null).show();
    }




}
