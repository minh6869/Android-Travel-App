package com.example.travelerapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.travelerapp.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;

public class AuthViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<Boolean> isAuthenticated = new MutableLiveData<>(false);
    private FirebaseAuth mAuth;

    public AuthViewModel() {
        userRepository = UserRepository.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize authentication state
        isAuthenticated.setValue(mAuth.getCurrentUser() != null);

        // Listen for auth changes
        mAuth.addAuthStateListener(firebaseAuth -> {
            isAuthenticated.setValue(firebaseAuth.getCurrentUser() != null);
        });
    }

    public LiveData<Boolean> isAuthenticated() {
        return isAuthenticated;
    }

    public void signOut() {
        mAuth.signOut();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up auth listener if needed
    }
}
