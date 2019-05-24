package com.example.androidkotlinseed.view.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.androidkotlinseed.R
import com.example.androidkotlinseed.domain.SuperHero
import com.example.androidkotlinseed.injection.BaseActivity
import com.example.androidkotlinseed.mvvm.HeroListViewModel
import com.example.androidkotlinseed.mvvm.ViewModelFactory
import com.example.androidkotlinseed.view.adapters.HeroBindingAdapter
import com.example.androidkotlinseed.view.adapters.HeroesAdapter
import com.example.androidkotlinseed.view.dialogs.CallErrorDialogFragment
import com.example.androidkotlinseed.view.dialogs.DialogsManager

import kotlinx.android.synthetic.main.activity_heroes_list.recycler_heroes as recyclerHeroes
import kotlinx.android.synthetic.main.activity_heroes_list.swipe_layout as swipeRefreshLayout

import javax.inject.Inject

class HeroesListActivity : BaseActivity(), HeroListViewModel.Listener, SwipeRefreshLayout.OnRefreshListener,
    HeroesAdapter.HeroItemClickListener {

    private val TAG = HeroesListActivity::class.simpleName

    @Inject lateinit var dialogsManager: DialogsManager
    @Inject lateinit var viewModelFactory: ViewModelFactory
    @Inject lateinit var heroListViewModel: HeroListViewModel
    @Suppress("unused")
    @Inject lateinit var heroBindingAdapter: HeroBindingAdapter

    private val heroListObserver = Observer<List<SuperHero>> { newList -> run {
        val heroesAdapter = HeroesAdapter(newList, this, this)
        recyclerHeroes.adapter = heroesAdapter }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heroes_list)
        getPresentationComponent().inject(this)

        heroListViewModel = ViewModelProviders.of(this, viewModelFactory).get(HeroListViewModel::class.java)
        heroListViewModel.heroList.observe(this, heroListObserver)

        recyclerHeroes.layoutManager = GridLayoutManager(this, 2)
        recyclerHeroes.setHasFixedSize(true)
    }

    override fun onStart() {
        super.onStart()
        swipeRefreshLayout.setOnRefreshListener(this)
        heroListViewModel.registerListener(this)

        recyclerHeroes.visibility = View.GONE
        swipeRefreshLayout.isRefreshing = true
        this.onRefresh()
    }

    override fun onStop() {
        super.onStop()

        heroListViewModel.unregisterListener(this)
        swipeRefreshLayout.setOnRefreshListener(this)
    }

    override fun onRefresh() {
        heroListViewModel.fetchHeroesAndNotify()
    }

    override fun onHeroesFetched(superHeroes: List<SuperHero>) {
        swipeRefreshLayout.isRefreshing = false
        recyclerHeroes.visibility = View.VISIBLE
        // Uncomment to test liveData
        // Handler().postDelayed({ heroListViewModel.clearList() }, 3500)
    }

    override fun onHeroesFetchFailed(msg: String) {
        Log.e(TAG, "Heroes call failed")
        dialogsManager.showDialogWithId(CallErrorDialogFragment.newInstance(), "")
    }

    override fun onClickHero(superHero: SuperHero) {
    }
}
