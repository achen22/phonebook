package com.example.phonebook.data;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PhonebookRemote {
    private static final String BASE_URL = "http://10.0.2.2/api/";
    private static final Retrofit RETROFIT = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    private static final ContactEndpoint ENDPOINT = RETROFIT
            .create(ContactEndpoint.class);
    private static final PhonebookRemote INSTANCE = new PhonebookRemote();

    private PhonebookRemote() {}

    public static PhonebookRemote getInstance() {
        return INSTANCE;
    }

    public ContactEndpoint getEndpoint() {
        return ENDPOINT;
    }
}
