package com.uiuc.stmbluetoothdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by chrx on 4/10/16.
 */
public class DetailedSavedScanFragment  extends Fragment {
    String name;
    String date = "Saved on: ";
    String filepath;
    View myView;

    public DetailedSavedScanFragment() {

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        name = args.getString("name");
        date += args.getString("date");
        filepath = args.getString("file_path");
        Log.v("Detailed View", filepath);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.detailed_saved_scan_fragment, container, false);

        ImageView imageView = (ImageView) myView.findViewById(R.id.imageView);
        TextView nameView = (TextView) myView.findViewById(R.id.name);
        TextView dateView = (TextView) myView.findViewById(R.id.time);

        imageView.setImageBitmap(getBitmap());
        nameView.setText(name);
        dateView.setText(date);
        return myView;
    }

    public Bitmap getBitmap() {
        BitmapFactory.Options option = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(filepath, option);
        return bitmap;
    }
}
