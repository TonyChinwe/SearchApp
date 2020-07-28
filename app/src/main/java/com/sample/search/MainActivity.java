package com.sample.search;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private EditText edtSearch;
    private Spinner spnState;
    private Spinner spnType;
    private Spinner spnMinPrice;
    private Spinner spnMaxPrice;
    private Spinner spnBeds;
    private Spinner spnBaths;
    private CardView stateAndLocalityCardView;
    private ToggleButton tBtnSale;
    private ToggleButton tBtnRent;
    private ToggleButton tBtnShortlet;
    private boolean showStateAndLocatily;
    private GoogleApiClient mGoogleSignInClient;
    private TextView btnBrowse;
    private  NavigationView navigationView;
    private FirebaseAuth mAuth;
    private HashMap<String, HashMap<String, List<String>>> siteMap = new HashMap<>();
    private ScrollView nsvScroll;
    private Spinner spnPropertyMode;
    private Spinner spnStateMode;
    private TextView txtCaption;
    private LinearLayout stateAndLocalityLinearLayout;
    private static PricingActivity pricingActivity;
    private static PaymentActivity paymentActivity;
    private SharedPreferences appPrefs;
    private LinearLayout lltSimilarProperties;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pricingActivity=new PricingActivity();
        paymentActivity=new PaymentActivity();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        SharedPreferences sharedPreferen=getApplicationContext().getSharedPreferences("idtoken", Context.MODE_PRIVATE);
        final String tokenId=sharedPreferen.getString("token","");
        System.out.println("Token is "+tokenId);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        nsvScroll = findViewById(R.id.nsvScroll);
        appPrefs = getSharedPreferences("ng.com.propertypro.user", MODE_PRIVATE);
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

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user !=null) {
            navigationView.getMenu().findItem(R.id.nav_login).setTitle("Logout");
        }
        else{
            navigationView.getMenu().findItem(R.id.nav_login).setTitle("Login");
        }
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("id", Context.MODE_PRIVATE);
        String str = sharedPreferences.getString("uid","");
        if(str.equals("")){
            String uniqueID= UUID.randomUUID().toString();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("uid", uniqueID);
            editor.commit();
        }
        edtSearch = findViewById(R.id.edtSearch);
        tBtnSale = findViewById(R.id.tBtnSale);
        tBtnRent = findViewById(R.id.tBtnRent);
        tBtnShortlet = findViewById(R.id.tBtnShortlet);
        spnState = findViewById(R.id.spnState);
        spnType = findViewById(R.id.spnType);
        spnBeds = findViewById(R.id.spnBeds);
        spnBaths = findViewById(R.id.spnBaths);
        spnMinPrice = findViewById(R.id.spnMinPrice);
        spnMaxPrice = findViewById(R.id.spnMaxPrice);
        stateAndLocalityCardView = findViewById(R.id.stateAndLocalityCardView);
        btnBrowse = findViewById(R.id.btnBrowse);
        txtCaption = findViewById(R.id.txtCaption);
        stateAndLocalityLinearLayout = findViewById(R.id.stateAndLocalityLinearLayout);
        spnPropertyMode = findViewById(R.id.spnPropertyMode);
        spnStateMode = findViewById(R.id.spnStateMode);
        lltSimilarProperties = findViewById(R.id.lltSimilarProperties);

        populateBedView();
        populateBathView();
        populateMaxPriceView();
        populateMinPriceView();
        populateStateView();
        populatePropertyTypeView();
        populateOfferView();
        addListener();

        SharedPreferences sharedPreference4 = getApplicationContext().getSharedPreferences("notification_message1", Context.MODE_PRIVATE);
        final String message4=sharedPreference4.getString("message1","");
        System.out.println("True display"+message4);
        SharedPreferences sharedPreference = getApplicationContext().getSharedPreferences("notification_message", Context.MODE_PRIVATE);
        final String message1=sharedPreference.getString("message","");
        System.out.println("Going to the display "+message1);
        if(message1 !=null&&!message1.equals("")){
            System.out.println("Going to the display");
            Intent intent = new Intent(MainActivity.this, MessageDisplayActivity.class);
            intent.putExtra("message",message1);
            startActivity(intent);
        }
    }

    private void populateSimilarProperties(Property[] properties){
        LayoutInflater layoutInflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for(final Property property : properties) {
            View view = layoutInflater.inflate(R.layout.result_card, null);
            similarProperties(view, property);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this,
                            PropertyActivity.class);
                    intent.putExtra("selectedProperty", property);
                    startActivity(intent);
                }
            });
            lltSimilarProperties.addView(view);
        }
    }

    private void similarProperties(View view, Property property){
        TextView txtCaption = view.findViewById(R.id.txtCaption);
        TextView txtTitle = view.findViewById(R.id.txtTitle);
        TextView txtPropertySummary = view.findViewById(R.id.txtPropertySummary);
        TextView txtPrice = view.findViewById(R.id.txtPrice);
        TextView txtBeds = view.findViewById(R.id.txtBeds);
        TextView txtBaths = view.findViewById(R.id.txtBaths);
        TextView txtToilets = view.findViewById(R.id.txtToilets);
        TextView txtPostedOn = view.findViewById(R.id.txtPostedOn);
        TextView txtChangedOn = view.findViewById(R.id.txtChangedOn);
        TextView txtPid = view.findViewById(R.id.txtPid);
        TextView txtMode = view.findViewById(R.id.txtMode);
        TextView txtAddress = view.findViewById(R.id.txtAddress);
        ImageView imgPropertyImage = view.findViewById(R.id.imgPropertyImage);
        ImageHelper.loadImageFromUrl(UrlUtil.view_image_api + "/large/"+property.getImage(), getBaseContext(), imgPropertyImage);
        txtCaption.setText(property.getCaption());
        txtCaption.setText(property.getCaption());
        txtTitle.setText(property.getTitle() + " for "+property.getMode());
        txtPropertySummary.setText(property.getSummary()+" ...");

        if(Currency.DOLLAR.equals(property.getPrice().getCurrency())) {
            txtPrice.setText("$ "+ Utility.format(property.getPrice().getPrice()));
        } else {
            txtPrice.setText("₦ "+Utility.format(property.getPrice().getPrice()));
        }
        txtPid.setText("PID: "+property.getPid());
        txtPid.setAllCaps(true);

        txtMode.setText("For "+property.getMode());
        txtMode.setAllCaps(true);

        if(property.getBeds() != null && property.getBeds() > 0) {
            txtBeds.setText((property.getBeds() > 1) ? property.getBeds()+" Beds": property.getBeds()+" Bed");
            txtBeds.setVisibility(View.VISIBLE);
        }else{
            txtBeds.setText("0 Bed");
        }

        if(property.getBaths() != null && property.getBaths() > 0) {
            txtBaths.setText((property.getBaths() > 1) ? property.getBaths()+" Baths": property.getBeds()+" Bath");
            txtBaths.setVisibility(View.VISIBLE);
        }else{
            txtBaths.setText("0 Bath");
        }

        if(property.getToilets() != null && property.getToilets() > 0) {
            txtToilets.setText((property.getToilets() > 1) ? property.getToilets()+" Toilets": property.getToilets()+" Toilet");
            txtToilets.setVisibility(View.VISIBLE);
        }else{
            txtToilets.setText("0 Toilet");
        }

        Date postedOn = new Date(property.getCreatedOn());
        Date today = new Date();

        if(postedOn.after(today)) {
            txtPostedOn.setText("Added Today");
        } else {
            txtPostedOn.setText("Added " +Utility.formatDate(postedOn, "dd MMM yyyy"));
        }

        if(property.getUpdatedOn()!= null) {
            txtChangedOn.setVisibility(View.VISIBLE);
            Date changedOn = new Date(property.getUpdatedOn());
            if(changedOn.after(today)) {
                txtChangedOn.setText("Updated Today");
            } else {
                txtChangedOn.setText("Updated " + Utility.formatDate(changedOn, "dd MMM yyyy"));
            }
        }else{
            txtChangedOn.setVisibility(View.GONE);
        }

        txtAddress.setText(property.getLocation());
    }

    private void populatePropertyTypeView(){
        List<Form> propertyTypeList = PropertyTypeUtility.getInstance().init(getBaseContext()).getPropertyTypeList();
        Form form = new Form();
        form.setOption("All");
        form.setValue("");
        propertyTypeList.add(0, form);
        ArrayAdapter<Form> adapter = new ArrayAdapter<>(this,
                R.layout.support_simple_spinner_dropdown_item, propertyTypeList);
        spnType.setAdapter(adapter);
    }

    private void populateStateView(){
        List<Form> stateList  = LocalityUtility.getInstance().init(getBaseContext()).getStateList();
        stateList = Utility.reOrderStateList(stateList);
        ArrayAdapter<Form> formArrayAdapter = new ArrayAdapter<>(this,
                R.layout.support_simple_spinner_dropdown_item, stateList);
        spnState.setAdapter(formArrayAdapter);
    }

    private void populateMaxPriceView(){
        PriceMinAndMax priceMinAndMax = PriceMinAndMax.getInstance();
        List<Form> priceList = priceMinAndMax.getMaxPriceList();
        ArrayAdapter<Form> adapter = new ArrayAdapter<>(this,
                R.layout.support_simple_spinner_dropdown_item, priceList);
        spnMaxPrice.setAdapter(adapter);
    }

    private void populateMinPriceView(){
        PriceMinAndMax priceMinAndMax = PriceMinAndMax.getInstance();
        List<Form> priceList = priceMinAndMax.getMinPriceList();
        ArrayAdapter<Form> adapter = new ArrayAdapter<>(this,
                R.layout.support_simple_spinner_dropdown_item, priceList);
        spnMinPrice.setAdapter(adapter);
    }

    private void populateBedView(){
        List<String> bedList = new ArrayList<>();
        for(int x = 1; x <= 10; ++x){
            bedList.add(""+x);
        }
        bedList.add(0, "None");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.support_simple_spinner_dropdown_item, bedList);
        spnBeds.setAdapter(adapter);
    }

    private void populateBathView(){
        List<String> bathList = new ArrayList<>();
        for(int x = 1; x <= 10; ++x){
            bathList.add(""+x);
        }
        bathList.add(0, "None");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.support_simple_spinner_dropdown_item, bathList);
        spnBaths.setAdapter(adapter);
    }

    public void browseByStateAndLocality(View view){
        if(!showStateAndLocatily) {
            showStateAndLocatily = true;
            stateAndLocalityCardView.setVisibility(View.VISIBLE);
            btnBrowse.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.arrow_dropup, 0);

            nsvScroll.post(new Runnable() {
                public void run() {
                    nsvScroll.scrollTo(0, nsvScroll.getScrollY() + stateAndLocalityCardView.getWidth());
                }
            });

        }else{
            showStateAndLocatily = false;
            stateAndLocalityCardView.setVisibility(View.GONE);
            btnBrowse.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.arrow_dropdown, 0);
            /*
            nsvScroll.post(new Runnable() {
                public void run() {
                    nsvScroll.scrollTo(0, nsvScroll.getTop());
                }
            });
            */
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void search(View view){
        String search = edtSearch.getText().toString().trim();
        String propertyMode = getSelectedPropertyMode();
        String state = ((Form)spnState.getSelectedItem()).getOption();
        String type = ((Form)spnType.getSelectedItem()).getOption();
        String minPrice = ((Form)spnMinPrice.getSelectedItem()).getValue();
        String maxPrice = ((Form)spnMaxPrice.getSelectedItem()).getValue();
        String beds = (String)spnBeds.getSelectedItem();
        String baths = (String)spnBaths.getSelectedItem();
        SearchParam searchParams = new SearchParam();
        searchParams.setSearch(search);
        searchParams.setPropertyMode(propertyMode);
        searchParams.setState(state);
        searchParams.setType(type);
        searchParams.setMinPrice(minPrice);
        searchParams.setBaths(baths);
        searchParams.setBeds(beds);
        searchParams.setMaxPrice(maxPrice);
        Intent intent = new Intent(this, ng.com.propertypro.user.activity.ResultActivity.class);
        intent.putExtra("searchParam", searchParams);
        startActivity(intent);
    }

    public void initSelectedPropertyMode(View view){

        int viewId = view.getId();
        if(viewId == R.id.tBtnSale){
            tBtnRent.setChecked(false);
            tBtnShortlet.setChecked(false);
        }

        if(viewId == R.id.tBtnRent){
            tBtnSale.setChecked(false);
            tBtnShortlet.setChecked(false);
        }

        if(viewId == R.id.tBtnShortlet){
            tBtnSale.setChecked(false);
            tBtnRent.setChecked(false);
        }
    }

    public String getSelectedPropertyMode(){
        String propertyMode = null;
        if(tBtnSale.isChecked()){
            propertyMode = "sale";
        }
        if(tBtnRent.isChecked()){
            propertyMode = "rent";
        }
        if(tBtnShortlet.isChecked()){
            propertyMode = "shortlet";
        }
        return propertyMode;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {

        } else if (id == R.id.nav_site_map) {
            Intent intent = new Intent(MainActivity.this, SiteMapActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_favourites) {

            Intent intent = new Intent(MainActivity.this, FavouriteActivity.class);
            startActivity(intent);
        }

        else if (id == R.id.nav_alert_setting) {
            Intent intent = new Intent(MainActivity.this, AlertListActivity.class);
            startActivity(intent);
        }
        else if(id==R.id.nav_post_request){
            Intent intent = new Intent(MainActivity.this, PostRequestListActivity.class);
            startActivity(intent);
        }

        else if(id==R.id.nav_post_property){
            Intent intent = new Intent(MainActivity.this, PostAPropertyActivity.class);
            AgentProperty property = new AgentProperty();
            intent.putExtra("agentProperty", property);
            startActivity(intent);
        }

        else if(id==R.id.my_messages){
            Intent intent = new Intent(MainActivity.this, MessageDisplayActivity.class);
            startActivity(intent);


        }

        else if(id==R.id.pricing){
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("loginstate", Context.MODE_PRIVATE);
            String str = sharedPreferences.getString("AgentLogin", "");
            System.out.println("MY ID"+sharedPreferences.getString("AgentId",""));
            if(str.equals("true")){
                Intent intent = new Intent(MainActivity.this, SubscriptionManager.class);
                startActivity(intent);

            }
            else{
                Toast.makeText(getApplicationContext(),"Sign in as an agent first",Toast.LENGTH_LONG).show();
            }
        }

        else if(id==R.id.nav_my_listing){
            Intent intent = new Intent(MainActivity.this, MyListingActivity.class);
            startActivity(intent);
        }

        else if(id==R.id.nav_lead){
            Intent intent = new Intent(MainActivity.this, LeadActivity.class);
            startActivity(intent);
        }

        else if(id==R.id.nav_privacy_policy){
            Intent intent = new Intent(MainActivity.this, PrivacyPolicyActivity.class);
            startActivity(intent);
        }

        else if(id==R.id.nav_login) {

            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("loginstate", Context.MODE_PRIVATE);
            String str = sharedPreferences.getString("AgentLogin", "");
            SharedPreferences sharedPreferences2 = getApplicationContext().getSharedPreferences("loginAutofill", Context.MODE_PRIVATE);
            if (str.equals("") || str.equals("false")) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    for (UserInfo user1 : FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
                        if (user1.getProviderId().equals("facebook.com")) {
                            LoginManager.getInstance().logOut();
                        }
                    }
                    if (user != null) {
                        googleSignOut();
                        navigationView.getMenu().findItem(R.id.nav_login).setTitle("Login");
                    }
                } else {
                    navigationView.getMenu().findItem(R.id.nav_login).setTitle("Logout");
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
            }
            else {

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("AgentLogin", "false");
                editor.commit();
                SharedPreferences.Editor editor1 = sharedPreferences.edit();
                editor1.putString("AgentId", "");
                editor1.commit();
                SharedPreferences.Editor editor4 = sharedPreferences.edit();
                editor4.putString("login", "Logout");
                editor4.commit();
                SharedPreferences.Editor editor2 = sharedPreferences2.edit();
                editor2.putString("maillogin", "");
                editor2.commit();
                SharedPreferences.Editor editor3 = sharedPreferences2.edit();
                editor3.putString("passlogin", "");
                editor3.commit();
                navigationView.getMenu().findItem(R.id.nav_login).setTitle("Login");
                Toast.makeText(getApplicationContext(),"You are logged out",Toast.LENGTH_LONG).show();
                agentMenu(false);
            }
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void googleSignOut(){
        mAuth.signOut();
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("loginstate", Context.MODE_PRIVATE);
        String str = sharedPreferences.getString("login","");
        if(str.equals("")){
            Toast.makeText(getApplicationContext(),"Log in first",Toast.LENGTH_SHORT).show();
        }
        else if(str.equals("Login")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("login", "Logout");
            editor.commit();
            Toast.makeText(getApplicationContext(), "Successfully sign out", Toast.LENGTH_SHORT).show();
        }
        else if(str.equals("Logout")){
            Toast.makeText(getApplicationContext(),"Already logged out",Toast.LENGTH_SHORT).show();
        }

        Auth.GoogleSignInApi.signOut(mGoogleSignInClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {

                        Toast.makeText(getApplicationContext(), "Successfully sign out", Toast.LENGTH_SHORT).show();
                    }

                });

    }

    private void resetOnStateChange(){
        txtCaption.setText("");
        txtCaption.setVisibility(View.VISIBLE);
        stateAndLocalityLinearLayout.removeAllViews();
    }

    private void addListener(){
        spnPropertyMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedMode = ((Form)spnPropertyMode.getSelectedItem()).getValue();
                List<Form> stateList;
                if(!StringUtils.isEmpty(selectedMode)){
                    stateList = SiteMapUtility.getInstance().init(getBaseContext()).getStates(selectedMode);
                    stateList = Utility.reOrderStateList(stateList);
                }else{
                    stateList = new ArrayList<>();
                }
                Form form = new Form();
                form.setOption("Select a state");
                form.setValue("");
                stateList.add(0, form);
                ArrayAdapter<Form> formArrayAdapter = new ArrayAdapter<>(MainActivity.this,
                        R.layout.support_simple_spinner_dropdown_item, stateList);
                spnStateMode.setAdapter(formArrayAdapter);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        spnStateMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i != 0) {
                    resetOnStateChange();
                    txtCaption.setVisibility(View.VISIBLE);
                    onPropertyModeStateNodeChanged();
                }
                if(i == 0){
                    resetOnStateChange();
                    txtCaption.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("last_search");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SharedPreferences appPrefs = MainActivity.this.getSharedPreferences("loginstate", MODE_PRIVATE);
                String agentId = appPrefs.getString("AgentId", "");
                SearchParam searchParam = dataSnapshot.child(agentId).getValue(SearchParam.class);
                if(searchParam != null) {
                    LoadSimilarProperties loadSimilarProperties = new LoadSimilarProperties(searchParam);
                    loadSimilarProperties.execute();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private void onPropertyModeStateNodeChanged(){
        String selectedMode = ((Form)spnPropertyMode.getSelectedItem()).getValue();
        String selectedState = ((Form)spnStateMode.getSelectedItem()).getValue();
        List<Form> localities = SiteMapUtility.getInstance().init(getBaseContext()).getLocalities(selectedMode, selectedState);
        txtCaption.setText("Properties for "+ selectedMode + " in "+selectedState);
        TextView stateTextView = new TextView(this);
        //stateTextView.setTypeface(stateTextView.getTypeface(), Typeface.BOLD);
        stateTextView.setTextColor(getResources().getColor(R.color.black));
        stateTextView.setTextSize(18);
        stateTextView.setText(Html.fromHtml("<u>"+selectedState.toUpperCase()+"</u>"));
        LinearLayout.LayoutParams stateViewParams = new LinearLayout.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stateTextView.setLayoutParams(stateViewParams);
        FlexboxLayout flexboxLayout = new FlexboxLayout(this);
        FlexboxLayout.LayoutParams flexboxLayoutParam =  new FlexboxLayout.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        flexboxLayout.setFlexWrap(FlexboxLayout.FLEX_WRAP_WRAP);
        flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_ROW);
        flexboxLayout.setAlignItems(FlexboxLayout.ALIGN_ITEMS_CENTER);
        flexboxLayout.setAlignContent(FlexboxLayout.ALIGN_CONTENT_CENTER);
        flexboxLayout.setLayoutParams(flexboxLayoutParam);

        int localityCounter = 0;
        for(Form locality : localities){
            TextView localityTextView = new TextView(this);
            localityTextView.setTextSize(15);
            localityTextView.setText(Html.fromHtml("<u>"+ Utility.capitaliseFirstChar(locality.getValue())+"</u>"));

            localityTextView.setPadding(10, 10, 10, 10);
            localityTextView.setTextColor(getResources().getColor(R.color.black));
            LinearLayout.LayoutParams lgaViewParams = new LinearLayout.LayoutParams
                    (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            localityTextView.setLayoutParams(lgaViewParams);

            View verticalBar = new View(this);
            verticalBar.setBackgroundColor(getResources().getColor(R.color.black));
            LinearLayout.LayoutParams verticalBarParams = new LinearLayout.LayoutParams
                    (1, 20);
            verticalBar.setLayoutParams(verticalBarParams);

            flexboxLayout.addView(localityTextView);

            if(localityCounter < localities.size()-1) {
                flexboxLayout.addView(verticalBar);
                ++localityCounter;
            }
            localityTextView.setOnClickListener(new LocalityViewListener(locality.getValue(), selectedMode));
        }

        stateTextView.setOnClickListener(new StateViewListener(selectedState, selectedMode));
        stateAndLocalityLinearLayout.addView(stateTextView);
        stateAndLocalityLinearLayout.addView(flexboxLayout);
    }

    private void populateOfferView(){
        List<Form> propertyModes = SiteMapUtility.getInstance().init(getBaseContext()).getModes();
        Form form = new Form();
        form.setOption("Select a offer type");
        form.setValue("");
        propertyModes.add(0, form);
        ArrayAdapter<Form> formArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                propertyModes);
        spnPropertyMode.setAdapter(formArrayAdapter);
    }

    public class StateViewListener implements View.OnClickListener{
        private String state;
        private String mode;

        public StateViewListener(String state, String mode){
            this.state = state;
            this.mode = mode;
        }

        @Override
        public void onClick(View view) {
            String searchString = "Properties for "+mode +" in " +state;
            Toast.makeText(MainActivity.this, searchString, Toast.LENGTH_SHORT).show();
            SearchParam searchParams = new SearchParam();
            searchParams.setState(state);
            searchParams.setPropertyMode(mode);
            Intent intent = new Intent(MainActivity.this,
                    ng.com.propertypro.user.activity.ResultActivity.class);
            intent.putExtra("searchParam", searchParams);
            startActivity(intent);
        }
    }

    public class LocalityViewListener implements View.OnClickListener{

        private String axis;
        private String mode;

        public LocalityViewListener(String axis, String mode){
            this.axis = axis;
            this.mode = mode;
        }

        @Override
        public void onClick(View view) {
            String searchString = "Properties for "+mode +" in " +axis;
            Toast.makeText(MainActivity.this, searchString , Toast.LENGTH_SHORT).show();
            SearchParam searchParam = new SearchParam();
            searchParam.setAxis(axis);
            searchParam.setPropertyMode(mode);
            Intent intent = new Intent(MainActivity.this,
                    ng.com.propertypro.user.activity.ResultActivity.class);
            intent.putExtra("searchParam", searchParam);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(Utility.isAgent(getApplicationContext())) {
            agentMenu(true);
        }

        SharedPreferences sharedPreference = getApplicationContext().getSharedPreferences("notification_message", Context.MODE_PRIVATE);
        final String message1=sharedPreference.getString("message","");
        System.out.println("Going to the display "+message1);
        if(!message1.equals("")){
            System.out.println("Going to the display");
            Intent intent = new Intent(MainActivity.this, MessageDisplayActivity.class);
            intent.putExtra("message",message1);
            startActivity(intent);
        }

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("loginstate", Context.MODE_PRIVATE);
        String isAgent=sharedPreferences.getString("AgentLogin","");
        if(isAgent.equals("true")){
            navigationView.getMenu().findItem(R.id.nav_login).setTitle("Logout");
        }
        else {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                navigationView.getMenu().findItem(R.id.nav_login).setTitle("Logout");

            } else {
                navigationView.getMenu().findItem(R.id.nav_login).setTitle("Login");
            }
        }
    }

    public void agentMenu(boolean state){
        MenuItem navPostProperty = navigationView.getMenu().findItem(R.id.nav_post_property);
        navPostProperty.setVisible(state);
        MenuItem navMyListing = navigationView.getMenu().findItem(R.id.nav_my_listing);
        navMyListing.setVisible(state);
        MenuItem navLead = navigationView.getMenu().findItem(R.id.nav_lead);
        navLead.setVisible(state);
        MenuItem navMyMessages = navigationView.getMenu().findItem(R.id.my_messages);
        navMyMessages.setVisible(state);
    }

    private class LoadSimilarProperties extends AsyncTask<Void, Void, AppStatusCode> {

        private PropertyResponse propertyResponse;
        private SearchParam searchParam;

        public LoadSimilarProperties(SearchParam searchParam){
            this.searchParam = searchParam;
        }

        @Override
        protected AppStatusCode doInBackground(Void... voids) {
            if(HttpServiceProvider.isNetworkConnected(getApplicationContext())){
                try {
                    Map<String, String> queryMap = ResultActivity.getQueryMapWithPagination(searchParam, 0, 10);
                    propertyResponse = ResultActivity.search(queryMap);
                    return AppStatusCode.NETWORK_OK;
                } catch (IOException e) {
                    return AppStatusCode.INTERNET_CONNECTION_ERROR;
                }
            }else{
                return AppStatusCode.NETWORK_ACCESS_ERROR;
            }
        }

        @Override
        protected void onPostExecute(AppStatusCode appStatusCode){
            super.onPostExecute(appStatusCode);
            if(appStatusCode == AppStatusCode.NETWORK_OK){
                Property[] propertyList =  propertyResponse.getResults();
                if(propertyList.length > 0){
                    populateSimilarProperties(propertyList);
                    lltSimilarProperties.setVisibility(View.VISIBLE);
                }else{
                    lltSimilarProperties.setVisibility(View.GONE);
                }
            }
        }
    }




}
}
