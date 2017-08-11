package com.mthaler.knittings;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class KnittingListFragment extends ListFragment {

    private ArrayList<Knitting> knittings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.knittings_title);
        this.knittings = Knittings.get(getActivity()).getKnittings();
        KnittingAdapter adapter = new KnittingAdapter(knittings);
        setListAdapter(adapter);
        setHasOptionsMenu(true);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // get the Crime from the adapter
        Knitting c = ((KnittingAdapter)getListAdapter()).getItem(position);
        // start an instance of CrimePagerActivity
        Intent i = new Intent(getActivity(), KnittingPagerActivity.class);
        i.putExtra(KnittingFragment.EXTRA_KNITTING_ID, c.getId());
        startActivityForResult(i, 0);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_knittings_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new_knitting:
                Knitting knitting = new Knitting();
                Knittings.get(getActivity()).addKnitting(knitting);
                Intent intent = new Intent(getActivity(), KnittingPagerActivity.class);
                intent.putExtra(KnittingFragment.EXTRA_KNITTING_ID, knitting.getId());
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class KnittingAdapter extends ArrayAdapter<Knitting> {

        public KnittingAdapter(ArrayList<Knitting> knittings) {
            super(getActivity(), android.R.layout.simple_list_item_1, knittings);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // if we weren't given a view, inflate one
            if (null == convertView) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.list_item_knitting, null);
            }

            // configure the view for this Crime
            Knitting knitting = getItem(position);

            TextView titleTextView =
                    (TextView)convertView.findViewById(R.id.knitting_list_item_titleTextView);
            titleTextView.setText(knitting.getTitle());
            TextView dateTextView =
                    (TextView)convertView.findViewById(R.id.knitting_list_item_dateTextView);
            dateTextView.setText(knitting.getStarted().toString());
            CheckBox solvedCheckBox =
                    (CheckBox)convertView.findViewById(R.id.knitting_list_item_solvedCheckBox);
            solvedCheckBox.setChecked(true);

            return convertView;
        }
    }
}
