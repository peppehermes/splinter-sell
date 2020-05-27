package it.polito.mad.splintersell

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.android.material.navigation.NavigationView
import it.polito.mad.splintersell.data.FirestoreViewModel
import it.polito.mad.splintersell.data.storage

class MainActivity : AppCompatActivity(), DrawerLayout.DrawerListener {

    private val firestoreViewModel: FirestoreViewModel by viewModels()
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private val TAG = "MAIN_ACTIVITY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.addDrawerListener(this)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        // Passing each show_profile_menu ID as a set of Ids because each
        // show_profile_menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_show_profile,
                R.id.nav_item_list,
                R.id.nav_on_sale_list,
                R.id.nav_items_of_interest_list,
                R.id.nav_bought_items_list,
                R.id.nav_sign_out
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun manageNavigationHeader() {
        val navView: NavigationView = findViewById(R.id.nav_view)
        val headerView = navView.getHeaderView(0)
        val textViewNick = headerView.findViewById(R.id.nav_profile_name) as TextView
        val textViewMail = headerView.findViewById(R.id.nav_profile_mail) as TextView
        val imgView = headerView.findViewById(R.id.imageView) as ImageView

        // Retrieve User data
        val myUser = firestoreViewModel.createdUserLiveData!!.value
        textViewNick.text = myUser!!.nickname
        textViewMail.text = myUser.email

        Glide.with(this).using(FirebaseImageLoader())
            .load(storage.child("/profileImages/${myUser.photoName}")).into(imgView)
    }

    override fun onDrawerStateChanged(newState: Int) {
        this.manageNavigationHeader()
    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) { /* do nothing */
    }

    override fun onDrawerClosed(drawerView: View) { /* do nothing */
    }

    override fun onDrawerOpened(drawerView: View) { /* do nothing */
    }
}
