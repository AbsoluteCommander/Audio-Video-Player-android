package `is`.xyz.mpv

import `is`.xyz.filepicker.AbstractFilePickerFragment
import `is`.xyz.mpv.config.SettingsActivity
import android.Manifest
import android.app.UiModeManager
import android.content.Context.UI_MODE_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.io.File
import java.io.FileFilter

class MainFragment2 : Fragment(R.layout.activity_main), AbstractFilePickerFragment.OnFilePickedListener {
    private var fragment: MPVFilePickerFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.w(TAG, "${javaClass.name} created")

        fragment = parentFragmentManager.findFragmentById(R.id.file_picker_fragment) as MPVFilePickerFragment

        (activity as AppCompatActivity?)?.supportActionBar?.setTitle(R.string.mpv_activity)

        if (PackageManager.PERMISSION_GRANTED ==
            ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            initFilePicker()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissions.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // FIXME need to reinit fragment
        }
    }

    private fun initFilePicker() {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        if (sharedPrefs.getBoolean("is.xyz.MainActivity_filter_state", false)) {
            fragment!!.filterPredicate = MEDIA_FILE_FILTER
        }

        // TODO: rework or remove this setting
        val defaultPathStr = sharedPrefs.getString("default_file_manager_path",
            Environment.getExternalStorageDirectory().path)
        val defaultPath = File(defaultPathStr)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // check that the preferred path is inside a storage volume
            val vols = Utils.getStorageVolumes(requireContext())
            val vol = vols.find { defaultPath.startsWith(it.path) }
            if (vol == null) {
                // looks like it wasn't
                Log.w(TAG, "default path set to $defaultPath but no such storage volume")
                with (fragment!!) {
                    root = vols.first().path
                    goToDir(vols.first().path)
                }
            } else {
                with (fragment!!) {
                    root = vol.path
                    goToDir(defaultPath)
                }
            }
        } else {
            // Old device: go to preferred path but don't restrict root
            fragment!!.goToDir(defaultPath)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val uiModeManager = requireActivity().getSystemService(UI_MODE_SERVICE) as UiModeManager
        if (uiModeManager.currentModeType != Configuration.UI_MODE_TYPE_TELEVISION)
            inflater.inflate(R.menu.menu_main, menu)
        else
            menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "...") // dummy menu item to indicate presence
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val context = requireContext()
        val id = item.itemId

        if (id == R.id.action_external_storage) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                val path = Environment.getExternalStorageDirectory()
                fragment!!.goToDir(path) // do something potentially useful
                return true
            }

            val vols = Utils.getStorageVolumes(context)

            with (AlertDialog.Builder(context)) {
                setItems(vols.map { it.description }.toTypedArray()) { dialog, item ->
                    val vol = vols[item]
                    with (fragment!!) {
                        root = vol.path
                        goToDir(vol.path)
                    }
                    dialog.dismiss()
                }
                show()
            }
            return true
        } else if (id == R.id.action_file_filter) {
            val old: Boolean
            with (fragment!!) {
                old = filterPredicate != null
                filterPredicate = if (!old) MEDIA_FILE_FILTER else null
            }
            with (Toast.makeText(context, "", Toast.LENGTH_SHORT)) {
                setText(if (!old) R.string.notice_show_media_files else R.string.notice_show_all_files)
                show()
            }
            // remember state for next time
            with (PreferenceManager.getDefaultSharedPreferences(context).edit()) {
                this.putBoolean("is.xyz.MainActvity_filter_state", !old)
                apply()
            }
            return true
        } else if (id == R.id.action_open_url) {
            // https://stackoverflow.com/questions/10903754/#answer-10904665
            val input = EditText(context)
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI

            with (AlertDialog.Builder(context)) {
                setTitle(R.string.action_open_url)
                setView(input)
                setPositiveButton(R.string.dialog_ok) { _, _ ->
                    playFile(input.text.toString())
                }
                setNegativeButton(R.string.dialog_cancel) { dialog, _ ->
                    dialog.cancel()
                }
                show()
            }
        } else if (id == R.id.action_settings) {
            val i = Intent(context, SettingsActivity::class.java)
            startActivity(i)
            return true
        }
        return false
    }

    // FIXME: dispatchKeyEvent missing?!

    private fun playFile(filepath: String) {
        val i = Intent(requireContext(), MPVActivity::class.java)
        i.putExtra("filepath", filepath)
        startActivity(i)
    }

    override fun onFilePicked(file: File) {
        playFile(file.absolutePath)
    }

    override fun onDirPicked(dir: File) {
        // mpv will play directories as playlist of all contained files
        playFile(dir.absolutePath)
    }

    override fun onCancelled() {
    }

    // FIXME: onBackPressed missing

    companion object {
        private const val TAG = "mpv"

        private val MEDIA_FILE_FILTER = FileFilter { file ->
            if (file.isDirectory) {
                val contents: Array<String> = file.list() ?: arrayOf()
                // filter hidden files due to stuff like ".thumbnails"
                contents.filterNot { it.startsWith('.') }.any()
            } else {
                Utils.MEDIA_EXTENSIONS.contains(file.extension.toLowerCase())
            }
        }
    }
}
