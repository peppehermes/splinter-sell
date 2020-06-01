package it.polito.mad.splintersell.ui.profile_edit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import it.polito.mad.splintersell.data.UserModel

class EditProfileViewModel : ViewModel() {
    private var _user: MutableLiveData<UserModel> = MutableLiveData()
    private var _photo: MutableLiveData<String>? = MutableLiveData()
    private var _oldNick: MutableLiveData<String> = MutableLiveData()

    init {
        _user.value = UserModel()
        _user.value!!.userId = FirebaseAuth.getInstance().currentUser!!.uid
    }

    internal var user: MutableLiveData<UserModel>
        get() {
            return _user
        }
        set(value) {
            _user = value
        }

    internal var photo: MutableLiveData<String>?
        get() {
            return _photo
        }
        set(value) {
            _photo = value
        }

    internal var oldNick: MutableLiveData<String>
        get() {
            return _oldNick
        }
        set(value) {
            _oldNick = value
        }
}