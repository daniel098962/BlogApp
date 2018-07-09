package example.daniel.fuckingandroid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {


    private ImageView imagePost;
    private EditText edtSay;
    private Button btnPost;
    private Uri postImageUri;
    private ProgressBar postProgressBar;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private android.support.v7.widget.Toolbar postToolBar;
    private Bitmap compressedImageFile;

    private String current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        postToolBar = (android.support.v7.widget.Toolbar) findViewById(R.id.newPostBar);
        imagePost = (ImageView)findViewById(R.id.imagePlus);
        edtSay = (EditText)findViewById(R.id.edtSay);
        btnPost = (Button)findViewById(R.id.btnPost);
        postProgressBar = (ProgressBar)findViewById(R.id.postProgressBar);
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore =FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        current_user_id = firebaseAuth.getCurrentUser().getUid();

        setSupportActionBar(postToolBar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imagePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // start picker to get image for cropping and then use the image in cropping activity
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        //.setMinCropResultSize(512, 512)
                        //.setAspectRatio(1,1)
                        .start(NewPostActivity.this);

            }
        });

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String desc = edtSay.getText().toString();

                if (!TextUtils.isEmpty(desc) && postImageUri != null)
                {
                    postProgressBar.setVisibility(View.VISIBLE);

                    final String randomName = UUID.randomUUID().toString();

                    StorageReference filePath = storageReference.child("post_image").child(randomName + ".jpg");
                    filePath.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            final String downloadUri = task.getResult().getDownloadUrl().toString();


                            if (task.isSuccessful())
                            {
                                File newImageFile = new File(postImageUri.getPath());

                                try {
                                    compressedImageFile = new Compressor(NewPostActivity.this)
                                            .setMaxHeight(100)
                                            .setMaxWidth(100)
                                            .setQuality(2)
                                            .compressToBitmap(newImageFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] thumbData = baos.toByteArray();

                                UploadTask uploadTask = storageReference.child("post_images/thumbs").child(randomName + ".jpg").putBytes(thumbData);
                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        String downloadthumbUri = taskSnapshot.getDownloadUrl().toString();

                                        Map<String , Object> postMap = new HashMap<>();
                                        postMap.put("image_uri", downloadUri);
                                        postMap.put("thumb",downloadthumbUri);
                                        postMap.put("desc", desc);
                                        postMap.put("user_id", current_user_id);
                                        postMap.put("timestamp", FieldValue.serverTimestamp());

                                        firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {

                                                if (task.isSuccessful())
                                                {

                                                    Intent intent = new Intent(NewPostActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finish();

                                                }
                                                else
                                                {

                                                }

                                                postProgressBar.setVisibility(View.INVISIBLE);

                                            }
                                        });

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        //Error handing

                                    }
                                });

                            }
                            else
                            {

                                postProgressBar.setVisibility(View.INVISIBLE);

                            }

                        }
                    });
                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                postImageUri = result.getUri();
                imagePost.setImageURI(postImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


}
