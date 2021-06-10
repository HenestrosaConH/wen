package com.example.videomeeting.activities;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.videomeeting.R;
import com.example.videomeeting.models.User;
import com.example.videomeeting.utils.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import static com.example.videomeeting.utils.Constants.*;

public class SetupProfileActivity extends AppCompatActivity {

    private PreferenceManager prefManager;
    //Firebase
    private final DatabaseReference usernamesRef = FirebaseDatabase.getInstance().getReference()
            .child(KEY_COLLECTION_USERNAMES);
    private FirebaseUser firebaseUser;
    private String username;
    private boolean isUserNameValid = false;
    //ProfilePic
    private static final int ASK_FOR_IMAGE = 1;
    private Uri imageUri;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;
    private String imageURL = KEY_IMAGE_URL_DEFAULT;
    private ImageView profileIV;

    public SetupProfileActivity() {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefManager = new PreferenceManager(getApplicationContext());
        isProfSetup();
        setContentView(R.layout.activity_setup_profile);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        setupUserNameET();
        setupProfileIV();
        checkData();
    }

    /**
     * Checks if the profile has been already setup
     */
    private void isProfSetup() {
        if (!prefManager.getBoolean(PREF_NEEDS_TO_SETUP_PROFILE)) {
            Intent i = new Intent(SetupProfileActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }

    /**
     * Setup profile ImageView
     */
    private void setupProfileIV() {
        profileIV = findViewById(R.id.profileIV);
        profileIV.setOnClickListener(view -> pickImage());
    }

    /**
     * Setup username EditText
     */
    private void setupUserNameET() {
        EditText usernameET = findViewById(R.id.usernameET);
        usernameET.addTextChangedListener(new TextWatcher()  {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void afterTextChanged(Editable s) {
                username = usernameET.getText().toString().trim();
                checkUserName(usernameET);
            }
        });
    }

    /**
     * Checks the validity of the username input by the user
     */
    private void checkUserName(EditText usernameET) {
        if (username.isEmpty()) {
            usernameET.setError(getString(R.string.enter_user_name));
        } else if (username.length() < 4) {
            usernameET.setError(getString(R.string.username_too_short));
            isUserNameValid = false;
        } else if (!username.matches("^[a-zA-Z0-9]+$")) {
            usernameET.setError(getString(R.string.only_letter_or_numbers));
            isUserNameValid = false;
        } else {
            usernamesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(username.toLowerCase())) {
                        usernameET.setError(getString(R.string.username_already_exists));
                        isUserNameValid = false;
                    } else {
                        Drawable correct = getResources().getDrawable(R.drawable.ic_correct);
                        correct.setBounds(0, 0, correct.getIntrinsicWidth(), correct.getIntrinsicHeight());
                        usernameET.setError(getString(R.string.available), correct);
                        isUserNameValid = true;
                    }
                }

                @Override
                public void onCancelled(@NotNull DatabaseError databaseError) { }
            });
        }
    }

    /**
     * When we click on the setup profile Button, the method makes sure that the input information is correct
     */
    private void checkData() {
        ProgressBar setupProfilePB = findViewById(R.id.setupProfilePB);
        Button setupProfileBT = findViewById(R.id.setupProfileBT);
        setupProfileBT.setOnClickListener(v -> {
            if (isUserNameValid) {
                sendDataToDB(setupProfilePB, setupProfileBT);
            } else {
                Toast.makeText(SetupProfileActivity.this, getString(R.string.user_name_invalid), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * If the information is valid, then we create the user into Firebase
     */
    private void sendDataToDB(ProgressBar setupProfilePB, Button setupProfileBT) {
        setupProfilePB.setVisibility(View.VISIBLE);
        setupProfileBT.setVisibility(View.INVISIBLE);

        User user = new User(
                username,
                "",
                imageURL
        );

        usernamesRef.child(username.toLowerCase()).setValue(true);

        FirebaseDatabase.getInstance()
                .getReference(KEY_COLLECTION_USERS)
                .child(firebaseUser.getUid())
                .setValue(user, (error, ref) -> {
                    Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    prefManager.putBoolean(PREF_IS_SIGNED_IN, true);
                    prefManager.putBoolean(PREF_NEEDS_TO_SETUP_PROFILE, false);
                    startActivity(intent);
                });
    }

    /**
     * Picks image from gallery
     */
    private void pickImage() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i, ASK_FOR_IMAGE);
    }

    /**
     * Gets file extension
     */
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = SetupProfileActivity.this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    /**
     * Uploads the image selected by the user into Firebase
     */
    private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(SetupProfileActivity.this);
        progressDialog.setMessage(getString(R.string.uploading));
        progressDialog.show();

        if (imageUri != null) {
            final StorageReference fileRef =
                    FirebaseStorage.getInstance().getReference(KEY_STORAGE_PROFILE_PICTURES).child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            uploadTask = fileRef.putFile(imageUri);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) throw task.getException();
                return fileRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    if (downloadUri != null) {
                        imageURL = downloadUri.toString();
                    }

                    FirebaseDatabase.getInstance().getReference(KEY_COLLECTION_USERS)
                            .child(firebaseUser.getUid())
                            .child(KEY_IMAGE_URL)
                            .setValue(imageURL)
                            .addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    profileIV.setPadding(0,0,0,0);
                                    profileIV.setBackgroundResource(0);
                                    Glide.with(SetupProfileActivity.this)
                                            .load(imageURL)
                                            .circleCrop()
                                            .into(profileIV);
                                } else {
                                    Toast.makeText(this, getString(R.string.unexpected_error), Toast.LENGTH_SHORT).show();
                                }
                            });
                    progressDialog.dismiss();
                } else {
                    Toast.makeText(SetupProfileActivity.this, getString(R.string.unexpected_error), Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> Toast.makeText(SetupProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show());
        } else {
            Toast.makeText(SetupProfileActivity.this, getString(R.string.select_an_image), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ASK_FOR_IMAGE
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            imageUri = data.getData();
            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(SetupProfileActivity.this, getString(R.string.uploading), Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        }
    }

}