package com.example.railway;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class SignIn extends AppCompatActivity {
    private static final String TAG = "SignIn";
    private EditText editTextEmail, editTextPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private Button signIn,signUp;
    private DatabaseReference databaseReference;
    private RadioGroup radioStateGroup;
    private RadioButton radioStateButton;
    private ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        actionBar=getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#783887")));
        mAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.edit_text_email);
        editTextPassword = findViewById(R.id.edit_text_password);
        signIn = findViewById(R.id.button_sign_in);
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);
        radioStateGroup=findViewById(R.id.radioState_1);
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        signUp=findViewById(R.id.button_sign_up);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SignIn.this, SignUp.class);
                startActivity(intent);
    }
});


        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUserAccount();
            }
        });

    }

    private void loginUserAccount() {
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        final String emailOrPassword = editTextEmail.getText().toString();
        Log.e(TAG,"emailOrPassword : "+emailOrPassword);
        String pw = editTextPassword.getText().toString();
        int selectedId = radioStateGroup.getCheckedRadioButtonId();
        radioStateButton = findViewById(selectedId);
        final String state=radioStateButton.getText().toString();
        if (TextUtils.isEmpty(emailOrPassword)) {
            Toast.makeText(getApplicationContext(), getString(R.string.please_enter_email), Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(pw)) {
            Toast.makeText(getApplicationContext(), getString(R.string.please_enter_password), Toast.LENGTH_LONG).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(emailOrPassword, pw)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("Users");
                            ref.addValueEventListener(new ValueEventListener(){
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot datas : dataSnapshot.getChildren()) {
                                        String keys = datas.child("state").getValue().toString();
                                        String keys1 = datas.child("state1").getValue().toString();
                                        String email_1 = datas.child("email").getValue().toString();
                                        String phone_1 = datas.child("phone").getValue().toString();
                                        Log.e(TAG,"STATE : "+state);
                                        Log.e(TAG,"PHONE : "+phone_1);
                                        Log.e(TAG,"emailOrPassword : "+emailOrPassword);
                                        if ((keys.equals("GateMan")||keys1.equals("حارس البوابة"))
                                                &&(email_1.equals(emailOrPassword)||phone_1.equals(emailOrPassword))) {
                                            if (keys.equals(state)||keys1.equals(state)){
                                                Toast.makeText(getApplicationContext(), getString(R.string.sign_up_successfully), Toast.LENGTH_LONG).show();
                                                Intent intent = new Intent(SignIn.this, GateMan.class);
                                                startActivity(intent);
                                                break;
                                            }
                                            else if(!keys.equals(state)) {
                                                Toast.makeText(SignIn.this,getString(R.string.account_state)+getString(R.string.title_activity_gate_man),Toast.LENGTH_LONG).show();
                                                break;
                                            }

                                        } else if ((keys.equals("Driver")||keys1.equals("سائق"))
                                                &&(email_1.equals(emailOrPassword)||phone_1.equals(emailOrPassword))) {
                                            if(keys.equals(state)||keys1.equals(state)){
                                                Toast.makeText(getApplicationContext(),getString(R.string.sign_up_successfully), Toast.LENGTH_LONG).show();
                                                Intent intent = new Intent(SignIn.this, Driver.class);
                                                startActivity(intent);
                                                break;
                                            }
                                            else if (!keys.equals(state)){
                                                Toast.makeText(SignIn.this,getString(R.string.account_state)+getString(R.string.title_activity_driver),Toast.LENGTH_LONG).show();
                                                break;
                                            }


                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }

                            });
                        }
                        else {
                            Toast.makeText(getApplicationContext(), getString(R.string.login_failed), Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_login, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ar:
                setLocale("ar");
                Toast.makeText(this, R.string.toast_arabic, Toast.LENGTH_LONG).show();
                return true;
            case R.id.en:
                setLocale("en");
                Toast.makeText(this,R.string.toast_english, Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(this, SignIn.class);
        startActivity(refresh);
        finish();
    }
    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(a);
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }}, 2000);
    }
}
