package com.mthaler.knittings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.UUID;

public class KnittingFragment extends Fragment {
    public static final String EXTRA_KNITTING_ID = "com.mthaler.knitting.KNITTING_ID";

    private static final String DIALOG_DATE = "date";
    private static final int REQUEST_DATE = 0;

    Knitting knitting;

    public static KnittingFragment newInstance(long knittingId) {
        Bundle args = new Bundle();
        args.putLong(EXTRA_KNITTING_ID, knittingId);

        final KnittingFragment fragment = new KnittingFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final long knittingId = getArguments().getLong(EXTRA_KNITTING_ID);
        knitting = KnittingsDataSource.getInstance(getActivity()).getKnitting(knittingId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_knitting, parent, false);

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

        final Button buttonStarted = v.findViewById(R.id.knittings_started);
        buttonStarted.setText(knitting.getStarted().toString());
        buttonStarted.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentManager fm = getActivity()
                        .getSupportFragmentManager();
                DatePickerFragment dialog = DatePickerFragment
                        .newInstance(knitting.getStarted());
                dialog.setTargetFragment(KnittingFragment.this, REQUEST_DATE);
                dialog.show(fm, DIALOG_DATE);
            }
        });

        final Button buttonFinished = v.findViewById(R.id.knittings_finished);
        buttonFinished.setText(knitting.getFinished() != null ? knitting.getFinished().toString() : "");
        buttonFinished.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                FragmentManager fm = getActivity()
//                        .getSupportFragmentManager();
//                DatePickerFragment dialog = DatePickerFragment
//                        .newInstance(mCrime.getDate());
//                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
//                dialog.show(fm, DIALOG_DATE);
            }
        });
        return v;
    }
}
