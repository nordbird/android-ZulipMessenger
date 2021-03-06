package ru.nordbird.tfsmessenger.ui.mvi.base.presenter

import androidx.annotation.CallSuper
import ru.nordbird.tfsmessenger.utils.reflection.ReflectionUtils

abstract class BasePresenter<View, Action> protected constructor(
    viewClass: Class<View>
) : Presenter<View, Action> {

    private val stubView: View = ReflectionUtils.createStub(viewClass)
    private var realView: View? = null

    val view: View
        get() = realView ?: stubView


    @CallSuper
    override fun attachView(view: View) {
        this.realView = view
    }

    @CallSuper
    override fun detachView(isFinishing: Boolean) {
        realView = null
    }

    fun hasView(): Boolean {
        return view != null
    }
}