package com.hill.water.Home;

import static androidx.fragment.app.FragmentManagerKt.commit;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.hill.water.Fragments.AdminApprovalFragment;
import com.hill.water.Fragments.AgentLocationFragment;
import com.hill.water.Fragments.DataFragment;
import com.hill.water.Fragments.InvoiceFragment;
import com.hill.water.Fragments.StockFragment;
import com.hill.water.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    Fragment selectedFragment = null;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.home);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mDatabase.child("users").child(user.getUid()).child("role").get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    userRole = task.getResult().getValue(String.class);

                    if (userRole != null && userRole.equals("Admin")) {

                        bottomNavigationView.getMenu().findItem(R.id.stock).setVisible(true);
                    } else {

                        bottomNavigationView.getMenu().findItem(R.id.stock).setVisible(true);
                    }
                }
            });
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new InvoiceFragment())
                .commit();

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuitem) {
                switch (menuitem.getItemId()) {

                    case R.id.home:
                        selectedFragment = new InvoiceFragment();
                        break;

                    case R.id.stock:
                        selectedFragment = new StockFragment();
                        break;

                    case R.id.data:
                            selectedFragment = new DataFragment();
                        break;

                    case R.id.requests:
                        if (userRole.equals("Admin")) {
                            selectedFragment = new AdminApprovalFragment();
                        } else {
                            Toast.makeText(HomeActivity.this, "Access Denied", Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case R.id.maps:
                            selectedFragment = new AgentLocationFragment();
                        break;

                }

                if (selectedFragment != null) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                    transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);

                    transaction.replace(R.id.fragment_container, selectedFragment);

                    transaction.commit();
                }
                return true;
            }
        });
    }
}
