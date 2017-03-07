package activity;


import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fortunekenya.m_payslips.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class about extends Fragment {
private TextView versionNo,website,email,phone;

    public about() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        versionNo = (TextView)rootView.findViewById(R.id.version);
        website = (TextView)rootView.findViewById(R.id.website);
        email=(TextView)rootView.findViewById(R.id.email);
        phone=(TextView)rootView.findViewById(R.id.phone);

        try{
            PackageManager manager = getActivity().getPackageManager();
            PackageInfo info = manager.getPackageInfo(getActivity().getPackageName(), 0);
            String version = info.versionName;
            versionNo.setText("Version "+version);
            website.setClickable(true);
            website.setMovementMethod(LinkMovementMethod.getInstance());
            website.setText(Html.fromHtml("<a href='http://www.fortunekenya.com'>www.fortunekenya.com</a>"));
            email.setClickable(true);
            phone.setClickable(true);
            phone.setText(Html.fromHtml("<a href=''>0722769149</a>"));
            email.setText(Html.fromHtml("<a href='support@fortunekenya.com'>support@fortunekenya.com</a>"));
            email.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.setType("plain/text");
                    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"support@fortunekenya.com"});
                    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
                    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
                    getActivity().startActivity(Intent.createChooser(emailIntent, "Send mail..."));
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
       return rootView;
    }

}
