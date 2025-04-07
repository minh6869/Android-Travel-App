// com.example.travelerapp.repository.UserRepository.java
package com.example.travelerapp.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.travelerapp.model.User;

public class UserRepository {
    private static UserRepository instance;
    private MutableLiveData<User> currentUser = new MutableLiveData<>();

    private UserRepository() {
        // Initialize repository, possibly with a database or API service
    }

    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> login(String email, String password) {
        MutableLiveData<Boolean> loginResult = new MutableLiveData<>();

        // Mock implementation - replace with actual authentication logic
        if (email != null && password != null && !email.isEmpty() && !password.isEmpty()) {
            User user = new User();
            user.setEmail(email);
            // Set other user details after successful login
            currentUser.setValue(user);
            loginResult.setValue(true);
        } else {
            loginResult.setValue(false);
        }

        return loginResult;
    }

    public LiveData<Boolean> signup(String username, String email, String password) {
        MutableLiveData<Boolean> signupResult = new MutableLiveData<>();

        // Mock implementation - replace with actual registration logic
        if (username != null && email != null && password != null &&
                !username.isEmpty() && !email.isEmpty() && !password.isEmpty()) {

            User newUser = new User(username, email, password);
            // Save user to database or remote service
            currentUser.setValue(newUser);
            signupResult.setValue(true);
        } else {
            signupResult.setValue(false);
        }

        return signupResult;
    }

    public void logout() {
        currentUser.setValue(null);
    }
}