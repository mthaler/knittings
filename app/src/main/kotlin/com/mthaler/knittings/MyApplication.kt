package com.mthaler.knittings

import android.util.Log
import androidx.multidex.MultiDexApplication
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mthaler.knittings.database.*
import com.mthaler.knittings.database.table.CategoryTable
import com.mthaler.knittings.database.table.KnittingTable
import com.mthaler.knittings.database.table.NeedleTable
import com.mthaler.knittings.database.table.RowCounterTable
import com.mthaler.knittings.settings.Theme
import com.mthaler.knittings.model.Database
import com.mthaler.knittings.model.ExportDatabase
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.toDatabase
import com.mthaler.knittings.settings.ThemeRepository
import org.json.JSONObject
import java.io.File

class MyApplication : MultiDexApplication(), DatabaseApplication {

    private val TAG = "MyApplication"


    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(KnittingTable.SQL_ADD_DURATION)
            Log.i(TAG, "Added duration colomn to knitting table")
            database.execSQL(KnittingTable.SQL_ADD_CATEGORY)
            Log.i(TAG, "Added category ID colomn to knitting table")

            try {
                CategoryTable.create(database)
                Log.d(TAG, "Category table created")
            } catch (ex: Exception) {
                Log.e(TAG, "Could not create category table", ex)
            }
        }
    }


    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            try {
                database.execSQL(KnittingTable.SQL_ADD_STATUS)
                Log.i(TAG, "Added status column to knitting table")
            } catch (ex: Exception) {
                Log.e(TAG, "Could not add status column to knitting table", ex)
            }

            try {
                NeedleTable.create(database)
                Log.d(TAG, "Needle table created")
            } catch (ex: Exception) {
                Log.e(TAG, "Could not create needle table", ex)
            }
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(NeedleTable.SQL_ADD_TYPE)
            Log.i(TAG, "Added type column to needle table")
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            try {
                RowCounterTable.create(database)
                Log.d(TAG, "RowCounter table created")
            } catch (ex: Exception) {
                Log.e(TAG, "Could not create row counter table", ex)
            }
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Empty implementation, because the schema isn't changing.
        }
    }

    val db = Room.databaseBuilder(
        applicationContext,
        AppDatabase::class.java, "database-name"
    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6).build()

    override fun onCreate() {
        super.onCreate()
        KnittingsDataSource.init(this)
        ObservableDatabase.init(KnittingsDataSource)
        Theme.setThemes(ThemeRepository.themes)
    }

    override val dropboxAppKey: String = "6ybf7tgqdbhf641"

    override fun getCategoryDataSource(): CategoryDataSource = KnittingsDataSource

    override fun getPhotoDataSource(): PhotoDataSource = KnittingsDataSource

    override fun getProjectsDataSource(): ProjectsDataSource<Knitting> = KnittingsDataSource

    override fun getApplicationSettings(): ApplicationSettings = object : ApplicationSettings {

        override fun getFileProviderAuthority(): String = "com.mthaler.knittings.fileprovider"

        override fun emptyCategoryListBackground(): Int = R.drawable.categories

        override fun categoryListBackground(): Int = R.drawable.categories2
    }

    override fun createExportDatabase(): ExportDatabase<Knitting> = Database.createDatabase()

    override fun createExportDatabaseFromJSON(json: JSONObject, externalFilesDir: File): ExportDatabase<Knitting> = json.toDatabase(this, externalFilesDir)
}