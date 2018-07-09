package example.daniel.fuckingandroid;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private Toolbar mainToolbar;
    private FirebaseAuth mAuth;
    private FloatingActionButton btnAddPost;
    private FirebaseFirestore firebaseFirestore;
    private String current_user_id;
    private BottomNavigationView mainBottomNav;
    private HomeFragment homeFragment;
    private AccountFragment accountFragment;
    private NotificationFragment notificationFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainToolbar = (Toolbar)findViewById(R.id.mainToolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("AVLink");

        homeFragment = new HomeFragment();
        notificationFragment = new NotificationFragment();
        accountFragment = new AccountFragment();
        mainBottomNav = (BottomNavigationView)findViewById(R.id.mainBottomNav);


            replaceFragment(homeFragment);
            mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()) {

                        case R.id.bottom_home: {
                            replaceFragment(homeFragment);
                            return true;
                        }
                        case R.id.bottom_Notification: {
                            replaceFragment(notificationFragment);
                            return true;
                        }
                        case R.id.bottom_account: {
                            replaceFragment(accountFragment);
                            return true;
                        }

                        default:
                            return false;

                    }
                }
            });


        btnAddPost = (FloatingActionButton)findViewById(R.id.btnAddPost);
        mainBottomNav = (BottomNavigationView)findViewById(R.id.mainBottomNav);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        btnAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewPostActivity.class);
                startActivity(intent);
            }
        });

    }

    public void onStart()
    {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null)
        {
            sendToLogin();
        }
        else
        {
            current_user_id = mAuth.getCurrentUser().getUid();
            firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful())
                    {
                        if (!task.getResult().exists())
                        {
                            Intent intent = new Intent(MainActivity.this,SetupActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else
                        {

                        }
                    }
                    else
                    {
                        String errorMessage = task.getException().toString();
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    private void sendToLogin() {

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {

            case R.id.actionSearch :
            {
                return true;
            }
            case R.id.actionSetting :
            {
                Intent intent = new Intent(MainActivity.this, SetupActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.actionLogout :
            {
                Logout();
                return true;
            }
            default:
                return false;

        }

    }

    private void Logout() {

        mAuth.signOut();
        sendToLogin();

    }

    private void replaceFragment(android.support.v4.app.Fragment fragment)
    {
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_contrain, fragment);
        fragmentTransaction.commit();
    }

}
