package com.hill.water.Splash;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hill.water.Home.HomeActivity;
import com.hill.water.LoginAndSignup.LoginActivity;
import com.hill.water.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreen extends AppCompatActivity {

    private static int SPLASH_SCREEN = 2500;
    Animation topanim,bottomanim;
    LottieAnimationView splash;
    TextView appname;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.WHITE);
        }
        splash = findViewById(R.id.appAnimation);
        appname = findViewById(R.id.appname);

        topanim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomanim = AnimationUtils.loadAnimation(this,R.anim.bottom_animation);
        splash.setAnimation(topanim);
        appname.setAnimation(bottomanim);
        mAuth = FirebaseAuth.getInstance();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    checkUserApprovalStatus(currentUser.getUid());
                } else {
                    goToLoginActivity();
                }
            }
        }, SPLASH_SCREEN);
    }

    private void checkUserApprovalStatus(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("status");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = snapshot.getValue(String.class);
                if ("approved".equals(status)) {
                    splash.cancelAnimation();
                    splash.setVisibility(View.GONE);
                    appname.setVisibility(View.GONE);
                    goToHomeActivity();
                } else {
                    mAuth.signOut();
                    splash.cancelAnimation();
                    splash.setVisibility(View.GONE);
                    appname.setVisibility(View.GONE);
                    goToLoginActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SplashScreen.this, "Failed to check user status", Toast.LENGTH_SHORT).show();
                goToLoginActivity();
            }
        });
    }

    private void goToHomeActivity() {
        Intent intent = new Intent(SplashScreen.this, HomeActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in_splash, R.anim.fade_out_splash);
        finish();
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in_splash, R.anim.fade_out_splash);
        finish();
    }
}