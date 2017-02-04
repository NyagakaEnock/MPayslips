package activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.fortunekenya.m_payslips.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Enock on 10/2/2016.
 */
public class Profile extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextView username,Email,staffid,PINNo,DateEmployed,BankAccountNo,BANK,BRACH,DESGINATION,DEPT;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        this.mToolbar = ((Toolbar)findViewById(R.id.toolbar3));
        setSupportActionBar(this.mToolbar);

        getSupportActionBar().setTitle("My Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        username = (TextView) findViewById(R.id.NAMES);
        Email = (TextView) findViewById(R.id.Email);
        staffid = (TextView) findViewById(R.id.ID);
        PINNo = (TextView) findViewById(R.id.PIN);
        DateEmployed = (TextView) findViewById(R.id.DateEmployed);
        BankAccountNo= (TextView) findViewById(R.id.accountNO);
        BANK = (TextView) findViewById(R.id.BANK);
        BRACH = (TextView) findViewById(R.id.Branch);
        DESGINATION = (TextView) findViewById(R.id.DESG);
        DEPT = (TextView) findViewById(R.id.DPT);
        SharedPreferences prefs = getSharedPreferences("MySessions", 0);
        try {

            String jsonResponce = prefs.getString("jsonResponce", null);
            JSONObject jsonObject = new JSONObject(jsonResponce);
            JSONArray jresult = jsonObject.getJSONArray("result");
            JSONObject object = jresult.getJSONObject(jresult.length() - 1);


            JSONArray CurrentPeriodArr = jsonObject.getJSONArray("PaySlip");
            JSONObject CurrentPeriodobject = CurrentPeriodArr.getJSONObject(jresult.length() - 1);

            username.setText( object.getString("ALLNames"));
            Email.setText(object.getString("emailAddress"));
            staffid.setText(object.getString("EmployeeNo"));
            PINNo.setText(object.getString("PINNo"));
            DateEmployed.setText(object.getString("DateEmployed").substring(0,10));
            BankAccountNo.setText(object.getString("BankAccountNo"));

            BANK.setText(CurrentPeriodobject.getString("BankName"));
            BRACH.setText(CurrentPeriodobject.getString("CompanyName"));
            DESGINATION.setText(CurrentPeriodobject.getString("DesignationDescription"));
            DEPT.setText(CurrentPeriodobject.getString("DepartmentDescription"));
        }catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //getMenuInflater().inflate(R.menu.payslip_menu, menu);
    return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
