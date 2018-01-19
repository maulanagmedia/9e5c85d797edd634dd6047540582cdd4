package gmedia.net.id.pullens;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.pullens.Adapter.ListPlaylistAdapter;
import gmedia.net.id.pullens.DetailOrder.DetailOrder;
import gmedia.net.id.pullens.Utils.FormatItem;
import gmedia.net.id.pullens.Utils.ServerURL;

public class PlaylistActivity extends AppCompatActivity {

    private static ItemValidation iv = new ItemValidation();
    private static int startIndex = 0;
    private static int count = 10;
    private static String keyword = "";
    private static ListView lvPlaylist;
    private static ProgressBar pbLoading;
    private static Button btnRefresh;
    private static List<CustomItem> masterList;
    private static SessionManager session;
    private static ListPlaylistAdapter adapter;
    private static boolean isLoading = false;
    private static View footerList;
    private static WifiManager wifi;
    private static String currentSSIDName = "";
    private static String currentSSIDMac = "";
    private static String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        setTitle("Playlist Music");

        initUI();
    }

    private void initUI() {

        lvPlaylist= (ListView) findViewById(R.id.lv_playlist);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        btnRefresh = (Button) findViewById(R.id.btn_refresh);
        LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        footerList = li.inflate(R.layout.footer_list, null);

        // Initial
        startIndex = 0;
        keyword = "";
        isLoading = false;
        masterList = new ArrayList<>();
        session = new SessionManager(PlaylistActivity.this);

        initEvent();
    }

    private void initEvent() {

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startIndex = 0;
                btnRefresh.setVisibility(View.GONE);
                getPlaylist(PlaylistActivity.this);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        isLoading = false;
        startIndex = 0;
        keyword = "";
        getPlaylist(PlaylistActivity.this);
    }

    private static void getPlaylist(final Context context) {

        btnRefresh.setVisibility(View.GONE);
        pbLoading.setVisibility(View.VISIBLE);
        masterList = new ArrayList<>();

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("keyword", keyword);
            jBody.put("start", String.valueOf(startIndex));
            jBody.put("count", String.valueOf(count));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getPlaylist, "", "", 0, session.getUid(), new ApiVolley.VolleyCallback() {
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
                            masterList.add(new CustomItem(jo.getString("id"), jo.getString("title"), jo.getString("artist"), jo.getString("link") ));
                        }
                    }

                    setPlaylistAdapter(context, masterList);
                    pbLoading.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    setPlaylistAdapter(context,null);
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(context, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
                    btnRefresh.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String result) {
                setPlaylistAdapter(context, null);
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(context, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
                btnRefresh.setVisibility(View.VISIBLE);
            }
        });
    }

    public static void validateWifiPlaylist(final Context context, final String idPlaylist){

        wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (wifi.isWifiEnabled() == false)
        {

            wifi.setWifiEnabled(true);
            Toast.makeText(context, "Mohon sambungkan wifi anda ke AP "+ context.getResources().getString(R.string.ssid_name), Toast.LENGTH_LONG).show();
            ((Activity) context).startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
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


            pbLoading.setVisibility(View.VISIBLE);

            JSONObject jBody = new JSONObject();
            try {
                jBody.put("ssid", currentSSIDName);
                jBody.put("mac", currentSSIDMac);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.validateWifi, "", "", 0, session.getUid(), new ApiVolley.VolleyCallback() {
                @Override
                public void onSuccess(String result) {

                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getJSONObject("metadata").getString("status");

                        if(iv.parseNullInteger(status) == 200){

                            JSONArray jsonArray = response.getJSONArray("response");
                            if(jsonArray.length() > 0){
                                for(int i = 0; i < jsonArray.length(); i++){

                                    addToRequest(context, idPlaylist);
                                    break;
                                }
                            }else{

                                pbLoading.setVisibility(View.GONE);
                                Snackbar.make(((AppCompatActivity)context).findViewById(android.R.id.content), "Mohon sambungkan wifi anda ke AP "+ context.getResources().getString(R.string.ssid_name),
                                        Snackbar.LENGTH_INDEFINITE).setAction("OK",
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                ((Activity)context).startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                                            }
                                        }).show();
                            }

                        }else{

                            wifi.setWifiEnabled(true);
                            Toast.makeText(context, "Mohon sambungkan wifi anda ke AP "+ context.getResources().getString(R.string.ssid_name), Toast.LENGTH_LONG).show();
                            ((Activity) context).startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                            pbLoading.setVisibility(View.GONE);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        pbLoading.setVisibility(View.GONE);
                        Toast.makeText(context, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onError(String result) {
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(context, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private static void addToRequest(final Context context, String idPlaylist){

        SessionManager sessionManager = new SessionManager(context);
        JSONObject jData = new JSONObject();
        final ProgressDialog progressDialog = new ProgressDialog(context, R.style.AppTheme_Custom_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Menyimpan...");
        progressDialog.show();

        try {
            jData.put("idsong", idPlaylist);
            jData.put("timestamp", iv.getCurrentDate(FormatItem.formatTimestamp));
            jData.put("played","0");
            jData.put("uid",sessionManager.getUid());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jBody = new JSONObject();

        try {
            jBody.put("data", jData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.saveQueue, "", "", 0, sessionManager.getUid(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                String message = "Terjadi kesalahan dalam mengakses data, harap ulangi";

                try {
                    JSONObject responseAPI = new JSONObject(result);
                    String status = responseAPI.getJSONObject("metadata").getString("status");

                    if(iv.parseNullDouble(status) == 200){

                        message = responseAPI.getJSONObject("response").getString("message");
                    }

                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();

                    startIndex = 0;
                    btnRefresh.setVisibility(View.GONE);
                    getPlaylist(context);

                } catch (JSONException e) {
                    progressDialog.dismiss();
                    Toast.makeText(context, "Terjadi kesalahan saat mengakses data, harap ulangi kembali", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {
                progressDialog.dismiss();
                Toast.makeText(context, "Terjadi kesalahan saat mengakses data, harap ulangi kembali", Toast.LENGTH_LONG).show();
            }
        });
    }

    private static void setPlaylistAdapter(final Context context, List<CustomItem> listItem){

        lvPlaylist.setAdapter(null);

        if(listItem != null && listItem.size() > 0){

            adapter = new ListPlaylistAdapter((Activity) context, listItem);
            lvPlaylist.setAdapter(adapter);

            lvPlaylist.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {

                    int threshold = 1;
                    int count = lvPlaylist.getCount();

                    if (i == SCROLL_STATE_IDLE) {
                        if (lvPlaylist.getLastVisiblePosition() >= count - threshold && !isLoading) {

                            isLoading = true;
                            lvPlaylist.addFooterView(footerList);
                            startIndex += count;
                            getMoreData(context);
                        }
                    }
                }

                @Override
                public void onScroll(AbsListView absListView, int i, int i1, int i2) {

                }
            });
        }
    }

    private static void getMoreData(Context context) {

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

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getPlaylist, "", "", 0, session.getUid(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            moreList.add(new CustomItem(jo.getString("id"), jo.getString("title"), jo.getString("artist"), jo.getString("link") ));
                        }

                        lvPlaylist.removeFooterView(footerList);
                        if(adapter!= null) adapter.addMoreData(moreList);
                    }
                    isLoading = false;
                } catch (JSONException e) {
                    e.printStackTrace();
                    isLoading = false;
                    lvPlaylist.removeFooterView(footerList);
                }
            }

            @Override
            public void onError(String result) {
                isLoading = false;
                lvPlaylist.removeFooterView(footerList);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.music_menu, menu);
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

                keyword = queryText;
                startIndex = 0;
                getPlaylist(PlaylistActivity.this);
                iv.hideSoftKey(PlaylistActivity.this);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                keyword = newText;
                startIndex = 0;
                getPlaylist(PlaylistActivity.this);

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
