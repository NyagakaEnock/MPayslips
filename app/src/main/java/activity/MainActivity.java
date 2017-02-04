package activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fortunekenya.m_payslips.R;
import com.squareup.okhttp.OkHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import model.EmployeeAPI;
import model.Globals;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private Button payslip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        payslip = (Button)findViewById(R.id.payslip);
        this.mToolbar = ((Toolbar)findViewById(R.id.toolbar));
        setSupportActionBar(this.mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        Fragment fragment = new Dashboard();
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fragment);
            fragmentTransaction.commit();

        } else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.Logout) {
            Intent intent = new Intent(MainActivity.this, Login.class);
            SharedPreferences.Editor editor = getSharedPreferences("MySessions", MODE_PRIVATE).edit();
            editor.clear();
            startActivity(intent);
            finish();
            return true;
        }else if (id == R.id.action_search) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "Let me recommend you this application.\n" +
                    "\n" +
                    "M-Payslip Mobile App Allows Employees to view their Payslips and P9 Forms. The App is Integrated with the Main Payroll System. \n" +
                    "\n" +
                    "http://developer.fortunekenya.com/android/MPayslips.apk\n" +
                    "\n" +
                    "For more Information call Fortune Technologies Ltd on 0722769149 or Email support@fortunekenya.com";
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "M-Payslip Android App");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
            return true;
        }else if(id == R.id.about)
        {
            Intent intent = new Intent(MainActivity.this, AboutApp.class);
            startActivity(intent);
        }else if(id == R.id.Exit)
        {
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Exit?")
                    .setMessage("Are you sure you want to exit?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            finish();
                        }
                    }).create().show();
        }else if(id==R.id.action_settings)
        {
                       /* Intent intent = new Intent(getActivity(), PaySlip.class);
                Dashboard.this.startActivity(intent);*/
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            LayoutInflater inflater = (MainActivity.this).getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.payslip_password, null);
            final TextView password  = (TextView)dialogView.findViewById(R.id.txtPassword2);
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
                                    if(password.length()==0) {
                                        Toast.makeText(getApplicationContext(),"Please Enter your Current Payslip Password",Toast.LENGTH_LONG).show();
                                    }else if(txtNew.length()==0) {
                                        Toast.makeText(getApplicationContext(),"Please Enter your new Payslip Password",Toast.LENGTH_LONG).show();
                                    }else if(txtConfirm.length()==0) {
                                        Toast.makeText(getApplicationContext(),"Please Confirm your new Payslip Password",Toast.LENGTH_LONG).show();
                                    }else if(!txtConfirm.getText().toString().equals(txtNew.getText().toString())) {
                                        Toast.makeText(getApplicationContext(),"Your Passwords do Not Match",Toast.LENGTH_LONG).show();
                                    }else{
                                        SharedPreferences prefs = getSharedPreferences("MySessions", 0);
                                        final String StaffIDNO = prefs.getString("EmployeeNo", "");
                                        String Password = password.getText().toString();
                                        String Confirm = txtConfirm.getText().toString();

                                        ChangePassword(Password, Confirm,StaffIDNO);

                                    }

                                }
                            }


                    );
            builder.create();
            builder.show();
        }
        return  true;
    }
    public  void ChangePassword(String Password,String Confirm, String StaffId)
    {
        final ProgressDialog loading = ProgressDialog.show(MainActivity.this, "", "Please wait...", false, false);
        RestAdapter.Builder builder = new RestAdapter.Builder();
        final Globals globalRecordFetch = new Globals(getApplicationContext());
        builder.setEndpoint(globalRecordFetch.ROOT_URL);
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setReadTimeout(120 * 1000, TimeUnit.MILLISECONDS);
        builder.setClient(new OkClient(okHttpClient));
        builder.setLogLevel(RestAdapter.LogLevel.FULL);
        RestAdapter restAdapter = builder.build();
        EmployeeAPI api = restAdapter.create(EmployeeAPI.class);
        api.ChangePassword(
                Password,
                Confirm,
                StaffId,
                new Callback<Response>() {
                    @Override
                    public void success(Response result, Response response) {
                        BufferedReader reader = null;
                        String output = "";
                        try {
                            reader = new BufferedReader(new InputStreamReader(result.getBody().in()));
                            output = reader.readLine();

                                MyalertDialog(output);


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

    public void MyalertDialog(String msg)
    {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(MainActivity.this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Response")
                .setMessage(msg)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok, null).show();


    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        MainActivity.super.onBackPressed();
                    }
                }).create().show();
    }
}
