package org.fossasia.openevent.general

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.navigation
import kotlinx.android.synthetic.main.activity_main.mainFragmentCoordinatorLayout
import org.fossasia.openevent.general.auth.RC_CREDENTIALS_READ
import org.fossasia.openevent.general.auth.SmartAuthViewModel
import org.fossasia.openevent.general.auth.SmartAuthUtil
import org.fossasia.openevent.general.auth.AuthFragment
import org.fossasia.openevent.general.utils.Utils.navAnimGone
import org.fossasia.openevent.general.utils.Utils.navAnimVisible
import org.jetbrains.anko.design.snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

const val PLAY_STORE_BUILD_FLAVOR = "playStore"
const val EVENT_IDENTIFIER = "eventIdentifier"

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private var currentFragmentId: Int = 0
    private val smartAuthViewModel by viewModel<SmartAuthViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val hostFragment = supportFragmentManager.findFragmentById(R.id.frameContainer)
        if (hostFragment is NavHostFragment)
            navController = hostFragment.navController
        setupBottomNavigationMenu(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            currentFragmentId = destination.id
            handleNavigationVisibility(currentFragmentId)
        }
        handleAppLinkIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleAppLinkIntent(intent)
    }

    private fun handleAppLinkIntent(intent: Intent?) {
        val appLinkData = intent?.data
        if (appLinkData != null) {
            val bundle = Bundle()
            bundle.putString(EVENT_IDENTIFIER, appLinkData.lastPathSegment)
            navController.navigate(R.id.eventDetailsFragment, bundle)
        }
    }

    private fun setupBottomNavigationMenu(navController: NavController) {
        setupWithNavController(navigation, navController)
        navigation.setOnNavigationItemReselectedListener {
            val hostFragment = supportFragmentManager.findFragmentById(R.id.frameContainer)
            if (hostFragment is NavHostFragment) {
                val currentFragment = hostFragment.childFragmentManager.fragments.first()
                if (currentFragment is BottomIconDoubleClick) currentFragment.doubleClick()
            }
        }
    }

    private fun handleNavigationVisibility(id: Int) {
        when (id) {
            R.id.eventsFragment,
            R.id.searchFragment,
            R.id.profileFragment,
            R.id.orderUnderUserFragment,
            R.id.favoriteFragment -> navAnimVisible(navigation, this@MainActivity)
            else -> navAnimGone(navigation, this@MainActivity)
        }
    }

    override fun onBackPressed() {
        Toast.makeText(this, "Called1", Toast.LENGTH_SHORT).show()
        val hostFragment = supportFragmentManager.findFragmentById(R.id.frameContainer)
        if (hostFragment is NavHostFragment) {
            val currentFragment = hostFragment.childFragmentManager.fragments.first()
            if (currentFragment is ComplexBackPressFragment) {
                Toast.makeText(this, "Called2", Toast.LENGTH_SHORT).show()
                currentFragment.handleBackPress()
                if (currentFragment is AuthFragment)
                    Toast.makeText(this, "Called3", Toast.LENGTH_SHORT).show()
                    mainFragmentCoordinatorLayout.snackbar(R.string.sign_in_canceled)
                return
            }
        }
        when (currentFragmentId) {
            R.id.orderCompletedFragment -> navController.popBackStack(R.id.eventDetailsFragment, false)
            R.id.welcomeFragment -> finish()
            else -> {
                Toast.makeText(this, "Called4", Toast.LENGTH_SHORT).show()
                super.onBackPressed()
            }
        }
    }

    /**
     * Called by EditProfileFragment to go to previous fragment
     */
    fun onSuperBackPressed() {
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (BuildConfig.FLAVOR == PLAY_STORE_BUILD_FLAVOR && requestCode == RC_CREDENTIALS_READ) {
            if (resultCode == Activity.RESULT_OK) {
                // Fill in the email field in LoginFragment
                val email = SmartAuthUtil.getEmailAddressFromIntent(data)
                if (email != null) {
                    smartAuthViewModel.apply {
                        mutableId.value = email
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}

interface BottomIconDoubleClick {
    fun doubleClick()
}

interface ComplexBackPressFragment {
    fun handleBackPress()
}
