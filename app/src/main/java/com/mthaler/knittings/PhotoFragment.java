package com.mthaler.knittings;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class PhotoFragment extends Fragment implements PhotoDetailsView {

    private Photo photo;

    private ImageView imageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // crreate view
        final View v = inflater.inflate(R.layout.fragment_photo, parent, false);

        imageView = (ImageView) v.findViewById(R.id.image);

        return v;
    }

    @Override
    public void init(Photo photo) {
        this.photo = photo;
        imageView.setImageBitmap(PictureUtils.getScaledBitmap(photo.getFilename().getAbsolutePath(), getActivity()));
    }

    @Override
    public void deletePhoto() {
        // delete database entry
        KnittingsDataSource.getInstance(getActivity()).deletePhoto(photo);
        photo = null;
    }
}
