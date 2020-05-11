package it.polito.mad.splintersell

import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.navigateUp
import it.polito.mad.splintersell.ui.profile_show.EXTRA_EMAIL
import it.polito.mad.splintersell.ui.profile_show.EXTRA_NICKNAME
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream

const val filename = "proPic"

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_item_list, R.id.nav_show_profile, R.id.nav_signOut), drawerLayout)
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

    fun refreshDataForDrawer(){
        val navView: NavigationView = findViewById(R.id.nav_view)
        val headerView = navView.getHeaderView(0)
        retrievePreferencesMain(headerView)
    }

    interface DrawerLocker{
        fun setDrawerLocked(shouldLock:Boolean)
    }
}
