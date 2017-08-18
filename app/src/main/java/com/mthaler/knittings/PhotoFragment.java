package com.mthaler.knittings;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * PhotoFragment displays a photo and the description
 */
public class PhotoFragment extends Fragment implements PhotoDetailsView {

    private Photo photo;

    private ImageView imageView;
    private EditText editTextDescription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_photo, parent, false);

        imageView = v.findViewById(R.id.image);

        // initialize description edit text
        editTextDescription = v.findViewById(R.id.photo_description);
        editTextDescription.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                photo.setDescription(c.toString());
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // this space intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // this one too
            }
        });

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        // we update the photo in the database when onPause is called
        // this is the case when the activity is party hidden or if an other activity is started
        if (photo != null) KnittingsDataSource.getInstance(getActivity()).updatePhoto(photo);
    }

    @Override
    public void init(Photo photo) {
        this.photo = photo;
        // we use a view tree observer to get the width and the height of the image view and scale the image accordingly reduce memory usage
        final ViewTreeObserver viewTreeObserver = imageView.getViewTreeObserver();
        viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                PhotoFragment.this.imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                final int width = imageView.getMeasuredWidth();
                final int height = imageView.getMeasuredHeight();
                // loading and scaling the bitmap is expensive, use async task to do the work
                final AsyncTask<String, Void, Bitmap> scaleBitmapTask = new AsyncTask<String, Void, Bitmap>() {
                    @Override
                    protected Bitmap doInBackground(String... strings) {
                        return PictureUtils.decodeSampledBitmapFromPath(strings[0], width, height);
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                        }
                    }
                };
                scaleBitmapTask.execute(PhotoFragment.this.photo.getFilename().getAbsolutePath());
                return true;
            }
        });
        editTextDescription.setText(photo.getDescription());
    }

    @Override
    public void deletePhoto() {
        // check if the photo is used as default photo
        Knitting knitting = KnittingsDataSource.getInstance(getActivity()).getKnitting(photo.getKnittingID());
        if (knitting.getDefaultPhoto() != null && knitting.getDefaultPhoto().getId() == photo.getId()) {
            knitting.setDefaultPhoto(null);
            KnittingsDataSource.getInstance(getActivity()).updateKnitting(knitting);
        }
        // delete database entry
        KnittingsDataSource.getInstance(getActivity()).deletePhoto(photo);
        photo = null;
    }
}
