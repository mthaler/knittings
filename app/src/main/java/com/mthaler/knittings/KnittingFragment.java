package com.mthaler.knittings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import java.util.UUID;

public class KnittingFragment extends Fragment {
    public static final String EXTRA_KNITTING_ID = "com.mthaler.knitting.KNITTING_ID";

    private static final String DIALOG_DATE = "date";
    private static final int REQUEST_DATE = 0;

    Knitting mKnitting;
    EditText mTitleField;
    Button mDateButton;
    CheckBox mSolvedCheckBox;

    public static KnittingFragment newInstance(UUID knittingId) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_KNITTING_ID, knittingId);

        KnittingFragment fragment = new KnittingFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UUID knittingId = (UUID)getArguments().getSerializable(EXTRA_KNITTING_ID);
        mKnitting = Knittings.get(getActivity()).getKnitting(knittingId);
    }

    public void updateDate() {
        mDateButton.setText(mKnitting.getStarted().toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_knitting, parent, false);

        mTitleField = (EditText)v.findViewById(R.id.knitting_title);
        mTitleField.setText(mKnitting.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                mKnitting.setTitle(c.toString());
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // this space intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // this one too
            }
        });

        mDateButton = (Button)v.findViewById(R.id.knittings_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                FragmentManager fm = getActivity()
//                        .getSupportFragmentManager();
//                DatePickerFragment dialog = DatePickerFragment
//                        .newInstance(mCrime.getDate());
//                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
//                dialog.show(fm, DIALOG_DATE);
            }
        });

        mSolvedCheckBox = (CheckBox)v.findViewById(R.id.knitting_solved);
        mSolvedCheckBox.setChecked(true);
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // set the crime's solved property
                //mCrime.setSolved(isChecked);
            }
        });



        return v;
    }
}
