package com.example.phonebook.data;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ContactEndpoint {
    @HEAD
    Call<Void> head();

    @GET("Contact/")
    Call<List<Contact>> all();

    @POST("Contact/")
    Call<Contact> insert(@Body Contact contact);

    @GET("Contact/{id}")
    Call<Contact> get(@Path("id") long id);

    @PUT("Contact/{id}")
    Call<Void> update(@Path("id") long id, @Body Contact contact);

    @DELETE("Contact/{id}")
    Call<Contact> delete(@Path("id") long id);
}
