package com.crux.pratd.travelbphc;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.hbb20.CountryCodePicker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * Created by rutvora (www.github.com/rutvora)
 */

public class PhoneVerification extends AppCompatActivity implements View.OnClickListener {

    EditText OTP;
    EditText phoneNumber;
    Button verify;
    boolean codeSent,verified;
    String mVerificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phone_verification);

        findViewById(R.id.number).setVisibility(View.VISIBLE);
        findViewById(R.id.code).setVisibility(View.VISIBLE);
        findViewById(R.id.button2).setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
//        switch (viewId) {
//            case R.id.verify:
//                verify();
//                break;
//        }
    }

    private void verify() {
        String number = phoneNumber.getText().toString();
        final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                LoginActivity.user.updatePhoneNumber(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            addUserDetailsToDatabase();
                        } else {
                            Toast.makeText(PhoneVerification.this, "Error, Please try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(PhoneVerification.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {

                mVerificationId = verificationId;
                codeSent = TRUE;
                OTP.setVisibility(View.VISIBLE);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        OTP.setVisibility(View.GONE);
                        codeSent = FALSE;
                    }
                }, 120000);

            }
        };

        if (codeSent) {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, OTP.getText().toString());
            mCallbacks.onVerificationCompleted(credential);

        } else {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    number,        // Phone number to verify
                    120,                 // Timeout duration
                    TimeUnit.SECONDS,   // Unit of timeout
                    PhoneVerification.this,               // Activity (for callback binding)
                    mCallbacks);        // OnVerificationStateChangedCallbacks
        }
    }

    private void addUserDetailsToDatabase() {
        Map<String, Object> map = new HashMap<>(3);
        map.put("Number", LoginActivity.user.getPhoneNumber());
        map.put("Name", LoginActivity.user.getDisplayName());
//        map.put("Photo", LoginActivity.user.getPhotoUrl().toString());
        LoginActivity.db
                .get()
                .collection("Users")
                .document(LoginActivity.user.getUid())
                .set(map, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent intent = new Intent(PhoneVerification.this, plannerActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PhoneVerification.this, "Error adding phone number", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public void sendotp(final View view)
    {
        String mobile=((EditText)findViewById(R.id.number)).getText().toString();
        if(mobile.length()<=0){
            Toast.makeText(getApplicationContext(),"Enter a mobile number",Toast.LENGTH_SHORT).show();
            return;
        }
        view.setClickable(false);
        view.setBackgroundColor(Color.GRAY);

        findViewById(R.id.otp).setVisibility(View.INVISIBLE);
        findViewById(R.id.verifybutton).setVisibility(View.INVISIBLE);
        findViewById(R.id.textView2).setVisibility(View.VISIBLE);
        ((TextView)findViewById(R.id.textView2)).setText("Waiting for SMS...");
        findViewById(R.id.progressBar2).setVisibility(View.VISIBLE);
        final TextView count=findViewById(R.id.countdown);
        count.setVisibility(View.VISIBLE);
        final CountDownTimer cdt = new CountDownTimer(26000, 1000) {
            public void onTick(long millisUntilFinished) {
                count.setText(Long.toString(millisUntilFinished/1000));
            }
            public void onFinish() {
                count.setText("");
            }
        };
        cdt.start();

        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                if(verified)
                    return;
                verified=true;
                cdt.cancel();
                signInWithPhoneAuthCredential(credential);
            }
            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                findViewById(R.id.textView2).setVisibility(View.INVISIBLE);
                findViewById(R.id.progressBar2).setVisibility(View.INVISIBLE);
                findViewById(R.id.countdown).setVisibility(View.INVISIBLE);
                findViewById(R.id.otp).setVisibility(View.INVISIBLE);
                findViewById(R.id.verifybutton).setVisibility(View.INVISIBLE);
                if (e instanceof FirebaseAuthInvalidCredentialsException){
                    Toast.makeText(getApplicationContext(),"Enter a valid mobile number",Toast.LENGTH_SHORT).show();
                }
                else if (e instanceof FirebaseTooManyRequestsException){
                    Toast.makeText(getApplicationContext(),"You have made several login attempts, try later",Toast.LENGTH_SHORT).show();
                }
                cdt.cancel();
                view.setClickable(true);
                view.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }
            @Override
            public void onCodeSent(final String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                final EditText enterOTP=findViewById(R.id.otp);
                enterOTP.setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.textView2)).setText("Auto-retrieving OTP...");
                Button verify=findViewById(R.id.verifybutton);
                verify.setVisibility(View.VISIBLE);
                verify.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                verify.setClickable(true);
                verify.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.setClickable(false);
                        view.setBackgroundColor(Color.GRAY);
                        if(enterOTP.getText().toString().equals("")){
                            Toast.makeText(getApplicationContext(),"Oops! You forgot to enter the OTP",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, enterOTP.getText().toString());
                        if(verified)
                            return;
                        verified=true;
                        signInWithPhoneAuthCredential(credential);
                    }
                });
            }
            @Override
            public void onCodeAutoRetrievalTimeOut(final String verificationId) {
                ((TextView)findViewById(R.id.textView2)).setText("Failed to auto retrieve OTP, enter OTP manually");
                findViewById(R.id.progressBar2).setVisibility(View.INVISIBLE);
                findViewById(R.id.countdown).setVisibility(View.INVISIBLE);
            }
        };
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                ((CountryCodePicker)findViewById(R.id.code)).getSelectedCountryCodeWithPlus()+ mobile,//phone number to verify
                15,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        LoginActivity.user.updatePhoneNumber(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    addUserDetailsToDatabase();
                } else {
                    Log.w("Status", "signInWithCredential:failure", task.getException());
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(getApplicationContext(),"Invalid Code, Retry",Toast.LENGTH_SHORT).show();
                        findViewById(R.id.verifybutton).setClickable(true);
                        findViewById(R.id.verifybutton).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        verified=false;
                    }
                }
            }
        });
    }
}
