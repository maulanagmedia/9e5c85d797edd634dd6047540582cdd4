package gmedia.net.id.pullens.StartOrder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.vision.text.Line;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gmedia.net.id.pullens.R;
import gmedia.net.id.pullens.RiawayatOrder.RiwayatOrder;
import gmedia.net.id.pullens.Utils.SavedMejaManager;
import gmedia.net.id.pullens.Utils.ServerLocalManager;
import gmedia.net.id.pullens.Utils.ServerURL;

public class StartOrderActivity extends AppCompatActivity {

    private String TAG = "TAG";
    private LinearLayout llScan;
    private ItemValidation iv = new ItemValidation();
    private String barcodeValue = "";
    private ProgressBar pbLoading;
    private SessionManager session;
    private ServerURL serverURL;
    private ServerLocalManager serverManager;
    private WifiManager wifi;
    private String currentSSIDName = "";
    private String currentSSIDMac = "";
    private SavedMejaManager mejaManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_order);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Start Order");

        initUI();
    }

    private void initUI() {

        llScan = (LinearLayout) findViewById(R.id.ll_scan);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        barcodeValue = "";
        session= new SessionManager(StartOrderActivity.this);
        serverURL = new ServerURL(StartOrderActivity.this);
        serverManager = new ServerLocalManager(StartOrderActivity.this);
        mejaManager = new SavedMejaManager(StartOrderActivity.this);
        currentSSIDName = "";
        currentSSIDMac = "";

        initEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();

        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifi.isWifiEnabled() == false)
        {
            //Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
            /*Snackbar.make(findViewById(android.R.id.content), "Wifi is disabled, please making it enabled",
                    Snackbar.LENGTH_SHORT).setAction("OK",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    }).show();*/
            wifi.setWifiEnabled(true);
            Toast.makeText(getApplicationContext(), "Mohon sambungkan wifi anda ke AP "+ getResources().getString(R.string.ssid_name), Toast.LENGTH_LONG).show();
            startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
        }else{
            refreshAPinfo(StartOrderActivity.this);
        }
    }

    private void initEvent() {

        llScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openScanBarcode();
            }
        });
    }

    private void refreshAPinfo(Context context) {

        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifi.getConnectionInfo();
        currentSSIDName = wifiInfo.getSSID().replace("\"","");
        currentSSIDMac = wifiInfo.getBSSID();
        Log.d(TAG, "refreshAPinfo: "+currentSSIDName);
        /*ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !StringUtil.isBlank(connectionInfo.getSSID())) {
                ssid = connectionInfo.getSSID();
            }
        }
        return ssid;*/
    }

    private void openScanBarcode() {

        IntentIntegrator integrator = new IntentIntegrator(StartOrderActivity.this);
        //integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        integrator.initiateScan();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {

                Log.d(TAG, "onActivityResult: Scan failed ");
            } else {

                Log.d(TAG, "barcode: "+result.getContents());
                barcodeValue = result.getContents();
                validateWifi();
                //getServer();
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void validateWifi(){

        pbLoading.setVisibility(View.VISIBLE);

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("ssid", currentSSIDName);
            jBody.put("mac", currentSSIDMac);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(StartOrderActivity.this, jBody, "POST", ServerURL.validateWifi, "", "", 0, session.getUid(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray jsonArray = response.getJSONArray("response");
                        if(jsonArray.length() > 0){
                            for(int i = 0; i < jsonArray.length(); i++){

                                getServer();
                                break;
                            }
                        }else{

                            pbLoading.setVisibility(View.GONE);
                            Snackbar.make(findViewById(android.R.id.content), "Mohon sambungkan wifi anda ke AP "+ getResources().getString(R.string.ssid_name),
                                    Snackbar.LENGTH_INDEFINITE).setAction("OK",
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                                        }
                                    }).show();
                        }

                    }else{

                        wifi.setWifiEnabled(true);
                        Toast.makeText(getApplicationContext(), "Mohon sambungkan wifi anda ke AP "+ getResources().getString(R.string.ssid_name), Toast.LENGTH_LONG).show();
                        startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                        pbLoading.setVisibility(View.GONE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(StartOrderActivity.this, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String result) {
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(StartOrderActivity.this, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getServer() {

        pbLoading.setVisibility(View.VISIBLE);

        ApiVolley request = new ApiVolley(StartOrderActivity.this, new JSONObject(), "GET", ServerURL.getServer, "", "", 0, session.getUid(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        String serverAddress = response.getJSONObject("response").getString("server");

                        if(serverAddress != null && serverAddress.length() > 0){
                            serverManager.saveServer(serverAddress);
                            serverURL = new ServerURL(StartOrderActivity.this);

                            if(barcodeValue != null && barcodeValue.length() > 0){

                                getMejaByBarcode();
                            }else{
                                Toast.makeText(StartOrderActivity.this, "Barcode tidak terdeteksi dengan benar", Toast.LENGTH_LONG).show();
                            }
                        }
                    }else{
                        pbLoading.setVisibility(View.GONE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(StartOrderActivity.this, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String result) {
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(StartOrderActivity.this, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getMejaByBarcode(){

        pbLoading.setVisibility(View.VISIBLE);
        serverURL = new ServerURL(StartOrderActivity.this);

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("barcode", barcodeValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(StartOrderActivity.this, jBody, "POST", serverURL.getMejaByBarcode(), "", "", 0, session.getUid(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                String message = "Terjadi kesalahan saat memuat data, mohon ulangi kembali";
                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    message = response.getJSONObject("metadata").getString("message");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray jsonArray = response.getJSONArray("response");
                        for(int i = 0; i < jsonArray.length(); i++){

                            JSONObject jo = jsonArray.getJSONObject(i);
                            String kdmeja = jo.getString("kdmeja");
                            String nmmeja = jo.getString("nmmeja");
                            mejaManager.saveMeja(kdmeja, nmmeja);
                            Intent intent = new Intent(StartOrderActivity.this,RiwayatOrder.class);
                            startActivity(intent);
                            break;
                        }
                    }else{

                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                        startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                    }

                    pbLoading.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(StartOrderActivity.this, message, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String result) {
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(StartOrderActivity.this, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
}
