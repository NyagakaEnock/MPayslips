package activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
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

import adapter.OnPinchListener;
import model.EmployeeAPI;
import model.Globals;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

/**
 * Created by Enock on 10/4/2016.
 */
public class P9Form extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextView EmployeePin,EmployeeName,EmployerPin,EmployerName,ChargeablePaySum,PAYEAmountSum,Card;
    public  int periodVal=208;
    ScrollView mZoomableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.p9form);
        this.mToolbar = ((Toolbar) findViewById(R.id.toolbar4));
        setSupportActionBar(this.mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("P9 Form");
        EmployeePin = (TextView)findViewById(R.id.EmployeePIN);
        EmployeeName = (TextView)findViewById(R.id.EmployeeName);
        EmployerPin = (TextView)findViewById(R.id.EmployerPIN);
        EmployerName = (TextView)findViewById(R.id.EmployerName);
        PAYEAmountSum =  (TextView)findViewById(R.id.PAYEAmountSum);
        ChargeablePaySum =  (TextView)findViewById(R.id.ChargeablePaySum);
        Card =  (TextView)findViewById(R.id.Card);
        mZoomableLayout = (ScrollView)findViewById(R.id.scrollViewP);
        SharedPreferences prefs = getSharedPreferences("MySessions", 0);
        String jsonResponce = prefs.getString("jsonResponce", null);
        //final ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(this, new OnPinchListener());
        prepareHeadingInfo("P9",jsonResponce,"EmployeeDetails",jsonResponce);


    }

    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.p9_menu, menu);
        SharedPreferences prefs = getSharedPreferences("MySessions", 0);
        String jsonResponce = prefs.getString("jsonResponce", null);
        try {
            JSONObject jsonObject = new JSONObject(jsonResponce);
            JSONArray jresult = jsonObject.getJSONArray("currentYEAR");


            for (int i = 0; i <= jresult.length() - 1; i++) {
                JSONObject menuObject = jresult.getJSONObject(i);
                String CurrentPeriod =  menuObject.getString("CurrentYear");
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
            finish(); // close this activity and return to preview activity (if there is any)
        }else if(item.getItemId() == R.id.pdf)
        {
            CreatePDF();
        }else{
            String period = item.getTitle().toString();
            SharedPreferences prefs = getSharedPreferences("MySessions", 0);
            String StaffIDNO = prefs.getString("EmployeeNo", "");

            ReloadP9Form(StaffIDNO, period);
            Card.setText("TAX DEDUCTION CARD "+period);
        }

        return super.onOptionsItemSelected(item);
    }
    public void ReloadP9Form(final String StaffNo, String Period) {

        final ProgressDialog loading = ProgressDialog.show(P9Form.this, "", "Please wait...", false, false);
        RestAdapter.Builder builder = new RestAdapter.Builder();
        final Globals globalRecordFetch = new Globals(getApplicationContext());
        builder.setEndpoint(globalRecordFetch.ROOT_URL);

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setReadTimeout(120 * 1000, TimeUnit.MILLISECONDS);
        builder.setClient(new OkClient(okHttpClient));
        builder.setLogLevel(RestAdapter.LogLevel.FULL);
        RestAdapter restAdapter = builder.build();
        EmployeeAPI api = restAdapter.create(EmployeeAPI.class);
        api.ReloadP9Form(
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
                           if(output.equals("False"))
                           {
                              MyalertDialog("Your P9's for this period has not been Authorized for view");
                           }else {
                               prepareHeadingInfo("P9",output,"EmployeeDetails",output);
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
    public void MyalertDialog(String msg)
    {

        AlertDialog.Builder builder =  new AlertDialog.Builder(P9Form.this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Response")
                .setMessage(msg)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok,null).show();
    }
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
    private static void addImage(Document document, byte[] byteArray)
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
        float left = 5;
        float right = 5;
        float top = 30;
        float bottom = 0;
        File folder = new File(Environment.getExternalStorageDirectory()+File.separator+"P9Form");
        folder.mkdirs();

        Date date = new Date() ;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date);

        final File myFile = new File(folder + timeStamp + ".pdf");
        try {
            OutputStream output  = new FileOutputStream(myFile);
            Document document = new Document(PageSize.A4.rotate());

            try{
                PdfWriter.getInstance(document, output);
                document.open();
                document.setMargins(left, right, 0, bottom);
               // Rotate event = new Rotate();
               // writer.setPageEvent(event);
                LinearLayout view2 = (LinearLayout)findViewById(R.id.P9MainLayout);

                view2.setDrawingCacheEnabled(true);
                Bitmap screen2= getBitmapFromView(view2);
                ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                screen2.compress(Bitmap.CompressFormat.JPEG,100, stream2);
                byte[] byteArray2 = stream2.toByteArray();
                addImage(document,byteArray2);

                document.close();
                AlertDialog.Builder builder =  new AlertDialog.Builder(P9Form.this, R.style.AppCompatAlertDialogStyle);
                builder.setTitle("Success")
                        .setMessage("P9 Form PDF File Generated Successfully.")
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
    public  void setTitle(String title)
    {
        getSupportActionBar().setTitle(title);
    }
    public void prepareHeadingInfo(String arrayName,String jsonResponce,String arrayName2,String jsonResponce2)
    {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponce2);
            JSONArray jresult = jsonObject.getJSONArray(arrayName2);


            for(int i=0;i<=jresult.length()-1;i++)
            {
                JSONObject LeaveApplicationObject = jresult.getJSONObject(i);
                //setTitle(LeaveApplicationObject.getString("CurrentYear")+" P9 Form");
                String EmployerNameStr =  LeaveApplicationObject.getString("CompanyName");
                String EmployeeNameStr =  LeaveApplicationObject.getString("ALLNames");
                String EmployerPinStr =   LeaveApplicationObject.getString("CompanyPinNO");
                String EmployeePinStr = LeaveApplicationObject.getString("PINNo");
                EmployeeName.setText(EmployeeNameStr);
                EmployeePin.setText(EmployeePinStr);
                EmployerName.setText(EmployerNameStr);
                EmployerPin.setText(EmployerPinStr);
            }
            init(arrayName,jsonResponce);
        }catch (JSONException e)
        {
            e.printStackTrace();
        }


    }
    public  void AddVerticalLine(View verticalLine,TableRow tableLayout)
    {
        verticalLine.setLayoutParams(new TableRow.LayoutParams(1, TableRow.LayoutParams.MATCH_PARENT));
        verticalLine.setBackgroundColor(Color.rgb(50, 50, 50));
        tableLayout.addView(verticalLine);
    }
    public void init(String arrayName,String jsonResponce) {
        TableLayout stk = (TableLayout) findViewById(R.id.p9Table);
        TableLayout stk2 = (TableLayout) findViewById(R.id.p9Table2);
        TableLayout stk3 = (TableLayout) findViewById(R.id.p9Table3);
        TableLayout footer = (TableLayout) findViewById(R.id.Sum);

        stk.removeAllViews();
        stk2.removeAllViews();
        stk3.removeAllViews();
        footer.removeAllViews();
        try {
            JSONObject jsonObject = new JSONObject(jsonResponce);
            JSONArray jresult = jsonObject.getJSONArray(arrayName);
            TableRow headerRow = new TableRow(this);
            TableRow headerRow2 = new TableRow(this);
            TableRow headerRow3 = new TableRow(this);
            TableRow headerRowX = new TableRow(this);
            TableRow footerRow = new TableRow(this);
           RenderTableHeader(headerRow, stk);
          RenderTableHeaderX(headerRowX, stk);
           RenderTableHeader2(headerRow2, stk);

            RenderTableHeader3(headerRow3, stk2);
            RenderTableBody(stk3,arrayName,jsonResponce);
           RenderTableSumation(footer,footerRow,"Summation",jsonResponce);
           RenderFooter("Summation",jsonResponce);

        }catch (JSONException e)
        {
            e.printStackTrace();
        }

    }
    public  int Cont;
    public void RenderTableHeader(TableRow  headerRow,TableLayout stk)
    {
        View verticalLine = new View(this);
        AddVerticalLine(verticalLine,headerRow);
        TextView MONTH = new TextView(this);
        FormatTextView(MONTH,50,getString(R.string.Period),headerRow);
        verticalLine = new View(this);
        AddVerticalLine(verticalLine,headerRow);
        MONTH.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

        TextView Basic = new TextView(this);
        FormatTextView(Basic,50,getString(R.string.Basic),headerRow);
        //Basic.setWidth(pxToDp(400));
        Basic.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

        verticalLine = new View(this);
        AddVerticalLine(verticalLine,headerRow);
        TextView Benefit = new TextView(this);
        FormatTextView(Benefit,40,getString(R.string.Benefit),headerRow);
       // Benefit.setWidth(pxToDp(400));
        Benefit.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

        verticalLine = new View(this);
        AddVerticalLine(verticalLine,headerRow);
        TextView Quarters = new TextView(this);
        FormatTextView(Quarters,50,getString(R.string.Quarters),headerRow);
       // Quarters.setWidth(pxToDp(400));
        Quarters.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

        verticalLine = new View(this);
        AddVerticalLine(verticalLine,headerRow);
        TextView txtBalance1 = new TextView(this);
        FormatTextView(txtBalance1,60,getString(R.string.Gross),headerRow);
       // txtBalance1.setWidth(pxToDp(400));
        txtBalance1.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

        verticalLine = new View(this);
        AddVerticalLine(verticalLine,headerRow);
        TextView Contribution = new TextView(this);
        FormatTextView(Contribution,200,getString(R.string.Contribution),headerRow);
      //  Contribution.setWidth(pxToDp(1200));
        Contribution.getLayoutParams().width = (int) getResources().getDimension(R.dimen.contribution);

        verticalLine = new View(this);
        AddVerticalLine(verticalLine,headerRow);
        TextView Owner = new TextView(this);
        FormatTextView(Owner,70,getString(R.string.Owner),headerRow);
      //  Owner.setWidth(pxToDp(400));
        Owner.getLayoutParams().width = (int) getResources().getDimension(R.dimen.owner);

        verticalLine = new View(this);
        AddVerticalLine(verticalLine,headerRow);
        TextView Retirement = new TextView(this);
        FormatTextView(Retirement,100,getString(R.string.Retirement),headerRow);
       // Retirement.setWidth(pxToDp(500));
        Retirement.getLayoutParams().width = (int) getResources().getDimension(R.dimen.retirement);

        verticalLine = new View(this);
        AddVerticalLine(verticalLine,headerRow);
        TextView Chargeable = new TextView(this);
        FormatTextView(Chargeable,70,getString(R.string.Chargeable),headerRow);
        //Chargeable.setWidth(pxToDp(400));
        Chargeable.getLayoutParams().width = (int) getResources().getDimension(R.dimen.chargeable);

        verticalLine = new View(this);
        AddVerticalLine(verticalLine,headerRow);
        TextView Tax = new TextView(this);
        FormatTextView(Tax,70,getString(R.string.Tax),headerRow);
        //Tax.setWidth(pxToDp(400));
        Tax.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

        verticalLine = new View(this);
        AddVerticalLine(verticalLine,headerRow);
        TextView Personal = new TextView(this);
        FormatTextView(Personal,70,getString(R.string.Personal),headerRow);
        //Personal.setWidth(pxToDp(400));
        Personal.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

        TextView Insurance = new TextView(this);
        FormatTextView(Insurance,70,getString(R.string.Insurance),headerRow);
        //Insurance.setWidth(pxToDp(400));
        Insurance.getLayoutParams().width = (int) getResources().getDimension(R.dimen.insurance);

        verticalLine = new View(this);
        AddVerticalLine(verticalLine,headerRow);
        TextView Paye = new TextView(this);
        FormatTextView(Paye,70,getString(R.string.Paye),headerRow);
        //Paye.setWidth(pxToDp(400));
        Paye.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

        verticalLine = new View(this);
        AddVerticalLine(verticalLine,headerRow);
        stk.addView(headerRow);


    }
    public void RenderTableHeaderX(TableRow  headerRow,TableLayout stk)
    {
        View verticalLine = new View(this);
        AddVerticalLine(verticalLine,headerRow);
        TextView MONTH = new TextView(this);
        FormatTextView(MONTH,50,"",headerRow);
        verticalLine = new View(this);
        AddVerticalLine(verticalLine,headerRow);


        TextView Basic = new TextView(this);
        FormatTextView(Basic,50,"",headerRow);

        verticalLine = new View(this);
        AddVerticalLine(verticalLine,headerRow);


        TextView Benefit = new TextView(this);
        FormatTextView(Benefit,40,"",headerRow);
        verticalLine = new View(this);
        AddVerticalLine(verticalLine,headerRow);


        TextView Quarters = new TextView(this);
        FormatTextView(Quarters,50,"",headerRow);
        verticalLine = new View(this);
        AddVerticalLine(verticalLine,headerRow);


        TextView txtBalance1 = new TextView(this);
        FormatTextView(txtBalance1,60,"",headerRow);

        verticalLine = new View(this);
        AddVerticalLine(verticalLine,headerRow);
        TextView Contribution = new TextView(this);
        FormatTextView(Contribution,200,"KSH",headerRow);

        verticalLine = new View(this);
        AddVerticalLine(verticalLine,headerRow);
        TextView Owner = new TextView(this);
        FormatTextView(Owner,70,"",headerRow);

        verticalLine = new View(this);
        AddVerticalLine(verticalLine,headerRow);
        TextView Retirement = new TextView(this);
        FormatTextView(Retirement,100,"",headerRow);

        verticalLine = new View(this);
        AddVerticalLine(verticalLine,headerRow);
        TextView Chargeable = new TextView(this);
        FormatTextView(Chargeable,70,"KSH",headerRow);

        verticalLine = new View(this);
        AddVerticalLine(verticalLine,headerRow);
        TextView Tax = new TextView(this);
        FormatTextView(Tax,70,"KSH",headerRow);

        verticalLine = new View(this);
        AddVerticalLine(verticalLine,headerRow);
        TextView Personal = new TextView(this);
        FormatTextView(Personal,70,"",headerRow);

        verticalLine = new View(this);

        TextView Insurance = new TextView(this);
        FormatTextView(Insurance,70,"",headerRow);

        verticalLine = new View(this);
        AddVerticalLine(verticalLine,headerRow);
        TextView Paye = new TextView(this);
        FormatTextView(Paye,70,"(J-K)",headerRow);

        verticalLine = new View(this);
        AddVerticalLine(verticalLine,headerRow);
        stk.addView(headerRow);

       View v = new View(this);
        v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, 1));
        v.setBackgroundColor(Color.rgb(51, 51, 51));
        stk.addView(v);

    }
    public void RenderTableHeader2(TableRow  headerRow,TableLayout stk)
    {
        View verticalLine2 = new View(this);
        AddVerticalLine(verticalLine2,headerRow);
        TextView MONTH2 = new TextView(this);
        FormatTextView2(MONTH2,50,"",headerRow);
        verticalLine2 = new View(this);
        AddVerticalLine(verticalLine2,headerRow);


        TextView Basic2 = new TextView(this);
        FormatTextView2(Basic2,50,"A",headerRow);
        verticalLine2 = new View(this);
        AddVerticalLine(verticalLine2,headerRow);
        TextView Benefit2 = new TextView(this);
        FormatTextView2(Benefit2,50,"B",headerRow);

        verticalLine2 = new View(this);
        AddVerticalLine(verticalLine2,headerRow);
        TextView Quarters2 = new TextView(this);
        FormatTextView2(Quarters2,50,"C",headerRow);

        verticalLine2 = new View(this);
        AddVerticalLine(verticalLine2,headerRow);
        TextView txtBalance2 = new TextView(this);
        FormatTextView2(txtBalance2,60,"D",headerRow);


        verticalLine2 = new View(this);
        AddVerticalLine(verticalLine2,headerRow);
        TextView Contribution2 = new TextView(this);
        FormatTextView2(Contribution2,200,"E",headerRow);


        verticalLine2 = new View(this);
        AddVerticalLine(verticalLine2,headerRow);
        TextView Owner2 = new TextView(this);
        FormatTextView2(Owner2,70,"F",headerRow);

        verticalLine2 = new View(this);
        AddVerticalLine(verticalLine2,headerRow);
        TextView Retirement2 = new TextView(this);
        FormatTextView2(Retirement2,100,"G",headerRow);

        verticalLine2 = new View(this);
        AddVerticalLine(verticalLine2,headerRow);
        TextView Chargeable2 = new TextView(this);
        FormatTextView2(Chargeable2,70,"H",headerRow);

        verticalLine2 = new View(this);
        AddVerticalLine(verticalLine2,headerRow);
        TextView Tax2 = new TextView(this);
        FormatTextView2(Tax2,70,"J",headerRow);

        verticalLine2 = new View(this);
        AddVerticalLine(verticalLine2,headerRow);
        TextView Personal2 = new TextView(this);
        FormatTextView2(Personal2,70,"K 1162",headerRow);

        verticalLine2 = new View(this);

        TextView Insurance2 = new TextView(this);
        FormatTextView2(Insurance2,70,"600",headerRow);

        verticalLine2 = new View(this);
        AddVerticalLine(verticalLine2,headerRow);
        TextView Paye2 = new TextView(this);
        FormatTextView2(Paye2,70,"L",headerRow);

        verticalLine2 = new View(this);
        AddVerticalLine(verticalLine2,headerRow);
        stk.addView(headerRow);



    }
    public void RenderTableBody(TableLayout stk,String arrayName,String jsonResponce)
    {

        try {
            JSONObject jsonObject = new JSONObject(jsonResponce);
            JSONArray jresult = jsonObject.getJSONArray(arrayName);


            for (int i = 0; i <= jresult.length() - 1; i++) {
                DecimalFormat df = new DecimalFormat("#.00");
                JSONObject object = jresult.getJSONObject(i);

                Double GrossAmount = Double.parseDouble(object.getString("GrossAmount"));
                Double NonCashBenefits = Double.parseDouble(object.getString("NonCashBenefits"));
                Double Quarters = Double.parseDouble(object.getString("Quarters"));
                Double Totalabc = Double.parseDouble(object.getString("Totalabc"));
                Double Calculated = Double.parseDouble(object.getString("Calculated"));
                Double Actual = Double.parseDouble(object.getString("Actual"));
                Double Limit = Double.parseDouble(object.getString("Limit"));
                Double OwnerOccupiedInterest = Double.parseDouble(object.getString("OwnerOccupiedInterest"));
                Double ContributionBenefit = Double.parseDouble(object.getString("ContributionBenefit"));
                Double ChargeablePay = Double.parseDouble(object.getString("ChargeablePay"));
                Double Tax = Double.parseDouble(object.getString("Tax"));
                Double MPR = Double.parseDouble(object.getString("MPR"));
                Double MIR = Double.parseDouble(object.getString("MIR"));
                Double PAYEAmount = Double.parseDouble(object.getString("PAYEAmount"));

                TableRow headerRow = new TableRow(this);
                View verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                TextView MONTH2 = new TextView(this);
                FormatTextView2(MONTH2, 50, object.getString("CurrentPeriod"), headerRow);
                verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                MONTH2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

                TextView Basic2 = new TextView(this);
                FormatTextView3(Basic2, 50, df.format(GrossAmount)+"", headerRow);
                Basic2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

                verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                TextView Benefit2 = new TextView(this);
                FormatTextView3(Benefit2, 50, df.format(NonCashBenefits)+"", headerRow);
                Benefit2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

                verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                TextView Quarters2 = new TextView(this);
                FormatTextView3(Quarters2, 50, df.format(Quarters)+"", headerRow);
                Quarters2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

                verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                TextView txtBalance2 = new TextView(this);
                FormatTextView3(txtBalance2, 60, df.format(Totalabc)+"", headerRow);
                txtBalance2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);
//============================================
                verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                TextView Contribution2 = new TextView(this);
                FormatTextView3(Contribution2, 66, df.format(Calculated)+"", headerRow);
                Contribution2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);


                verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                TextView Contribution3 = new TextView(this);
                FormatTextView3(Contribution3, 66, df.format(Actual)+"", headerRow);
                Contribution3.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

                verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                TextView Contribution4 = new TextView(this);
                FormatTextView3(Contribution4, 66, df.format(Limit)+"", headerRow);
                Contribution4.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);
//=============================
                verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                TextView Owner2 = new TextView(this);
                FormatTextView3(Owner2, 70, df.format(OwnerOccupiedInterest)+"", headerRow);
                Owner2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.owner);

                verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                TextView Retirement2 = new TextView(this);
                FormatTextView3(Retirement2, 100, df.format(ContributionBenefit)+"", headerRow);
                Retirement2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.retirement);

                verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                TextView Chargeable2 = new TextView(this);
                FormatTextView3(Chargeable2, 70, df.format(ChargeablePay)+"", headerRow);
                Chargeable2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.chargeable);

                verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                TextView Tax2 = new TextView(this);
                FormatTextView3(Tax2, 70, df.format(Tax)+"", headerRow);
                Tax2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

                verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                TextView Personal2 = new TextView(this);
                FormatTextView3(Personal2, 70, df.format(MPR)+"", headerRow);
                Personal2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);
                //verticalLine2 = new View(this);
                // AddVerticalLine(verticalLine2, headerRow);
                TextView Paye2 = new TextView(this);
                FormatTextView3(Paye2, 70, df.format(MIR)+"", headerRow);
                Paye2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.insurance);

                verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                TextView Paye3 = new TextView(this);
                FormatTextView3(Paye3, 70, df.format(PAYEAmount)+"", headerRow);
                Paye3.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

                verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                stk.addView(headerRow);

                View v2 = new View(this);
                v2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
                v2.setBackgroundColor(Color.rgb(51, 51, 51));
                stk.addView(v2);


            }
        }catch (JSONException e)
        {
            e.printStackTrace();
        }

    }
    public void RenderTableSumation(TableLayout stk,TableRow headerRow,String arrayName,String jsonResponce)
    {

        try {
            JSONObject jsonObject = new JSONObject(jsonResponce);
            JSONArray jresult = jsonObject.getJSONArray(arrayName);


            for (int i = 0; i <= jresult.length() - 1; i++) {
                DecimalFormat df = new DecimalFormat("#.00");
                JSONObject object = jresult.getJSONObject(i);

                Double GrossAmount = Double.parseDouble(object.getString("GrossSum"));
                Double NonCashBenefits = Double.parseDouble(object.getString("NonCashBenefitsSum"));
                Double Quarters = Double.parseDouble(object.getString("QuartersSum"));
                Double Totalabc = Double.parseDouble(object.getString("TotalabcSum"));
                Double Calculated = Double.parseDouble(object.getString("CalculatedSum"));
                Double Actual = Double.parseDouble(object.getString("ActualSum"));
                Double Limit = Double.parseDouble(object.getString("LimitSum"));
                Double OwnerOccupiedInterest = Double.parseDouble(object.getString("OwnerOccupiedInterestSum"));
                Double ContributionBenefit = Double.parseDouble(object.getString("ContributionBenefitSum"));
                Double ChargeablePay = Double.parseDouble(object.getString("ChargeablePaySum"));
                Double Tax = Double.parseDouble(object.getString("TaxSum"));
                Double MPR = Double.parseDouble(object.getString("MPRSum"));
                Double MIR = Double.parseDouble(object.getString("MIRSum"));
                Double PAYEAmount = Double.parseDouble(object.getString("PAYEAmountSum"));
                PAYEAmountSum.setText(df.format(PAYEAmount)+"");
                ChargeablePaySum.setText(df.format(ChargeablePay)+"");

                View verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                TextView MONTH2 = new TextView(this);
                FormatTextView2(MONTH2, 50, "Totals", headerRow);
                verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                MONTH2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

                TextView Basic2 = new TextView(this);
                FormatTextView3(Basic2, 50, df.format(GrossAmount)+"", headerRow);
                Basic2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

                verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                TextView Benefit2 = new TextView(this);
                FormatTextView3(Benefit2, 50, df.format(NonCashBenefits)+"", headerRow);
                Benefit2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

                verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                TextView Quarters2 = new TextView(this);
                FormatTextView3(Quarters2, 50, df.format(Quarters)+"", headerRow);
                Quarters2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

                verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                TextView txtBalance2 = new TextView(this);
                FormatTextView3(txtBalance2, 60, df.format(Totalabc)+"", headerRow);
                txtBalance2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);
//============================================
                verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                TextView Contribution2 = new TextView(this);
                FormatTextView3(Contribution2, 66, df.format(Calculated)+"", headerRow);
                Contribution2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

                verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                TextView Contribution3 = new TextView(this);
                FormatTextView3(Contribution3, 66, df.format(Actual)+"", headerRow);
                Contribution3.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

                verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                TextView Contribution4 = new TextView(this);
                FormatTextView3(Contribution4, 66, df.format(Limit)+"", headerRow);
                Contribution4.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);
//=============================
                verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                TextView Owner2 = new TextView(this);
                FormatTextView3(Owner2, 70, df.format(OwnerOccupiedInterest)+"", headerRow);
                Owner2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.owner);

                verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                TextView Retirement2 = new TextView(this);
                FormatTextView3(Retirement2, 100, df.format(ContributionBenefit)+"", headerRow);
                Retirement2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.retirement);

                verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                TextView Chargeable2 = new TextView(this);
                FormatTextView3(Chargeable2, 70, df.format(ChargeablePay)+"", headerRow);
                Chargeable2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.chargeable);

                verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                TextView Tax2 = new TextView(this);
                FormatTextView3(Tax2, 70, df.format(Tax)+"", headerRow);
                Tax2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

                verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                TextView Personal2 = new TextView(this);
                FormatTextView3(Personal2, 70, df.format(MPR)+"", headerRow);
                Personal2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);
                //verticalLine2 = new View(this);
               // AddVerticalLine(verticalLine2, headerRow);
                TextView Paye2 = new TextView(this);
                FormatTextView3(Paye2, 70, df.format(MIR)+"", headerRow);
                Paye2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.insurance);

                verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                TextView Paye3 = new TextView(this);
                FormatTextView3(Paye3, 70, df.format(PAYEAmount)+"", headerRow);
                Paye3.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

                verticalLine2 = new View(this);
                AddVerticalLine(verticalLine2, headerRow);
                stk.addView(headerRow);

                View v2 = new View(this);
                v2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
                v2.setBackgroundColor(Color.rgb(51, 51, 51));
                stk.addView(v2);


            }
        }catch (JSONException e)
        {
            e.printStackTrace();
        }

    }
    public void RenderTableHeader3(TableRow  headerRow,TableLayout stk)
    {
        View verticalLine2 = new View(this);
        AddVerticalLine(verticalLine2,headerRow);
        TextView MONTH2 = new TextView(this);
        FormatTextView(MONTH2,220,"",headerRow);
        verticalLine2 = new View(this);
        AddVerticalLine(verticalLine2,headerRow);
        MONTH2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

        TextView Basic2 = new TextView(this);
        FormatTextView(Basic2,50,"KSH",headerRow);
        verticalLine2 = new View(this);
        Basic2.setWidth(pxToDp(400));
        Basic2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

        AddVerticalLine(verticalLine2,headerRow);
        TextView Benefit2 = new TextView(this);
        FormatTextView(Benefit2,50,"KSH",headerRow);
        Benefit2.setWidth(pxToDp(400));
        verticalLine2 = new View(this);
        AddVerticalLine(verticalLine2,headerRow);
        Benefit2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

        TextView Quarters2 = new TextView(this);
        FormatTextView(Quarters2,40,"KSH",headerRow);
        Quarters2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

        verticalLine2 = new View(this);
        AddVerticalLine(verticalLine2,headerRow);

        TextView txtBalance2 = new TextView(this);
        FormatTextView(txtBalance2,60,"KSH",headerRow);
        verticalLine2 = new View(this);
         AddVerticalLine(verticalLine2,headerRow);
        txtBalance2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

        TextView Contribution2 = new TextView(this);
        FormatTextView2(Contribution2,66,getString(R.string.E130ofA),headerRow);
        Contribution2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

      verticalLine2 = new View(this);
        AddVerticalLine(verticalLine2,headerRow);
        TextView Contribution3 = new TextView(this);
        FormatTextView2(Contribution3,66,getString(R.string.E2Actual),headerRow);
        Contribution3.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

        verticalLine2 = new View(this);
        AddVerticalLine(verticalLine2,headerRow);
        TextView Contribution4 = new TextView(this);
        FormatTextView2(Contribution4,66,getString(R.string.E3Fixed),headerRow);
        Contribution4.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);


        verticalLine2 = new View(this);
        AddVerticalLine(verticalLine2,headerRow);
        TextView Owner2 = new TextView(this);
        FormatTextView(Owner2,70,getString(R.string.AmountOFInterest),headerRow);
        Owner2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.owner);


        verticalLine2 = new View(this);
        AddVerticalLine(verticalLine2,headerRow);
        TextView Retirement2 = new TextView(this);
        FormatTextView2(Retirement2,100,getString(R.string.TheLowestofEAddedofF),headerRow);
        Retirement2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.retirement);

        verticalLine2 = new View(this);
        AddVerticalLine(verticalLine2,headerRow);
        TextView Chargeable2 = new TextView(this);
        FormatTextView(Chargeable2,70,"",headerRow);
        Chargeable2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.chargeable);

        verticalLine2 = new View(this);
        AddVerticalLine(verticalLine2,headerRow);
        TextView Tax2 = new TextView(this);
        FormatTextView(Tax2,70,"",headerRow);
        Tax2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

        verticalLine2 = new View(this);
        AddVerticalLine(verticalLine2,headerRow);
        TextView Personal2 = new TextView(this);
        FormatTextView(Personal2,140,"Total Ksh 1656",headerRow);
        Personal2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.Total);

        verticalLine2 = new View(this);
        AddVerticalLine(verticalLine2,headerRow);
        TextView Personal3 = new TextView(this);
        FormatTextView2(Personal3,70,"KSH",headerRow);
        Personal3.getLayoutParams().width = (int) getResources().getDimension(R.dimen.period);

        verticalLine2 = new View(this);
        AddVerticalLine(verticalLine2,headerRow);
        stk.addView(headerRow);

        View v2 = new View(this);
        v2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
        v2.setBackgroundColor(Color.rgb(51, 51, 51));
        stk.addView(v2);

    }
    public  void FormatTextView(TextView textView,int width,String title, TableRow headerRow)
    {

        TextView pf = new TextView(this);
        pf.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setText(Html.fromHtml(title));
        textView.setTextColor(Color.BLACK);
        textView.setGravity(Gravity.CENTER);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setPadding(0,5,2,0);
        textView.setSingleLine(false);
      //  textView.setWidth(pxToDp(width));
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
        headerRow.addView(textView);
    }
    public  void FormatTextView2(TextView textView,int width,String title, TableRow headerRow)
    {
        TextView pf = new TextView(this);
        pf.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        textView.setText(Html.fromHtml(title));
        textView.setTextColor(Color.BLACK);
        textView.setGravity(Gravity.CENTER);
        textView.setTypeface(null, Typeface.NORMAL);
        textView.setPadding(0,5,2,0);
        textView.setSingleLine(false);
        //textView.setWidth(pxToDp(width));
          textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
        headerRow.addView(textView);

       // TextView tv = new TextView(v.getContext());

    }
    public  void FormatTextViewXX(TextView textView,int width,String title, TableRow headerRow)
    {
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setText(Html.fromHtml(title));
        textView.setTextColor(Color.BLACK);
        textView.setGravity(Gravity.CENTER);
        textView.setTypeface(null, Typeface.NORMAL);
        textView.setPadding(0,5,2,0);
        textView.setSingleLine(false);
        textView.setWidth(pxToDp(width));
        textView.setEllipsize(TextUtils.TruncateAt.END);
        headerRow.addView(textView);

        // TextView tv = new TextView(v.getContext());

    }
    public  void RenderFooter(String arrayName,String jsonResponce)
    {

        TextView leftTextView = (TextView)findViewById(R.id.leftTextView);
        TextView rightTextView = (TextView)findViewById(R.id.rightTextView);
        try {
            JSONObject jsonObject = new JSONObject(jsonResponce);
            JSONArray jresult = jsonObject.getJSONArray(arrayName);
            leftTextView.setText(Html.fromHtml("1. Use<br>" +
                    "(a) For all liable employees and where director/employee<br>" +
                    "receives benefits in addition to cash emoluments.<br><br>(b) Where an employee is eligible to deductionn on owner<br>" +
                    "occupier interest Allowable and the total interest payable in the<br>year is Kshs 100,000/= and above.<br><br>2 (a) Deductible interest in respect of any month must be standard<br>" +
                    "K.shs 12,500/="));
            leftTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);

            rightTextView.setText(Html.fromHtml("(b)" +
                    "Attach<br>" +
                    "(i) Photostat copy of preceding years ' certificate or<br>" +
                    "confirmation of current years borrowing, if applicable from<br>" +
                    "financial Institution.<br><br>" +
                    "(ii) The DECLARATION duly signed by the employee to form" +
                    "P9A.<br><br>" +
                    "NAMES OF FINANCIAL INSTITUTION ADVANCING MORTAGE<br>" +
                    "LOAN<br>" +
                    "L.R. NO. OF OWNER OCCUPIED PROPERTY<br>" +
                    "DATE OF OCCUPATION OF HOUSE:"));
            rightTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }
    public  void FormatTextView3(TextView textView,int width,String title, TableRow headerRow)
    {

        textView.setText(Html.fromHtml(title));
        textView.setTextColor(Color.BLACK);
        textView.setGravity(Gravity.RIGHT);
        textView.setTypeface(null, Typeface.NORMAL);
        textView.setPadding(0,5,2,0);
        textView.setSingleLine(false);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        headerRow.addView(textView);
    }
    public int pxToDp(int px) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }
}
