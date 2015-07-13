package com.example.jleung.igmetrics;

import android.app.Fragment;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.content.Intent;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import model.Images;
import retrofit.RestAdapter;
import retrofit.Callback;
import retrofit.android.MainThreadExecutor;
import retrofit.client.Response;
import retrofit.RetrofitError;

import API.igmetricsAPI;
import model.FollowedBy;
import model.User;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    TextView tv;
    WebView webView;
    ListView followersListView;
    ArrayAdapter followersListViewAdapter;
    HashMap<String, String> userNameToUserId = new HashMap<String, String>();

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

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    public void hideLoginView()
    {
        webView.setVisibility(View.GONE);
        tv.setVisibility(View.VISIBLE);
        followersListView.setVisibility(View.VISIBLE);
    }

    public void populateFollowersListView(String access_token)
    {
        RestAdapter restAdapter = new RestAdapter.Builder().setLogLevel(RestAdapter.LogLevel.FULL).setEndpoint(InstagramAPI.APIURL).build();
        igmetricsAPI ig = restAdapter.create(igmetricsAPI.class);
        ig.getFollowedBy(346074560, access_token, new Callback<FollowedBy>() {
            @Override
            public void success(FollowedBy followedBy, Response response) {
                ArrayList<String> list = new ArrayList<String>();
                for (User user : followedBy.getUsers())
                {
                    userNameToUserId.put(user.getUsername(), user.getId());
                    list.add(user.getUsername());
                }
                followersListViewAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, list);
                followersListView.setAdapter(followersListViewAdapter);
            }

            @Override
            public void failure(RetrofitError error) {
                tv.setText(error.toString());
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String authURLString = InstagramAPI.AUTHURL + "?client_id=" + InstagramAPI.client_id + "&redirect_uri=" + InstagramAPI.CALLBACKURL + "&response_type=code&display=touch&scope=likes+comments+relationships";

        tv = (TextView) getView().findViewById(R.id.textView);
        followersListView = (ListView) getView().findViewById(R.id.followersListView);
        followersListView.setOnItemClickListener(new OnItemClickListener() {
                                                     @Override
                                                     public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                                                         SharedPreferences sharedPrefs = getActivity().getPreferences(Context.MODE_PRIVATE);
                                                         String access_token = sharedPrefs.getString(getActivity().getString(R.string.access_token), "");
                                                         if (access_token != "") {

                                                             RestAdapter restAdapter = new RestAdapter.Builder().setLogLevel(RestAdapter.LogLevel.FULL).setEndpoint(InstagramAPI.APIURL).build();
                                                             igmetricsAPI ig = restAdapter.create(igmetricsAPI.class);
                                                             ArrayList<String> users = new ArrayList<String>();

                                                             String user = (String) followersListViewAdapter.getItem(position);
                                                             String userid = userNameToUserId.get(user);
                                                             users.add(new String(userid));

                                                             ig.likeUserPhotos(users, access_token, new Callback<Images>() {
                                                                 @Override
                                                                 public void success(Images images, Response response) {
                                                                     final List<String> likedImages = images.getImages();

                                                                     AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                                                     alertDialogBuilder.setTitle("Images liked");
                                                                     alertDialogBuilder
                                                                             .setMessage("Number of images liked: " + likedImages.size())
                                                                             .setCancelable(false);
                                                                     alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                         @Override
                                                                         public void onClick(DialogInterface dialog, int which) {
                                                                             Intent intent = new Intent(getActivity(), GalleryActivity.class);
                                                                             intent.putExtra("urls", likedImages.toArray());
                                                                             getActivity().startActivity(intent);
                                                                         }
                                                                     });
                                                                     AlertDialog alertDialog = alertDialogBuilder.create();
                                                                     alertDialog.show();
                                                                 }

                                                                 @Override
                                                                 public void failure(RetrofitError error) {
                                                                 }
                                                             });
                                                         }
                                                     }
                                                 });

        webView = (WebView) getView().findViewById(R.id.webView);

        webView.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading (WebView view, String url){
                    if (url.startsWith(InstagramAPI.CALLBACKURL)) {
                        String parts[] = url.split("=");
                        String request_token = parts[1];

                        new AsyncTask<String, Void, Void>() {
                            protected Void doInBackground(String... request_token) {
                                try {
                                    URL url = new URL(InstagramAPI.TOKENURL);
                                    HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
                                    httpsURLConnection.setRequestMethod("POST");
                                    httpsURLConnection.setDoInput(true);
                                    httpsURLConnection.setDoOutput(true);
                                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpsURLConnection.getOutputStream());
                                    outputStreamWriter.write("client_id=" + InstagramAPI.client_id +
                                            "&client_secret=" + InstagramAPI.client_secret +
                                            "&grant_type=authorization_code" +
                                            "&redirect_uri=" + InstagramAPI.CALLBACKURL +
                                            "&code=" + request_token[0]);

                                    outputStreamWriter.flush();
                                    String response = streamToString(httpsURLConnection.getInputStream());
                                    JSONObject jsonObject = (JSONObject) new JSONTokener(response).nextValue();
                                    String accessTokenString = jsonObject.getString("access_token");

                                    SharedPreferences sharedPrefs = getActivity().getPreferences(Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPrefs.edit();
                                    editor.putString(getActivity().getString(R.string.access_token), accessTokenString);
                                    editor.commit();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }

                            protected void onPostExecute(Void unused) {
                                try {
                                    SharedPreferences sharedPrefs = getActivity().getPreferences(Context.MODE_PRIVATE);
                                    String access_token = sharedPrefs.getString(getActivity().getString(R.string.access_token), "");
                                    if (access_token != "") {
                                        populateFollowersListView(access_token);
                                        hideLoginView();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
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
    }
}
