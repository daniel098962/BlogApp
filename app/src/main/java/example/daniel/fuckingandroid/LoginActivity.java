package example.daniel.fuckingandroid;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText edtMail, edtPassword;
    private Button btnLogin, btnRegistor;
    private ProgressBar loginBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtMail = (EditText)findViewById(R.id.edt_email);
        edtPassword = (EditText)findViewById(R.id.edt_password);
        btnLogin = (Button)findViewById(R.id.btn_login);
        btnRegistor = (Button)findViewById(R.id.btn_registor);
        loginBar = (ProgressBar)findViewById(R.id.login_progressbar);
        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String loginEmail = edtMail.getText().toString();
                String loginPassword = edtPassword.getText().toString();

                if (!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPassword))
                {
                    loginBar.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(loginEmail, loginPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful())
                            {
                                System.out.println("Success");
                                sendToMain();
                            }
                            else
                            {
                                String errorMessage = task.getException().toString();
                                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                            loginBar.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });

        btnRegistor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegistorActivity.class);
                startActivity(intent);
            }
        });

    }

    public void onStart()
    {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
        {
            sendToMain();
        }
    }

    public void sendToMain()
    {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
