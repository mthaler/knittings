package com.mthaler.knittings.details

import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.Photo

data class KnittingWithPhotos(val knitting: Knitting, val photos: ArrayList<Photo>)