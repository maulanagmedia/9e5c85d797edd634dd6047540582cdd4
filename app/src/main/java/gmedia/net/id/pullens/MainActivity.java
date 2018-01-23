package gmedia.net.id.pullens;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.text.Line;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ImageUtils;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.RuntimePermissionsActivity;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.pullens.Adapter.ListHomeMenuAdapter;
import gmedia.net.id.pullens.FavoriteMenu.FavMakanan;
import gmedia.net.id.pullens.FavoriteMenu.FavMinuman;
import gmedia.net.id.pullens.RiawayatOrder.RiwayatOrder;
import gmedia.net.id.pullens.StartOrder.StartOrderActivity;
import gmedia.net.id.pullens.Utils.SavedMejaManager;
import gmedia.net.id.pullens.Utils.ServerLocalManager;
import gmedia.net.id.pullens.Utils.ServerURL;

public class MainActivity extends RuntimePermissionsActivity {

    private static boolean doubleBackToExitPressedOnce;
    private boolean exitState = false;
    private int timerClose = 2000;

    private static int startIndex = 0;
    private static int count = 10;
    private static String keyword = "";
    private LinearLayout llOrder;
    private ListView lvListMenu;
    private ProgressBar pbLoading;
    private Button btnRefresh;
    private List<CustomItem> masterList;
    private SessionManager session;
    private ItemValidation iv = new ItemValidation();
    private ListHomeMenuAdapter homeMenuAdapter;
    private boolean isLoading = false;
    private View footerList;
    private TextView tvTitle;
    private static final int REQUEST_PERMISSIONS = 20;
    private WifiManager wifi;

    private ServerURL serverURL;
    private ServerLocalManager serverManager;
    private String barcodeValue = "";
    private String currentSSIDName = "";
    private String currentSSIDMac = "";
    private SavedMejaManager mejaManager;
    private String TAG = "TAG";
    private boolean isScanBarcode = false;
    private Context context;
    private AlertDialog alertRating;
    private boolean isDialogOpen = false;
    private int[] tabIcons = {
            R.drawable.ic_rice_bowl,
            R.drawable.ic_cofee
    };

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_logout));
        /*getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_appbar);

        tvTitle = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.tv_title);
        setTitle("");
        tvTitle.setText("Favorite Menu");*/

        setTitle("Favorite Menu");
        context = this;

        //Check close statement
        doubleBackToExitPressedOnce = false;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            if (bundle.getBoolean("exit", false)) {
                exitState = true;
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }

        // for android > M
        if (ContextCompat.checkSelfPermission(
                MainActivity.this, android.Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {

            MainActivity.super.requestAppPermissions(new
                            String[]{android.Manifest.permission.ACCESS_WIFI_STATE, android.Manifest.permission.CHANGE_WIFI_STATE}, R.string
                            .runtime_permissions_txt
                    , REQUEST_PERMISSIONS);
        }

        initUI();

    }

    @Override
    public void onPermissionsGranted(int requestCode) {

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
        }
    }

    private void initUI() {

        llOrder = (LinearLayout) findViewById(R.id.ll_order);
        lvListMenu = (ListView) findViewById(R.id.lv_list_menu);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        btnRefresh = (Button) findViewById(R.id.btn_refresh);
        LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        footerList = li.inflate(R.layout.footer_list, null);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        LinearLayout tabOne = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        TextView tvTabOne =  (TextView) tabOne.findViewById(R.id.tab);
        ImageView ivTabOne =  (ImageView) tabOne.findViewById(R.id.iv);
        tvTabOne.setText("MAKANAN");
        ivTabOne.setImageDrawable(getResources().getDrawable(tabIcons[0]));

        /*TextView tabOne = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabOne.setText("MAKANAN");
        tabOne.setCompoundDrawablesWithIntrinsicBounds(tabIcons[0], 0, 0, 0);*/
        tabLayout.getTabAt(0).setCustomView(tabOne);

        LinearLayout tabTwo = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        TextView tvTabTwo =  (TextView) tabTwo.findViewById(R.id.tab);
        ImageView ivTabTwo =  (ImageView) tabTwo.findViewById(R.id.iv);
        tvTabTwo.setText("MINUMAN");
        ivTabTwo.setImageDrawable(getResources().getDrawable(tabIcons[1]));

        /*TextView tabTwo = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabTwo.setText("MINUMAN");
        tabTwo.setCompoundDrawablesWithIntrinsicBounds(tabIcons[1], 0, 0, 0);*/
        tabLayout.getTabAt(1).setCustomView(tabTwo);

        currentSSIDName = "";
        currentSSIDMac = "";
        barcodeValue = "";
        isScanBarcode = false;
        // Initial
        startIndex = 0;
        keyword = "";
        isLoading = false;
        masterList = new ArrayList<>();
        session = new SessionManager(MainActivity.this);
        serverURL = new ServerURL(MainActivity.this);
        serverManager = new ServerLocalManager(MainActivity.this);
        mejaManager = new SavedMejaManager(MainActivity.this);

        initEvent();
    }

    private void initEvent() {

        llOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*Intent intent = new Intent(MainActivity.this, StartOrderActivity.class);
                startActivity(intent);*/

                wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                if (wifi.isWifiEnabled() == false)
                {

                    wifi.setWifiEnabled(true);
                    Toast.makeText(MainActivity.this, "Mohon sambungkan wifi anda ke AP "+ getResources().getString(R.string.ssid_name), Toast.LENGTH_LONG).show();
                    startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                }else{

                    try {
                        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        WifiInfo wifiInfo = wifi.getConnectionInfo();
                        currentSSIDName = (wifiInfo.getSSID() != null) ? wifiInfo.getSSID().replace("\"","") : "";
                        currentSSIDMac = (wifiInfo.getBSSID() != null ) ? wifiInfo.getBSSID() : "";
                        Log.d(TAG, "refreshAPinfo: "+currentSSIDName);
                    }catch (Exception e){

                        currentSSIDName = "";
                        currentSSIDMac = "";
                    }

                    isScanBarcode = true;
                    validateWifiBarcode();
                }

            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startIndex = 0;
                btnRefresh.setVisibility(View.GONE);
                getHotMenu();
            }
        });

    }

    private void openScanBarcode() {

        IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
        //integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        integrator.initiateScan();
    }

    @Override
    protected void onResume() {
        super.onResume();

        isScanBarcode = false;
        //getServer();
    }

    private void refreshAPinfo(Context context) {

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
            Toast.makeText(MainActivity.this, "Mohon sambungkan wifi anda ke AP "+ getResources().getString(R.string.ssid_name), Toast.LENGTH_LONG).show();
            startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
        }else{

            try {
                wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifi.getConnectionInfo();
                currentSSIDName = (wifiInfo.getSSID() != null) ? wifiInfo.getSSID().replace("\"","") : "";
                currentSSIDMac = (wifiInfo.getBSSID() != null ) ? wifiInfo.getBSSID() : "";
                Log.d(TAG, "refreshAPinfo: "+currentSSIDName);
            }catch (Exception e){

                currentSSIDName = "";
                currentSSIDMac = "";
            }

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

            validateWifiList();
        }
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

        ApiVolley request = new ApiVolley(MainActivity.this, jBody, "POST", ServerURL.validateWifi, "", "", 0, session.getUid(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray jsonArray = response.getJSONArray("response");
                        if(jsonArray.length() > 0){
                            for(int i = 0; i < jsonArray.length(); i++){

                                isScanBarcode = true;
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
                        Toast.makeText(MainActivity.this, "Mohon sambungkan wifi anda ke AP "+ getResources().getString(R.string.ssid_name), Toast.LENGTH_LONG).show();
                        startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                        pbLoading.setVisibility(View.GONE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String result) {
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void validateWifiBarcode(){

        pbLoading.setVisibility(View.VISIBLE);

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("ssid", currentSSIDName);
            jBody.put("mac", currentSSIDMac);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(MainActivity.this, jBody, "POST", ServerURL.validateWifi, "", "", 0, session.getUid(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray jsonArray = response.getJSONArray("response");
                        if(jsonArray.length() > 0){
                            for(int i = 0; i < jsonArray.length(); i++){

                                getServerBarcode();
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
                        Toast.makeText(MainActivity.this, "Mohon sambungkan wifi anda ke AP "+ getResources().getString(R.string.ssid_name), Toast.LENGTH_LONG).show();
                        startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                        pbLoading.setVisibility(View.GONE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String result) {
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void validateWifiList(){

        pbLoading.setVisibility(View.VISIBLE);

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("ssid", currentSSIDName);
            jBody.put("mac", currentSSIDMac);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(MainActivity.this, jBody, "POST", ServerURL.validateWifi, "", "", 0, session.getUid(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray jsonArray = response.getJSONArray("response");
                        if(jsonArray.length() > 0){
                            for(int i = 0; i < jsonArray.length(); i++){

                                getServerList();
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
                        Toast.makeText(MainActivity.this, "Mohon sambungkan wifi anda ke AP "+ getResources().getString(R.string.ssid_name), Toast.LENGTH_LONG).show();
                        startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                        pbLoading.setVisibility(View.GONE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String result) {
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getServer() {

        pbLoading.setVisibility(View.VISIBLE);

        ApiVolley request = new ApiVolley(MainActivity.this, new JSONObject(), "GET", ServerURL.getServer, "", "", 0, session.getUid(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        String serverAddress = response.getJSONObject("response").getString("server");

                        if(serverAddress != null && serverAddress.length() > 0){
                            serverManager.saveServer(serverAddress);
                            serverURL = new ServerURL(MainActivity.this);

                            if(isScanBarcode){

                                if(barcodeValue != null && barcodeValue.length() > 0){

                                    getMejaByBarcode();
                                }else{
                                    Toast.makeText(MainActivity.this, "QR Code tidak terdeteksi dengan benar", Toast.LENGTH_LONG).show();
                                    isScanBarcode = false;
                                }
                            }else{

                                pbLoading.setVisibility(View.GONE);
                                isLoading = false;
                                startIndex = 0;
                                keyword = "";
                                //getHotMenu();
                            }
                        }
                    }else{
                        pbLoading.setVisibility(View.GONE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String result) {
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getServerBarcode() {

        pbLoading.setVisibility(View.VISIBLE);

        ApiVolley request = new ApiVolley(MainActivity.this, new JSONObject(), "GET", ServerURL.getServer, "", "", 0, session.getUid(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        String serverAddress = response.getJSONObject("response").getString("server");

                        if(serverAddress != null && serverAddress.length() > 0){
                            serverManager.saveServer(serverAddress);
                            serverURL = new ServerURL(MainActivity.this);

                            openScanBarcode();
                        }else{

                            pbLoading.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, "Server tidak ditemukan, mohon coba kembali", Toast.LENGTH_LONG).show();
                        }
                    }else{
                        pbLoading.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Server tidak ditemukan, mohon coba kembali", Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String result) {
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getServerList() {

        pbLoading.setVisibility(View.VISIBLE);

        ApiVolley request = new ApiVolley(MainActivity.this, new JSONObject(), "GET", ServerURL.getServer, "", "", 0, session.getUid(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        String serverAddress = response.getJSONObject("response").getString("server");

                        if(serverAddress != null && serverAddress.length() > 0){
                            serverManager.saveServer(serverAddress);
                            serverURL = new ServerURL(MainActivity.this);

                            isLoading = false;
                            startIndex = 0;
                            keyword = "";
                            getHotMenu();
                        }
                    }else{
                        pbLoading.setVisibility(View.GONE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String result) {
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getMejaByBarcode(){

        pbLoading.setVisibility(View.VISIBLE);
        serverURL = new ServerURL(MainActivity.this);

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("barcode", barcodeValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(MainActivity.this, jBody, "POST", serverURL.getMejaByBarcode(), "", "", 0, session.getUid(), new ApiVolley.VolleyCallback() {
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
                            Intent intent = new Intent(MainActivity.this,RiwayatOrder.class);
                            startActivity(intent);
                            break;
                        }
                    }else{

                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                        startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                    }

                    pbLoading.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String result) {
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getHotMenu() {

        btnRefresh.setVisibility(View.GONE);
        serverManager = new ServerLocalManager(MainActivity.this);
        if(serverManager.getServer() != null && serverManager.getServer().length() > 0){
            serverURL = new ServerURL(MainActivity.this);
        }else{

            pbLoading.setVisibility(View.GONE);
            btnRefresh.setVisibility(View.GONE);
            setHomeMenuAdapter(null);
            return;
        }

        pbLoading.setVisibility(View.VISIBLE);
        masterList = new ArrayList<>();

        startIndex = 0;
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("keyword", keyword);
            jBody.put("start", String.valueOf(startIndex));
            jBody.put("count", String.valueOf(count));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(MainActivity.this, jBody, "POST", ServerURL.getHomeMenu, "", "", 0, session.getUid(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    masterList = new ArrayList<>();

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray jsonArray = response.getJSONArray("response");
                        for(int i = 0; i < jsonArray.length(); i++){

                            JSONObject jo = jsonArray.getJSONObject(i);
                            masterList.add(new CustomItem(jo.getString("id"), jo.getString("nama"), jo.getString("harga"), jo.getString("gambar"), jo.getString("keterangan"),  jo.getString("timestamp"), jo.getString("total_rating")));
                        }
                    }

                    setHomeMenuAdapter(masterList);
                    pbLoading.setVisibility(View.GONE);
                    btnRefresh.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    setHomeMenuAdapter(null);
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
                    btnRefresh.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String result) {
                setHomeMenuAdapter(null);
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
                btnRefresh.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setHomeMenuAdapter(List<CustomItem> listItem){

        isDialogOpen = false;
        lvListMenu.setAdapter(null);

        if(listItem != null && listItem.size() > 0){

            homeMenuAdapter = new ListHomeMenuAdapter(MainActivity.this, listItem);
            lvListMenu.setAdapter(homeMenuAdapter);

            lvListMenu.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {

                    int threshold = 1;
                    int count = lvListMenu.getCount();

                    if (i == SCROLL_STATE_IDLE) {
                        if (lvListMenu.getLastVisiblePosition() >= count - threshold && !isLoading) {

                            isLoading = true;
                            lvListMenu.addFooterView(footerList);
                            startIndex += count;
                            getMoreData();
                        }
                    }
                }

                @Override
                public void onScroll(AbsListView absListView, int i, int i1, int i2) {

                }
            });
        }

        lvListMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                CustomItem item = (CustomItem) adapterView.getItemAtPosition(i);

                if(!isDialogOpen){
                    getPenjualanByUid(item.getItem1(), item.getItem2());
                }

            }
        });
    }

    private void getPenjualanByUid(final String id, final String namaMenu) {

        isDialogOpen = true;
        pbLoading.setVisibility(View.VISIBLE);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("uid", session.getUid());
            jBody.put("kdbrg", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(MainActivity.this, jBody, "POST", serverURL.getPenjualanByUid(), "", "", 0, session.getUid(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                pbLoading.setVisibility(View.GONE);
                isDialogOpen = false;
                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray jsonArray = response.getJSONArray("response");
                        if(jsonArray.length()>0){

                            getDetailRatingMenu(id);
                        }else{
                            showDialog(MainActivity.this, 3, "Anda hanya dapat memberikan rating setelah memesan "+ namaMenu);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String result) {

                isDialogOpen = false;
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getDetailRatingMenu(final String id) {

        isDialogOpen = true;
        pbLoading.setVisibility(View.VISIBLE);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("uid", session.getUid());
            jBody.put("kdbrg", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(MainActivity.this, jBody, "POST", ServerURL.getDetailRating, "", "", 0, session.getUid(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                pbLoading.setVisibility(View.GONE);
                isDialogOpen = false;
                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray jsonArray = response.getJSONArray("response");
                        if(jsonArray.length()>0){

                            JSONObject jo = jsonArray.getJSONObject(0);
                            final String idRating = jo.getString("id");
                            String rating = jo.getString("rating");
                            String nmMenu = jo.getString("nmbrg");
                            float ratingFloat = iv.parseNullFloat(rating);
                            String feedback = jo.getString("feedback");
                            String photo = jo.getString("photo");

                            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            LayoutInflater inflater = (LayoutInflater) ((Activity) context).getSystemService(LAYOUT_INFLATER_SERVICE);
                            View viewDialog = inflater.inflate(R.layout.layout_rating_menu, null);
                            builder.setView(viewDialog);
                            builder.setCancelable(false);

                            final ImageView ivProfile = (ImageView) viewDialog.findViewById(R.id.iv_profile);
                            final ImageView ivClose = (ImageView) viewDialog.findViewById(R.id.iv_close);
                            final TextView tvMenu = (TextView) viewDialog.findViewById(R.id.tv_menu);
                            final RatingBar rbMenu = (RatingBar) viewDialog.findViewById(R.id.rb_menu);
                            final EditText edtFeedback = (EditText) viewDialog.findViewById(R.id.edt_feedback);
                            final Button btnSubmit = (Button) viewDialog.findViewById(R.id.btn_submit);

                            ImageUtils iu = new ImageUtils();
                            iu.LoadCircleRealImage(MainActivity.this, photo, ivProfile);
                            tvMenu.setText(nmMenu);
                            rbMenu.setRating(ratingFloat);
                            edtFeedback.setText(feedback);

                            alertRating = builder.create();
                            alertRating.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                            ivClose.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    if(alertRating != null) alertRating.dismiss();
                                }
                            });

                            btnSubmit.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    if(rbMenu.getRating() == 0){
                                        Toast.makeText(MainActivity.this, "Mohon berikan rating anda", Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    String rating = iv.floatToString(rbMenu.getRating());
                                    JSONObject jBody = new JSONObject();
                                    try {
                                        jBody.put("uid", session.getUid());
                                        jBody.put("kdbrg", id);
                                        jBody.put("rating", rating);
                                        jBody.put("feedback", edtFeedback.getText().toString());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    saveDetailRating(idRating, jBody);
                                }
                            });

                            alertRating.show();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String result) {
                isDialogOpen = false;
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveDetailRating(final String id, JSONObject jData) {

        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this,
                R.style.AppTheme_Custom_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Menyimpan...");
        progressDialog.show();

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("id", id);
            jBody.put("data", jData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(MainActivity.this, jBody, "POST", ServerURL.saveDetailRating, "", "", 0, session.getUid(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                progressDialog.dismiss();
                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        if(alertRating != null) alertRating.dismiss();

                        getHotMenu();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String result) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
            }
        });
    }

    private static void showDialog(Context context, int state, String message){

        if(state == 2){ // failed
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = (LayoutInflater) ((Activity) context).getSystemService(LAYOUT_INFLATER_SERVICE);
            View viewDialog = inflater.inflate(R.layout.layout_failed, null);
            builder.setView(viewDialog);
            builder.setCancelable(false);

            final TextView tvText1 = (TextView) viewDialog.findViewById(R.id.tv_text1);
            tvText1.setText(message);
            final Button btnOK = (Button) viewDialog.findViewById(R.id.btn_ok);

            final AlertDialog alert = builder.create();
            alert.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

            btnOK.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view2) {

                    if(alert != null) alert.dismiss();
                }
            });

            alert.show();
        }else if(state == 3){

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = (LayoutInflater) ((Activity) context).getSystemService(LAYOUT_INFLATER_SERVICE);
            View viewDialog = inflater.inflate(R.layout.layout_warning, null);
            builder.setView(viewDialog);
            builder.setCancelable(false);

            final TextView tvText1 = (TextView) viewDialog.findViewById(R.id.tv_text1);
            tvText1.setText(message);
            final Button btnOK = (Button) viewDialog.findViewById(R.id.btn_ok);

            final AlertDialog alert = builder.create();
            alert.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

            btnOK.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view2) {

                    if(alert != null) alert.dismiss();
                }
            });

            alert.show();
        }
    }

    private void getMoreData() {

        serverManager = new ServerLocalManager(MainActivity.this);
        if(serverManager.getServer() != null && serverManager.getServer().length() > 0){
            serverURL = new ServerURL(MainActivity.this);
        }else{

            setHomeMenuAdapter(null);
            return;
        }

        isLoading = true;
        final List<CustomItem> moreList = new ArrayList<>();
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("keyword", keyword);
            jBody.put("start", String.valueOf(startIndex));
            jBody.put("count", String.valueOf(count));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(MainActivity.this, jBody, "POST", ServerURL.getHomeMenu, "", "", 0, session.getUid(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            moreList.add(new CustomItem(jo.getString("id"), jo.getString("nama"), jo.getString("harga"), jo.getString("gambar"), jo.getString("keterangan"),  jo.getString("timestamp"), jo.getString("total_rating")));
                        }

                        lvListMenu.removeFooterView(footerList);
                        if(homeMenuAdapter!= null) homeMenuAdapter.addMoreData(moreList);
                    }
                    isLoading = false;
                } catch (JSONException e) {
                    e.printStackTrace();
                    isLoading = false;
                    lvListMenu.removeFooterView(footerList);
                }
            }

            @Override
            public void onError(String result) {
                isLoading = false;
                lvListMenu.removeFooterView(footerList);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                showLogoutDialog();
                return true;
            case R.id.menu_music:

                Intent intent = new Intent(MainActivity.this, PlaylistActivity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showLogoutDialog() {

        AlertDialog builder = new AlertDialog.Builder(MainActivity.this)
                .setIcon(getResources().getDrawable(R.mipmap.ic_launcher))
                .setTitle("Konfirmasi")
                .setMessage("Apakah anda yakin ingin logout?")
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                       Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                       intent.putExtra("logout", true);
                       intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                       startActivity(intent);
                       finish();
                       overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                })
                .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        final SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryText) {

                /*keyword = queryText;
                startIndex = 0;
                getHotMenu();*/
                FavMakanan.keyword = queryText;
                FavMinuman.keyword = queryText;

                FavMakanan.getServer();
                FavMinuman.getServer();

                iv.hideSoftKey(MainActivity.this);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                /*keyword = newText;
                startIndex = 0;
                getHotMenu();*/

                FavMakanan.keyword = newText;
                FavMinuman.keyword = newText;

                FavMakanan.getServer();
                FavMinuman.getServer();

                return true;
            }
        });

        MenuItemCompat.OnActionExpandListener expandListener = new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {

                return true;
            }
        };
        MenuItemCompat.setOnActionExpandListener(searchItem, expandListener);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {

        // Origin backstage
        if (doubleBackToExitPressedOnce) {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            intent.putExtra("exit", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            //System.exit(0);
        }

        if(!exitState && !doubleBackToExitPressedOnce){
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getResources().getString(R.string.app_exit), Toast.LENGTH_SHORT).show();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, timerClose);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if(position == 0){

                return new FavMakanan();
            }else{
                return new FavMinuman();
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }
    }
}
