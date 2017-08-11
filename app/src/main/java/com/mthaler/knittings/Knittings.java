package com.mthaler.knittings;

import android.content.Context;

import java.util.ArrayList;
import java.util.UUID;

public class Knittings {

    private ArrayList<Knitting> knittings;

    private static Knittings sKnittings;
    private Context context;

    private Knittings(Context context) {
        this.context = context;
        knittings = new ArrayList<>();
    }

    public static Knittings get(Context c) {
        if (sKnittings == null) {
            sKnittings = new Knittings(c.getApplicationContext());
        }
        return sKnittings;
    }

    public Knitting getKnitting(UUID id) {
        for (Knitting knitting : knittings) {
            if (knitting.getId().equals(id)) {
                return knitting;
            }
        }
        return null;
    }

    public void addKnitting(Knitting knitting) {
        knittings.add(knitting);
    }

    public ArrayList<Knitting> getKnittings() {
        return knittings;
    }
}
