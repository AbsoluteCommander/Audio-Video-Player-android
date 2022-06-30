package `is`.xyz.mpv

import `is`.xyz.mpv.config.SettingsActivity
import `is`.xyz.mpv.databinding.FragmentExampleBinding
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

class MainFragment : Fragment(R.layout.fragment_example) {
    private lateinit var binding: FragmentExampleBinding

    private lateinit var documentTreeOpener: ActivityResultLauncher<Intent>
    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        documentTreeOpener = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            it.data?.data?.let { root ->
                val i = Intent(context, FilePickerActivity::class.java)
                i.putExtra("skip", FilePickerActivity.DOC_PICKER)
                i.putExtra("root", root.toString())
                filePickerLauncher.launch(i)
            }
        }
        filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK) {
                Log.v(TAG, "file picker cancelled")
                return@registerForActivityResult
            }
            val path = it.data?.getStringExtra("path")
            if (path != null)
                playFile(path)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentExampleBinding.bind(view)
        Log.w(TAG, "${javaClass.name} created")

        binding.button1.setOnClickListener {
            documentTreeOpener.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE))
        }
        binding.button2.setOnClickListener {
            val input = EditText(context)
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI

            with (AlertDialog.Builder(requireContext())) {
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
        }
        binding.button3.setOnClickListener {
            val i = Intent(context, FilePickerActivity::class.java)
            i.putExtra("skip", FilePickerActivity.FILE_PICKER)
            filePickerLauncher.launch(i)
        }
        binding.button4.setOnClickListener {
            startActivity(Intent(context, SettingsActivity::class.java))
        }
    }

    private fun playFile(filepath: String) {
        val i: Intent
        if (filepath.startsWith("content://")) {
            i = Intent(Intent.ACTION_VIEW, Uri.parse(filepath))
        } else {
            i = Intent()
            i.putExtra("filepath", filepath)
        }
        i.setClass(requireContext(), MPVActivity::class.java)
        startActivity(i)
    }

    companion object {
        private const val TAG = "mpv"
    }
}
