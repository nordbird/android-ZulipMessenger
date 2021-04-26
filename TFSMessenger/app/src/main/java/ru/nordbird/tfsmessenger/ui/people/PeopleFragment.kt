package ru.nordbird.tfsmessenger.ui.people

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.databinding.FragmentPeopleBinding
import ru.nordbird.tfsmessenger.di.GlobalDI
import ru.nordbird.tfsmessenger.ui.mvi.base.MviFragment
import ru.nordbird.tfsmessenger.ui.recycler.adapter.Adapter
import ru.nordbird.tfsmessenger.ui.recycler.base.BaseViewHolder
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickListener
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickType
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.*
import ru.nordbird.tfsmessenger.ui.rx.RxSearchObservable

class PeopleFragment : MviFragment<PeopleView, PeoplePresenter>(), PeopleView {

    private var _binding: FragmentPeopleBinding? = null
    private val binding get() = _binding!!
    private lateinit var activityListener: PeopleFragmentListener

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
    private val adapter = Adapter<ViewTyped>(holderFactory)

    override fun getPresenter(): PeoplePresenter = GlobalDI.INSTANCE.peoplePresenter

    override fun getMviView(): PeopleView = this

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is PeopleFragmentListener) {
            activityListener = context
        } else {
            throw RuntimeException("$context must implement PeopleFragmentListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
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
    }

    override fun render(state: PeopleState) {
        adapter.items = state.items

        state.error?.let { throwable -> showError(throwable) }
    }

    override fun handleUiEffect(uiEffect: PeopleUiEffect) {
        when (uiEffect) {
            is PeopleUiEffect.SearchUsersError -> {
                showError(uiEffect.error)
            }
        }
    }

    private fun showError(throwable: Throwable) {
        Snackbar.make(binding.root, throwable.message.toString(), Snackbar.LENGTH_SHORT).show()
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

}