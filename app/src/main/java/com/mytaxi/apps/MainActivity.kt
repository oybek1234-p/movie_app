package com.mytaxi.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.etebarian.meowbottomnavigation.MeowBottomNavigation
import com.google.android.material.navigation.NavigationView
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import com.ixidev.gdpr.GDPRChecker
import com.mytaxi.fragment.*
import com.mytaxi.util.*
import com.squareup.picasso.Picasso
import github.com.st235.lib_expandablebottombar.ExpandableBottomBar
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.math.log
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private var drawerLayout: DrawerLayout? = null
    private var navigationView: LinearLayout? = null
    var toolbar: Toolbar? = null
    private var fragmentManager: FragmentManager? = null
    private var doubleBackToExitPressedOnce = false
    var myApplication: MyApplication? = null
    private var bottomBar: MeowBottomNavigation? = null
    private var currentFragment: String? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    private var nameTextView: TextView?=null
    private var photoView: ImageView?=null
    private var editTextView: TextView?=null
    private var login: TextView?=null
    private var settingsFragment: SettingFragment?=null

    private fun updateDrawer() {
        val myApplication = MyApplication.getInstance()
        val isLogged = MyApplication.getInstance().isLogin
        nameTextView?.apply {
            myApplication.userName.ifEmpty { if (myApplication.isUserLoading) "Loading" else if (myApplication.isLogin) "User name" else "Sign in" }
            setOnClickListener {
                if (!isLogged) {
                    startActivity(Intent(this@MainActivity,SignInActivity::class.java))
                }
            }
        }
        val photo = MyApplication.getInstance().userPhoto

        login?.apply {
            if (isLogged) {
                text = "Sign out"
            } else {
                text = "Login"
            }
        }
        editTextView?.visibility = if (isLogged) View.VISIBLE else View.GONE
        if (photoView!=null) {
            if (photo.isNotEmpty()) {
                Picasso.get().load(photo).into(photoView)
            } else {
                Picasso.get().load(R.drawable.user_icon).into(photoView)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_cander)

        IsRTL.ifSupported(this)

        fragmentManager = supportFragmentManager

        myApplication = MyApplication.getInstance()

        GDPRChecker()
            .withContext(this@MainActivity)
            .withPrivacyUrl(getString(R.string.privacy_url))
            .withPublisherIds(Constant.adMobPublisherId)
            .withTestMode("9424DF76F06983D1392E609FC074596C")
            .check()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        navigationView = findViewById(R.id.navigation_view)
        drawerLayout = findViewById(R.id.drawer_layout)
        bottomBar = findViewById(R.id.expandable_bottom_bar)

        settingsFragment = SettingFragment().apply {
            context = this@MainActivity
            runnable = Runnable {
                drawerLayout?.closeDrawers()
            }
            supportFragmentManager.beginTransaction().add(R.id.navigation_view,this,null,).commit()
//            onCreateView(LayoutInflater.from(this@MainActivity),null,null)
//            onResume()
//            onStart()
        }

        navigationView?.apply {
            setOnClickListener {
                //Do not remove
            }
            nameTextView = findViewById(R.id.name_view)
            photoView = findViewById(R.id.photo_view)

            editTextView = findViewById<TextView>(R.id.edit_profile)
            editTextView?.setOnClickListener {
                startActivity(Intent(this@MainActivity,EditProfileActivity::class.java))
             //   loadFrag(EditProfileFragment(),getString(R.string.menu_edit),fragmentManager!!)
            }
        }

        toolbar?.setNavigationOnClickListener {
            if (navigationView!=null && drawerLayout != null) {
                val drawerLayout = drawerLayout!!

                val open = !drawerLayout.isDrawerOpen(navigationView!!)
                if (open) {
                    drawerLayout.openDrawer(navigationView!!)
                } else {
                    drawerLayout.closeDrawer(navigationView!!)
                }
                updateDrawer()
            }
        }


        val mAdViewLayout = findViewById<LinearLayout>(R.id.adView)

        BannerAds.showBannerAds(this, mAdViewLayout)

        val homeFragment = HomeFragment()
        //Bottombar
        bottomBar?.apply {
            add(MeowBottomNavigation.Model(R.id.menu_go_home,R.drawable.ic_home))
            add(MeowBottomNavigation.Model(R.id.menu_go_movie,R.drawable.ic_movie))
            add(MeowBottomNavigation.Model(R.id.menu_language,R.drawable.ic_language))
            add(MeowBottomNavigation.Model(R.id.menu_go_dashboard,R.drawable.ic_dashboard))
        }

        bottomBar?.setOnClickMenuListener {
            val id = it.id
            when (id) {
                R.id.menu_go_home -> {
                    loadFrag(homeFragment, getString(R.string.menu_home), fragmentManager!!)
                }
                R.id.menu_go_movie -> {
                    val movieTabFragment = GenreFragment()
                    val bundleMovie = Bundle()
                    bundleMovie.putBoolean("isShow", false)
                    movieTabFragment.arguments = bundleMovie
                    loadFrag(movieTabFragment, getString(R.string.menu_movie), fragmentManager!!)
                }
                R.id.menu_language -> {
                    val languageFragment = LanguageFragment()
                    val bundleMovie = Bundle()
                    bundleMovie.putBoolean("isShow", false)
                    languageFragment.arguments = bundleMovie
                    loadFrag(languageFragment, getString(R.string.menu_language), fragmentManager!!)
                }
                R.id.menu_go_dashboard -> {
                    if (myApplication!!.isLogin) {
                        val intentDashBoard = Intent(this@MainActivity, DashboardActivity::class.java)
                        intentDashBoard.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intentDashBoard)
                    } else {
                        val intentSignIn = Intent(this@MainActivity, SignInActivity::class.java)
                        intentSignIn.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intentSignIn)
                        finish()
                    }
                }
//                R.id.menu_go_setting -> {
//                    val settingFragment = SettingFragment()
//                    loadFrag(settingFragment, getString(R.string.menu_setting), fragmentManager!!)
//                }
            }
        }
        bottomBar?.setOnReselectListener {
            val id = it.id
            when (id) {
                R.id.menu_go_home -> {
                    loadFrag(homeFragment, getString(R.string.menu_home), fragmentManager!!)
                }
                R.id.menu_go_movie -> {
                    val movieTabFragment = GenreFragment()
                    val bundleMovie = Bundle()
                    bundleMovie.putBoolean("isShow", false)
                    movieTabFragment.arguments = bundleMovie
                    loadFrag(movieTabFragment, getString(R.string.menu_movie), fragmentManager!!)
                }
                R.id.menu_language -> {
                    val languageFragment = LanguageFragment()
                    val bundleMovie = Bundle()
                    bundleMovie.putBoolean("isShow", false)
                    languageFragment.arguments = bundleMovie
                    loadFrag(languageFragment, getString(R.string.menu_language), fragmentManager!!)
                }
                R.id.menu_go_dashboard -> {
                    if (myApplication!!.isLogin) {
                        val intentDashBoard = Intent(this@MainActivity, DashboardActivity::class.java)
                        intentDashBoard.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intentDashBoard)
                    } else {
                        val intentSignIn = Intent(this@MainActivity, SignInActivity::class.java)
                        intentSignIn.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intentSignIn)
                        finish()
                    }
                }
//                R.id.menu_go_setting -> {
//                    val settingFragment = SettingFragment()
//                    loadFrag(settingFragment, getString(R.string.menu_setting), fragmentManager!!)
//                }
            }
        }
        bottomBar?.show(R.id.menu_go_home,true)
        loadFrag(homeFragment, getString(R.string.menu_home), fragmentManager!!)

        MyApplication.getInstance().loadUser {
            updateDrawer()
        }
        try {
            val info = packageManager.getPackageInfo(
                packageName,  //Insert your own package name.
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {
        } catch (e: NoSuchAlgorithmException) {
        }
    }

    private fun loadFrag(f1: Fragment?, name: String?, fm: FragmentManager) {
        for (i in 0 until fm.backStackEntryCount) {
            fm.popBackStack()
        }
        val ft = fm.beginTransaction()
        ft.replace(R.id.Container, f1!!, name)
        ft.commit()

        currentFragment = name
        setToolbar()
    }

    private fun setToolbar() {
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        drawerLayout!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        toolbar!!.navigationIcon = null

        if(currentFragment == getString(R.string.menu_home)) {
            setLogoAsTitle()
        } else {
            supportActionBar!!.setLogo(null)
            //drawerLayout!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            //setNavIcon()
            setToolbarTitle(currentFragment)
        }
        setNavIcon()
    }

    fun setToolbarTitle(title: String?) {
        if (supportActionBar != null) {
                supportActionBar!!.title = title
        }
    }

    private fun setLogoAsTitle(){

        val o: BitmapFactory.Options = BitmapFactory.Options()
        o.inTargetDensity = DisplayMetrics.DENSITY_DEFAULT
        val bmp: Bitmap = BitmapFactory.decodeResource(
            this.resources,
            R.drawable.header_logo, o
        )

        val w = (0.3 * bmp.width).roundToInt()
        val h = (0.3 * bmp.height).roundToInt()

        val dr: Drawable? = ResourcesCompat.getDrawable(resources, R.drawable.header_logo, null)
        val bitmap: Bitmap = (dr as BitmapDrawable).bitmap
        val logo: Drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, w, h, true))

        setToolbarTitle("")
        supportActionBar!!.setLogo(logo)
        supportActionBar!!.setDisplayUseLogoEnabled(true)
    }

    private fun setNavIcon() {
            val nightMode = NightModeSwitch(this)
            if (nightMode.nightMode.equals("ON", ignoreCase = true))
                toolbar!!.setNavigationIcon(R.drawable.ic_menu_dark_theme)
            else
                toolbar!!.setNavigationIcon(R.drawable.ic_menu_light_theme)
    }

    private fun logOut() {
        AlertDialog.Builder(this@MainActivity, R.style.AlertDialogStyle)
            .setTitle(getString(R.string.menu_logout))
            .setMessage(getString(R.string.logout_msg))
            .setPositiveButton(android.R.string.yes) { _, _ ->
                myApplication!!.saveIsLogin(false)
                val intent = Intent(applicationContext, SignInActivity::class.java)
                intent.putExtra("isLogout", true)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_search, menu)
        val searchMenuItem = menu.findItem(R.id.search)
        val searchView = searchMenuItem.actionView as SearchView
        val drawerSwitch = menu.findItem(R.id.menu_dark_theme).actionView as SwitchCompat
        val liveTV = menu.findItem(R.id.menu_go_tv)
        val sport = menu.findItem(R.id.menu_go_sport)

        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                searchMenuItem.collapseActionView()
                searchView.setQuery("", false)
            }
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(arg0: String): Boolean {

                val intent = Intent(this@MainActivity, SearchHorizontalActivity::class.java)
                intent.putExtra("search", arg0)
                startActivity(intent)
                searchView.clearFocus()
                return false
            }

            override fun onQueryTextChange(arg0: String): Boolean {

                return false
            }
        })

        val nightMode = NightModeSwitch(this)
        if (nightMode.nightMode.equals("OFF", ignoreCase = true)) {
            drawerSwitch.isChecked = false
        } else if (nightMode.nightMode.equals("ON", ignoreCase = true)) {
            drawerSwitch.isChecked = true
        }
        setToolbar()

        //Changing switch thumb and track color
        DrawableCompat.setTintList(
            drawerSwitch.thumbDrawable, ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()), intArrayOf(
                    ContextCompat.getColor(this, R.color.highlight),
                    Color.WHITE
                )
            )
        )

        DrawableCompat.setTintList(
            drawerSwitch.trackDrawable, ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()), intArrayOf(
                    ContextCompat.getColor(this, R.color.faded_highlight),
                    Color.parseColor("#4D000000")
                )
            )
        )

        drawerSwitch.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            val homeFragment = HomeFragment()
            loadFrag(
                homeFragment, getString(
                    R.string.menu_home
                ), fragmentManager!!
            )
            if (isChecked) {
                nightMode.nightMode = "ON"
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                nightMode.nightMode = "OFF"
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        liveTV.setOnMenuItemClickListener {
            val tvCategoryFragment = TVCategoryFragment()
            loadFrag(tvCategoryFragment, getString(R.string.menu_tv), fragmentManager!!)
            true
        }

        sport.setOnMenuItemClickListener {
            val sportFragment = SportFragment()
            loadFrag(sportFragment, getString(R.string.menu_sport), fragmentManager!!)
            true
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        when {
            drawerLayout!!.isDrawerOpen(GravityCompat.START) -> {
                drawerLayout!!.closeDrawer(GravityCompat.START)
            }
            fragmentManager!!.backStackEntryCount != 0 -> {
                val tag = fragmentManager!!.fragments[fragmentManager!!.backStackEntryCount - 1].tag
                setToolbarTitle(tag)
                super.onBackPressed()
            }
            else -> {
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed()
                    return
                }
                doubleBackToExitPressedOnce = true
                Toast.makeText(this, getString(R.string.back_key), Toast.LENGTH_SHORT).show()
                Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragment = fragmentManager!!.findFragmentByTag(getString(R.string.menu_profile))
        fragment?.onActivityResult(requestCode, resultCode, data)
    }

    fun setToolbarFromFragment(title: String?) {
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        drawerLayout!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        toolbar!!.navigationIcon = null
        supportActionBar!!.setLogo(null)
        setToolbarTitle(title)
    }
}