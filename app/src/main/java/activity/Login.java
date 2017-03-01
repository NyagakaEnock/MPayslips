package activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fortunekenya.m_payslips.R;
import com.squareup.okhttp.OkHttpClient;

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

/**
 * Created by Enock on 9/28/2016.
 */
public class Login extends AppCompatActivity implements View.OnClickListener{
    private Toolbar mToolbar;
    private TextView emailAdress,passWord;
    private Button loginButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        this.mToolbar = ((Toolbar)findViewById(R.id.toolbar1));
        setSupportActionBar(this.mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        emailAdress = (TextView)findViewById(R.id.loginEmail);
        passWord = (TextView)findViewById(R.id.Password);
        loginButton = (Button)findViewById(R.id.btnLogin);
        loginButton.setOnClickListener(this);


    }

    public void onClick(View view)
    {
        String email = emailAdress.getText().toString();
        String password = passWord.getText().toString();
        if(validateLogin()==true)
        {
            Login(email, password);
        }

    }
    public void Login(final String email, String password) {

        final ProgressDialog loading = ProgressDialog.show(Login.this, "", "Please wait...", false, false);
        RestAdapter.Builder builder = new RestAdapter.Builder();
        final Globals globalRecordFetch = new Globals(getApplicationContext());
        builder.setEndpoint(globalRecordFetch.ROOT_URL);

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setReadTimeout(120 * 1000, TimeUnit.MILLISECONDS);
        builder.setClient(new OkClient(okHttpClient));

        builder.setLogLevel(RestAdapter.LogLevel.FULL);
        RestAdapter restAdapter = builder.build();
        EmployeeAPI api = restAdapter.create(EmployeeAPI.class);
        api.Login(
                email,
                password,
                new Callback<Response>() {
                    @Override
                    public void success(Response result, Response response) {
                        BufferedReader reader = null;
                        String output = "";
                        try {
                            reader = new BufferedReader(new InputStreamReader(result.getBody().in()));
                            output = reader.readLine();

                            if(!output.equals("False"))
                            {

                                Intent intent = new Intent(Login.this, MainActivity.class);
                                SharedPreferences.Editor editor = getSharedPreferences("MySessions", MODE_PRIVATE).edit();
                                editor.putString("jsonResponce",output);
                                editor.commit();
                                startActivity(intent);
                                finish();

                            }else {
                                    MyalertDialog("Access Denied. Check your Username and Password and try again");

                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        loading.dismiss();
                    }
                    @Override
                    public void failure(RetrofitError error) {
                        loading.dismiss();
                        MyalertDialog("Connection Failed.\n Please check your internet connection and try again."+error.getMessage());
                    }
                }
        );


    }
    public void MyalertDialog(String msg)
    {

        AlertDialog.Builder builder =  new AlertDialog.Builder(Login.this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Response")
                .setMessage(msg)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok,null).show();
    }
    private boolean validateLogin()
    {
        if(emailAdress.length()==0)
        {
            Toast.makeText(getApplicationContext(),"Enter Your User Name or Email Address",Toast.LENGTH_LONG).show();
            return false;
        }else if(passWord.length()==0)
        {
            Toast.makeText(getApplicationContext(),"Enter Your Password",Toast.LENGTH_LONG).show();
            return  false;
        }else{
            return true;
        }

    }
}
