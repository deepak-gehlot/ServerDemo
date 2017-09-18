package com.saver.android.bindingadapter;

import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * Created by RWS 6 on 7/20/2017.
 */

public class ImageBinding {

    @BindingAdapter({"bind:imageFile"})
    public static void loadImageFromFile(ImageView imageView, File file) {
        Glide.with(imageView.getContext())
                .load(file)
                .override(350, 450)
                .into(imageView);

       /* Glide
                .with(imageView.getContext())
                .load(file)
                .override(500, 500) // resizes the image to these dimensions (in pixel)
                .centerCrop() // this cropping technique scales the image so that it fills the requested bounds and then crops the extra.
                .into(imageView);*/
    }

    @BindingAdapter({"bind:imageFileBlur"})
    public static void loadImageFromFileBlur(ImageView imageView, File file) {
        Glide.with(imageView.getContext())
                .load(file)
                .bitmapTransform(new BlurTransformation(imageView.getContext(), 30, 10))
                .into(imageView);
    }
}
