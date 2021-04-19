package ru.nordbird.tfsmessenger.ui.people

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.databinding.FragmentPeopleBinding
import ru.nordbird.tfsmessenger.ui.recycler.adapter.Adapter
import ru.nordbird.tfsmessenger.ui.recycler.base.BaseViewHolder
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickListener
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickType
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.*
import ru.nordbird.tfsmessenger.ui.rx.RxSearchObservable

class PeopleFragment : Fragment() {

    private var _binding: FragmentPeopleBinding? = null
    private val binding get() = _binding!!
    private lateinit var activityListener: PeopleFragmentListener

    private val userInteractor = PeopleInteractor
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

        val searchObservable = RxSearchObservable.fromView(searchView)
        val searchDisposable = userInteractor.filterUsers(searchObservable)
            .subscribe {
                adapter.items = it
                binding.rvUsers.layoutManager?.scrollToPosition(0)
            }
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

        updateUsers()
    }

    private fun updateUsers() {
        showShimmer()
        val usersDisposable = userInteractor.getUsers()
            .filter { it.isNotEmpty() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe (
                { adapter.items = it },
                { showError(it) }
            )
        compositeDisposable.add(usersDisposable)
    }

    private fun showError(throwable: Throwable) {
        adapter.items = listOf(ErrorUi())
        Snackbar.make(binding.root, throwable.message.toString(), Snackbar.LENGTH_SHORT).show()
    }

    private fun showShimmer() {
        adapter.items = listOf(UserShimmerUi(), UserShimmerUi(), UserShimmerUi())
    }

    private fun onUserClick(holder: BaseViewHolder<*>) {
        activityListener.onOpenUserProfile(adapter.items[holder.absoluteAdapterPosition].uid)
    }

    private fun onReloadClick() {
        updateUsers()
    }

    interface PeopleFragmentListener {
        fun onOpenUserProfile(userId: String)
    }

}