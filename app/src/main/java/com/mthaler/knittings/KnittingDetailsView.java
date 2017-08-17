package com.mthaler.knittings;

public interface KnittingDetailsView {

    void init(Knitting knitting);

    void deleteKnitting();

    Knitting getKnitting();
}
