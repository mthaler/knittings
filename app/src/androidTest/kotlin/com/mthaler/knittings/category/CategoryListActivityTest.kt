package com.mthaler.knittings.category

import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CategoryListActivityTest {

      @Test
      fun testCategoryListActivityRecreate() {
          val scenario = launchActivity<CategoryListActivity>()
          scenario.recreate()
      }
}