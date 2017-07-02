package com.gogoapps.androidcomponents.facebook

import android.app.Activity
import android.content.Intent
import com.facebook.CallbackManager
import com.facebook.FacebookAuthorizationException
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import io.reactivex.Observable
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

class FacebookRepositoryImpl(
    val callbackManager: CallbackManager,
    val loginManager: LoginManager
) : FacebookRepository
{

    val permissions = listOf("email")
    val subscribers: MutableList<Subscriber<in LoginResult>> = mutableListOf()

    init
    {
        loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult>
        {
            override fun onCancel()
            {
                subscribers.forEach {
                    it.onComplete()
                }
                subscribers.clear()
            }

            override fun onError(e: FacebookException)
            {
                val exception = if (e is FacebookAuthorizationException && e.localizedMessage == "net::ERR_INTERNET_DISCONNECTED")
                    NoConnectionException() else e
                subscribers.forEach { it.onError(exception) }
                subscribers.clear()
            }

            override fun onSuccess(loginResult: LoginResult)
            {
                subscribers.forEach { it.onNext(loginResult) }
                subscribers.clear()
            }

        })
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent
    )
    {
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    override fun requestFacebookToken(activity: Activity): Observable<LoginResult>
    {
        return Observable.fromPublisher {
            it.onSubscribe(subscription(it, activity))
            subscribers.add(it)
        }
    }

    private fun subscription(
        subscriber: Subscriber<in LoginResult>,
        activity: Activity
    ) = object : Subscription
    {
        override fun cancel()
        {
            subscribers.remove(subscriber)
            subscriber.onComplete()
        }

        override fun request(n: Long)
        {
            loginManager.logInWithReadPermissions(activity, permissions)
        }
    }
}
