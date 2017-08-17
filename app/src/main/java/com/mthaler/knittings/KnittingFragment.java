package com.mthaler.knittings;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * KnittingFragment shows a single knitting
 *
 * It is used for adding new knittings or displaying / editing existing knittings
 */
public class KnittingFragment extends Fragment implements KnittingDetailsView {

    public static final String EXTRA_KNITTING_ID = "com.mthaler.knitting.KNITTING_ID";

    private static final String DIALOG_DATE = "date";
    private static final int REQUEST_STARTED = 0;
    private static final int REQUEST_FINISHED = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;

    private Knitting knitting = new Knitting(-1);

    private EditText editTextTitle;
    private EditText editTextDescription;
    private TextView textViewStarted;
    private TextView textViewFinished;
    private EditText editTextNeedleDiameter;
    private EditText editTextSize;
    private GridView gridView;
    private File currentPhotoPath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // crreate view
        final View v = inflater.inflate(R.layout.fragment_knitting, parent, false);

        // initialize title text field
        editTextTitle = v.findViewById(R.id.knitting_title);
        editTextTitle.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                knitting.setTitle(c.toString());
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // this space intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // this one too
            }
        });

        // initialize description text field
        editTextDescription = v.findViewById(R.id.knitting_description);
        editTextDescription.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                knitting.setDescription(c.toString());
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // this space intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // this one too
            }
        });

        textViewStarted = v.findViewById(R.id.knitting_started);
        textViewStarted.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final FragmentManager fm = getActivity().getSupportFragmentManager();
                final DatePickerFragment dialog = DatePickerFragment.newInstance(knitting.getStarted());
                dialog.setTargetFragment(KnittingFragment.this, REQUEST_STARTED);
                dialog.show(fm, DIALOG_DATE);
            }
        });

        // initialize finish date button
        textViewFinished = v.findViewById(R.id.knitting_finished);
        textViewFinished.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final FragmentManager fm = getActivity().getSupportFragmentManager();
                final DatePickerFragment dialog = DatePickerFragment.newInstance(knitting.getFinished() != null ? knitting.getFinished() : new Date());
                dialog.setTargetFragment(KnittingFragment.this, REQUEST_FINISHED);
                dialog.show(fm, DIALOG_DATE);
            }
        });

        editTextNeedleDiameter = v.findViewById(R.id.knitting_needle_diameter);
        editTextNeedleDiameter.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                try {
                    knitting.setNeedleDiameter(Double.parseDouble(c.toString()));
                } catch (Exception ex) {
                    knitting.setNeedleDiameter(0.0);
                }
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // this space intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                try {
                    knitting.setNeedleDiameter(Double.parseDouble(c.toString()));
                } catch (Exception ex) {
                    knitting.setNeedleDiameter(0.0);
                }
            }
        });

        editTextSize = v.findViewById(R.id.knitting_size);
        editTextSize.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                try {
                    knitting.setSize(Double.parseDouble(c.toString()));
                } catch (Exception ex) {
                    knitting.setSize(0.0);
                }
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // this space intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                try {
                    knitting.setSize(Double.parseDouble(c.toString()));
                } catch (Exception ex) {
                    knitting.setSize(0.0);
                }
            }
        });

        gridView = (GridView) v.findViewById(R.id.gridView);

        // initialize image view
//        imageView = v.findViewById(R.id.imageView);
//        imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                final File photoFile = KnittingsDataSource.getInstance(getActivity()).getPhotoFile(knitting);
//                final PackageManager packageManager = getActivity().getPackageManager();
//                boolean canTakePhoto = photoFile != null && takePictureIntent.resolveActivity(packageManager) != null;
//                if (canTakePhoto) {
//                    Uri uri = FileProvider.getUriForFile(getContext(), "com.mthaler.knittings.fileprovider", photoFile);
//                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//                }
//
//            }
//        });

        final Button buttonTakePhoto = v.findViewById(R.id.buttonTakePhoto);
        buttonTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        });

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        // we update the knitting in the database when onPause is called
        // this is the case when the activity is party hidden or if an other activity is started
        if (knitting != null) KnittingsDataSource.getInstance(getActivity()).updateKnitting(knitting);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_STARTED) {
            Date date = (Date)data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            knitting.setStarted(date);
            textViewStarted.setText(DateFormat.getDateInstance().format(knitting.getStarted()));
        } else if (requestCode == REQUEST_FINISHED) {
            Date date = (Date)data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            knitting.setFinished(date);
            textViewFinished.setText(DateFormat.getDateInstance().format(knitting.getFinished()));
        } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
            final File photoFile = currentPhotoPath;
            final Bitmap bitmap = PictureUtils.getScaledBitmap(photoFile.getPath(), getActivity());
            // add photo to database
            KnittingsDataSource.getInstance(getActivity()).createPhoto(currentPhotoPath, knitting.getId(), null);
        }
    }

    @Override
    public void init(Knitting knitting) {
        this.knitting = knitting;
        editTextTitle.setText(knitting.getTitle());
        editTextDescription.setText(knitting.getDescription());
        textViewStarted.setText(DateFormat.getDateInstance().format(knitting.getStarted()));
        textViewFinished.setText(knitting.getFinished() != null ? DateFormat.getDateInstance().format(knitting.getFinished()) : "");
        editTextNeedleDiameter.setText(Double.toString(knitting.getNeedleDiameter()));
        editTextSize.setText(Double.toString(knitting.getSize()));
        final List<Photo> photos = KnittingsDataSource.getInstance(getActivity()).getAllPhotos(knitting);
        GridViewAdapter gridAdapter = new GridViewAdapter(getActivity(), R.layout.grid_item_layout, photos);
        gridView.setAdapter(gridAdapter);
//        final File photoFile = KnittingsDataSource.getInstance(getActivity()).getPhotoFile(knitting);
//        if (photoFile.exists()) {
//            final Bitmap bitmap = PictureUtils.getScaledBitmap(photoFile.getPath(), getActivity());
//            imageView.setImageBitmap(bitmap);
//        }
    }

    /**
     * Deletes the displayed knitting
     *
     * The method will delete the knitting from the database and also remove the photo if it exists
     */
    public void deleteKnitting() {
        // if photo exists, delete it
        final File photoFile = KnittingsDataSource.getInstance(getActivity()).getPhotoFile(knitting);
        if (photoFile.exists()) {
            photoFile.delete();
        }
        // delete database entry
        KnittingsDataSource.getInstance(getActivity()).deleteKnitting(knitting);
        knitting = null;
    }
}
