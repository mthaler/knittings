package com.mthaler.knittings;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import java.io.File;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {

    public static final String CURRENT_PHOTO_PATH = "current_photo_path";
    public static final String KNITTING_ID = "knitting_id";
    private static final int REQUEST_IMAGE_CAPTURE = 0;

    private Knitting knitting = new Knitting(-1);

    private GridView gridView;
    private File currentPhotoPath;

    public static PhotoGalleryFragment newInstance(Knitting knitting) {
        final PhotoGalleryFragment fragment = new PhotoGalleryFragment();
        Bundle args = new Bundle();
        args.putLong(KNITTING_ID, knitting.getId());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(CURRENT_PHOTO_PATH)) {
                currentPhotoPath = new File(savedInstanceState.getString(CURRENT_PHOTO_PATH));
            }
            if (savedInstanceState.containsKey(KNITTING_ID)) {
                knitting = KnittingsDataSource.getInstance(getActivity()).getKnitting(savedInstanceState.getLong(KNITTING_ID));
            }
        } else {
            final long knittingID = getArguments().getLong(KNITTING_ID);
            knitting = KnittingsDataSource.getInstance(getActivity()).getKnitting(knittingID);
        }

        gridView = v.findViewById(R.id.gridView);
        final List<Photo> photos = KnittingsDataSource.getInstance(getActivity()).getAllPhotos(knitting);
        final GridViewAdapter gridAdapter = new GridViewAdapter(getActivity(), R.layout.grid_item_layout, photos);
        gridView.setAdapter(gridAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            if (position < gridView.getAdapter().getCount() - 1) {
                final Photo photo = (Photo) parent.getItemAtPosition(position);
                //Create intent
                final Intent intent = new Intent(getActivity(), PhotoActivity.class);
                intent.putExtra(PhotoActivity.EXTRA_PHOTO_ID, photo.getId());
                intent.putExtra(KnittingActivity.EXTRA_KNITTING_ID, knitting.getId());

                //Start details activity
                startActivity(intent);
            } else {
                final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                final File photoFile = KnittingsDataSource.getInstance(getActivity()).getPhotoFile(knitting);
                currentPhotoPath = photoFile;
                final PackageManager packageManager = getActivity().getPackageManager();
                boolean canTakePhoto = photoFile != null && takePictureIntent.resolveActivity(packageManager) != null;
                if (canTakePhoto) {
                    Uri uri = FileProvider.getUriForFile(getContext(), "com.mthaler.knittings.fileprovider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            // add photo to database
            final int orientation = PictureUtils.getOrientation(currentPhotoPath.getAbsolutePath());
            final Bitmap preview = PictureUtils.decodeSampledBitmapFromPath(currentPhotoPath.getAbsolutePath(), 200, 200);
            final Bitmap rotatedPreview = PictureUtils.rotateBitmap(preview, orientation);
            Photo photo = KnittingsDataSource.getInstance(getActivity()).createPhoto(currentPhotoPath, knitting.getId(), rotatedPreview, "");
            // add first photo as default photo
            if (knitting.getDefaultPhoto() == null) {
                knitting.setDefaultPhoto(photo);
            }
            // update grid view
            final List<Photo> photos = KnittingsDataSource.getInstance(getActivity()).getAllPhotos(knitting);
            final GridViewAdapter gridAdapter = new GridViewAdapter(getActivity(), R.layout.grid_item_layout, photos);
            gridView.setAdapter(gridAdapter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (knitting != null) {
            final List<Photo> photos = KnittingsDataSource.getInstance(getActivity()).getAllPhotos(knitting);
            final GridViewAdapter gridAdapter = new GridViewAdapter(getActivity(), R.layout.grid_item_layout, photos);
            gridView.setAdapter(gridAdapter);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (currentPhotoPath != null) {
            outState.putString(CURRENT_PHOTO_PATH, currentPhotoPath.getAbsolutePath());
        }
        if (knitting != null) {
            outState.putLong(KNITTING_ID, knitting.getId());
        }
        super.onSaveInstanceState(outState);
    }
}
