package com.uiuc.stmbluetoothdemo;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
     * @param path The path to the image
     * @return The ImageDialog Fragment
     */
    public static ImageDialogFragment newInstance(String path) {
        ImageDialogFragment frag = new ImageDialogFragment();

        Bundle args = new Bundle();         //Set an argument bundle for the fragment, for when it gets displayed
        args.putString("Image_Path", path);
        frag.setArguments(args);
        return frag;
    }

    /**
     * Returns an instance of the image Dialog Fragment, when you pass in a bitmap
     * @param bm The Bitmap
     * @return The ImageDialog Fragment
     */
    public static ImageDialogFragment newInstance(Bitmap bm) {
        ImageDialogFragment frag = new ImageDialogFragment();

        Bundle args = new Bundle();         //Set an argument bundle for the fragment, for when it gets displayed
        args.putString("Image_Path", "");
        args.putParcelable("BM", bm);
        frag.setArguments(args);
        return frag;
    }

    /**
     * Called when the dialog is Created
     * @param savedInstanceState
     * @return
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    /**
     * Called automatically when the image dialog is started
     */
    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);  //Set dialog to match the size of the parent
        }
    }

    /**
     * Opens up a dialog fragment with a SubsamplingScaleImageView - https://github.com/davemorrissey/subsampling-scale-image-view
     * Essentially a dialog with an image view that you can zoom in and out on
     * @param inflater      The layout infaltor for the dialog
     * @param container     The ViewGroup for the dialog
     * @param savedInstanceState    The previosu state
     * @return          The view that is created
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        path = getArguments().getString("Image_Path");      //Get the passed in image path variable
        SubsamplingScaleImageView imageView = new SubsamplingScaleImageView(getActivity());
        if(!path.equals("")) {
            imageView.setImage(ImageSource.uri(path));      //Set the view to be the image at the path
            imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        } else {
            Bitmap bm = (Bitmap) getArguments().getParcelable("BM");    //If a bitmap with passed in instead, set that to be the view
            imageView.setImage(ImageSource.bitmap(bm));
        }
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
