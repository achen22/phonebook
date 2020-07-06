package com.example.phonebook.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.lifecycle.MutableLiveData;

import com.example.phonebook.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PhonebookRemote {
    private final String TAG = getClass().getSimpleName();
    private final ContactEndpoint ENDPOINT;
    private static final PhonebookRemote INSTANCE = new PhonebookRemote();

    private MutableLiveData<String> message = new MutableLiveData<>();
    private ConnectivityManager connectivityManager;

    private PhonebookRemote() {
        String BASE_URL = BuildConfig.DEBUG
                ? "http://10.0.2.2:5000/api/"
                : "https://achen22.pythonanywhere.com/api/";
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .create();
        Retrofit RETROFIT = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
         ENDPOINT = RETROFIT.create(ContactEndpoint.class);
    }

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
     * @return false if message is empty, true otherwise
     */
    private boolean postMessage(String message) {
        this.message.postValue(message);
        if (message == null || message.isEmpty()) {
            return false;
        }
        Log.i(TAG, message);
        return true;
    }

    private boolean hasNetworkIssue() {
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
                return postMessage("Network cannot reach internet");
            }
        } else { // android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            if (activeNetwork == null) {
                return postMessage("No active network");
            } else if (!activeNetwork.isConnected()) {
                return postMessage("Network cannot make connections");
            }
        }

        return false;
    }

    /**
     * Check if API server responds successfully. Cannot be run on UI thread.
     * @return true if server response is successful, false otherwise
     */
    public boolean canConnectToBaseUrl() {
        try {
            return ENDPOINT.head().execute().isSuccessful();
        } catch (IOException e) {
            Log.e(TAG, "canConnectToBaseUrl: ", e);
            return false;
        }
    }

    public void all(@NonNull Consumer<List<Contact>> onSuccess) {
        if (hasNetworkIssue()) {
            return;
        }

        ENDPOINT.all().enqueue(new Callback<List<Contact>>() {
            @Override
            public void onResponse(@NonNull Call<List<Contact>> call,
                                   @NonNull Response<List<Contact>> response) {
                if (response.isSuccessful()) {
                    onSuccess.accept(response.body());
                } else {
                    onBadResponse("all", response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Contact>> call, @NonNull Throwable t) {
                PhonebookRemote.this.onFailure("all", t);
            }
        });
    }

    public void insert(@NonNull Contact contact, @NonNull Consumer<Contact> onSuccess) {
        if (hasNetworkIssue()) {
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

    public void update(Contact contact, @NonNull Runnable onSuccess, Runnable onNotFound) {
        if (hasNetworkIssue()) {
            return;
        }

        ENDPOINT.update(contact.getId(), contact).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    onSuccess.run();
                } else {
                    AsyncTask.execute(() -> {
                        if (!canConnectToBaseUrl()) {
                            onBadResponse("update", response);
                        } else if (response.code() == 404 && onNotFound != null) {
                            onNotFound.run();
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                PhonebookRemote.this.onFailure("update", t);
            }
        });
    }

    public void delete(long id, @NonNull Consumer<Contact> onSuccess) {
        if (hasNetworkIssue()) {
            return;
        }

        ENDPOINT.delete(id).enqueue(new Callback<Contact>() {
            @Override
            public void onResponse(@NonNull Call<Contact> call,
                                   @NonNull Response<Contact> response) {
                if (response.isSuccessful()) {
                    onSuccess.accept(response.body());
                } else {
                    AsyncTask.execute(() -> {
                        if (!canConnectToBaseUrl()) {
                            onBadResponse("delete", response);
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<Contact> call, @NonNull Throwable t) {
                PhonebookRemote.this.onFailure("delete", t);
            }
        });
    }

    private void onBadResponse(String method, Response<?> response) {
        String prefix = method == null || method.isEmpty()
                ? ""
                : method + "().";
        String msg = String.format("%sonBadResponse: got [%d] %s from %s",
                prefix, response.code(), response.message(), response.raw().request().url());
        Log.e(TAG, msg);
        postMessage("Server response error");
    }

    private void onFailure(String method, Throwable t) {
        String prefix = method == null || method.isEmpty()
                ? ""
                : method + "().";
        Log.e(TAG, prefix + "onFailure: ", t);
        postMessage("Communication error");
    }
}
