package com.gogoapps.androidcomponents.facebook

import android.app.Activity
import android.content.Intent
import com.facebook.login.LoginResult
import io.reactivex.Observable


interface FacebookRepository {

    fun requestFacebookToken(activity: Activity): Observable<LoginResult>

    fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent
    )
}