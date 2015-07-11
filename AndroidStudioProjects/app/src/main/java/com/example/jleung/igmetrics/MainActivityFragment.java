package com.example.jleung.igmetrics;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;


import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import retrofit.RestAdapter;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.RetrofitError;

import API.igmetricsAPI;
import model.followedByModel;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    TextView tv;
    WebView webView;

    public MainActivityFragment() {
    }

    public String streamToString(InputStream is) throws IOException {
        String string = "";

        if (is != null) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is));

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                reader.close();
            } finally {
                is.close();
            }

            string = stringBuilder.toString();
        }

        return string;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String authURLString = InstagramAPI.AUTHURL + "?client_id=" + InstagramAPI.client_id + "&redirect_uri=" + InstagramAPI.CALLBACKURL + "&response_type=code&display=touch&scope=likes+comments+relationships";

        tv = (TextView) getView().findViewById(R.id.helloworld);

        webView = (WebView) getView().findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith(InstagramAPI.CALLBACKURL)) {
                    String parts[] = url.split("=");
                    String request_token = parts[1];

                    new AsyncTask<String, Void, Void>() {
                        protected Void doInBackground(String... request_token)
                        {
                            try
                            {
                                URL url = new URL(InstagramAPI.TOKENURL);
                                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
                                httpsURLConnection.setRequestMethod("POST");
                                httpsURLConnection.setDoInput(true);
                                httpsURLConnection.setDoOutput(true);
                                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpsURLConnection.getOutputStream());
                                outputStreamWriter.write("client_id="+InstagramAPI.client_id+
                                        "&client_secret="+ InstagramAPI.client_secret +
                                        "&grant_type=authorization_code" +
                                        "&redirect_uri=" + InstagramAPI.CALLBACKURL+
                                        "&code=" + request_token[0]);

                                outputStreamWriter.flush();
                                String response = streamToString(httpsURLConnection.getInputStream());
                                JSONObject jsonObject = (JSONObject) new JSONTokener(response).nextValue();
                                String accessTokenString = jsonObject.getString("access_token");

                                SharedPreferences sharedPrefs = getActivity().getPreferences(Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPrefs.edit();
                                editor.putString(getActivity().getString(R.string.access_token), accessTokenString);
                                editor.commit();
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                            return null;
                        }
                        protected void onPostExecute(Void unused) {
                            SharedPreferences sharedPrefs = getActivity().getPreferences(Context.MODE_PRIVATE);
                            String access_token = sharedPrefs.getString(getActivity().getString(R.string.access_token), "");
                            if (access_token != "") {
                                tv.setText(access_token);
                                tv.setVisibility(View.VISIBLE);
                                webView.setVisibility(View.GONE);
                            }
                        }
                    }.execute(request_token);

                    return true;
                }
                return false;
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(authURLString);




        RestAdapter restAdapter = new RestAdapter.Builder().setLogLevel(RestAdapter.LogLevel.FULL).setEndpoint(InstagramAPI.APIURL).build();
        igmetricsAPI igmetrics = restAdapter.create(igmetricsAPI.class);
    }
}
