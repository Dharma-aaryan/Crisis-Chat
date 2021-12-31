package com.sir.crisischat.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.auth.User;
import com.sir.crisischat.Adapters.UsersAdapter;
import com.sir.crisischat.Listeners.UserListener;
import com.sir.crisischat.Models.Users;
import com.sir.crisischat.R;
import com.sir.crisischat.Utilities.Constants;
import com.sir.crisischat.Utilities.PreferenceManager;
import com.sir.crisischat.databinding.ActivityMainBinding;
import com.sir.crisischat.databinding.ActivityUsersBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;

public class UsersActivity extends BaseActivity implements UserListener {
    private ActivityUsersBinding activityUsersBinding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityUsersBinding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(activityUsersBinding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());

        setListeners();
        getUsers();
    }


    private void setListeners(){
        activityUsersBinding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void getUsers(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USERID);
                    if (task.isSuccessful() && task.getResult() != null){
                        List<Users> users = new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            Users user = new Users();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if (users.size() > 0) {
                            UsersAdapter usersAdapter = new UsersAdapter(users, this);
                            activityUsersBinding.userRecyclerView.setAdapter(usersAdapter);
                            activityUsersBinding.userRecyclerView.setVisibility(View.VISIBLE);
                        }
                        else{
                            showErrorMessage();
                        }
                    }
                    else {
                        showErrorMessage();
                    }
                });
    }

    private void showErrorMessage(){
        activityUsersBinding.textErrorMessage.setText(String.format("%s","No user Available"));
        activityUsersBinding.textErrorMessage.setVisibility(View.VISIBLE);
    }
    
    private void loading(Boolean isLoading){
        if(isLoading){
            activityUsersBinding.progressBar.setVisibility(View.VISIBLE);
        }
        else{
             activityUsersBinding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(Users user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }
}