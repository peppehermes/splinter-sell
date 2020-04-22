package it.polito.mad.splintersell

import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import it.polito.mad.splintersell.ui.profile_show.EXTRA_EMAIL
import it.polito.mad.splintersell.ui.profile_show.EXTRA_NICKNAME
import kotlinx.android.synthetic.main.fragment_show_profile.*
import kotlinx.android.synthetic.main.nav_header_main.*
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_item_list, R.id.nav_gallery, R.id.nav_slideshow,R.id.nav_item_details,R.id.nav_edit_item, R.id.nav_show_profile), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        val headerView = navView.getHeaderView(0)

        retrievePreferencesMain(headerView)

        }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    private fun retrievePreferencesMain(headerView: View) {

        val textViewNick = headerView.findViewById(R.id.nav_profile_name) as TextView
        val textViewMail = headerView.findViewById(R.id.nav_profile_mail) as TextView
        val imgView = headerView.findViewById(R.id.imageView) as ImageView

        val sharedPref: SharedPreferences = getPreferences(Context.MODE_PRIVATE) ?: return
        val profile: String? = sharedPref.getString("Profile", null)

        if (profile != null) {

            val jasonObject = JSONObject(profile)
            val savedNickname: String
            val savedEmail: String


            savedNickname = if (jasonObject.has(EXTRA_NICKNAME))
                jasonObject.getString(EXTRA_NICKNAME)
            else
                resources.getString(R.string.nick)

            savedEmail = if (jasonObject.has(EXTRA_EMAIL))
                jasonObject.getString(EXTRA_EMAIL)
            else
                resources.getString(R.string.mail)


            textViewMail.text = savedEmail
            textViewNick.text = savedNickname

        }


            val filename = "img"
            val file = File(this.filesDir, filename)
            val fileExists = file.exists()
            if (fileExists) {
                val fis: FileInputStream = openFileInput(filename)
                val bitmap = BitmapFactory.decodeStream(fis)
                val roundDrawable: RoundedBitmapDrawable =
                    RoundedBitmapDrawableFactory.create(resources, bitmap)
                roundDrawable.isCircular = true
                fis.close()
                imgView.setImageDrawable(roundDrawable)

            }




    }

}
