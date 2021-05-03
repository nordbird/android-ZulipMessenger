package ru.nordbird.tfsmessenger.ui.mvi.base.presenter

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class RxPresenter<V> protected constructor(viewClass: Class<V>) : BasePresenter<V>(viewClass) {
    private val disposables = CompositeDisposable()

    override fun detachView(isFinishing: Boolean) {
        disposables.clear()
//        if (isFinishing) {
//            disposables.dispose()
//        }
        super.detachView(isFinishing)
    }

    protected fun removeDisposable(disposable: Disposable?) {
        disposable?.let {
            disposables.remove(it)
        }
    }

    protected fun Disposable.disposeOnFinish(): Disposable {
        disposables += this
        return this
    }

    protected fun dispose(disposable: Disposable) {
        if (!disposables.remove(disposable)) {
            disposable.dispose()
        }
    }

    private operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
        add(disposable)
    }
}