package com.example.space;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.space.loginRegister.Login;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private TabsAccesorAdapter myTabsAccesorAdapter;
    private FirebaseUser currentUser;
    private FirebaseAuth auth;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference();

        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("SpaceChat");

        mViewPager = findViewById(R.id.main_tabs_pager);
        myTabsAccesorAdapter = new TabsAccesorAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(myTabsAccesorAdapter);

        mTabLayout = findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null)
        {
            sendUserToLogin();
        }
        else 
        {
            verifyUserExistance();
        }
    }

    private void verifyUserExistance()
    {
        String currentUserId = auth.getCurrentUser().getUid();
        rootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if ((dataSnapshot.child("name").exists()))
                {
                    Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Intent i = new Intent(MainActivity.this,Settings.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void sendUserToLogin()
    {
        Intent i = new Intent(this, Login.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
         super.onOptionsItemSelected(item);

         if (item.getItemId() == R.id.logout)
         {
            auth.signOut();
            sendUserToLogin();
         }
        if (item.getItemId() == R.id.settings)
        {
            Intent i = new Intent(this,Settings.class);
            startActivity(i);
        }
        if (item.getItemId() == R.id.find_friends)
        {

        }

        return true;
    }
}
