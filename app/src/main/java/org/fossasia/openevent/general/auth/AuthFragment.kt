package org.fossasia.openevent.general.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_auth.view.*
import org.fossasia.openevent.general.BuildConfig
import org.fossasia.openevent.general.PLAY_STORE_BUILD_FLAVOR
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.Utils.hideSoftKeyboard
import org.fossasia.openevent.general.utils.Utils.show
import org.fossasia.openevent.general.utils.Utils.progressDialog
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.design.snackbar
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class AuthFragment : Fragment() {
    private lateinit var rootView: View
    private val authViewModel by viewModel<AuthViewModel>()
    private val safeArgs: AuthFragmentArgs by navArgs()
    private val smartAuthViewModel by sharedViewModel<SmartAuthViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.FLAVOR == PLAY_STORE_BUILD_FLAVOR) {
            smartAuthViewModel.requestCredentials(SmartAuthUtil.getCredentialsClient(requireActivity()))
            smartAuthViewModel.isCredentialStored
                .nonNull()
                .observe(this, Observer {
                    if (it) redirectToLogin()
                })
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_auth, container, false)

        Utils.setToolbar(activity, "", true)
        setHasOptionsMenu(true)

        val progressDialog = progressDialog(context)

        val snackbarMessage = safeArgs.snackbarMessage
        if (!snackbarMessage.isNullOrEmpty()) rootView.snackbar(snackbarMessage)

        rootView.getStartedButton.setOnClickListener {
            hideSoftKeyboard(context, rootView)
            authViewModel.checkUser(rootView.email.text.toString())
        }

        authViewModel.isUserExists
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                if (it)
                    redirectToLogin(rootView.email.text.toString())
                else
                    redirectToSignUp()
                authViewModel.mutableStatus.postValue(null)
            })

        authViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                progressDialog.show(it)
            })

        smartAuthViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                progressDialog.show(it)
            })

        authViewModel.error
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.rootLayout.longSnackbar(it)
            })

        return rootView
    }

    private fun redirectToLogin(email: String = "") {
        Navigation.findNavController(rootView)
            .navigate(AuthFragmentDirections
                .actionAuthToLogIn(email, safeArgs.redirectedFrom)
            )
    }

    private fun redirectToSignUp() {
        val extras = FragmentNavigatorExtras(rootView.email to getString(R.string.transitionName))
        Navigation.findNavController(rootView)
            .navigate(AuthFragmentDirections
                .actionAuthToSignUp(rootView.email.text.toString(), safeArgs.redirectedFrom), extras
            )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
