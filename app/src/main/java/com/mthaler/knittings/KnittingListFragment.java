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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class KnittingListFragment extends ListFragment {

    private ArrayList<Knitting> knittings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.knittings_title);
        this.knittings = KnittingsDataSource.getInstance(getActivity()).getAllKnittings();
        KnittingAdapter adapter = new KnittingAdapter(knittings);
        setListAdapter(adapter);
        setHasOptionsMenu(true);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // get the Crime from the adapter
        Knitting c = ((KnittingAdapter)getListAdapter()).getItem(position);
        // start an instance of CrimePagerActivity
        Intent i = new Intent(getActivity(), KnittingActivity.class);
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
                Knitting knitting = KnittingsDataSource.getInstance(getActivity()).createKnitting("", "", new Date(), null);
                Intent intent = new Intent(getActivity(), KnittingActivity.class);
                intent.putExtra(KnittingFragment.EXTRA_KNITTING_ID, knitting.getId());
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        this.knittings = KnittingsDataSource.getInstance(getActivity()).getAllKnittings();
        KnittingAdapter adapter = new KnittingAdapter(knittings);
        setListAdapter(adapter);
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
            final Knitting knitting = getItem(position);

            final TextView titleTextView = convertView.findViewById(R.id.knitting_list_item_titleTextView);
            titleTextView.setText(knitting.getTitle());

            final TextView descriptionTextView = convertView.findViewById(R.id.knitting_list_item_descriptionTextView);
            descriptionTextView.setText(knitting.getDescription().toString());

            final TextView startedTextView = convertView.findViewById(R.id.knitting_list_item_startedTextView);
            startedTextView.setText(DateFormat.getDateTimeInstance().format(knitting.getStarted()));

            return convertView;
        }
    }
}
