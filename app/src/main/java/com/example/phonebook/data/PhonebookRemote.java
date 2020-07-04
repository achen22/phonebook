package com.example.phonebook.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
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

    private MutableLiveData<String> message = new MutableLiveData<>();
    private ConnectivityManager connectivityManager;

    private PhonebookRemote() {}

    public static PhonebookRemote getInstance(Context context) {
        if (INSTANCE.connectivityManager == null) {
            INSTANCE.connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        return INSTANCE;
    }

    public MutableLiveData<String> getMessage() {
        return message;
    }

    /**
     * Logs a message and posts it to LiveData. Use this to notify Observers when a Call fails.
     * @param message the message to log and post to LiveData
     * @return true if message is empty, false otherwise
     */
    private boolean postMessage(String message) {
        this.message.postValue(message);
        if (message == null || message.isEmpty()) {
            return true;
        }
        Log.i(TAG, message);
        return false;
    }

    private boolean hasInternet() {
        if (connectivityManager == null) {
            return postMessage("connectivityManager is null");
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Network activeNetwork = connectivityManager.getActiveNetwork();
            if (activeNetwork == null) {
                return postMessage("No active network");
            }

            NetworkCapabilities capabilities =
                    connectivityManager.getNetworkCapabilities(activeNetwork);
            if (capabilities == null) {
                return postMessage("Unknown network");
            } else if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                return postMessage("Unknown network");
            }
        } else {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            if (activeNetwork == null) {
                return postMessage("No active network");
            } else if (!activeNetwork.isConnected()) {
                return postMessage("Network cannot make connections");
            }
        }

        return true;
    }

    /**
     * Check if endpoint server responds successfully.
     * @return true if server response if successful, false otherwise
     */
    private boolean head() {
        try {
            return ENDPOINT.head().execute().isSuccessful();
        } catch (IOException e) {
            Log.e(TAG, "isConnected: ", e);
            return false;
        }
    }

    public void all(@NonNull Consumer<List<Contact>> onSuccess) {
        if (!hasInternet()) {
            return;
        }

        ENDPOINT.all().enqueue(new Callback<List<Contact>>() {
            @Override
            public void onResponse(@NonNull Call<List<Contact>> call,
                                   @NonNull Response<List<Contact>> response) {
                if (response.isSuccessful()) {
                    onSuccess.accept(response.body());
                } else {
                    onBadResponse("all", response, "Server response error");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Contact>> call, @NonNull Throwable t) {
                PhonebookRemote.this.onFailure("all", t, "Communication error");
            }
        });
    }

    public void insert(@NonNull Contact contact, @NonNull Consumer<Contact> onSuccess) {
        if (!hasInternet()) {
            return;
        }

        ENDPOINT.insert(contact).enqueue(new Callback<Contact>() {
            @Override
            public void onResponse(@NonNull Call<Contact> call,
                                   @NonNull Response<Contact> response) {
                if (response.isSuccessful()) {
                    onSuccess.accept(response.body());
                } else {
                    onBadResponse("insert", response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Contact> call, @NonNull Throwable t) {
                PhonebookRemote.this.onFailure("insert", t);
            }
        });
    }

    public void update(Contact contact) {
        if (!hasInternet()) {
            return;
        }

    }

    public void delete(long id) {
        if (!hasInternet()) {
            return;
        }

    }

    private void onFailure(String method, Throwable t) {
        onFailure(method, t, null);
    }

    private void onFailure(String method, Throwable t, String message) {
        String prefix = method == null || method.isEmpty()
                ? ""
                : method + "().";
        Log.e(TAG, prefix + "onFailure: ", t);
        postMessage(message);
    }

    private void onBadResponse(String method, Response<?> response) {
        onBadResponse(method, response, null);
    }

    private void onBadResponse(String method, Response<?> response, String message) {
        String prefix = method == null || method.isEmpty()
                ? ""
                : method + "().";
        String msg = String.format("%sonBadResponse: get [%d] %s from %s",
                prefix, response.code(), response.message(), response.raw().request().url());
        Log.e(TAG, msg);
        postMessage(message);
    }
}
