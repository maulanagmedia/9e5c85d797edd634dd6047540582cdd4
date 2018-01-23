package gmedia.net.id.pullens.FavoriteMenu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ImageUtils;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.pullens.Adapter.ListHomeMenuAdapter;
import gmedia.net.id.pullens.MainActivity;
import gmedia.net.id.pullens.R;
import gmedia.net.id.pullens.Utils.ServerLocalManager;
import gmedia.net.id.pullens.Utils.ServerURL;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class FavMakanan extends Fragment {

    private static Context context;
    private static boolean isDialogOpen = false;
    private static ListHomeMenuAdapter homeMenuAdapter;
    private static boolean isLoading = false;
    private static AlertDialog alertRating;
    private View layout;
    private static ListView lvMakanan;
    private static int startIndex = 0;
    private static int count = 10;
    public static String keyword = "";
    private static List<CustomItem> listMakanan;
    private static ProgressBar pbMakanan;
    private static Button btnRefresh;
    private static SessionManager session;
    private static ItemValidation iv = new ItemValidation();
    private static View footerList;
    private static ServerLocalManager serverManager;
    private static ServerURL serverURL;
    private static WifiManager wifi;
    private static String currentSSIDName = "";
    private static String currentSSIDMac = "";
    private static String TAG = "TAG";

    public FavMakanan() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_fav_makanan, container, false);
        context = getContext();
        initUI();
        return layout;
    }

    private void initUI() {

        lvMakanan = (ListView) layout.findViewById(R.id.lv_makanan);
        pbMakanan = (ProgressBar) layout.findViewById(R.id.pb_makanan);
        btnRefresh = (Button) layout.findViewById(R.id.btn_refresh);
        LayoutInflater li = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        footerList = li.inflate(R.layout.footer_list, null);
        listMakanan = new ArrayList<>();
        session = new SessionManager(context);
        serverManager = new ServerLocalManager(context);
        serverURL = new ServerURL(context);

        keyword = "";
        isDialogOpen = false;
        isLoading = false;

        getServer();

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startIndex = 0;
                btnRefresh.setVisibility(View.GONE);
                getHotMenu();
            }
        });
    }

    public static void getServer() {

        pbMakanan.setVisibility(View.VISIBLE);

        ApiVolley request = new ApiVolley(context, new JSONObject(), "GET", ServerURL.getServer, "", "", 0, session.getUid(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        String serverAddress = response.getJSONObject("response").getString("server");

                        if(serverAddress != null && serverAddress.length() > 0){
                            serverManager.saveServer(serverAddress);
                            serverURL = new ServerURL(context);

                            isLoading = false;
                            startIndex = 0;
                            getHotMenu();
                        }
                    }else{
                        pbMakanan.setVisibility(View.GONE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    pbMakanan.setVisibility(View.GONE);
                    Toast.makeText(context, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String result) {
                pbMakanan.setVisibility(View.GONE);
                Toast.makeText(context, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
            }
        });
    }

    private static void getHotMenu() {

        btnRefresh.setVisibility(View.GONE);
        pbMakanan.setVisibility(View.VISIBLE);
        listMakanan = new ArrayList<>();

        startIndex = 0;
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("jenis", "MAKANAN");
            jBody.put("keyword", keyword);
            jBody.put("start", String.valueOf(startIndex));
            jBody.put("count", String.valueOf(count));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getHomeMenu, "", "", 0, session.getUid(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    listMakanan = new ArrayList<>();

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray jsonArray = response.getJSONArray("response");
                        for(int i = 0; i < jsonArray.length(); i++){

                            JSONObject jo = jsonArray.getJSONObject(i);
                            listMakanan.add(new CustomItem(jo.getString("id"), jo.getString("nama"), jo.getString("harga"), jo.getString("gambar"), jo.getString("keterangan"),  jo.getString("timestamp"), jo.getString("total_rating")));
                        }
                    }

                    setHomeMenuAdapter(listMakanan);
                    pbMakanan.setVisibility(View.GONE);
                    btnRefresh.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    setHomeMenuAdapter(null);
                    pbMakanan.setVisibility(View.GONE);
                    Toast.makeText(context, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
                    btnRefresh.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String result) {
                setHomeMenuAdapter(null);
                pbMakanan.setVisibility(View.GONE);
                Toast.makeText(context, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
                btnRefresh.setVisibility(View.VISIBLE);
            }
        });
    }

    private static void setHomeMenuAdapter(List<CustomItem> listItem){

        isDialogOpen = false;
        lvMakanan.setAdapter(null);

        if(listItem != null && listItem.size() > 0){

            homeMenuAdapter = new ListHomeMenuAdapter((Activity) context, listItem);
            lvMakanan.setAdapter(homeMenuAdapter);

            lvMakanan.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {

                    int threshold = 1;
                    int count = lvMakanan.getCount();

                    if (i == SCROLL_STATE_IDLE) {
                        if (lvMakanan.getLastVisiblePosition() >= count - threshold && !isLoading) {

                            isLoading = true;
                            lvMakanan.addFooterView(footerList);
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

        lvMakanan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                CustomItem item = (CustomItem) adapterView.getItemAtPosition(i);

                if(!isDialogOpen){

                    wifi = (WifiManager) ((Activity)context).getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                    if (wifi.isWifiEnabled() == false)
                    {

                        wifi.setWifiEnabled(true);
                        Toast.makeText(context, "Mohon sambungkan wifi anda ke AP "+ context.getResources().getString(R.string.ssid_name), Toast.LENGTH_LONG).show();
                        ((Activity)context).startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                    }else{

                        try {
                            wifi = (WifiManager) ((Activity)context).getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                            WifiInfo wifiInfo = wifi.getConnectionInfo();
                            currentSSIDName = (wifiInfo.getSSID() != null) ? wifiInfo.getSSID().replace("\"","") : "";
                            currentSSIDMac = (wifiInfo.getBSSID() != null ) ? wifiInfo.getBSSID() : "";
                            Log.d(TAG, "refreshAPinfo: "+currentSSIDName);
                        }catch (Exception e){

                            currentSSIDName = "";
                            currentSSIDMac = "";
                        }

                        validateWifi(item.getItem1(), item.getItem2());
                    }
                    //getPenjualanByUid(item.getItem1(), item.getItem2());
                }
            }
        });
    }

    private static void validateWifi(final String id, final String nama){

        pbMakanan.setVisibility(View.VISIBLE);

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

                pbMakanan.setVisibility(View.GONE);
                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray jsonArray = response.getJSONArray("response");
                        if(jsonArray.length() > 0){
                            for(int i = 0; i < jsonArray.length(); i++){

                                getPenjualanByUid(id, nama);
                                break;
                            }
                        }else{


                            Snackbar.make(((Activity) context).findViewById(android.R.id.content), "Mohon sambungkan wifi anda ke AP "+ context.getResources().getString(R.string.ssid_name),
                                    Snackbar.LENGTH_INDEFINITE).setAction("OK",
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            ((Activity) context).startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                                        }
                                    }).show();
                        }

                    }else{

                        wifi.setWifiEnabled(true);
                        Toast.makeText(context, "Mohon sambungkan wifi anda ke AP "+ context.getResources().getString(R.string.ssid_name), Toast.LENGTH_LONG).show();
                        ((Activity) context).startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String result) {
                pbMakanan.setVisibility(View.GONE);
                Toast.makeText(context, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
            }
        });
    }

    private static void getMoreData() {

        isLoading = true;
        final List<CustomItem> moreList = new ArrayList<>();
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("jenis", "MAKANAN");
            jBody.put("keyword", keyword);
            jBody.put("start", String.valueOf(startIndex));
            jBody.put("count", String.valueOf(count));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getHomeMenu, "", "", 0, session.getUid(), new ApiVolley.VolleyCallback() {
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

                        lvMakanan.removeFooterView(footerList);
                        if(homeMenuAdapter!= null) homeMenuAdapter.addMoreData(moreList);
                    }
                    isLoading = false;
                } catch (JSONException e) {
                    e.printStackTrace();
                    isLoading = false;
                    lvMakanan.removeFooterView(footerList);
                }
            }

            @Override
            public void onError(String result) {
                isLoading = false;
                lvMakanan.removeFooterView(footerList);
            }
        });
    }

    private static void getPenjualanByUid(final String id, final String namaMenu) {

        isDialogOpen = true;
        pbMakanan.setVisibility(View.VISIBLE);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("uid", session.getUid());
            jBody.put("kdbrg", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", serverURL.getPenjualanByUid(), "", "", 0, session.getUid(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                pbMakanan.setVisibility(View.GONE);
                isDialogOpen = false;
                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray jsonArray = response.getJSONArray("response");
                        if(jsonArray.length()>0){

                            getDetailRatingMenu(id);
                        }else{
                            showDialog(context, 3, "Anda hanya dapat memberikan rating setelah memesan "+ namaMenu);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String result) {

                isDialogOpen = false;
                pbMakanan.setVisibility(View.GONE);
                Toast.makeText(context, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
            }
        });
    }

    private static void getDetailRatingMenu(final String id) {

        isDialogOpen = true;
        pbMakanan.setVisibility(View.VISIBLE);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("uid", session.getUid());
            jBody.put("kdbrg", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getDetailRating, "", "", 0, session.getUid(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                pbMakanan.setVisibility(View.GONE);
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
                            iu.LoadCircleRealImage(context, photo, ivProfile);
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
                                        Toast.makeText(context, "Mohon berikan rating anda", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(context, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String result) {
                isDialogOpen = false;
                pbMakanan.setVisibility(View.GONE);
                Toast.makeText(context, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
            }
        });
    }

    private static void saveDetailRating(final String id, JSONObject jData) {

        final ProgressDialog progressDialog = new ProgressDialog(context,
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

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.saveDetailRating, "", "", 0, session.getUid(), new ApiVolley.VolleyCallback() {
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
                    Toast.makeText(context, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String result) {
                progressDialog.dismiss();
                Toast.makeText(context, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
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
}
