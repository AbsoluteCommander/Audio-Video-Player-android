package `is`.xyz.mpv

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.RecyclerView

class MainActivity2 : AppCompatActivity(R.layout.activity_main2) {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Set the system UI to act as if the nav bar is hidden, so that we can
        // draw behind it. STABLE flag is historically recommended but was
        // deprecated in API level 30, so probably not strictly necessary, but
        // cargo-culting is fun.
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        super.onCreate(savedInstanceState)

        // With the app acting as if the navbar is hidden, we need to
        // account for it outselves. We want the recycler to directly
        // take the system UI padding so that we can tell it to draw
        // into the padded area while still respecting the padding for
        // input.
        val container = findViewById<FragmentContainerView>(R.id.fragment_container_view)
        container.setOnApplyWindowInsetsListener { view, insets ->
            view.setPadding(insets.systemWindowInsetLeft,
                insets.systemWindowInsetTop,
                insets.systemWindowInsetRight,
                insets.systemWindowInsetBottom)
            insets
        }

        supportActionBar?.setTitle(R.string.mpv_activity)

        android.util.Log.w(TAG, "${javaClass.name} created")

        if (savedInstanceState == null) {
            with (supportFragmentManager.beginTransaction()) {
                setReorderingAllowed(true)
                add(R.id.fragment_container_view, MainFragment())
                commit()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //menuInflater.inflate(R.menu.menu_main, menu)
        menu?.add(Menu.NONE, Menu.NONE, Menu.NONE, "activity")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return false
    }

    companion object {
        private const val TAG = "mpv"
    }
}
