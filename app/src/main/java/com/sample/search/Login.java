package com.sample.search;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Login extends AppCompatActivity {

    private static final int RC_SIGN_IN=1;
    private EditText recoverpasswordEditText;
    private AlertDialog recoverPasswordDialog;
    private Context context;
    private ProgressDialog progressDialog;
    private ProgressDialog recoverPasswordprogressDialog;
    private EditText mail;
    private GoogleApiClient mGoogleSignInClient;
    private  EditText password;
    private Button login;
    private FirebaseAuth mAuth;
    private SignInButton googlogin;
    private TextView signup;
    private TextView recoverPassword;
    private LoginButton loginButton;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private CallbackManager callbackManager;
    private LoginButton facebookLogin;
    private Menu menu;
    private MenuItem menuItem;
    private NavigationView navigationView;
    private static String username;
    private static String secretPassword;
    private SaveToFireBase saveToFireBase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        saveToFireBase=new SaveToFireBase();
        mail=(EditText)findViewById(R.id.mailogin);
        googlogin=(SignInButton) findViewById(R.id.sign_in_button);
        password=(EditText)findViewById(R.id.passwordlogin);
        login=(Button) findViewById(R.id.loginbt);
        signup=(TextView) findViewById(R.id.signupbt);
        recoverPassword=(TextView)findViewById(R.id.forgotpasswordbt);
        progressDialog=new ProgressDialog(this);
        recoverPasswordprogressDialog=new ProgressDialog(this);
        recoverPasswordprogressDialog.setIndeterminate(true);
        recoverPasswordprogressDialog.setMessage("Submitting your email...");
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Logging in...");
        signup.setPaintFlags(signup.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        recoverPassword.setPaintFlags(recoverPassword.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        callbackManager = CallbackManager.Factory.create();
        FacebookSdk.setApplicationId(getResources().getString(R.string.facebook_app_id));
        loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));


        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "ng.com.propertypro.user",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());

            }

            @Override
            public void onCancel() {
                //Toast.makeText(LoginActivity.this,"Facebook login cancelled",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(LoginActivity.this,exception.getLocalizedMessage(),Toast.LENGTH_LONG).show();
            }
        });


        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient=new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(getApplicationContext(),"Error signing in",Toast.LENGTH_SHORT).show();

                    }
                }).addApi(Auth.GOOGLE_SIGN_IN_API,gso).build();

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("loginAutofill", Context.MODE_PRIVATE);
        String str = sharedPreferences.getString("maillogin","");
        String pass = sharedPreferences.getString("passlogin","");
        if(str.equals("")){

        }
        else{
            mail.setText(str);
            password.setText(pass);
        }


        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(LoginActivity.this, SignInActivity.class);
                startActivity(intent);

            }
        });

        googlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        recoverPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LayoutInflater inflater = LayoutInflater.from(LoginActivity.this);
                View dialogLayout = inflater.inflate(R.layout.recoverpassword, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("Reenter your email to recover your password");
                builder.setView(dialogLayout);
                recoverpasswordEditText=(EditText)dialogLayout.findViewById(R.id.recoverpasswordedit);
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("loginAutofill", Context.MODE_PRIVATE);
                String str = sharedPreferences.getString("maillogin","");
                if(str.equals("")){
                }
                else{
                    recoverpasswordEditText.setText(str);
                }

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String mail=recoverpasswordEditText.getText().toString();
                        if(TextUtils.isEmpty(mail)){

                            recoverpasswordEditText.setError("PLease enter your email");
                            return;
                        }

                        if(!Patterns.EMAIL_ADDRESS.matcher(mail).matches()){
                            recoverpasswordEditText.setError("The email you provided is not valid");
                            return;
                        }
                        recoverPasswordprogressDialog.show();

                        mAuth.sendPasswordResetEmail(mail).addOnCompleteListener(new OnCompleteListener<Void>() {

                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                recoverPasswordprogressDialog.dismiss();

                                if (task.isSuccessful()) {

                                    Toast.makeText(LoginActivity.this, "Log in to your email. We have sent you instructions on how to reset your password!", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Failed to send reset password!", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

                recoverPasswordDialog = builder.create();
                recoverPasswordDialog.show();
            }
        });


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                final String email=mail.getText().toString();
                final String pass=password.getText().toString();

                if(email.trim().isEmpty()){
                    mail.setError("Provide your email address");
                    mail.requestFocus();
                    return;
                }
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    mail.setError("Your email address is not valid");
                    mail.requestFocus();
                    return;
                }

                if(pass.trim().isEmpty()){
                    password.setError("Provide a password");
                    password.requestFocus();
                    return;
                }

                username = email;
                secretPassword = pass;
                progressDialog.show();
                LoginToServer loginToServer = new LoginToServer();
                loginToServer.execute();

            }
        });

    }

    private void signIn() {
        progressDialog.show();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()){
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseWithGoogle(account);
            }

        }


    }

    private  void firebaseWithGoogle(final GoogleSignInAccount account){
        progressDialog.show();
        final AuthCredential credential= GoogleAuthProvider.getCredential(account.getIdToken(),null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();
                if(!task.isSuccessful()){

                    Toast.makeText(getApplicationContext(),"Authentication failed",Toast.LENGTH_SHORT).show();

                }
                else{

                    SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("loginstate", Context.MODE_PRIVATE);
                    String str = sharedPreferences.getString("login","");
                    if(str.equals("")){
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("login", "Login");
                        editor.commit();
                    }
                    else if(str.equals("Logout")){
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("login", "Login");
                        editor.commit();
                    }

                    Toast.makeText(getApplicationContext(),"Signed in as "+account.getEmail(),Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }

            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.signin, menu);
        this.menu=menu;
        menuItem=menu.findItem(R.id.signoutmenu);

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("loginstate", Context.MODE_PRIVATE);
        String str = sharedPreferences.getString("AgentLogin","");
        if(str.equals("true")){
            menuItem.setTitle("Logout");
        }
        else {

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null) {
                menuItem.setTitle("Logout");

            } else {
                menuItem.setTitle("Login");
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.signoutmenu) {

            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("loginstate", Context.MODE_PRIVATE);
            String str = sharedPreferences.getString("AgentLogin","");

            if(str.equals("true")){

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("AgentLogin", "false");
                editor.commit();
                SharedPreferences.Editor editor1 = sharedPreferences.edit();
                editor1.putString("AgentId", "");
                editor1.commit();

                SharedPreferences sharedPreferences1 = getApplicationContext().getSharedPreferences("loginAutofill", Context.MODE_PRIVATE);


                SharedPreferences.Editor editor2 = sharedPreferences1.edit();
                editor2.putString("maillogin", "");
                editor2.commit();
                SharedPreferences.Editor editor3 = sharedPreferences1.edit();
                editor3.putString("passlogin", "");
                editor3.commit();
                item.setTitle("login");
            }
            else {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    googleSignOut();
                } else {
                    Toast.makeText(getApplicationContext(), "Sign in first as a user", Toast.LENGTH_SHORT).show();
                }
            }
            return  true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void googleSignOut(){
        progressDialog.show();
        mAuth.signOut();

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("loginstate", Context.MODE_PRIVATE);
        String str = sharedPreferences.getString("login","");
        String isAgent=sharedPreferences.getString("AgentLogin","");

        if(isAgent.equals("true")){

            Toast.makeText(getApplicationContext(),"You are logged in as an agent",Toast.LENGTH_SHORT).show();
            return;
        }

        if(str.equals("")){

            Toast.makeText(getApplicationContext(),"Log in first",Toast.LENGTH_SHORT).show();
        }
        else if(str.equals("Login")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("login", "Logout");
            editor.commit();


            Toast.makeText(getApplicationContext(), "Successfully signed out", Toast.LENGTH_SHORT).show();
        }

        else if(str.equals("Logout")){
            Toast.makeText(getApplicationContext(),"Already logged out",Toast.LENGTH_SHORT).show();

        }

        Auth.GoogleSignInApi.signOut(mGoogleSignInClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        //  Intent intent = new Intent(MainActivity.this,MainActivity.class);
                        //  startActivity(intent);
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Successfully signed out", Toast.LENGTH_SHORT).show();

                    }

                });

    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("loginAutofill", Context.MODE_PRIVATE);
        String str = sharedPreferences.getString("maillogin","");
        String pass = sharedPreferences.getString("passlogin","");
        if(str.equals("")){

        }
        else{
            mail.setText(str);
            password.setText(pass);

        }

    }

    private void handleFacebookAccessToken(AccessToken token) {

        final AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                            startActivity(intent);
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this,"Successfully Logged in with facebook"+"  as "+user.getDisplayName(),Toast.LENGTH_LONG).show();

                        } else {
                            // If sign in fails, display a message to the user.

                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private class LoginToServer extends AsyncTask<Void, Integer, AppStatusCode> {

        String apiUrl = UrlUtil.BASE_API_PROPERTY_PRO +"m_login";

        @Override
        protected AppStatusCode doInBackground(Void... voids) {

            if(HttpServiceProvider.isNetworkConnected(getApplicationContext())){

                try {
                    String response =makeBasicAuthCall(apiUrl);
                    JSONObject jsonObject=new JSONObject(response);
                    String idString=(jsonObject.get("id")).toString();

                    SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("loginstate", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if(idString.equals("false")){
                        editor.putString("AgentLogin", "false");
                        editor.commit();
                        editor.putString("AgentId", "");
                        editor.commit();

                    }
                    else {

                        editor.putString("AgentLogin", "true");
                        editor.commit();
                        editor.putString("AgentId", idString);
                        editor.commit();

                        String str = sharedPreferences.getString("login", "");
                        if (str.equals("")) {
                            SharedPreferences.Editor editor1 = sharedPreferences.edit();
                            editor1.putString("login", "Login");
                            editor1.commit();
                        } else if (str.equals("Logout")) {
                            SharedPreferences.Editor editor2 = sharedPreferences.edit();
                            editor2.putString("login", "Login");
                            editor2.commit();
                        }

                        SharedPreferences sharedPreferences1 = getApplicationContext().getSharedPreferences("loginAutofill", Context.MODE_PRIVATE);

                        SharedPreferences.Editor editor2 = sharedPreferences1.edit();
                        editor2.putString("maillogin", username);
                        editor2.commit();

                        SharedPreferences.Editor editor3 = sharedPreferences1.edit();
                        editor3.putString("passlogin", secretPassword);
                        editor3.commit();

                    }

                } catch (IOException e) {
                    return AppStatusCode.INTERNET_CONNECTION_ERROR;
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return AppStatusCode.NETWORK_OK;

            }else{
                return AppStatusCode.NETWORK_ACCESS_ERROR;
            }
        }

        @Override
        public void onProgressUpdate(Integer... integer){
        }

        @Override
        protected void onPostExecute(AppStatusCode appStatusCode) {
            super.onPostExecute(appStatusCode);
            if(AppStatusCode.NETWORK_ACCESS_ERROR == appStatusCode){

                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("loginstate", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("AgentLogin", "false");
                editor.commit();

                Toast.makeText(getApplicationContext(),"No network Available",Toast.LENGTH_LONG).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);

            } else if(AppStatusCode.INTERNET_CONNECTION_ERROR == appStatusCode){
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("loginstate", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("AgentLogin", "false");
                editor.commit();
                Toast.makeText(getApplicationContext(),"No internet Connection",Toast.LENGTH_LONG).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
            else{

                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("loginstate", Context.MODE_PRIVATE);
                String isAgent=sharedPreferences.getString("AgentLogin","");
                if(isAgent.equals("false")) {

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) {
                        mAuth.signInWithEmailAndPassword(username, secretPassword)
                                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        progressDialog.dismiss();
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information

                                            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("loginstate", Context.MODE_PRIVATE);
                                            String str = sharedPreferences.getString("login", "");

                                            if (str.equals("")) {
                                                SharedPreferences.Editor editor1 = sharedPreferences.edit();
                                                editor1.putString("login", "Login");
                                                editor1.commit();
                                            } else if (str.equals("Logout")) {
                                                SharedPreferences.Editor editor2 = sharedPreferences.edit();
                                                editor2.putString("login", "Login");
                                                editor2.commit();
                                            }

                                            SharedPreferences.Editor editor3 = sharedPreferences.edit();
                                            editor3.putString("maillogin", username);
                                            editor3.commit();

                                            SharedPreferences.Editor editor4 = sharedPreferences.edit();
                                            editor4.putString("passlogin", secretPassword);
                                            editor4.commit();

                                            FirebaseUser user = mAuth.getCurrentUser();
                                            Toast.makeText(getApplicationContext(), "Signed in successfully as a user", Toast.LENGTH_LONG).show();
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent);
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    }
                }
                else{

                    SharedPreferences sharedPreference1 = getApplicationContext().getSharedPreferences("AgentIdSavedToFirebase", Context.MODE_PRIVATE);

                    String agentId = sharedPreferences.getString("AgentId", "");
                    saveToFireBase.saveAgentToFirebase(Long.parseLong(agentId), getApplicationContext(), "login");

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }

                progressDialog.dismiss();
            }
        }
    }


    public static String makeBasicAuthCall(String reqUrl) throws IOException {
        InputStream inputStream = null;
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestProperty ("Authorization",getBasicAuth());
            int responseCode = connection.getResponseCode();
            inputStream = new BufferedInputStream(connection.getInputStream());
            String streamToString = convertStreamToString(inputStream);
            System.out.println("StreamToString "+streamToString);
            if (!url.getHost().equals(connection.getURL().getHost())) {
                throw new IOException();
            }else {
                return streamToString;
            }
        }finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append('\n');
        }
        return sb.toString();
    }

    public static String getBasicAuth(){
        String userPassword = username + ":" + secretPassword;
        String encoding = Base64.encodeToString(userPassword.getBytes(), Base64.NO_WRAP);
        String basicAuth = "Basic " + encoding;
        return basicAuth;
    }


}



}
