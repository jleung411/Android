package com.example.jleung.igmetrics;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import retrofit.RestAdapter;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.RetrofitError;

import API.igmetricsAPI;
import model.igmetricsModel;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    TextView tv;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String API = "https://api.github.com";
        RestAdapter restAdapter = new RestAdapter.Builder().setLogLevel(RestAdapter.LogLevel.FULL).setEndpoint(API).build();
        igmetricsAPI igmetrics = restAdapter.create(igmetricsAPI.class);

        tv = (TextView) getView().findViewById(R.id.helloworld);

        igmetrics.getFeed("jleung411", new Callback<igmetricsModel>() {
            @Override
            public void success(igmetricsModel igmetrics, Response response) {
                tv.setText("Github Name :" + igmetrics.getName() + "\nWebsite :" + igmetrics.getBlog() + "\nCompany Name :" + igmetrics.getCompany());
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });
    }
}
