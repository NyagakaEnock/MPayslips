package activity;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.SyncStateContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.fortunekenya.m_payslips.R;
import com.squareup.okhttp.OkHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarException;

import model.EmployeeAPI;
import model.Globals;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class Dashboard extends Fragment {
    public static   ImageView imageView;
    private Resources mResources;
    private Bitmap mBitmap;
    private Button apply,approve,myprofile,notifications;
    private  TextView username,period,staffid;

    public Dashboard() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dashboard, container, false);
       imageView = (ImageView) rootView.findViewById(R.id.imageView2);

        apply = (Button) rootView.findViewById(R.id.payslip);
        approve = (Button) rootView.findViewById(R.id.btn_places1);
      notifications = (Button) rootView.findViewById(R.id.btn_places);

        myprofile = (Button) rootView.findViewById(R.id.btn_news_feed);
        username = (TextView) rootView.findViewById(R.id.username);
        period = (TextView) rootView.findViewById(R.id.txtPeriod);
        staffid = (TextView) rootView.findViewById(R.id.staffidNumber);
        SharedPreferences prefs = getActivity().getSharedPreferences("MySessions", 0);
        SharedPreferences.Editor editor =  getActivity().getSharedPreferences("MySessions", 0).edit();
        try {

            String jsonResponce = prefs.getString("jsonResponce", null);
            JSONObject jsonObject = new JSONObject(jsonResponce);
            JSONArray jresult = jsonObject.getJSONArray("result");
            JSONObject object = jresult.getJSONObject(jresult.length() - 1);


            JSONArray CurrentPeriodArr = jsonObject.getJSONArray("MyCurrentPeriod");
            JSONObject CurrentPeriodobject = CurrentPeriodArr.getJSONObject(jresult.length() - 1);
            username.setText( object.getString("ALLNames"));
            period.setText("Current Period "+ CurrentPeriodobject.getString("CurrentPeriod"));
            staffid.setText("EmployeeNo "+ object.getString("EmployeeNo"));
            editor.putString("EmployeeNo", object.getString("EmployeeNo"));
            editor.commit();
        }catch (JSONException e)
        {
            e.printStackTrace();
        }
        mResources = getResources();

        Globals globals = new Globals(getActivity());
        String NoImagepath = globals.ROOT_URL+"/uploads/" + "noImage.png";
        new DownLoadImageTask(imageView).execute(NoImagepath);
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent intent = new Intent(getActivity(), PaySlip.class);
                Dashboard.this.startActivity(intent);*/
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                LayoutInflater inflater = (getActivity()).getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog, null);
                final TextView password  = (TextView)dialogView.findViewById(R.id.txtPassword);
                builder.setTitle("PaySlip Password");
                builder.setCancelable(false);
                builder.setNegativeButton("Cancel",null);
                builder.setIcon(R.drawable.padlock2);
                builder.setView(dialogView)

                        .setPositiveButton("Check PaySlip", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        if(password.length()==0) {
                                            Toast.makeText(getActivity(),"Please Enter your Payslip Password",Toast.LENGTH_LONG).show();
                                        }else{
                                            String Password = password.getText().toString();
                                            SharedPreferences prefs = getActivity().getSharedPreferences("MySessions", 0);
                                            String StaffIDNO = prefs.getString("EmployeeNo", "");

                                            validatePassword(StaffIDNO, Password,PaySlip.class);
                                        }
                                    }
                                }


                        );
                builder.create();
                builder.show();
            }
        });
        myprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Profile.class);
                Dashboard.this.startActivity(intent);

              //========================
            }
        });
        notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                                 /* Intent intent = new Intent(getActivity(), PaySlip.class);
                Dashboard.this.startActivity(intent);*/
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final MainActivity mainActivity = new MainActivity();
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.payslip_password, null);
                final TextView txtNew  = (TextView)dialogView.findViewById(R.id.New);
                final TextView txtConfirm  = (TextView)dialogView.findViewById(R.id.Confirm);
                builder.setTitle("PaySlip Password");
                builder.setCancelable(false);
                builder.setNegativeButton("Cancel",null);
                builder.setIcon(R.drawable.padlock2);
                builder.setView(dialogView)
                        .setPositiveButton("Change Password", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                      if(txtNew.length()==0) {
                                            Toast.makeText(getActivity(),"Please Enter your new Payslip Password",Toast.LENGTH_LONG).show();
                                        }else if(txtConfirm.length()==0) {
                                            Toast.makeText(getActivity(),"Please Confirm your new Payslip Password",Toast.LENGTH_LONG).show();
                                        }else if(!txtConfirm.getText().toString().equals(txtNew.getText().toString())) {
                                            Toast.makeText(getActivity(),"Your Passwords do Not Match",Toast.LENGTH_LONG).show();
                                        }else{
                                            SharedPreferences prefs = getActivity().getSharedPreferences("MySessions", 0);

                                            final String StaffIDNO = prefs.getString("EmployeeNo", "");
                                            String Confirm = txtConfirm.getText().toString();

                                            mainActivity.ChangePassword(getActivity(),Confirm,StaffIDNO);

                                        }

                                    }
                                }


                        );
                builder.create();
                builder.show();

                //========================
            }
        });

        approve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Intent intent = new Intent(getActivity(), P9Form.class);
              //  Dashboard.this.startActivity(intent);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                LayoutInflater inflater = (getActivity()).getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog, null);
                final TextView password  = (TextView)dialogView.findViewById(R.id.txtPassword);
                builder.setTitle("PaySlip Password");
                builder.setCancelable(false);
                builder.setNegativeButton("Cancel",null);
                builder.setIcon(R.drawable.padlock2);
                builder.setView(dialogView)

                        .setPositiveButton("Check PaySlip", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        if(password.length()==0) {
                                            Toast.makeText(getActivity(),"Please Enter your Payslip Password",Toast.LENGTH_LONG).show();
                                        }else{
                                            String Password = password.getText().toString();
                                            SharedPreferences prefs = getActivity().getSharedPreferences("MySessions", 0);
                                            String StaffIDNO = prefs.getString("EmployeeNo", "");
                                            validatePassword(StaffIDNO, Password,P9Form.class);
                                        }
                                    }
                                }


                        );
                builder.create();
                builder.show();
            }
        });
        return rootView;
    }

    public void validatePassword(final String Username, String Password,final Class activity) {

        final ProgressDialog loading = ProgressDialog.show(getActivity(), "", "Please wait...", false, false);
        RestAdapter.Builder builder = new RestAdapter.Builder();
        final Globals globalRecordFetch = new Globals(getActivity());
        builder.setEndpoint(globalRecordFetch.ROOT_URL);
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setReadTimeout(120 * 1000, TimeUnit.MILLISECONDS);
        builder.setClient(new OkClient(okHttpClient));
        builder.setLogLevel(RestAdapter.LogLevel.FULL);
        RestAdapter restAdapter = builder.build();
        EmployeeAPI api = restAdapter.create(EmployeeAPI.class);
        api.validatePassword(
                Username,
                Password,
                new Callback<Response>() {
                    @Override
                    public void success(Response result, Response response) {
                        BufferedReader reader = null;
                        String output = "";
                        try {
                            reader = new BufferedReader(new InputStreamReader(result.getBody().in()));
                            output = reader.readLine();
                        if(output.equals("True"))
                        {
                            Intent intent = new Intent(getActivity(), activity);
                            Dashboard.this.startActivity(intent);
                        }else{
                            MyalertDialog("Access Denied. You Entered wrong Payslip Password".toString());
                        }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        loading.dismiss();
                    }
                    @Override
                    public void failure(RetrofitError error) {
                        loading.dismiss();
                        MyalertDialog("Connection Failed. Please Try again "+error.toString());
                    }
                }
        );


    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( resultCode == Activity.RESULT_OK) {
            String path = getPathFromCameraData(data, this.getActivity());
            if (path != null) {

               // TypedFile file = new TypedFile("multipart/form-data", new File(path));
                SharedPreferences prefs = getActivity().getSharedPreferences("MySessions", 0);
                final String StaffIDNO = prefs.getString("StaffIDNO", "");
               // UploadFile(file,StaffIDNO);
            }
        }
    }

    public static String getPathFromCameraData(Intent data, Context context) {
        Uri selectedImage = data.getData();
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return picturePath;
    }


    public void MyalertDialog(String msg)
    {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Response")
                .setMessage(msg)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok, null).show();


    }

    public class DownLoadImageTask extends AsyncTask<String, Void, Bitmap> {


        public DownLoadImageTask(ImageView imageView) {
            Dashboard.imageView = imageView;
        }

        /*
                Override this method to perform a computation on a background thread.
         */
        protected Bitmap doInBackground(String... urls) {
            String urlOfImage = urls[0];
            Bitmap logo = null;
            try {
                InputStream is = new URL(urlOfImage).openStream();
               // logo = BitmapFactory.decodeStream(is);

                BitmapFactory.Options options = new BitmapFactory.Options();


                options.inSampleSize = calculateInSampleSize(options, 100, 100);

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                logo = BitmapFactory.decodeStream(is, null, options);

            } catch (Exception e) { // Catch the download exception
                e.printStackTrace();
            }

            return logo;
        }

        /*
            onPostExecute(Result result)
                Runs on the UI thread after doInBackground(Params...).
         */
        protected void onPostExecute(Bitmap result) {


            // mBitmap = BitmapFactory.decodeResource(mResources,R.drawable.boy);
            mBitmap = result;
            RoundedBitmapDrawable drawable = createRoundedBitmapDrawableWithBorder(mBitmap);

            // imageView.setImageBitmap(result);
            // Set the ImageView image as drawable object
            imageView.setImageDrawable(drawable);
        }

    }
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
    public RoundedBitmapDrawable createRoundedBitmapDrawableWithBorder(Bitmap bitmap){
        RoundedBitmapDrawable roundedBitmapDrawable = null;
        try {
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            int borderWidthHalf = 10; // In pixels
            int bitmapRadius = Math.min(bitmapWidth, bitmapHeight) / 2;
            int bitmapSquareWidth = Math.min(bitmapWidth, bitmapHeight);
            int newBitmapSquareWidth = bitmapSquareWidth + borderWidthHalf;
            Bitmap roundedBitmap = Bitmap.createBitmap(newBitmapSquareWidth, newBitmapSquareWidth, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(roundedBitmap);
            canvas.drawColor(Color.parseColor("#1DA1F2"));
            int x = borderWidthHalf + bitmapSquareWidth - bitmapWidth;
            int y = borderWidthHalf + bitmapSquareWidth - bitmapHeight;
            canvas.drawBitmap(bitmap, x, y, null);
            Paint borderPaint = new Paint();
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(borderWidthHalf * 2);
            borderPaint.setColor(Color.parseColor("#ff29549f"));
            canvas.drawCircle(canvas.getWidth() / 2, canvas.getWidth() / 2, newBitmapSquareWidth / 2, borderPaint);
            roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(mResources, roundedBitmap);
            roundedBitmapDrawable.setCornerRadius(bitmapRadius);
            roundedBitmapDrawable.setAntiAlias(true);

        }catch (NullPointerException e)
        {
            e.printStackTrace();
        }
        return roundedBitmapDrawable;
    }
}
