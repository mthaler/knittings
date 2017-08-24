package com.mthaler.knittings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import java.text.DateFormat;
import java.util.Date;

/**
 * KnittingFragment shows a single knitting
 *
 * It is used for adding new knittings or displaying / editing existing knittings
 */
public class KnittingFragment extends Fragment {

    public static final String KNITTING_ID = "knitting_id";

    private static final String DIALOG_DATE = "date";
    private static final int REQUEST_STARTED = 0;
    private static final int REQUEST_FINISHED = 1;

    private Knitting knitting = new Knitting(-1);

    private TextView textViewStarted;
    private TextView textViewFinished;
    private EditText editTextSize;

    public static KnittingFragment newInstance(Knitting knitting) {
        final KnittingFragment fragment = new KnittingFragment();
        Bundle args = new Bundle();
        args.putLong(KNITTING_ID, knitting.getId());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // crreate view
        final View v = inflater.inflate(R.layout.fragment_knitting, parent, false);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KNITTING_ID)) {
                knitting = KnittingsDataSource.getInstance(getActivity()).getKnitting(savedInstanceState.getLong(KNITTING_ID));
            }
        } else {
            final long knittingID = getArguments().getLong(KNITTING_ID);
            knitting = KnittingsDataSource.getInstance(getActivity()).getKnitting(knittingID);
        }

        // initialize title text field
        final EditText  editTextTitle = v.findViewById(R.id.knitting_title);
        editTextTitle.setText(knitting.getTitle());
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
        final EditText editTextDescription = v.findViewById(R.id.knitting_description);
        editTextDescription.setText(knitting.getDescription());
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
        textViewStarted.setText(DateFormat.getDateInstance().format(knitting.getStarted()));
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
        textViewFinished.setText(knitting.getFinished() != null ? DateFormat.getDateInstance().format(knitting.getFinished()) : "");
        textViewFinished.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final FragmentManager fm = getActivity().getSupportFragmentManager();
                final DatePickerFragment dialog = DatePickerFragment.newInstance(knitting.getFinished() != null ? knitting.getFinished() : new Date());
                dialog.setTargetFragment(KnittingFragment.this, REQUEST_FINISHED);
                dialog.show(fm, DIALOG_DATE);
            }
        });

        final EditText editTextNeedleDiameter = v.findViewById(R.id.knitting_needle_diameter);
        editTextNeedleDiameter.setText(Double.toString(knitting.getNeedleDiameter()));
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

        final EditText editTextSize = v.findViewById(R.id.knitting_size);
        editTextSize.setText(Double.toString(knitting.getSize()));
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
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (knitting != null) {
            outState.putLong(KNITTING_ID, knitting.getId());
        }
        super.onSaveInstanceState(outState);
    }

    /**
     * Deletes the displayed knitting
     *
     * The method will delete the knitting from the database and also remove the photo if it exists
     */
    public void deleteKnitting() {
        // delete all photos from the database
        KnittingsDataSource.getInstance(getActivity()).deleteAllPhotos(knitting);
        // delete database entry
        KnittingsDataSource.getInstance(getActivity()).deleteKnitting(knitting);
        knitting = null;
    }
}
