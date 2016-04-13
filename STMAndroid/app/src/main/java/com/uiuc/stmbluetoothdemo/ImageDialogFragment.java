package com.uiuc.stmbluetoothdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

/**
 * Created by chrx on 4/13/16.
 */
public class ImageDialogFragment extends DialogFragment {
    String path;

    /**
     * Returns an instance of the image Dialog Fragment, when you pass in the path
     * @param path
     * @return
     */
    public static ImageDialogFragment newInstance(String path) {
        ImageDialogFragment frag = new ImageDialogFragment();

        Bundle args = new Bundle();         //Set an argument bundle for the fragment, for when it gets displayed
        args.putString("Image_Path", path);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        path = getArguments().getString("Image_Path");
        SubsamplingScaleImageView imageView = new SubsamplingScaleImageView(getActivity());
        imageView.setImage(ImageSource.uri(path));
        imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return imageView;
    }


    /**
     * Opens up the requested image from the file directory, and returns it
     * @return
     */
    public Bitmap getBitmap() {
        BitmapFactory.Options option = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(path, option);
        return bitmap;
    }
}
