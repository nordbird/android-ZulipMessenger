package ru.nordbird.tfsmessenger.ui.mvi.base.presenter

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class RxPresenter<V, Action> protected constructor(viewClass: Class<V>) : BasePresenter<V, Action>(viewClass) {
    private val disposables = CompositeDisposable()

    override fun detachView(isFinishing: Boolean) {
        disposables.clear()
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