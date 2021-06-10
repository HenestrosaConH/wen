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
import com.example.videomeeting.utils.PreferenceManager;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import static com.example.videomeeting.utils.Constants.*;

public class VerifyOTPActivity extends AppCompatActivity {

    private EditText inputCode1ET, inputCode2ET, inputCode3ET, inputCode4ET, inputCode5ET, inputCode6ET;
    private ProgressBar verifyCodePB;
    private Button verifyCodeBT;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_o_t_p);

        setupPhoneNumber();
        getOTPInputsViews();
        setupOTPInputs();

        verificationId = getIntent().getStringExtra(INTENT_VERIFICATION_ID);
        verifyCode();

        findViewById(R.id.resendCodeBT).setOnClickListener(v -> resendCode());
    }

    /**
     * Setups phone number TextView
     */
    private void setupPhoneNumber() {
        TextView phoneNumberTV = findViewById(R.id.phoneNumberTV);
        phoneNumberTV.setText(String.format("+34 %s", getIntent().getStringExtra(INTENT_PHONE_NUMBER)));
    }


    /**
     * Finds the EditText views
     */
    private void getOTPInputsViews() {
        inputCode1ET = findViewById(R.id.inputCode1ET);
        inputCode2ET = findViewById(R.id.inputCode2ET);
        inputCode3ET = findViewById(R.id.inputCode3ET);
        inputCode4ET = findViewById(R.id.inputCode4ET);
        inputCode5ET = findViewById(R.id.inputCode5ET);
        inputCode6ET = findViewById(R.id.inputCode6ET);
    }

    /**
     * Setups the listeners from the inputs TextViews
     */
    private void setupOTPInputs() {
        inputCode1ET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    inputCode2ET.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        inputCode2ET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    inputCode3ET.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        inputCode3ET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    inputCode4ET.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        inputCode4ET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    inputCode5ET.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        inputCode5ET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    inputCode6ET.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    /**
     * When the user clicks the send button, this method will be called in order to verify if the code is correct or not
     */
    private void verifyCode() {
        verifyCodePB = findViewById(R.id.verifyCodePB);
        verifyCodeBT = findViewById(R.id.verifyCodeBT);
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
                sendCode(code);
            }
        });
    }

    /**
     * Sends the verification code to Firebase
     * @param code verification input by the user
     */
    private void sendCode(String code) {
        changeViewsVisibility(View.VISIBLE, View.INVISIBLE);
        PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(
                verificationId,
                code
        );
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(task -> {
                    changeViewsVisibility(View.GONE, View.VISIBLE);
                    if (task.isSuccessful()) {
                        Intent intent;
                        PreferenceManager prefManager = new PreferenceManager(VerifyOTPActivity.this);
                        if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                            intent = new Intent(getApplicationContext(), SetupProfileActivity.class);
                            prefManager.putString(INTENT_PHONE_NUMBER, getIntent().getStringExtra(INTENT_PHONE_NUMBER));
                            prefManager.putBoolean(PREF_NEEDS_TO_SETUP_PROFILE, true);
                        } else {
                            intent = new Intent(getApplicationContext(), MainActivity.class);
                            prefManager.putBoolean(PREF_IS_SIGNED_IN, true);
                        }
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(VerifyOTPActivity.this, getString(R.string.verification_code_not_valid), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    /**
     * If he user doesn't receive the code and clicks "Resend it", this method will resend the verification code
     */
    private void resendCode() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+34" + getIntent().getStringExtra(INTENT_PHONE_NUMBER),
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
        );
    }

    private void changeViewsVisibility(int pbVis, int btVis) {
        verifyCodePB.setVisibility(pbVis);
        verifyCodeBT.setVisibility(btVis);
    }
}