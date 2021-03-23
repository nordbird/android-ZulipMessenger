package ru.nordbird.tfsmessenger.ui.people

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.databinding.FragmentPeopleBinding
import ru.nordbird.tfsmessenger.ui.recycler.adapter.Adapter
import ru.nordbird.tfsmessenger.ui.recycler.base.BaseViewHolder
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickListener
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickType
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.TfsHolderFactory
import ru.nordbird.tfsmessenger.ui.recycler.holder.UserUi

class PeopleFragment : Fragment() {

    private var _binding: FragmentPeopleBinding? = null
    private val binding get() = _binding!!
    private lateinit var activityListener: PeopleFragmentListener

    private val userInteractor = PeopleInteractor

    private val clickListener: ViewHolderClickListener = object : ViewHolderClickListener {
        override fun onViewHolderClick(holder: BaseViewHolder<*>, view: View, clickType: ViewHolderClickType?) {
            onUserClick(holder)
        }

        override fun onViewHolderLongClick(holder: BaseViewHolder<*>, view: View): Boolean = true
    }
    private val searchListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return sendFilterQuery(query)
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            return sendFilterQuery(newText)
        }
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
        updateUsers()

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView
        searchView.setOnQueryTextListener(searchListener)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onStart() {
        super.onStart()
        initToolbar()
    }

    private fun initToolbar() {
        with(activity as AppCompatActivity) {
            setSupportActionBar(binding.appbar.toolbar)
            supportActionBar?.title = getString(R.string.title_people)
        }
    }

    private fun sendFilterQuery(query: String?): Boolean {
        if (query != null) {
            userInteractor.filterUsers(query)
            updateUsers()
        }
        return true
    }

    private fun updateUsers() {
        adapter.items = userInteractor.getUsers()
    }

    private fun initUI() {
        binding.rvUsers.adapter = adapter
    }

    private fun onUserClick(holder: BaseViewHolder<*>) {
        val user = userInteractor.getUser(holder.itemId) ?: return
        activityListener.onOpenUserProfile(user)
    }

    interface PeopleFragmentListener {
        fun onOpenUserProfile(user: UserUi)
    }

}