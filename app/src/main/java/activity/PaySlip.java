package activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.fortunekenya.m_payslips.R;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.squareup.okhttp.OkHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import model.EmployeeAPI;
import model.Globals;
import model.ZoomLayout;
import pl.polidea.view.ZoomView;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

/**
 * Created by Enock on 9/29/2016.
 */
public class PaySlip extends AppCompatActivity {
    private ZoomView zoomView;
    private LinearLayout main_container;
    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;
    private Toolbar mToolbar;
    private Animator mCurrentAnimator;
    private Menu menu;
    private int mShortAnimationDuration;

    private TextView Department,Period,AllNames,Pinno,StaffNo,Desgination,Company,Bank,Leave,Account,Message,NetToGross;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payslip_view);
        this.mToolbar = ((Toolbar)findViewById(R.id.toolbar1));
        setSupportActionBar(this.mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Department = (TextView)findViewById(R.id.Department);
        Period = (TextView)findViewById(R.id.Period);
        AllNames = (TextView)findViewById(R.id.Names);
        Pinno = (TextView)findViewById(R.id.PINNO);
        StaffNo = (TextView)findViewById(R.id.STAFFNO);
        Desgination = (TextView)findViewById(R.id.DESIGNATION);
        Company = (TextView)findViewById(R.id.Company);
        Bank = (TextView)findViewById(R.id.Bank);
        Leave = (TextView)findViewById(R.id.Leave);
        Account = (TextView)findViewById(R.id.account);
        Message = (TextView)findViewById(R.id.Message);
        NetToGross = (TextView)findViewById(R.id.NetToGross);
        SharedPreferences prefs = getSharedPreferences("MySessions", 0);
        String jsonResponce = prefs.getString("jsonResponce", null);
        preparePaySlipData("PaySlip",jsonResponce);
    }

    public  void setTitle(String title)
    {
        getSupportActionBar().setTitle(title);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.payslip_menu, menu);
       SharedPreferences prefs = getSharedPreferences("MySessions", 0);
        String jsonResponce = prefs.getString("jsonResponce", null);
        try {
            JSONObject jsonObject = new JSONObject(jsonResponce);
            JSONArray jresult = jsonObject.getJSONArray("Periods");


            for (int i = 0; i <= jresult.length() - 1; i++) {
                JSONObject menuObject = jresult.getJSONObject(i);
                String CurrentPeriod =  menuObject.getString("CurrentPeriod");
                menu.add(CurrentPeriod);
            }
        }catch (JSONException e)
        {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }else if(item.getItemId() == R.id.action_search)
        {
            CreatePDF();
        }else{
            String period = item.getTitle().toString();
            SharedPreferences prefs = getSharedPreferences("MySessions", 0);
            String StaffIDNO = prefs.getString("EmployeeNo", "");

            ReloadPayslip(StaffIDNO, period);

        }

        return super.onOptionsItemSelected(item);
    }

    private int preparePaySlipData(String jsonArray,String jsonResponce) {
        int counter = 0;

        try {
            JSONObject jsonObject = new JSONObject(jsonResponce);
            JSONArray jresult = jsonObject.getJSONArray(jsonArray);
            counter= jresult.length();

            for(int i=0;i<=jresult.length()-1;i++)
            {
                JSONObject LeaveApplicationObject = jresult.getJSONObject(i);
                String DepartmentDescription = "Department "+LeaveApplicationObject.getString("DepartmentDescription");
                String CurrentPeriod = "Period "+LeaveApplicationObject.getString("CurrentPeriod");
                setTitle(LeaveApplicationObject.getString("CurrentPeriod")+" PaySlip");
                String DesignationDescription =  "Designation "+LeaveApplicationObject.getString("DesignationDescription");
                String PINNo = "PINNO "+LeaveApplicationObject.getString("PINNo");
                String StaffIDNo = "Staff ID No"+LeaveApplicationObject.getString("StaffNo");
                String AllNamesNames =  "Names "+ LeaveApplicationObject.getString("AllNames");

                String CompanyStr =  LeaveApplicationObject.getString("Campname");
                String BankStr =  LeaveApplicationObject.getString("BankName")+" "+LeaveApplicationObject.getString("CompanyName");
                String AccountStr =   LeaveApplicationObject.getString("BankAccountNo");
                String MessageStr = LeaveApplicationObject.getString("Message");
                String LeaveStr =  LeaveApplicationObject.getString("LeaveBalance");
                String NetToGrossStr =  LeaveApplicationObject.getString("NetToGrossRatio");
                Double Ration = Double.parseDouble(NetToGrossStr);
                Department.setText(DepartmentDescription);
                Period.setText(CurrentPeriod);
                Desgination.setText(DesignationDescription);
                Pinno.setText(PINNo);
                StaffNo.setText(StaffIDNo);
                AllNames.setText(AllNamesNames);

                Company.setText(CompanyStr);
                Bank.setText(BankStr);
                Account.setText(AccountStr);
                Message.setText(MessageStr);
                Leave.setText(LeaveStr);
                NetToGross.setText(Ration+"");

            }
            init(jsonArray,jsonResponce);
        }catch (JSONException e)
        {
            e.printStackTrace();
        }

        return  counter;
    }

    public void init(String arrayName,String jsonResponce) {
        TableLayout stk = (TableLayout) findViewById(R.id.payslipTable);
        stk.removeAllViews();
            try {
                JSONObject jsonObject = new JSONObject(jsonResponce);
                JSONArray jresult = jsonObject.getJSONArray(arrayName);

                TableRow headerRow = new TableRow(this);

                TextView txtDescription1 = new TextView(this);
                txtDescription1.setText("DESCRIPTION");
                txtDescription1.setTextColor(Color.BLACK);
                txtDescription1.setGravity(Gravity.LEFT);
                txtDescription1.setTypeface(null, Typeface.BOLD);
                txtDescription1.setPadding(0,5,20,0);
                headerRow.addView(txtDescription1);

                TextView HOURS = new TextView(this);
                HOURS.setText("HOURS");
                HOURS.setTextColor(Color.BLACK);
                HOURS.setGravity(Gravity.LEFT);
                HOURS.setTypeface(null, Typeface.BOLD);
                HOURS.setPadding(0,5,20,0);
                headerRow.addView(HOURS);

                TextView Amount1 = new TextView(this);
                Amount1.setText("AMOUNT");
                Amount1.setTextColor(Color.BLACK);
                Amount1.setGravity(Gravity.LEFT);
                Amount1.setTypeface(null, Typeface.BOLD);
                Amount1.setPadding(0,5,20,0);
                headerRow.addView(Amount1);

                TextView ToDate1 = new TextView(this);
                ToDate1.setText("TODATE");
                ToDate1.setTextColor(Color.BLACK);
                ToDate1.setGravity(Gravity.LEFT);
                ToDate1.setTypeface(null, Typeface.BOLD);
                ToDate1.setPadding(0,5,20,0);
                headerRow.addView(ToDate1);

                TextView txtBalance1 = new TextView(this);
                txtBalance1.setText("BALANCE");
                txtBalance1.setTextColor(Color.BLACK);
                txtBalance1.setGravity(Gravity.LEFT);
                txtBalance1.setTypeface(null, Typeface.BOLD);
                txtBalance1.setPadding(0,5,20,0);
                headerRow.addView(txtBalance1);
                stk.addView(headerRow);
                //View v = new View(this);
               // v.setLayoutParams(new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, 2 ));
               // v.setBackgroundColor(Color.parseColor("#B3B3B3"));
               // stk.addView(v);
                int line = 0;

               for(int i=0;i<=jresult.length()-1;i++)
                {

                    JSONObject LeaveApplicationObject = jresult.getJSONObject(i);
                    String Description = LeaveApplicationObject.getString("Description");
                    DecimalFormat df = new DecimalFormat("#.00");
                    double RecurrentAmount = Double.parseDouble(LeaveApplicationObject.getString("RecurrentAmount"));
                    double Balance =  Double.parseDouble(LeaveApplicationObject.getString("Balance"));
                    double AmountTodate = Double.parseDouble(LeaveApplicationObject.getString("AmountTodate"));
                    double NoofHours = Double.parseDouble(LeaveApplicationObject.getString("NoofHours"));
                    int previousLine = line;
                    line =  Integer.parseInt(LeaveApplicationObject.getString("PrinterGroup"));
                    if(line!=previousLine)
                    {
                        View v = new View(this);
                        v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 1));
                        v.setBackgroundColor(Color.rgb(51, 51, 51));
                        stk.addView(v);
                    }

                    TableRow tbrow = new TableRow(this);

                    TextView txtDescription = new TextView(this);
                    txtDescription.setText(Description);
                    txtDescription.setTextColor(Color.BLACK);
                    txtDescription.setGravity(Gravity.LEFT);
                    txtDescription.setTypeface(null, Typeface.NORMAL);
                    txtDescription.setPadding(0,5,20,0);
                    tbrow.addView(txtDescription);

                    TextView HRS = new TextView(this);
                    HRS.setText(NoofHours+"");
                    if(NoofHours>0)
                    {
                        HRS.setText(df.format(NoofHours)+"");
                    }else{
                        HRS.setText("");
                    }
                    HRS.setTextColor(Color.BLACK);
                    HRS.setGravity(Gravity.RIGHT);
                    HRS.setTypeface(null, Typeface.NORMAL);
                    HRS.setPadding(0,5,20,0);
                    tbrow.addView(HRS);

                    TextView Amount = new TextView(this);
                    if(RecurrentAmount>0)
                    {
                        Amount.setText(df.format(RecurrentAmount)+"");
                    }else{
                        Amount.setText("");
                    }

                    Amount.setTextColor(Color.BLACK);
                    Amount.setGravity(Gravity.RIGHT);
                    Amount.setTypeface(null, Typeface.NORMAL);
                    Amount.setPadding(0,5,20,0);

                    tbrow.addView(Amount);


                    TextView ToDate = new TextView(this);

                    if(AmountTodate>0)
                    {
                        ToDate.setText(df.format(AmountTodate)+"");
                    }else{
                        ToDate.setText("");
                    }
                    if ((line==2)||(line==1)||(line==5)) {
                        ToDate.setText("");
                    }
                    ToDate.setTextColor(Color.BLACK);
                    ToDate.setGravity(Gravity.RIGHT);
                    ToDate.setTypeface(null, Typeface.NORMAL);
                    ToDate.setPadding(0,5,20,0);
                    tbrow.addView(ToDate);


                    TextView txtBalance = new TextView(this);

                    if(Balance>0)
                    {
                        txtBalance.setText(df.format(Balance)+"");
                    }else{
                        txtBalance.setText("");
                    }
                    txtBalance.setTextColor(Color.BLACK);
                    txtBalance.setGravity(Gravity.RIGHT);
                    txtBalance.setTypeface(null, Typeface.NORMAL);
                    txtBalance.setPadding(0,5,20,0);
                    tbrow.addView(txtBalance);

                    stk.addView(tbrow);

                }

            }catch (JSONException e)
            {
                e.printStackTrace();
            }

        }
    //create bitmap from view and returns it
    private Bitmap getBitmapFromView(View view) {
        ScrollView hsv = (ScrollView) findViewById(R.id.scrollViewP);
        HorizontalScrollView horizontal = (HorizontalScrollView) findViewById(R.id.hsv);
        int totalHeight = hsv.getChildAt(0).getHeight();
        int totalWidth = horizontal.getChildAt(0).getWidth();
        Bitmap returnedBitmap = Bitmap.createBitmap(totalWidth, totalHeight,Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        }   else{
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }
    private static void addImage(Document document,byte[] byteArray)
    {
        Image image = null;
        try
        {
            image = Image.getInstance(byteArray);
        }
        catch (BadElementException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (MalformedURLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // image.scaleAbsolute(150f, 150f);
        try
        {
            document.add(image);
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public void CreatePDF()
    {

        File folder = new File(Environment.getExternalStorageDirectory()+File.separator+"PaySlips");
        folder.mkdirs();

       Date date = new Date() ;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date);

       final File myFile = new File(folder + timeStamp + ".pdf");
        try {
            OutputStream output  = new FileOutputStream(myFile);
            Document document = new Document(PageSize.A4);
            try{
                PdfWriter.getInstance(document, output);
                document.open();
              LinearLayout view2 = (LinearLayout)findViewById(R.id.MainLayout);

                view2.setDrawingCacheEnabled(true);
                Bitmap screen2= getBitmapFromView(view2);
                ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                screen2.compress(Bitmap.CompressFormat.JPEG,100, stream2);
                byte[] byteArray2 = stream2.toByteArray();
                addImage(document,byteArray2);

                    document.close();
                    AlertDialog.Builder builder =  new AlertDialog.Builder(PaySlip.this, R.style.AppCompatAlertDialogStyle);
                    builder.setTitle("Success")
                            .setMessage("Payslip PDF File Generated Successfully.")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton)
                                {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setDataAndType(Uri.fromFile(myFile), "application/pdf");
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    startActivity(intent);
                                }

                            }).show();

                //document.add(new Paragraph(mBodyEditText.getText().toString()));
            }catch (DocumentException e)
            {
                //loading.dismiss();
                e.printStackTrace();
            }

        }catch (FileNotFoundException e)
        {
           // loading.dismiss();
          e.printStackTrace();
        }


    }
    public void ReloadPayslip(final String StaffNo, String Period) {

        final ProgressDialog loading = ProgressDialog.show(PaySlip.this, "", "Please wait...", false, false);
        RestAdapter.Builder builder = new RestAdapter.Builder();
        final Globals globalRecordFetch = new Globals(getApplicationContext());
        builder.setEndpoint(globalRecordFetch.ROOT_URL);

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setReadTimeout(120 * 1000, TimeUnit.MILLISECONDS);
        builder.setClient(new OkClient(okHttpClient));

        builder.setLogLevel(RestAdapter.LogLevel.FULL);
        RestAdapter restAdapter = builder.build();
        EmployeeAPI api = restAdapter.create(EmployeeAPI.class);
        api.ReloadPayslip(
                StaffNo,
                Period,
                new Callback<Response>() {
                    @Override
                    public void success(Response result, Response response) {
                        BufferedReader reader = null;
                        String output = "";
                        try {
                            reader = new BufferedReader(new InputStreamReader(result.getBody().in()));
                            output = reader.readLine();

                            preparePaySlipData("PaySlip",output);
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

        AlertDialog.Builder builder =  new AlertDialog.Builder(PaySlip.this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Response")
                .setMessage(msg)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok,null).show();
    }
}
