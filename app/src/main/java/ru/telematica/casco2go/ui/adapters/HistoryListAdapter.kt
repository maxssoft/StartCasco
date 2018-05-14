package ru.telematica.casco2go.ui.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import org.greenrobot.eventbus.EventBus
import ru.telematica.casco2go.ui.adapters.pagination.*
import java.util.ArrayList

import ru.telematica.casco2go.R
import ru.telematica.casco2go.model.ScoringData
import ru.telematica.casco2go.model.eventbus.OpenFragmentEvent
import ru.telematica.casco2go.ui.fragments.FragmentTypes
import ru.telematica.casco2go.utils.DateUtils
import ru.telematica.casco2go.utils.isNull
import java.text.SimpleDateFormat

/**
 * Created by m.sidorov on 05.05.2018.
 */
class HistoryListAdapter(listener: PaginationAdapterListener) : PaginationLoaderAdapter(listener) {

    private val HISTORY_ITEM_TYPE = 1;

    var items: ArrayList<ScoringData> = ArrayList()

    init {
        loaderBackground = android.R.color.transparent
        loaderLayoutId = 0
        offsetToLoad = 8
    }

    override fun getItemsCount(): Int {
        return items.size
    }

    override fun getItemsViewType(position: Int): Int {
        return HISTORY_ITEM_TYPE
    }

    override fun onCreateViewHolders(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        when (viewType){
            HISTORY_ITEM_TYPE -> {
                val view = LayoutInflater.from(parent?.getContext()).inflate(R.layout.item_history_layout, parent, false)
                return ViewHolderHistoryItem(view)
            }
            else -> return null
        }
    }

    override fun onBindViewHolders(holder: RecyclerView.ViewHolder?, position: Int) {
        when (holder?.getItemViewType()) {
            HISTORY_ITEM_TYPE -> (holder as ViewHolderHistoryItem).setData(items.get(position))
        }
    }

    override fun isItemsEmpty(): Boolean {
        return items.isEmpty()
    }

    inner class ViewHolderHistoryItem : RecyclerView.ViewHolder, View.OnClickListener {

        var indicator: View? = null
        var dateTrip: TextView? = null
        var costTrip: TextView? = null

        lateinit var item: ScoringData

        constructor(root: View) : super(root) {

            indicator = root.findViewById(R.id.indicator)
            dateTrip = root.findViewById(R.id.dateTrip)
            costTrip = root.findViewById(R.id.costTrip)

            root.setOnClickListener(this)
        }

        fun setData(item: ScoringData) {
            this.item = item

            when(item.tripStatus){
                ScoringData.TripStatus.LEVEL50 -> indicator?.background = itemView?.context?.resources?.getDrawable(R.drawable.circle_indicator_orange)
                ScoringData.TripStatus.LEVEL65 -> indicator?.background = itemView?.context?.resources?.getDrawable(R.drawable.circle_indicator_yellow)
                ScoringData.TripStatus.LEVEL80 -> indicator?.background = itemView?.context?.resources?.getDrawable(R.drawable.circle_indicator_green)
                else -> indicator?.background = itemView?.context?.resources?.getDrawable(R.drawable.circle_indicator_red)
            }

            if (item.startTime != null){
                dateTrip?.setText(SimpleDateFormat(DateUtils.DATETIME_VIEW_FORMAT).format(item.startTime))
            }
            costTrip?.setText(itemView?.context?.getString(R.string.price, item.tripCost));
        }

        override fun onClick(view: View) {
            val event = OpenFragmentEvent(FragmentTypes.SCORING_FRAGMENT, true, false)
            event.data = item
            EventBus.getDefault().post(event)
        }
    }

}

