package ru.telematica.casco2go.ui.fragments

import android.os.Bundle
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
import ru.telematica.casco2go.utils.JsonUtils

/**
 * Created by m.sidorov on 01.05.2018.
 */
class ScoringFragment : BaseFragment() {

    @BindView(R.id.backButton)
    lateinit var backButton: View

    @BindView(R.id.toolbarTitle)
    lateinit var textTitle: TextView

    companion object {
        @JvmStatic
        fun newInstance(data: ScoringData): ScoringFragment {
            val json = JsonUtils.toJson(data)
            val bundle = Bundle()
            bundle.putString(TripStatisticFragment.SCORING_DATA_PARAM, json)
            val fragment = ScoringFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_scoring, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view!!)

        val fragment = TripStatisticFragment.newInstance(arguments)
        childFragmentManager
                .beginTransaction()
                .replace(R.id.statistic_container, fragment, "statisticFragment")
                .commit()

        textTitle.setText(R.string.scoring_title)
        backButton.setOnClickListener { backClick() }
    }

    private fun backClick(){
        activity?.supportFragmentManager?.popBackStack()
    }

}