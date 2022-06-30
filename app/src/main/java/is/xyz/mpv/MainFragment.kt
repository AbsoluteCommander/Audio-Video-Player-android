package `is`.xyz.mpv

import `is`.xyz.mpv.config.SettingsActivity
import `is`.xyz.mpv.databinding.FragmentExampleBinding
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

class MainFragment : Fragment(R.layout.fragment_example) {
    private lateinit var binding: FragmentExampleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentExampleBinding.bind(view)
        android.util.Log.w("mpv", "${javaClass.name} created")

        binding.button1.setOnClickListener {
            val i = Intent(context, FilePickerActivity::class.java)
            i.putExtra("skip", FilePickerActivity.DOC_PICKER)
            startActivity(i)
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
            // maybe keeping it as an activity isn't so bad?
            startActivity(Intent(context, MainActivity::class.java))
            activity?.finish()
        }
        binding.button4.setOnClickListener {
            startActivity(Intent(context, SettingsActivity::class.java))
        }
    }

    private fun playFile(filepath: String) {
        val i = Intent(context, MPVActivity::class.java)
        i.putExtra("filepath", filepath)
        startActivity(i)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "fragment")
    }
}
