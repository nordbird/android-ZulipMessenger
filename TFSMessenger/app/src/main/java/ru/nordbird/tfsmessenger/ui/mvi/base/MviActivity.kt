package ru.nordbird.tfsmessenger.ui.mvi.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.nordbird.tfsmessenger.ui.mvi.base.presenter.Presenter

abstract class MviActivity<View, Action, P : Presenter<View, Action>> : AppCompatActivity(),
    MviViewCallback<View, Action, P> {

    private val mviHelper: MviHelper<View, Action, P> by lazy { MviHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mviHelper.create()
    }

    override fun onDestroy() {
        mviHelper.destroy(isFinishing)
        super.onDestroy()
    }
}