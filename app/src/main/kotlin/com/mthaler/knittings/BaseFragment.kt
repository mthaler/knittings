package com.mthaler.knittings

import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

    open fun onBackPressed() {}
}