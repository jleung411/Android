package com.example.jleung.igmetrics;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.content.Intent;


/**
 * A placeholder fragment containing a simple view.
 */
public class GalleryActivityFragment extends Fragment {

    ListView thumbsListView;

    public GalleryActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gallery, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        String[] urls;
        if (savedInstanceState == null) {
            Intent intent = getActivity().getIntent();
            if(intent == null) {
                urls = null;
            } else {
                urls = intent.getStringArrayExtra("urls");
            }
        } else {
            urls = (String[]) savedInstanceState.getSerializable("urls");
        }

        thumbsListView = (ListView) getView().findViewById(R.id.thumbsListView);
        CustomListAdapter adapter = new CustomListAdapter(getActivity(), urls);
        thumbsListView.setAdapter(adapter);
    }
}
