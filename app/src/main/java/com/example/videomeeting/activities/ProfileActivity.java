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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.videomeeting.R;
import com.example.videomeeting.utils.Constants;
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

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    //Profile pic
    private static final int ASK_FOR_IMAGE = 1;
    private Uri imageUri;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;
    private String imageURL = Constants.CURRENT_USER.getImageURL();
    private StorageReference fileReference;
    private ImageView profileIV;
    private ImageView defaultProfileIV;
    //User parameters
    private final DatabaseReference userNamesReference = FirebaseDatabase.getInstance().getReference()
            .child(Constants.KEY_COLLECTION_USERNAME);
    private String username = Constants.CURRENT_USER.getUserName();
    private String about = Constants.CURRENT_USER.getAbout();
    private boolean isUserNameValid = true;
    private String isLastSeenEnabled = Constants.KEY_IS_LAST_SEEN_ENABLED;

    public ProfileActivity() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setTitle(getString(R.string.profile));
        Objects.requireNonNull(ProfileActivity.this.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        profileIV = findViewById(R.id.profileIV);
        profileIV.setOnClickListener(view -> pickImage());
        defaultProfileIV = findViewById(R.id.defaultProfileIV);
        defaultProfileIV.setOnClickListener(view -> pickImage());
        bindProfileIV();

        EditText usernameET = findViewById(R.id.usernameET);
        username = Constants.CURRENT_USER.getUserName();
        usernameET.setText(Constants.CURRENT_USER.getUserName());
        usernameET.addTextChangedListener(new TextWatcher()  {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void afterTextChanged(Editable s)  {
                username = usernameET.getText().toString().trim();
                if (username.isEmpty()) {
                    usernameET.setError(getString(R.string.enter_user_name));
                    isUserNameValid = false;
                } else if (username.length() < 4) {
                    usernameET.setError(getString(R.string.username_too_short));
                    isUserNameValid = false;
                } else if (!username.matches("^[a-zA-Z0-9]+$")) {
                    usernameET.setError(getString(R.string.only_letter_or_numbers));
                    isUserNameValid = false;
                } else {
                    userNamesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(username.toLowerCase())) {
                                if (Constants.CURRENT_USER.getUserName().equals(username)) {
                                    isUserNameValid = true;
                                } else {
                                    usernameET.setError(getString(R.string.username_already_exists));
                                    isUserNameValid = false;
                                }
                            } else {
                                Drawable correct = ContextCompat.getDrawable(ProfileActivity.this, R.drawable.ic_correct);
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
        });

        TextView countdownTV = findViewById(R.id.countdownTV);
        EditText aboutET = findViewById(R.id.aboutET);
        if (Constants.CURRENT_USER.getAbout() != null) {
            aboutET.setText(Constants.CURRENT_USER.getAbout());
            countdownTV.setText(String.valueOf(100 - Constants.CURRENT_USER.getAbout().length()));
        }

        aboutET.addTextChangedListener(new TextWatcher()  {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int aft) { }

            @Override
            public void afterTextChanged(Editable s) {
                countdownTV.setText(String.valueOf(100 - s.toString().length()));
                if (100 - s.toString().length() <= 20) {
                    countdownTV.setTextColor(
                            ContextCompat.getColor(
                                    ProfileActivity.this,
                                    android.R.color.holo_red_light
                            )
                    );
                } else {
                    countdownTV.setTextColor(
                            ContextCompat.getColor(
                                    ProfileActivity.this,
                                    R.color.colorSecondaryText
                            )
                    );
                }
                about = aboutET.getText().toString().trim();
            }
        });

        TextView phoneTV = findViewById(R.id.phoneTV);
        phoneTV.setText(Constants.CURRENT_USER.getPhoneNumber());
        findViewById(R.id.phoneLY).setOnClickListener(v -> Toast.makeText(this, getString(R.string.cannot_edit_the_number), Toast.LENGTH_SHORT).show());

        TextView lastSeenTV = findViewById(R.id.lastSeenTV);
        loadLastSeen(lastSeenTV);
        findViewById(R.id.updateIV).setOnClickListener(view -> pickImage());
        findViewById(R.id.deleteIV).setOnClickListener(view -> deleteProfilePic());
        findViewById(R.id.lastSeenLY).setOnClickListener(v -> setLastSeen(lastSeenTV));
        findViewById(R.id.editBT).setOnClickListener(v -> saveChanges());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (haveChangesBeenMade()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
            builder.setTitle(R.string.sure_)
                    .setMessage(R.string.some_changes_have_been_made)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        saveChanges();
                        super.onBackPressed();
                    })
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                        //if (!imageURL.equals(Constants.KEY_IMAGE_URL_DEFAULT))
                            //fileReference.delete(); TODO fix this
                        super.onBackPressed();
                    })
                    .create().show();
        } else
            super.onBackPressed();
    }

    private void bindProfileIV() {
        if (imageURL.equals(Constants.KEY_IMAGE_URL_DEFAULT)) {
            defaultProfileIV.setVisibility(View.VISIBLE);
            profileIV.setVisibility(View.GONE);
        } else {
            Glide.with(ProfileActivity.this)
                    .load(imageURL)
                    .circleCrop()
                    .into(profileIV);
        }
    }

    private void deleteProfilePic() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ProfileActivity.this);
        alertBuilder.setTitle(R.string.sure_)
                .setMessage(R.string.profile_image_will_be_removed)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    if (!imageURL.equals(Constants.KEY_IMAGE_URL_DEFAULT)) {
                        //Deleting file from the Firebase Storage
                        FirebaseStorage.getInstance()
                                .getReferenceFromUrl(imageURL)
                                .delete();
                        //Deleting image URL from user row
                        imageURL = Constants.KEY_IMAGE_URL_DEFAULT;
                        FirebaseDatabase.getInstance().getReference(Constants.KEY_COLLECTION_USER)
                                .child(Constants.FIREBASE_USER.getUid())
                                .child(Constants.KEY_IMAGE_URL)
                                .setValue(imageURL);

                        Constants.CURRENT_USER.setImageURL(imageURL);
                    }
                    profileIV.setVisibility(View.GONE);
                    defaultProfileIV.setVisibility(View.VISIBLE);
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .create().show();
    }

    private void loadLastSeen(TextView lastSeenTV) {
        isLastSeenEnabled = Constants.CURRENT_USER.isLastSeenEnabled();
        if (isLastSeenEnabled.equals(Constants.KEY_LAST_SEEN_TRUE))
            lastSeenTV.setText(R.string.everyone);
        else if (isLastSeenEnabled.equals(Constants.KEY_LAST_SEEN_CONTACTS))
            lastSeenTV.setText(R.string.my_contacts);
        else
            lastSeenTV.setText(R.string.nobody);
    }

    private void setLastSeen(TextView lastSeenTV) {
        String[] items = {
                getString(R.string.everyone),
                getString(R.string.my_contacts),
                getString(R.string.nobody)
        };

        int checkedItem = 0; //Everyone by default
        if (isLastSeenEnabled.equals(Constants.KEY_LAST_SEEN_CONTACTS))
            checkedItem = 1;
        else if (isLastSeenEnabled.equals(Constants.KEY_LAST_SEEN_FALSE))
            checkedItem = 2;

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProfileActivity.this);
        alertDialog.setTitle(R.string.last_seen)
                .setSingleChoiceItems(items, checkedItem, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            isLastSeenEnabled = Constants.KEY_LAST_SEEN_TRUE;
                            lastSeenTV.setText(getString(R.string.everyone));
                            break;
                        case 1:
                            isLastSeenEnabled = Constants.KEY_LAST_SEEN_CONTACTS;
                            lastSeenTV.setText(getString(R.string.my_contacts));
                            break;
                        case 2:
                            isLastSeenEnabled = Constants.KEY_LAST_SEEN_FALSE;
                            lastSeenTV.setText(getString(R.string.nobody));
                            break;
                    }
                }).create().show();
    }

    private void saveChanges() {
        if (isUserNameValid) {
            DatabaseReference userReference = FirebaseDatabase.getInstance().getReference(Constants.KEY_COLLECTION_USER)
                    .child(Constants.FIREBASE_USER.getUid());

            if (!Constants.CURRENT_USER.getImageURL().equals(imageURL)) {
                if (!Constants.CURRENT_USER.getImageURL().equals(Constants.KEY_IMAGE_URL_DEFAULT))
                    FirebaseStorage.getInstance()
                            .getReferenceFromUrl(Constants.CURRENT_USER.getImageURL())
                            .delete();

                userReference.child(Constants.KEY_IMAGE_URL)
                        .setValue(imageURL);
            }

            if (!Constants.CURRENT_USER.getUserName().equals(username)) {
                userNamesReference.child(Constants.CURRENT_USER.getUserName().toLowerCase()).removeValue();
                userNamesReference.child(username.toLowerCase()).setValue(Constants.KEY_USERNAME_STATE_CHANGED);
                userReference.child(Constants.KEY_USERNAME).setValue(username);
            }

            if (Constants.CURRENT_USER.getAbout() == null ||
                    !Constants.CURRENT_USER.getAbout().equals(about))
                userReference.child(Constants.KEY_ABOUT)
                        .setValue(about);

            if (!Constants.CURRENT_USER.isLastSeenEnabled().equals(isLastSeenEnabled))
                userReference.child(Constants.KEY_IS_LAST_SEEN_ENABLED)
                        .setValue(isLastSeenEnabled);

            if (haveChangesBeenMade()) {
                userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Constants.CURRENT_USER.setImageURL(imageURL);
                        Constants.CURRENT_USER.setUserName(username);
                        Constants.CURRENT_USER.setAbout(about);
                        Constants.CURRENT_USER.setLastSeenEnabled(isLastSeenEnabled);
                        Toast.makeText(ProfileActivity.this, getString(R.string.data_updated_successfully), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProfileActivity.this, getString(R.string.unexpected_error), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else
            Toast.makeText(this, getString(R.string.user_name_invalid), Toast.LENGTH_SHORT).show();
    }

    private boolean haveChangesBeenMade() {
        if (isUserNameValid) {
            return !Constants.CURRENT_USER.getImageURL().equals(imageURL) ||
                    !Constants.CURRENT_USER.getUserName().equals(username) ||
                    !Constants.CURRENT_USER.isLastSeenEnabled().equals(isLastSeenEnabled) ||
                    (Constants.CURRENT_USER.getAbout() != null && !Constants.CURRENT_USER.getAbout().equals(about));
        } else
            return false;
    }

    private void pickImage() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i, ASK_FOR_IMAGE);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = ProfileActivity.this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(ProfileActivity.this);
        progressDialog.setMessage(getString(R.string.uploading));
        progressDialog.show();

        if (imageUri != null) {
            fileReference =
                    FirebaseStorage.getInstance().getReference(Constants.KEY_STORAGE_PROFILE_PICTURE)
                            .child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) throw Objects.requireNonNull(task.getException());
                return fileReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    if (downloadUri != null)
                        if (!imageURL.equals(Constants.KEY_IMAGE_URL_DEFAULT))
                            FirebaseStorage.getInstance()
                                    .getReferenceFromUrl(imageURL)
                                    .delete();

                        imageURL = downloadUri.toString();

                        defaultProfileIV.setVisibility(View.GONE);
                        Glide.with(ProfileActivity.this)
                                .load(imageURL)
                                .circleCrop()
                                .into(profileIV);
                        profileIV.setVisibility(View.VISIBLE);

                    progressDialog.dismiss();
                } else
                    Toast.makeText(ProfileActivity.this, getString(R.string.unexpected_error), Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show());
        } else
            Toast.makeText(ProfileActivity.this, getString(R.string.select_an_image), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ASK_FOR_IMAGE
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            imageUri = data.getData();
            if (uploadTask != null && uploadTask.isInProgress())
                Toast.makeText(ProfileActivity.this, getString(R.string.uploading), Toast.LENGTH_SHORT).show();
            else
                uploadImage();
        }
    }
}