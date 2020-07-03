package com.example.phonebook.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PhonebookRemote {
    private final String TAG = getClass().getSimpleName();
    private final String BASE_URL = "http://10.0.2.2:5000/api/";
    private final Retrofit RETROFIT = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    private final ContactEndpoint ENDPOINT = RETROFIT
            .create(ContactEndpoint.class);
    private static final PhonebookRemote INSTANCE = new PhonebookRemote();

    private ConnectivityManager connectivityManager;

    private PhonebookRemote() {}

    public static PhonebookRemote getInstance(Context context) {
        if (INSTANCE.connectivityManager == null) {
            INSTANCE.connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        return INSTANCE;
    }

    public boolean isConnected() {
        if (connectivityManager == null) {
            return false;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Network activeNetwork = connectivityManager.getActiveNetwork();
            if (activeNetwork == null) {
                return false;
            }

            NetworkCapabilities capabilities =
                    connectivityManager.getNetworkCapabilities(activeNetwork);
            return capabilities != null
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        }

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public void all(@NonNull Consumer<List<Contact>> onSuccess,
                    @Nullable Consumer<Response<?>> onFailure) {
        ENDPOINT.all().enqueue(new Callback<List<Contact>>() {
            @Override
            public void onResponse(@NonNull Call<List<Contact>> call,
                                   @NonNull Response<List<Contact>> response) {
                if (response.isSuccessful()) {
                    onSuccess.accept(response.body());
                } else if (onFailure != null) {
                    onFailure.accept(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Contact>> call, @NonNull Throwable t) {
                if (onFailure != null) {
                    onFailure.accept(null);
                }
            }
        });
    }

    public void insert(Contact contact) {

    }

    public void update(Contact contact) {

    }

    public void delete(int id) {

    }
}
