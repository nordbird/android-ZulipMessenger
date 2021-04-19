package ru.nordbird.tfsmessenger.ui.mvi.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import ru.nordbird.tfsmessenger.ui.mvi.base.presenter.Presenter

abstract class MviFragment<View, P : Presenter<View>> : Fragment(),
    MviViewCallback<View, P> {

    private val mviHelper: MviHelper<View, P> by lazy { MviHelper(this) }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mviHelper.create()
    }

    override fun onDestroyView() {
        val isFinishing = isRemoving || requireActivity().isFinishing
        mviHelper.destroy(isFinishing)
        super.onDestroyView()
    }
}