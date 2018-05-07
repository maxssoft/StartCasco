package ru.telematica.casco2go.ui.fragments

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.telematica.casco2go.App
import ru.telematica.casco2go.R
import ru.telematica.casco2go.model.ScoringData
import ru.telematica.casco2go.ui.adapters.HistoryListAdapter
import ru.telematica.casco2go.ui.adapters.pagination.PaginationAdapterListener
import io.reactivex.Completable

/**
 * Created by m.sidorov on 01.05.2018.
 */
class HistoryFragment : BaseFragment(){

    @BindView(R.id.backButton)
    lateinit var backButton: View

    @BindView(R.id.toolbarTitle)
    lateinit var textTitle: TextView

    @BindView(R.id.loaderView)
    lateinit var loaderView: View

    @BindView(R.id.emptyResView)
    lateinit var emptyResView: View

    @BindView(R.id.emptyTitleText)
    lateinit var emptyTitleText: TextView

    @BindView(R.id.historyList)
    lateinit var historyList: RecyclerView

    companion object {
        @JvmStatic
        fun newInstance(): HistoryFragment {
            return HistoryFragment()
        }
    }

    private lateinit var presenter: HistoryPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = HistoryPresenter(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view!!)

        updateInfo()
    }

    private fun updateInfo() {
        textTitle.setText(R.string.trip_history_title)
        backButton.setOnClickListener { backClick() }
        emptyTitleText.setText(R.string.trip_history_empty)

        // val decorator = GridSpacesItemDecoration(PixelUtils.dpToPx(1))
        historyList.setLayoutManager(LinearLayoutManager(activity))
        historyList.setAdapter(presenter.adapter)

        presenter.loadData()
    }

    private fun backClick(){
        activity?.supportFragmentManager?.popBackStack()
    }

    override fun onDetach() {
        super.onDetach()
        presenter.release()
    }

    fun setLoading(value: Boolean){
        if (value) {
            loaderView.visibility = View.VISIBLE
        } else {
            loaderView.visibility = View.INVISIBLE
        }
    }

    fun setNoData(value: Boolean){
        if (value) {
            emptyResView.visibility = View.VISIBLE
            historyList.visibility = View.INVISIBLE
        } else {
            emptyResView.visibility = View.INVISIBLE
            historyList.visibility = View.VISIBLE
        }
    }

}

class HistoryPresenter(val view: HistoryFragment) : PaginationAdapterListener  {

    private var subscriptions: CompositeDisposable = CompositeDisposable()
    private var allDataLoaded: Boolean = false

    val adapter: HistoryListAdapter = HistoryListAdapter(this)

    fun loadData(){
        adapter.items.clear()
        adapter.reset()
        load(null)
    }

    override fun onLoad(){
        load(adapter.items.lastOrNull())
    }

    fun release(){
        disposeSubscriptions()
    }

    private fun setLoading(value: Boolean){
        adapter.setLoading(value)
        view.setLoading(value)
    }

    private fun load(lastItem: ScoringData?){
        setLoading(true)
        subscriptions.add(
                App.instance.httpService
                        .loadHistory(lastItem)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            setLoading(false)
                            addItems(it)
                            if (it.size < App.HISTORY_PAGE_SIZE){
                                adapter.setHasNoMoreData()
                            }
                        }, {
                            setLoading(false)
                            view.showError(view.getString(R.string.error_server_unknown), it)
                        }))
    }

    private fun addItems(items: List<ScoringData>){
        val startIndex = adapter.itemsCount
        adapter.items.addAll(items)
        adapter.notifyItemRangeChanged(startIndex, adapter.itemsCount - startIndex)

        view.setNoData(adapter.items.isEmpty())
    }


    @Synchronized
    private fun disposeSubscriptions(){
        subscriptions.dispose()
        subscriptions = CompositeDisposable()
    }

}

