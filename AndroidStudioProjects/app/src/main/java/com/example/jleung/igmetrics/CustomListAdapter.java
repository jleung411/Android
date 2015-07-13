package com.example.jleung.igmetrics;


import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import java.io.InputStream;
import java.net.URL;

public class CustomListAdapter extends ArrayAdapter<String> {

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urlname = urls[0];
            Bitmap bmp = null;
            try {
                URL url = new URL(urlname);
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bmp;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    private final Activity context;
    private final String[] urls;

    public CustomListAdapter(Activity context, String[] urls) {
        super(context, R.layout.gallery, urls);

        this.context=context;
        this.urls=urls;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.gallery, null, true);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
        new DownloadImageTask((ImageView) rowView.findViewById(R.id.imageView)).execute(urls);

        return rowView;
    };
}