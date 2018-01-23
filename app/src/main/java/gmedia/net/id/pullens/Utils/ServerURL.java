package gmedia.net.id.pullens.Utils;

import android.content.Context;

/**
 * Created by Shinmaul on 12/22/2017.
 */

public class ServerURL {

    private Context context;
    private String baseURLLocal = "";
    private ServerLocalManager localServer;

    private static final String baseURL = "http://pullens.gmedia.bz/";
    //private static final String baseURL = "http://192.168.12.147/pullens/";

    public static final String login = baseURL + "api_user/auth/login/";
    public static final String getHomeMenu = baseURL + "api_user/dashboard/get_home_menu/";
    public static final String getDetailRating = baseURL + "api_user/dashboard/get_detail_rating/";
    public static final String saveDetailRating = baseURL + "api_user/dashboard/save_detail_rating/";
    public static final String validateWifi = baseURL + "api_user/dashboard/validate_wifi/";
    public static final String getServer = baseURL + "api_user/dashboard/get_server/";
    public static final String getPlaylist = baseURL + "api_user/dashboard/get_playlist/";
    public static final String saveQueue = baseURL + "api_user/dashboard/save_queue/";

    public ServerURL(Context context){

        this.context = context;

        localServer = new ServerLocalManager(context);
        baseURLLocal = (localServer.getServer() != null) ? localServer.getServer():"";
        baseURLLocal = "http://" + localServer.getServer() + "/api/";
    }

    public String getMejaByBarcode(){ return baseURLLocal + "userorder/get_meja_by_barcode/";}
    public String updatePrinterStatus(){ return baseURLLocal + "userorder/update_printer_status/";}
    public String saveOrder(){ return baseURLLocal + "userorder/save/";}
    public String getNoBukti(){ return baseURLLocal + "userorder/generate_nobukti/";}
    public String getSugestionCatatan(){ return baseURLLocal + "userorder/get_sugestion_catatan/";}
    public String getKategori(){ return baseURLLocal + "userorder/get_kategori/";}
    public String getMenu(){ return baseURLLocal + "userorder/get_menu/";}
    public String getUpselling(){ return baseURLLocal + "userorder/get_upselling/";}
    public String saveOrderPerOne(){ return baseURLLocal + "userorder/save_by_order/";}
    public String updatePenjualan(){ return baseURLLocal + "userorder/update_penjualan/";}
    public String getCurrentOrder(){ return baseURLLocal + "userorder/get_current_order/";}
    public String getHotMenu(){ return baseURLLocal + "userorder/get_hot_menu/";}
    public String getPenjualanByUid(){ return baseURLLocal + "userorder/get_penjualan_by_uid/";}

    public String getRiwayatOrder(){ return baseURLLocal + "userriwayat/get_riwayat_transaksi/";}
    public String getDetailRiwayatOrder(){ return baseURLLocal + "userriwayat/get_detail_riwayat_transaksi/";}
    public String getPrinter(){ return baseURLLocal + "userprinter/get_printer/";}

}
