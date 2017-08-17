package com.mthaler.knittings;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PhotoFragment extends Fragment implements PhotoDetailsView {

    private Photo photo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // crreate view
        final View v = inflater.inflate(R.layout.fragment_photo, parent, false);
        return v;
    }

    @Override
    public void init(Photo photo) {

    }

    @Override
    public void deletePhoto() {

    }
}
