package com.example.travelerapp.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.travelerapp.repository.PaymentRepository;

public class PaymentViewModelFactory implements ViewModelProvider.Factory {
    private final PaymentRepository repository;

    public PaymentViewModelFactory(PaymentRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(PaymentViewModel.class)) {
            return (T) new PaymentViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}