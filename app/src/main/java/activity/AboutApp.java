package activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.fortunekenya.m_payslips.R;

/**
 * Created by Enock on 10/6/2016.
 */
public class AboutApp extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextView versionNo,website,email,phone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_about);
        this.mToolbar = ((Toolbar) findViewById(R.id.toolbar5));
        setSupportActionBar(this.mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("About App");
        versionNo = (TextView)findViewById(R.id.version);
        website = (TextView)findViewById(R.id.website);
        email=(TextView)findViewById(R.id.email);
        phone=(TextView)findViewById(R.id.phone);

        try{
            PackageManager manager =getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            String version = info.versionName;
            versionNo.setText(version);
            website.setClickable(true);
            website.setMovementMethod(LinkMovementMethod.getInstance());
            website.setText(Html.fromHtml("<a href='http://www.fortunekenya.com'>www.fortunekenya.com</a>"));
            email.setClickable(true);
            phone.setClickable(true);
            phone.setText(Html.fromHtml("<a href=''>Telephone 0722769149</a>"));
            email.setText(Html.fromHtml("<a href='support@fortunekenya.com'>support@fortunekenya.com</a>"));
            email.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.setType("plain/text");
                    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"support@fortunekenya.com"});
                    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
                    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
                    startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                }
            });
            phone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:0722769149"));
                    startActivity(intent);
                }
            });
        }catch (PackageManager.NameNotFoundException e){

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return true;
    }
}
