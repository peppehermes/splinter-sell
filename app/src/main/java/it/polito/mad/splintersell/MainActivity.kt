package it.polito.mad.splintersell

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.navigateUp
import com.bumptech.glide.Glide
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.polito.mad.splintersell.data.FirestoreViewModel
import it.polito.mad.splintersell.data.UserModel
import it.polito.mad.splintersell.data.storage

class MainActivity : AppCompatActivity() {

    private val user = Firebase.auth.currentUser

    private val firestoreViewModel: FirestoreViewModel by viewModels()
    lateinit var userLiveData: LiveData<UserModel>


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

         Log.d("Xeros", user?.uid.toString())

         // Retrieve User data
         firestoreViewModel.fetchMyUserFromFirestore()
         firestoreViewModel.myUserNav.observe(this, androidx.lifecycle.Observer {
             textViewNick.text = it.nickname
             textViewMail.text = it.email

             Log.d("Xeros", "Data have changed")


             Glide.with(this)
                 .using(FirebaseImageLoader())
                 .load(storage.child("/profileImages/${it.photoName}"))
                 .into(imgView)

         })




         /*


        textViewMail.text = user!!.email
        textViewNick.text = user!!.displayName




        db.collection("users")
            .document(user!!.uid)
            .get()
            .addOnSuccessListener {

                    res ->
                if (res.exists()) {
                    val userData: UserModel? = res.toObject(
                        UserModel::class.java
                    )

                    Glide.with(applicationContext)
                        .using(FirebaseImageLoader())
                        .load(storage.child("/profileImages/${userData!!.photoName}"))
                        .into(imgView)

                }


            }


          */
    }


    fun refreshDataForDrawer(){
        val navView: NavigationView = findViewById(R.id.nav_view)
        val headerView = navView.getHeaderView(0)
        retrievePreferencesMain(headerView)
    }




}
