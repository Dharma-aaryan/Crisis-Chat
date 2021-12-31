package com.sir.crisischat.Activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.sir.crisischat.Utilities.Constants;
import com.sir.crisischat.Utilities.PreferenceManager;
import com.sir.crisischat.databinding.ActivityRegisterBinding;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding activityRegisterBinding;
    private PreferenceManager preferenceManager;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityRegisterBinding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(activityRegisterBinding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());

        setListeners();
    }

    private void setListeners(){
        activityRegisterBinding.textSignIn.setOnClickListener(v -> onBackPressed());
        activityRegisterBinding.ButtonRegister.setOnClickListener(v -> {
            if (isValidRegisterDetails()){
                Register();
            }
        });
        activityRegisterBinding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    
    private void Register(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, activityRegisterBinding.inputName.getText().toString());
        user.put(Constants.KEY_EMAIL, activityRegisterBinding.inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, activityRegisterBinding.inputPassword.getText().toString());
        user.put(Constants.KEY_IMAGE, encodedImage);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Constants.KEY_IS_LOGGED_IN, true);
                    preferenceManager.putString(Constants.KEY_USERID, documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME, activityRegisterBinding.inputName.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception -> {
                    loading(false);
                    showToast(exception.getMessage());

                });
    }

    private String encodedImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK){
                    if (result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            activityRegisterBinding.imageProfile.setImageBitmap(bitmap);
                            activityRegisterBinding.textAddImage.setVisibility(View.GONE);
                            encodedImage = encodedImage(bitmap);
                        }
                        catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private boolean isValidRegisterDetails(){
        if(encodedImage == null){
            showToast("Select profile image");
            return false;
        }
        else if (activityRegisterBinding.inputName.getText().toString().trim().isEmpty()){
            showToast("Enter Name");
            return false;
        }
        else if (activityRegisterBinding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter Email");
            return false;
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(activityRegisterBinding.inputEmail.getText().toString()).matches()) {
            showToast("Enter Valid Email");
            return false;
        }
        else if (activityRegisterBinding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter Password");
            return false;
        }
        else if (activityRegisterBinding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Confirm Password");
            return false;
        }
        else if (!activityRegisterBinding.inputPassword.getText().toString().
                equals(activityRegisterBinding.inputConfirmPassword.getText().toString())) {
            showToast("Password Must be same in both fields");
            return false;
        }
        else{
            return true;
        }
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            activityRegisterBinding.ButtonRegister.setVisibility(View.INVISIBLE);
            activityRegisterBinding.progressBar.setVisibility(View.VISIBLE);
        }
        else {
            activityRegisterBinding.progressBar.setVisibility(View.INVISIBLE);
            activityRegisterBinding.ButtonRegister.setVisibility(View.VISIBLE);
        }
    }
}