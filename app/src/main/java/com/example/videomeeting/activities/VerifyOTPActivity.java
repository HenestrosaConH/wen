package com.example.videomeeting.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.videomeeting.R;
import com.example.videomeeting.utils.Constants;
import com.example.videomeeting.utils.PreferenceManager;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerifyOTPActivity extends AppCompatActivity {

    private EditText inputCode1ET, inputCode2ET, inputCode3ET, inputCode4ET, inputCode5ET, inputCode6ET;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_o_t_p);

        TextView phoneNumberTV = findViewById(R.id.phoneNumberTV);
        phoneNumberTV.setText(String.format("+34 %s", getIntent().getStringExtra(Constants.INTENT_PHONE_NUMBER)));

        inputCode1ET = findViewById(R.id.inputCode1ET);
        inputCode2ET = findViewById(R.id.inputCode2ET);
        inputCode3ET = findViewById(R.id.inputCode3ET);
        inputCode4ET = findViewById(R.id.inputCode4ET);
        inputCode5ET = findViewById(R.id.inputCode5ET);
        inputCode6ET = findViewById(R.id.inputCode6ET);

        setupOTPinputs();

        final ProgressBar verifyCodePB = findViewById(R.id.verifyCodePB);
        final Button verifyCodeBT = findViewById(R.id.verifyCodeBT);

        verificationId = getIntent().getStringExtra(Constants.INTENT_VERIFICATION_ID);

        verifyCodeBT.setOnClickListener(v -> {
            if (TextUtils.isEmpty(inputCode1ET.getText().toString())
                    || TextUtils.isEmpty(inputCode2ET.getText().toString())
                    || TextUtils.isEmpty(inputCode3ET.getText().toString())
                    || TextUtils.isEmpty(inputCode4ET.getText().toString())
                    || TextUtils.isEmpty(inputCode5ET.getText().toString())
                    || TextUtils.isEmpty(inputCode6ET.getText().toString()))  {
                Toast.makeText(VerifyOTPActivity.this, getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
            }

            String code =
                    inputCode1ET.getText().toString() +
                            inputCode2ET.getText().toString() +
                            inputCode3ET.getText().toString() +
                            inputCode4ET.getText().toString() +
                            inputCode5ET.getText().toString() +
                            inputCode6ET.getText().toString();

            if (verificationId != null) {
                verifyCodePB.setVisibility(View.VISIBLE);
                verifyCodeBT.setVisibility(View.INVISIBLE);
                PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(
                        verificationId,
                        code
                );
                FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential)
                        .addOnCompleteListener(task -> {
                            verifyCodePB.setVisibility(View.GONE);
                            verifyCodeBT.setVisibility(View.VISIBLE);
                            if (task.isSuccessful()) {
                                Intent intent;
                                PreferenceManager preferenceManager = new PreferenceManager(VerifyOTPActivity.this);
                                if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                                    intent = new Intent(getApplicationContext(), SetupProfileActivity.class);
                                    preferenceManager.putString(Constants.INTENT_PHONE_NUMBER, getIntent().getStringExtra(Constants.INTENT_PHONE_NUMBER));
                                    preferenceManager.putBoolean(Constants.PREF_NEEDS_TO_SETUP_PROFILE, true);
                                } else {
                                    intent = new Intent(getApplicationContext(), MainActivity.class);
                                    preferenceManager.putBoolean(Constants.PREF_IS_SIGNED_IN, true);
                                }

                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                Toast.makeText(VerifyOTPActivity.this, getString(R.string.verification_code_not_valid), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        findViewById(R.id.resendCodeBT).setOnClickListener(v -> PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+34" + getIntent().getStringExtra(Constants.INTENT_PHONE_NUMBER),
                //Once the SMS has been sent, the user can't get a new code for 60 seconds
                60,
                TimeUnit.SECONDS,
                VerifyOTPActivity.this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) { }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(VerifyOTPActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String newVerificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        verificationId = newVerificationId;
                        Toast.makeText(VerifyOTPActivity.this, getString(R.string.code_sent), Toast.LENGTH_SHORT).show();
                    }
                }
        ));
    }

    private void setupOTPinputs() {
        inputCode1ET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty())
                    inputCode2ET.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        inputCode2ET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty())
                    inputCode3ET.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        inputCode3ET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty())
                    inputCode4ET.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        inputCode4ET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty())
                    inputCode5ET.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        inputCode5ET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty())
                    inputCode6ET.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }
}