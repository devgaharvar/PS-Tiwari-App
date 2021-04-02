package com.pstiwari.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.pstiwari.android.call.BaseActivity;
import com.pstiwari.android.calls.Global;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccesserAdapter myTabsAccesserAdapter;

    private FirebaseUser currentUser;
    DatabaseReference deleteC;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserID = null;
    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE= 2323;
    private AdView mAdView;
    private ValueEventListener aListener;
    public static boolean shouldSkipUpdateStatus = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mAuth = FirebaseAuth.getInstance();
        deleteC = FirebaseDatabase.getInstance().getReference(Global.CALLS);
        try {
            currentUserID = mAuth.getCurrentUser().getUid();
        }
        catch (NullPointerException ignored) {
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && !Settings.canDrawOverlays(getApplicationContext()))
        {
            RequestPermission();
        }
        currentUser = mAuth.getCurrentUser();
        RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.keepSynced(true);
        if (currentUser == null)
        {
            sendUserToLoginActivity();
            MainActivity.this.finish();
        }
        else
        {
            //updateUserStatus("online");

            VerifyUserExistance();
        }

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Dr. PS Tiwari - 7059613492");



        myViewPager = (ViewPager) findViewById(R.id.main_tabs_pager);


        myTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);
        myTabsAccesserAdapter = new TabsAccesserAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccesserAdapter);

        myTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                System.out.println("88888888888888888888"+tab.getPosition());
                myViewPager.setCurrentItem(tab.getPosition());
                //myTabsAccesserAdapter.refreshFragment(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        /*if(getIntent().getExtras() != null && getIntent().getExtras().getString("FORWARD_MSG") != null){
            forwardMsg = getIntent().getExtras().getString("FORWARD_MSG");
            myViewPager.setCurrentItem(1);
        }*/


        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            System.out.println("getInstanceId failed "+task.getException());
                            return;
                        }

                        // Get new Instance ID token, and update it on Firebase db
                        String token = task.getResult().getToken();
                        //System.out.println("device fcm token "+token);
                        if(currentUser != null){
                            FirebaseDatabase.getInstance().getReference().child("Users").
                                    child(currentUser.getUid()).child("device_token").setValue(token);
                        }
                    }
                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(getApplicationContext())) {
                    //     PermissionDenied();
                }
                else
                {
                    // Permission Granted-System will work
                }

            }
        }
    }

    private void updateTokens() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (task.isSuccessful()) {
                    String token = task.getResult().getToken();
                    if (token != null) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("tokens", token);
                        DatabaseReference mToken = FirebaseDatabase.getInstance().getReference(Global.tokens);
                        mToken.child(mAuth.getCurrentUser().getUid()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                            }
                        });
                    }
                }
            }
        });
    }
    private void RequestPermission() {
        // Check if Android M or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Show alert dialog to the user saying a separate permission is needed
            // Launch the settings activity if the user prefers
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getApplicationContext().getPackageName()));
            startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }
    @Override
    protected void onStart() {

        super.onStart();

        /*
        if (currentUser == null)
        {
            sendUserToLoginActivity();
        }
        else
        {
            updateUserStatus("online");

            VerifyUserExistance();
        }*/
    }

    @Override
    protected void onResume(){
        super.onResume();
        updateUserStatus("online");
        shouldSkipUpdateStatus = false;
    }


    @Override
    protected void onStop()
    {
        super.onStop();

        if (currentUser != null && !shouldSkipUpdateStatus)
        {
            updateUserStatus("offline");
        }
    }



    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (currentUser != null && !shouldSkipUpdateStatus)
        {
            updateUserStatus("offline");
        }
    }



    private void VerifyUserExistance() {

        final String currentUserID = mAuth.getCurrentUser().getUid();
        aListener=RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if ((dataSnapshot.child("name").exists()))
                {
                    //Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_LONG).show();

                    RootRef.child("Users").child(currentUserID).removeEventListener(aListener);
                }
                else
                {
                    sendUserToSettingsActivity();
                    RootRef.child("Users").child(currentUserID).removeEventListener(aListener);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu, menu);

        updateTokens();
      return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
       super.onOptionsItemSelected(item);

       if (item.getItemId() == R.id.main_logout_option)
       {
           updateUserStatus("offline");
           shouldSkipUpdateStatus = false;
           mAuth.signOut();
           sendUserToLoginActivity();
       }

        if (item.getItemId() == R.id.main_settings_option)
        {
            sendUserToSettingsActivity();
        }

        if (item.getItemId() == R.id.main_find_friends_option)
        {
            sendUserToFindFriendsActivity();
        }
        if (item.getItemId()==R.id.clear_call_log)
        {
            deleteC.child(mAuth.getCurrentUser().getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(),"Call log deleted", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                }
            });
        }

        return true;
    }


    private void sendUserToLoginActivity() {

        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
    }

    private void sendUserToSettingsActivity() {

        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        MainActivity.shouldSkipUpdateStatus = true;
        startActivity(settingsIntent);
        finish();
    }

    private void sendUserToFindFriendsActivity() {

        Intent findFriendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        MainActivity.shouldSkipUpdateStatus = true;
        startActivity(findFriendsIntent);
    }


    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS}, 1);
        }
    }


    private void updateUserStatus(String state)
    {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);
        RootRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);

    }
}
