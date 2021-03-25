package ru.nordbird.tfsmessenger.ui.people

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.data.model.Resource
import ru.nordbird.tfsmessenger.data.model.Status
import ru.nordbird.tfsmessenger.databinding.FragmentPeopleBinding
import ru.nordbird.tfsmessenger.ui.recycler.adapter.Adapter
import ru.nordbird.tfsmessenger.ui.recycler.base.BaseViewHolder
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickListener
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickType
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.ErrorUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.TfsHolderFactory
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserUi
import ru.nordbird.tfsmessenger.ui.rx.RxSearchObservable

class PeopleFragment : Fragment() {

    private var _binding: FragmentPeopleBinding? = null
    private val binding get() = _binding!!
    private lateinit var activityListener: PeopleFragmentListener

    private val userInteractor = PeopleInteractor
    private val compositeDisposable = CompositeDisposable()
    private lateinit var searchObservable: Observable<String>

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
        initUI()

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView

        searchObservable = RxSearchObservable.fromView(searchView)
        val searchDisposable = userInteractor.filterUsers(searchObservable)
        compositeDisposable.add(searchDisposable)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onStart() {
        super.onStart()
        initToolbar()
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

        val usersDisposable = userInteractor.getUsers()
            .subscribe { updateUsers(it) }
        compositeDisposable.add(usersDisposable)
    }

    private fun updateUsers(resource: Resource<List<UserUi>>) {
        when (resource.status) {
            Status.ERROR -> adapter.items = listOf(ErrorUi())
            else -> adapter.items = resource.data ?: emptyList()
        }
    }

    private fun onUserClick(holder: BaseViewHolder<*>) {
        userInteractor.getUser(holder.itemId)
            .subscribe {
                activityListener.onOpenUserProfile(it)
            }
            .dispose()
    }

    private fun onReloadClick() {
        userInteractor.loadUsers()
    }

    interface PeopleFragmentListener {
        fun onOpenUserProfile(user: UserUi)
    }

}