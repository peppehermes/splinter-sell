package it.polito.mad.splintersell

import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import it.polito.mad.splintersell.data.User
import it.polito.mad.splintersell.data.storage
import it.polito.mad.splintersell.ui.profile_show.EXTRA_EMAIL
import it.polito.mad.splintersell.ui.profile_show.EXTRA_NICKNAME
import it.polito.mad.splintersell.ui.profile_show.db
import it.polito.mad.splintersell.ui.profile_show.user
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
            R.id.onSaleListFragment, R.id.nav_item_list, R.id.nav_show_profile, R.id.nav_signOut), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

     private fun retrievePreferencesMain(headerView: View) {
        val textViewNick = headerView.findViewById(R.id.nav_profile_name) as TextView
        val textViewMail = headerView.findViewById(R.id.nav_profile_mail) as TextView
        val imgView = headerView.findViewById(R.id.imageView) as ImageView


        textViewMail.text = user!!.email
        textViewNick.text = user!!.displayName

        //TODO: aggiustare il retrieve dell'immagine dopo aver costruito il viewmodel dello user

        db.collection("users")
            .document(user!!.uid)
            .get()
            .addOnSuccessListener {

                    res ->
                if (res.exists()) {
                    val userData: User? = res.toObject(
                        User::class.java
                    )

                    Glide.with(applicationContext)
                        .using(FirebaseImageLoader())
                        .load(storage.child("/profileImages/${userData!!.photoName}"))
                        .into(imgView)

                }


            }
    }

    fun refreshDataForDrawer(){
        val navView: NavigationView = findViewById(R.id.nav_view)
        val headerView = navView.getHeaderView(0)
        retrievePreferencesMain(headerView)
    }


}
