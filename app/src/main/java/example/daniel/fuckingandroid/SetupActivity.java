package example.daniel.fuckingandroid;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private CircleImageView setUpImage;
    private Uri mainImageUri = null;
    private EditText setUpName;
    private Button btnSetUpSave;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private ProgressBar setUpBar;
    private FirebaseFirestore firebaseFirestore;
    private String userID;
    private boolean isChanged = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        setUpImage = (CircleImageView)findViewById(R.id.setupIamge);
        setUpName = (EditText)findViewById(R.id.edtSetUpName);
        btnSetUpSave = (Button)findViewById(R.id.btnSetUpSave);
        storageReference = FirebaseStorage.getInstance().getReference();
        setUpBar = (ProgressBar)findViewById(R.id.setUpProgressBar);

        android.support.v7.widget.Toolbar setupToolbar = findViewById(R.id.setupToolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Account Settings");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();

        setUpBar.setVisibility(View.VISIBLE);
        btnSetUpSave.setEnabled(false);
        firebaseFirestore.collection("Users").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful())
                {

                    if (task.getResult().exists())
                    {
                        Toast.makeText(SetupActivity.this, "Data Exists", Toast.LENGTH_SHORT).show();
                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");

                        mainImageUri = Uri.parse(image);

                        setUpName.setText(name);
                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.default_image);

                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(setUpImage);

                    }
                    else
                    {
                        Toast.makeText(SetupActivity.this, "Data doesn't exists", Toast.LENGTH_SHORT).show();
                    }

                }
                else
                {
                    Toast.makeText(SetupActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
                setUpBar.setVisibility(View.INVISIBLE);
                btnSetUpSave.setEnabled(true);
            }
        });


        setUpImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {

                    if (ContextCompat.checkSelfPermission(SetupActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    {

                        Toast.makeText(SetupActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                    }
                    else
                    {
                        // start picker to get image for cropping and then use the image in cropping activity
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1,1)
                                .start(SetupActivity.this);
                    }

                }
                else
                {

                    // start picker to get image for cropping and then use the image in cropping activity
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1,1)
                            .start(SetupActivity.this);

                }

            }
        });

        btnSetUpSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String userName = setUpName.getText().toString();
                setUpBar.setVisibility(View.VISIBLE);

                if (isChanged) {

                    if (!TextUtils.isEmpty(userName) && mainImageUri != null) {

                        userID = firebaseAuth.getCurrentUser().getUid();



                        StorageReference userImage = storageReference.child("profile_image").child(userID + ".jpg");
                        userImage.putFile(mainImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                if (task.isSuccessful()) {

                                    storeFireStore(task, userName);

                                } else {
                                    String errorMessage = task.getException().getMessage();
                                    Toast.makeText(SetupActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                    setUpBar.setVisibility(View.INVISIBLE);
                                }
                            }
                        });

                    }

                }
                else
                {

                    storeFireStore(null, userName);

                }
            }
        });

    }

    public void storeFireStore(@NonNull Task<UploadTask.TaskSnapshot> task, String userName)
    {
        Uri download_uri;
        if (task != null) {

            download_uri = task.getResult().getDownloadUrl();

        }
        else
        {
            download_uri = mainImageUri;
        }
        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", userName);
        userMap.put("image", download_uri.toString());

        firebaseFirestore.collection("Users").document(userID).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(SetupActivity.this, "The user Setting is Uploaded",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SetupActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    Toast.makeText(SetupActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
                setUpBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mainImageUri = result.getUri();
                setUpImage.setImageURI(mainImageUri);

                isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}