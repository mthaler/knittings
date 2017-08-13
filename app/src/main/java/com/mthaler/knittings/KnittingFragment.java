package com.mthaler.knittings;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import java.io.File;
import java.text.DateFormat;

/**
 * KnittingFragment shows a single knitting
 *
 * It is used for adding new knittings or displaying / editing existing knittings
 */
public class KnittingFragment extends Fragment {

    public static final String EXTRA_KNITTING_ID = "com.mthaler.knitting.KNITTING_ID";

    private static final String DIALOG_DATE = "date";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private Knitting knitting;
    private ImageView imageView;

    /**
     * Creates a new knitting fragment and attaches the given knitting id
     *
     * @param id knitting id
     * @return new knitting fragment with knitting id attached
     */
    public static KnittingFragment newInstance(long id) {
        Bundle args = new Bundle();
        args.putLong(EXTRA_KNITTING_ID, id);

        final KnittingFragment fragment = new KnittingFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the attached knitting id
        final long id = getArguments().getLong(EXTRA_KNITTING_ID);
        // get knitting for the given id from database
        knitting = KnittingsDataSource.getInstance(getActivity()).getKnitting(id);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // crreate view
        final View v = inflater.inflate(R.layout.fragment_knitting, parent, false);

        // initialize title text field
        final EditText textFieldTitle  = v.findViewById(R.id.knitting_title);
        textFieldTitle.setText(knitting.getTitle());
        textFieldTitle.addTextChangedListener(new TextWatcher() {
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
        final EditText textFieldDescription = v.findViewById(R.id.knitting_description);
        textFieldDescription.setText(knitting.getDescription());
        textFieldDescription.addTextChangedListener(new TextWatcher() {
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

        final EditText editTextgStarted = v.findViewById(R.id.knitting_started);
        editTextgStarted.setText(DateFormat.getDateInstance().format(knitting.getStarted()));
        editTextgStarted.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentManager fm = getActivity()
                        .getSupportFragmentManager();
                DatePickerFragment dialog = DatePickerFragment
                        .newInstance(knitting.getStarted());
                dialog.setTargetFragment(KnittingFragment.this, REQUEST_DATE);
                dialog.show(fm, DIALOG_DATE);
            }
        });

        // initialize finish date button
        final EditText editTextFinished = v.findViewById(R.id.knitting_finished);
        editTextFinished.setText(knitting.getFinished() != null ? knitting.getFinished().toString() : "");
        editTextFinished.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                FragmentManager fm = getActivity()
//                        .getSupportFragmentManager();
//                DatePickerFragment dialog = DatePickerFragment
//                        .newInstance(mCrime.getDate());
//                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
//                dialog.show(fm, DIALOG_DATE);
            }
        });

        final EditText editTextNeedleDiameter = v.findViewById(R.id.knitting_needle_diameter);
        editTextNeedleDiameter.setText(Double.toString(knitting.getNeedleDiameter()));
        textFieldDescription.addTextChangedListener(new TextWatcher() {
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
                // this one too
            }
        });

        // initialize image view
        imageView = v.findViewById(R.id.imageView);
        // if there is a photo, display it
        final File photoFile = KnittingsDataSource.getInstance(getActivity()).getPhotoFile(knitting);
        if (photoFile.exists()) {
            final Bitmap bitmap = PictureUtils.getScaledBitmap(photoFile.getPath(), getActivity());
            imageView.setImageBitmap(bitmap);
        }
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                final File photoFile = KnittingsDataSource.getInstance(getActivity()).getPhotoFile(knitting);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_knitting, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_delete_knitting:
                // if photo exists, delete it
                final File photoFile = KnittingsDataSource.getInstance(getActivity()).getPhotoFile(knitting);
                if (photoFile.exists()) {
                    photoFile.delete();
                }
                // delete database entry
                KnittingsDataSource.getInstance(getActivity()).deleteKnitting(knitting);
                getActivity().finish();
                knitting = null;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            final File photoFile = KnittingsDataSource.getInstance(getActivity()).getPhotoFile(knitting);
            final Bitmap bitmap = PictureUtils.getScaledBitmap(photoFile.getPath(), getActivity());
            imageView.setImageBitmap(bitmap);
        }
    }
}
