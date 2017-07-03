package com.gogoapps.androidcomponent.usecase

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

abstract class ObservableUseCase<T> :
    Observable<T>(),
    Disposable
{

    companion object {

        operator fun <R> invoke(getObservable: () -> Observable<R>)
            = object : ObservableUseCase0<R>()
        {
            override val getObservable = getObservable
        }

        operator fun <A, R> invoke(getObservable: (arg: A) -> Observable<R>)
            = object : ObservableUseCase1<A, R>()
        {
            override val getObservable = getObservable
        }
    }

    protected abstract val observable: Observable<T>
    protected var disposable: Disposable? = null

    override fun subscribeActual(observer: Observer<in T>)
    {
        observable.subscribe(observer)
    }

    internal fun <T> Observable<T>.init() = doOnSubscribe {
        disposable?.dispose()
        disposable = it
    }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    override fun dispose() {
        disposable?.dispose()
        disposable = null
    }

    override fun isDisposed(): Boolean = disposable?.isDisposed ?: true
}

abstract class ObservableUseCase0<T>(
) :
    ObservableUseCase<T>()
{
    protected abstract val getObservable: () -> Observable<T>

    private var _observable: Observable<T>? = null
    override val observable: Observable<T>
        get() = _observable ?: getObservable().init().also {
            _observable = it
        }

    operator fun invoke(): ObservableUseCase0<T>
    {
        _observable = null
        observable
        return this
    }
}

abstract class ObservableUseCase1<in A, R> :
    ObservableUseCase<R>()
{
    protected abstract val getObservable: (arg: A) -> Observable<R>

    private var _observable: Observable<R>? = null
    override val observable: Observable<R>
        get() = _observable!!

    operator fun invoke(arg: A): Observable<R>
    {
        return getObservable(arg).init().also { _observable = it }
    }
}