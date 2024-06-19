package com.example.Cyber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import static com.example.Cyber.ProductsActivity.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.Cyber.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
                RegisterUser();
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
//            firebaseAuth = FirebaseAuth.getInstance();
//            firebaseAuth.createUserWithEmailAndPassword(userEmail,userPassword)
//                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
//                        @Override
//                        public void onSuccess(AuthResult authResult) {
//                            Toast.makeText(RegisterActivity.this, "New user has been registered"+ authResult.getAdditionalUserInfo(), Toast.LENGTH_SHORT).show();
//                            Log.d(TAG, "onSuccess: "+ authResult.getAdditionalUserInfo());
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                            Log.d(TAG, "onFailure: Failed to register new user "+e.getMessage());
//                        }
//                    });
            //TODO verify email first before allowing sign up
            //Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
        }
    }
}