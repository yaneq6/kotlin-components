package com.gogoapps.androidcomponents.facebook

import com.facebook.CallbackManager
import com.facebook.login.LoginManager

interface FacebookModule
{
    val facebookRepository: FacebookRepository
}

class FacebookComponent : FacebookModule
{

    val loginManager: LoginManager by lazy { LoginManager.getInstance() }
    val callbackManager: CallbackManager by lazy { CallbackManager.Factory.create() }

    override val facebookRepository by lazy {
        FacebookRepositoryImpl(
            callbackManager,
            loginManager
        )
    }
}