package it.polito.mad.splintersell.ui.sign_in

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SignInViewModel : ViewModel() {
    enum class AuthenticationState {
        UNAUTHENTICATED,        // Initial state, the user needs to authenticate
        AUTHENTICATED,        // The user has authenticated successfully
        INVALID_AUTHENTICATION  // Authentication failed
    }

    val authenticationState = MutableLiveData<AuthenticationState>()

    init {
        // In this example, the user is always unauthenticated when MainActivity is launched
        authenticationState.value =
            AuthenticationState.UNAUTHENTICATED
    }

    fun refuseAuthentication() {
        authenticationState.value = AuthenticationState.UNAUTHENTICATED
    }

    fun invalidAuthentication() {
        authenticationState.value = AuthenticationState.INVALID_AUTHENTICATION
    }

    fun authenticate() {
        authenticationState.value = AuthenticationState.AUTHENTICATED
    }


}
