package com.sir.crisischat.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sir.crisischat.Utilities.Constants;
import com.sir.crisischat.Utilities.PreferenceManager;
import com.sir.crisischat.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding activityLoginBinding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(Constants.KEY_IS_LOGGED_IN)){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        activityLoginBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(activityLoginBinding.getRoot());

        setListeners();
    }

    private void setListeners(){
        activityLoginBinding.textCreateNewAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),RegisterActivity.class)));
        activityLoginBinding.ButtonLogin.setOnClickListener(v -> {
            if(isValidLoginDetails()){
                Login();
            }
        });
    }

    private void Login(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL,activityLoginBinding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,activityLoginBinding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null
                            &&  task.getResult().getDocuments().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_LOGGED_IN, true);
                        preferenceManager.putString(Constants.KEY_USERID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    else{
                        loading(false);
                        showToast("Unable To Login");
                    }
                });
    }

    private void loading(Boolean isLoading){
        if (isLoading){
            activityLoginBinding.ButtonLogin.setVisibility(View.INVISIBLE);
            activityLoginBinding.progressBar.setVisibility(View.VISIBLE);
        }
        else {
            activityLoginBinding.ButtonLogin.setVisibility(View.VISIBLE);
            activityLoginBinding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidLoginDetails(){
        if (activityLoginBinding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Enter Email");
            return false;
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(activityLoginBinding.inputEmail.getText().toString()).matches()){
            showToast("Invalid Email");
            return false;
        }
        else if (activityLoginBinding.inputPassword.getText().toString().trim().isEmpty()){
            showToast("Enter Password");
            return false;
        }
        else{
            return true;
        }
    }
}