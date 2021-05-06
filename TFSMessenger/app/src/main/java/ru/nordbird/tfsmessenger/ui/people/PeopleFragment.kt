package ru.nordbird.tfsmessenger.ui.people

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import ru.nordbird.tfsmessenger.App
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.databinding.FragmentPeopleBinding
import ru.nordbird.tfsmessenger.extensions.isFinishing
import ru.nordbird.tfsmessenger.extensions.userMessage
import ru.nordbird.tfsmessenger.ui.main.MainActivity
import ru.nordbird.tfsmessenger.ui.mvi.base.MviFragment
import ru.nordbird.tfsmessenger.ui.people.base.PeopleAction
import ru.nordbird.tfsmessenger.ui.people.base.PeoplePresenter
import ru.nordbird.tfsmessenger.ui.people.base.PeopleUiEffect
import ru.nordbird.tfsmessenger.ui.people.base.PeopleView
import ru.nordbird.tfsmessenger.ui.recycler.adapter.Adapter
import ru.nordbird.tfsmessenger.ui.recycler.base.*
import ru.nordbird.tfsmessenger.ui.recycler.holder.*
import ru.nordbird.tfsmessenger.ui.rx.RxSearchObservable
import javax.inject.Inject

class PeopleFragment : MviFragment<PeopleView, PeopleAction, PeoplePresenter>(), PeopleView {

    private var _binding: FragmentPeopleBinding? = null
    private val binding get() = _binding!!
    private lateinit var activityListener: PeopleFragmentListener

    @Inject
    lateinit var peoplePresenter: PeoplePresenter

    private val compositeDisposable = CompositeDisposable()

    private val clickListener: ViewHolderClickListener = object : ViewHolderClickListener {
        override fun onViewHolderClick(holder: BaseViewHolder<*>, view: View, clickType: ViewHolderClickType?) {
            when (holder.itemViewType) {
                R.layout.item_user -> onUserClick(holder)
                R.layout.item_error -> onReloadClick()
            }
        }

        override fun onViewHolderLongClick(holder: BaseViewHolder<*>, view: View): Boolean = true
    }

    private val holderFactory = TfsHolderFactory(clickListener = clickListener)
    private val diffUtilCallback = DiffUtilCallback<ViewTyped>()
    private val adapter = Adapter(holderFactory, diffUtilCallback)

    private var lastState: PeopleState = PeopleState()

    override fun getPresenter(): PeoplePresenter = peoplePresenter

    override fun getMviView(): PeopleView = this

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is PeopleFragmentListener) {
            activityListener = context
        } else {
            throw RuntimeException("$context must implement PeopleFragmentListener")
        }
        App.instance.providePeopleComponent().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lastState = lastState.copy(filterQuery = savedInstanceState?.getString(STATE_LAST_QUERY, "") ?: "")
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPeopleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initToolbar()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView

        val searchObservable = RxSearchObservable.fromView(searchView)
        val searchDisposable = searchObservable.map { PeopleAction.SearchUsers(it) }.subscribe(getPresenter().input)
        compositeDisposable.add(searchDisposable)

        if (lastState.filterQuery.isNotBlank()) {
            val query = lastState.filterQuery
            searchItem.expandActionView()
            searchView.setQuery(query, true)
            searchView.clearFocus()
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_LAST_QUERY, lastState.filterQuery)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
    }

    override fun onDetach() {
        if (isFinishing()) App.instance.clearPeopleComponent()
        super.onDetach()
    }

    private fun initToolbar() {
        with(activity as AppCompatActivity) {
            setSupportActionBar(binding.appbar.toolbar)
            supportActionBar?.title = getString(R.string.title_people)
        }
    }

    private fun initUI() {
        binding.rvUsers.adapter = adapter

        getPresenter().input.accept(PeopleAction.LoadUsers)

        val disposable = adapter.updateAction.subscribe {
            if (lastState.needScroll) {
                binding.rvUsers.layoutManager?.scrollToPosition(0)
            }
        }
        compositeDisposable.add(disposable)
    }

    override fun render(state: PeopleState) {
        lastState = state
        adapter.items = state.items
    }

    override fun handleUiEffect(uiEffect: PeopleUiEffect) {
        when (uiEffect) {
            is PeopleUiEffect.ActionError -> {
                showError(uiEffect.error)
            }
        }
    }

    private fun showError(throwable: Throwable) {
        val view = (requireActivity() as MainActivity).rootView
        Snackbar.make(view, throwable.userMessage(requireContext()), Snackbar.LENGTH_SHORT).show()
    }

    private fun onUserClick(holder: BaseViewHolder<*>) {
        val user = adapter.items[holder.absoluteAdapterPosition] as UserUi
        activityListener.onOpenUserProfile(user.id)
    }

    private fun onReloadClick() {
        getPresenter().input.accept(PeopleAction.LoadUsers)
    }

    interface PeopleFragmentListener {
        fun onOpenUserProfile(userId: Int)
    }

    companion object {
        private const val STATE_LAST_QUERY = "state_last_query"
    }

}