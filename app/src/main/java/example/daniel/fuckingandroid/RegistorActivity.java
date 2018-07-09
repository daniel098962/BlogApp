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

public class RegistorActivity extends AppCompatActivity {

    private EditText edtMail, edtPassword, edtConfirm;
    private Button btnRegistor, btnBack;
    private FirebaseAuth mAuth;
    private ProgressBar registorBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registor);

        edtMail = (EditText)findViewById(R.id.edt_email);
        edtPassword = (EditText)findViewById(R.id.edt_password);
        edtConfirm = (EditText)findViewById(R.id.edtConfirm);
        btnRegistor = (Button)findViewById(R.id.btn_registor);
        btnBack = (Button)findViewById(R.id.btn_back);
        mAuth = FirebaseAuth.getInstance();
        registorBar = (ProgressBar)findViewById(R.id.registor_progressbar);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnRegistor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = edtMail.getText().toString();
                String password = edtPassword.getText().toString();
                String confirm = edtConfirm.getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirm))
                {
                    if (password.equals(confirm))
                    {
                        registorBar.setVisibility(View.VISIBLE);
                        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful())
                                {
                                    Intent intent = new Intent(RegistorActivity.this, SetupActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else
                                {
                                    String errorMessage = task.getException().getMessage();
                                    Toast.makeText(RegistorActivity.this,errorMessage, Toast.LENGTH_SHORT).show();

                                }
                                registorBar.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                }
                else
                {
                    Toast.makeText(RegistorActivity.this, "Password andConfirm Password are different", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
}
