package com.gogoapps.androidcomponent.usecase

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

abstract class FlowableUseCase0<T> :
    Flowable<T>(),
    Subscription,
    Disposable
{
    private var subscriber: Subscriber<in T>? = null
    private var disposable: Disposable? = null
    private var complete: Boolean = false
    val isCompleted get() = complete

    val observer = object : Observer<T>
    {

        override fun onSubscribe(d: Disposable) {
            disposable = d
        }

        override fun onComplete() = Unit

        override fun onNext(t: T) {
            subscriber?.onNext(t)
            afterNext(t)
        }

        override fun onError(e: Throwable) {
            subscriber?.onError(e)
        }
    }

    override fun subscribeActual(s: Subscriber<in T>)
    {
        subscriber = s
        s.onSubscribe(this)
    }

    override fun request(amount: Long) {
        if (subscriber != null) {
            getObservable(amount).init().doAfterNext { afterNext(it) }.subscribe(observer)
        }
    }

    override fun cancel()
    {
        complete = true
        subscriber?.onComplete()
        dispose()
        subscriber = null
    }



    override fun isDisposed() = disposable?.isDisposed ?: true

    override fun dispose()
    {
        disposable?.dispose()
        disposable = null
    }

    abstract fun getObservable(amount: Long): Observable<T>

    open fun Observable<T>.init(): Observable<T> =
            subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    protected open var afterNext: (next: T) -> Unit = {}
}

abstract class FlowableUseCase1<in A, R> :
    FlowableUseCase0<R>()
{
    private var arg: A? = null

    operator fun invoke(arg: A) : Flowable<R> {
        this.arg = arg
        return this
    }

    final override fun getObservable(amount: Long) = getObservable(arg!!, amount)

    abstract fun getObservable(arg: A, amount: Long) : Observable<R>

    override fun cancel()
    {
        super.cancel()
        arg = null
    }
}