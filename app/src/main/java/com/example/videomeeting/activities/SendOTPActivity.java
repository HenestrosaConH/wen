package com.example.videomeeting.activities;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.videomeeting.R;
import com.example.videomeeting.utils.Constants;
import com.example.videomeeting.utils.PreferenceManager;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.concurrent.TimeUnit;

public class SendOTPActivity extends AppCompatActivity {

    private String phoneNumber;

    /*
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            Intent i = new Intent(SendOTPActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(Constants.PREF_IS_SIGNED_IN)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        } else if (preferenceManager.getBoolean(Constants.PREF_NEEDS_TO_SETUP_PROFILE)) {
            Intent intent = new Intent(getApplicationContext(), SetupProfileActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_send_o_t_p);

        EditText phoneNumberET = findViewById(R.id.phoneNumberET);
        phoneNumberET.addTextChangedListener(new PhoneNumberFormattingTextWatcher("ES"));

        ProgressBar sendCodePB = findViewById(R.id.sendCodePB);
        Button sendCodeBT = findViewById(R.id.sendCodeBT);
        sendCodeBT.setOnClickListener(v -> {
            phoneNumber = phoneNumberET.getText().toString();
            if (TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(SendOTPActivity.this, getString(R.string.enter_phone_number), Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isPhoneNumberValid(phoneNumber, "ZZ")) {//ZZ is the same as null. It's used because the country code is explicitly written on the phone number
                AlertDialog.Builder builder = new AlertDialog.Builder(SendOTPActivity.this);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    builder.setMessage(Html.fromHtml("<b>+34 " + phoneNumber + "</b><br><br>" + getString(R.string.is_not_a_valid_phone_number_for) + " " + getString(R.string.spain), Html.FROM_HTML_MODE_LEGACY));
                } else {
                    builder.setMessage(Html.fromHtml("+34 " + phoneNumber + "<br><br>" + getString(R.string.is_not_a_valid_phone_number_for) + " " + getString(R.string.spain)));
                }
                builder.setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss());
                builder.create().show();
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(SendOTPActivity.this);
            builder.setCancelable(false);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
                builder.setMessage(Html.fromHtml(getString(R.string.we_will_be_verifying) + "<br><br><b>+34 " + phoneNumber + "</b><br><br>" + getString(R.string.would_you_like_to_edit_the_number), Html.FROM_HTML_MODE_LEGACY));
            else
                builder.setMessage(Html.fromHtml(getString(R.string.we_will_be_verifying) + "<br><br><b>+34 " + phoneNumber + "</b><br><br>" + getString(R.string.would_you_like_to_edit_the_number)));

            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                sendCodePB.setVisibility(View.VISIBLE);
                sendCodeBT.setVisibility(View.INVISIBLE);

                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        "+34" + phoneNumber,
                        //Once the SMS has been sent, the user won't get a new code in the next 60 seconds
                        60,
                        TimeUnit.SECONDS,
                        SendOTPActivity.this,
                        new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                sendCodePB.setVisibility(View.GONE);
                                sendCodeBT.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                sendCodePB.setVisibility(View.GONE);
                                sendCodeBT.setVisibility(View.VISIBLE);
                                Toast.makeText(SendOTPActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                sendCodePB.setVisibility(View.GONE);
                                sendCodeBT.setVisibility(View.VISIBLE);
                                Intent intent = new Intent(getApplicationContext(), VerifyOTPActivity.class);
                                intent.putExtra(Constants.INTENT_PHONE_NUMBER, phoneNumber);
                                intent.putExtra(Constants.INTENT_VERIFICATION_ID, verificationId);
                                startActivity(intent);
                            }
                        }
                );
            });
            builder.setNegativeButton(R.string.edit, (dialog, which) -> dialog.dismiss());
            builder.create().show();
        });
    }

    public boolean isPhoneNumberValid(String phoneNumber, String countryCode) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse("+34" + phoneNumber, countryCode);
            return phoneUtil.isValidNumber(numberProto);
        } catch (NumberParseException e) {
            Toast.makeText(SendOTPActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}