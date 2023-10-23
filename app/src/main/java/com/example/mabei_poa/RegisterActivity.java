package com.example.mabei_poa;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.mabei_poa.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    ActivityRegisterBinding  binding;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();

        binding.btnLoginSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(RegisterActivity.this, "Sorry, can't register new user", Toast.LENGTH_SHORT).show();
                //RegisterUser();
            }
        });

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,MainActivity.class));
            }
        });
    }

    private void RegisterUser() {
        String userEmail = binding.userEmail.getText().toString().trim();
        String userPassword = binding.userPassword.getText().toString().trim();
        String userReenterPassword = binding.userReenteredPassword.getText().toString().trim();

        if(userEmail.isEmpty() || userPassword.isEmpty() || userReenterPassword.isEmpty()){
            Toast.makeText(this, "Enter all the fields to proceed", Toast.LENGTH_SHORT).show();
        } else if(!userPassword.equals(userReenterPassword)){
            Toast.makeText(this, "Password & ReEntered Password don't match", Toast.LENGTH_SHORT).show();
        } else if(userPassword.length() < 6){
            Toast.makeText(this, "Password is too short", Toast.LENGTH_SHORT).show();
        }

        else {
            firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.createUserWithEmailAndPassword(userEmail,userPassword);
            //TODO verify email first before allowing sign up
            Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
        }
    }
}