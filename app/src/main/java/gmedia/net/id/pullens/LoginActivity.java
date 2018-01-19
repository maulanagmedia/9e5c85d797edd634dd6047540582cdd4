package gmedia.net.id.pullens;

import android.*;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.text.Line;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.RuntimePermissionsActivity;
import com.maulana.custommodul.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import gmedia.net.id.pullens.Utils.ServerURL;

public class LoginActivity extends RuntimePermissionsActivity implements GoogleApiClient.OnConnectionFailedListener{

    private static final int RC_SIGN_IN_GOOGLE = 9001;
    private FirebaseAuth auth;
    private CallbackManager mCallbackManager;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private final String TAG = "LoginA";
    public static GoogleApiClient googleApiClient;
    private FirebaseUser user;
    private String loginMethod;
    private ProgressDialog progressDialog;
    private ItemValidation iv = new ItemValidation();
    private boolean signUpFlag = false;
    private SessionManager session;
    private LoginButton btnFacebookOri;
    private LinearLayout llLoginFB;
    private LinearLayout llLoginGoogle;
    private String jenisLogin = "";

    private static boolean doubleBackToExitPressedOnce;
    private boolean exitState = false;
    private int timerClose = 2000;

    private static final int REQUEST_PERMISSIONS = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        AppEventsLogger.activateApp(this);

        //Check close statement
        doubleBackToExitPressedOnce = false;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            if (bundle.getBoolean("exit", false)) {
                exitState = true;
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        }

        // for android > M
        if (ContextCompat.checkSelfPermission(
                LoginActivity.this, android.Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                LoginActivity.this, android.Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {

            LoginActivity.super.requestAppPermissions(new
                            String[]{android.Manifest.permission.ACCESS_WIFI_STATE, android.Manifest.permission.CHANGE_WIFI_STATE}, R.string
                            .runtime_permissions_txt
                    , REQUEST_PERMISSIONS);
        }

        session = new SessionManager(LoginActivity.this);
        initGoogleAuth();
        initFirebaseAuth();
        initUI();
        initFacebookAuth();
        //checkLogin();
    }

    @Override
    public void onPermissionsGranted(int requestCode) {


    }

    //region Google Login
    private void initGoogleAuth() {

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this,this)
                .addOnConnectionFailedListener(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        googleApiClient.connect();
    }

    private void initFirebaseAuth() {

        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();

        // logout
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            if(bundle.getBoolean("logout",false)){
                try{
                    auth.signOut();
                    LoginManager.getInstance().logOut();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid() + " " + user.getEmail() + " " + user.getDisplayName());
                    if(session.isSaved()){
                        jenisLogin =  session.getUserInfo(SessionManager.TAG_JENIS);
                    }

                    postSuccessLogin();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    private void initUI() {

        btnFacebookOri = (LoginButton) findViewById(R.id.btn_fb_ori);
        llLoginFB = (LinearLayout) findViewById(R.id.ll_facebook);
        llLoginGoogle = (LinearLayout) findViewById(R.id.ll_google);

        llLoginFB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                LoginManager.getInstance().logOut();
                btnFacebookOri.performClick();
                signUpFlag = true;
            }
        });

        llLoginGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                signIn();
                signUpFlag = true;
            }
        });
    }

    private void signIn() {
        googleApiClient.clearDefaultAccountAndReconnect();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN_GOOGLE);
    }

    //region Facebook Authorization
    private void initFacebookAuth() {

        mCallbackManager = CallbackManager.Factory.create();
        btnFacebookOri.setReadPermissions("email", "public_profile");
        btnFacebookOri.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                jenisLogin = "FACEBOOK";
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "onError: "+error.toString());
            }
        });

    }

    private void handleFacebookAccessToken(AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            dismissProgressDialog();
                            Toast.makeText(LoginActivity.this, "Authentication failed. Your account already linked by Google login",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }
    //endregion

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Google login
        if(requestCode == RC_SIGN_IN_GOOGLE){

            jenisLogin = "GOOGLE";
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()){

                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                showProgressDialog();
            }else{

                Log.d(TAG, "onAuthStateChanged:signed_out");
                // User not Authenticated
            }
        }else{

            // FB Login
            if(resultCode == -1){

                jenisLogin = "FACEBOOK";
                mCallbackManager.onActivityResult(requestCode, resultCode, data);
                showProgressDialog();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            dismissProgressDialog();
                            Toast.makeText(LoginActivity.this, "Authentication failed. Your account already linked by facebook login",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void checkLogin(){

        user = auth.getCurrentUser();
        if (user != null) {
            // User is signed in
            Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid() + " " + user.getEmail() + " " + user.getDisplayName());

            if(session.isSaved()){
                jenisLogin =  session.getUserInfo(SessionManager.TAG_JENIS);
            }

            postSuccessLogin();
        } else {
            // User is signed out
            Log.d(TAG, "onAuthStateChanged:signed_out");
            /*try {
                Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                Log.d(TAG, "Sign out from google");
                            }
                        });
            }catch (Exception e){

                e.printStackTrace();
            }*/
        }
    }

    private void postSuccessLogin(){

        if(user != null){

            String uid = "";
            try {
                uid = user.getUid();

            }catch (Exception e){
                e.printStackTrace();
                uid = "";
            }

            if(!uid.equals("")){
                JSONObject jData = new JSONObject();

                try {
                    jData.put("uid",user.getUid());
                    jData.put("nama",user.getDisplayName());
                    jData.put("email",user.getEmail());
                    jData.put("photo",String.valueOf(user.getPhotoUrl()));
                    jData.put("jenis_login",jenisLogin);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONObject jBody = new JSONObject();

                try {
                    jBody.put("data", jData);
                    jBody.put("uid", user.getUid());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ApiVolley request = new ApiVolley(LoginActivity.this, jBody, "POST", ServerURL.login, "", "", 0, user.getUid(), new ApiVolley.VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {

                        try {
                            JSONObject responseAPI = new JSONObject(result);

                            String status = responseAPI.getJSONObject("metadata").getString("status");

                            if(iv.parseNullDouble(status) == 200){

                                String message = responseAPI.getJSONObject("metadata").getString("message");
                                session.createLoginSession(user.getUid(),user.getEmail(),user.getDisplayName(),user.getEmail(), String.valueOf(user.getPhotoUrl()), jenisLogin, "1");
                                Toast.makeText(LoginActivity.this, message,Toast.LENGTH_LONG).show();
                                dismissProgressDialog();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                finish();
                                startActivity(intent);
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            }

                            dismissProgressDialog();
                        } catch (JSONException e) {
                            dismissProgressDialog();
                            Toast.makeText(LoginActivity.this, "Terjadi kesalahan saat mengakses data, harap ulangi kembali", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(String result) {
                        dismissProgressDialog();
                        Toast.makeText(LoginActivity.this, "Terjadi kesalahan saat mengakses data, harap ulangi kembali", Toast.LENGTH_LONG).show();
                    }
                });
            }else{
                Toast.makeText(LoginActivity.this, "Harap login kembali", Toast.LENGTH_LONG).show();
                dismissProgressDialog();
            }
        }else{
            Toast.makeText(LoginActivity.this, "Harap login kembali", Toast.LENGTH_LONG).show();
            dismissProgressDialog();
        }
    }

    private void showProgressDialog(){
        progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Custom_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();
    }

    private void dismissProgressDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        dismissProgressDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            auth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        // Origin backstage
        if (doubleBackToExitPressedOnce) {
            Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
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
}
