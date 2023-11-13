package com.spyglass.rgisvisionsample

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.Settings
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.spyglass.rgisvisionsample.databinding.ActivityMainBinding
import com.spyglass.rgisvisionsample.utils.FileUtils
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var selectedDatabasePath: String? = null
    private var selectedConfigPath: String? = null
    private var selectedTable: String? = null
    private var selectedSearchColumn: String? = null
    private var selectedDescriptionColumn: String? = null

    private val selectDatabaseLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let {
                    FileUtils(this@MainActivity).getPath(it)?.let { databasePath ->
                        binding.tvSelectedDatabasePath.text = databasePath
                        openDatabase(File(databasePath))
                    }
                }
            }
        }

    private val selectConfigLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let {
                    FileUtils(this@MainActivity).getPath(it)?.let { configPath ->
                        binding.tvSelectedConfigPath.text = configPath
                        selectedConfigPath = configPath
                    }
                }
            }
        }

    private val rgisVisionAppLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { intent ->
                    intent.getBundleExtra(getString(R.string.key_rgis_vision_response))
                        ?.let { scanResponse ->
                            binding.tvScanResponseJson.text =
                                scanResponse.getString(getString(R.string.key_rgis_vision_response_json))
                        }
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                result.data?.let { intent ->
                    intent.getBundleExtra(getString(R.string.key_rgis_vision_exception))
                        ?.let { exceptionBundle ->
                            showSnackBar(
                                exceptionBundle.getString(getString(R.string.key_rgis_vision_exception))
                                    ?: "Unknown Error"
                            )
                        }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLaunchRgisVision.setOnClickListener {
            launchRgisVision()
        }

        binding.btnSelectDatabase.setOnClickListener {
            if (checkReadStoragePermission().not()) {
                requestReadStoragePermission()
            } else {
                launchSelectDatabaseIntent()
            }
        }

        binding.btnSelectConfig.setOnClickListener {
            if (checkReadStoragePermission().not()) {
                requestReadStoragePermission()
            } else {
                launchSelectConfigIntent()
            }
        }
    }

    private fun launchSelectDatabaseIntent() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, true)
        }
        selectDatabaseLauncher.launch(intent)
    }

    private fun launchSelectConfigIntent() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, true)
        }
        selectConfigLauncher.launch(intent)
    }

    private fun openDatabase(databaseFile: File) {
        runCatching {
            val sqliteDatabase = SQLiteDatabase.openOrCreateDatabase(databaseFile, null)
            val tableNames = mutableListOf<String>()
            val cursor =
                sqliteDatabase.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)

            while (cursor.moveToNext()) {
                val tableName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                if (tableName != "android_metadata" && tableName != "sqlite_sequence") {
                    tableNames.add(tableName)
                }
            }

            cursor.close()

            val tableAdapter =
                ArrayAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    android.R.id.text1,
                    tableNames
                )

            binding.dropdownDatabaseTable.setAdapter(tableAdapter)

            binding.dropdownDatabaseTable.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    selectedTable = tableNames[position]

                    val tableCursor: Cursor =
                        sqliteDatabase.query(selectedTable, null, null, null, null, null, null)

                    val columnsAdapter =
                        ArrayAdapter(
                            this,
                            android.R.layout.simple_list_item_1,
                            android.R.id.text1,
                            tableCursor.columnNames
                        )

                    binding.dropdownSearchColumn.setAdapter(columnsAdapter)
                    binding.dropdownDescriptionColumn.setAdapter(columnsAdapter)

                    binding.dropdownSearchColumn.onItemClickListener =
                        AdapterView.OnItemClickListener { _, _, searchPosition, _ ->
                            selectedSearchColumn = tableCursor.columnNames[searchPosition]
                        }

                    binding.dropdownDescriptionColumn.onItemClickListener =
                        AdapterView.OnItemClickListener { _, _, descriptionPosition, _ ->
                            selectedDescriptionColumn = tableCursor.columnNames[descriptionPosition]
                        }

                    tableCursor.close()
                }

            selectedDatabasePath = databaseFile.path
            //sqliteDatabase.close()
        }.getOrElse {
            it.printStackTrace()
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.error_database_cannot_open))
                .setMessage(it.message.toString())
                .setNegativeButton(getString(android.R.string.ok)) { dialog, _ ->
                    dialog.dismiss()
                }.show()
        }
    }

    private fun launchRgisVision() {
        if (checkAllParamsIsDone().not())
            return

        val intent = Intent(Intent.ACTION_VIEW)

        intent.apply {
            component = ComponentName(
                getString(R.string.rgis_vision_app_package),
                getString(R.string.rgis_vision_app_scanner_activity)
            )

            putExtra(getString(R.string.key_config_file_path), selectedConfigPath)
            putExtra(getString(R.string.key_database_file_path), selectedDatabasePath)
            putExtra(getString(R.string.key_table_name), selectedTable)
            putExtra(getString(R.string.key_search_column), selectedSearchColumn)
            putExtra(getString(R.string.key_description_column), selectedDescriptionColumn)
        }

        try {
            rgisVisionAppLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            showSnackBar(getString(R.string.error_rgis_vision_app_not_found))
        }
    }

    private fun checkAllParamsIsDone(): Boolean {
        if (selectedDatabasePath == null) {
            showSnackBar(getString(R.string.error_please_select_database))
            return false
        }

        if (selectedTable == null) {
            showSnackBar(getString(R.string.error_please_select_table))
            return false
        }

        if (selectedSearchColumn == null) {
            showSnackBar(getString(R.string.error_please_select_search_column))
            return false
        }

        if (selectedDescriptionColumn == null) {
            showSnackBar(getString(R.string.error_please_select_description_column))
            return false
        }

        return true
    }

    private fun checkReadStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val readStorage =
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            val writeStorage =
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            readStorage == PackageManager.PERMISSION_GRANTED &&
                    writeStorage == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestReadStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:${packageName}")
            startActivity(intent)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                11
            )
        }
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}