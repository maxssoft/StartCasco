package ru.telematica.casco2go.ui.adapters.pagination;

import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import kotlin.jvm.Synchronized;
import ru.telematica.casco2go.R;

/**
 * Created by robert on 12.10.17.
 */

public abstract class PaginationLoaderAdapter extends RecyclerView.Adapter {

    private WeakReference<PaginationAdapterListener> listener;

    protected PaginationAdapterListener getListener() {
        return listener.get();
    }

    private RecyclerView.OnScrollListener scrollListener;

    protected final static int LOADER_VIEW_TYPE = -1;

    protected boolean loading = false;

    @Synchronized
    public void setLoading(boolean value) {
        loading = value;
    }

    protected boolean canLoadNextPage = true;

    @Synchronized
    public void setHasNoMoreData() {
        canLoadNextPage = false;
        notifyDataSetChanged();
    }

    @Synchronized
    public void reset() {
        loading = false;
        canLoadNextPage = true;
        notifyDataSetChanged();
    }

    protected RecyclerView recyclerView;

    private static final int DEFAULT_OFFSET_TO_LOAD = 8;

    protected int offsetToLoad = DEFAULT_OFFSET_TO_LOAD;

    private static Parcelable recyclerState = null;

    protected Integer loaderBackground;

    protected int loaderLayoutId = R.layout.view_recycler_footer_loader;

    public PaginationLoaderAdapter(PaginationAdapterListener listener) {
        this.listener = new WeakReference<>(listener);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;

        if (scrollListener != null) {
            recyclerView.removeOnScrollListener(scrollListener);
        }
        scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy <= 0) return;
                if (!loading && getItemsCount() > 0 && canLoadNextPage) {
                    loadDataIfNeed();
                }
            }
        };

        recyclerView.addOnScrollListener(scrollListener);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = null;
        if (scrollListener != null) {
            recyclerView.removeOnScrollListener(scrollListener);
        }
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Synchronized
    public void loadDataIfNeed() {
        if (loading){
            return;
        }
        LinearLayoutManager layoutManager = ((LinearLayoutManager) getLayoutManager());
        if (layoutManager != null && layoutManager.findLastVisibleItemPosition() >= getItemCount() - offsetToLoad) {
            setLoading(true);
            getListener().onLoad();
        }
    }

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == LOADER_VIEW_TYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(loaderLayoutId, parent, false);
            return new LoaderViewHolder(view);
        } else {
            return onCreateViewHolders(parent, viewType);
        }
    }

    @Override
    public final void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == LOADER_VIEW_TYPE) {
            ((LoaderViewHolder) holder).setView();
        } else
        if (position >= 0 && position < getItemCount()){
            onBindViewHolders(holder, position);
        }
    }

    @Override
    public final int getItemCount() {
        if (getItemsCount() == 0) {
            return 0;
        } else if (canLoadNextPage && loaderLayoutId > 0) {
            return getItemsCount() + 1;
        } else {
            return getItemsCount();
        }
    }

    @Override
    public final int getItemViewType(int position) {
        if (canLoadNextPage && loaderLayoutId > 0 && position >= 0 && position == getItemCount() - 1) {
            return LOADER_VIEW_TYPE;
        } else
            return getItemsViewType(position);
    }

    public class LoaderViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.recyclerFooterLoaderView)
        View recyclerFooterLoaderView;

        LoaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void setView() {
            if (loaderBackground != null) {
                recyclerFooterLoaderView.setBackgroundColor(loaderBackground);
            }
        }
    }

    private RecyclerView.LayoutManager getLayoutManager() {
        return recyclerView != null ? recyclerView.getLayoutManager() : null;
    }

    public void onSaveInstanceState() {
        if (getLayoutManager() != null) {
            recyclerState = getLayoutManager().onSaveInstanceState();
        }
    }

    public void onRestoreInstanceState() {
        if (recyclerState != null && getLayoutManager() != null) {
            getLayoutManager().onRestoreInstanceState(recyclerState);
        }
    }

    public abstract boolean isItemsEmpty();

    public abstract int getItemsCount();

    public abstract int getItemsViewType(int position);

    public abstract RecyclerView.ViewHolder onCreateViewHolders(ViewGroup parent, int viewType);

    public abstract void onBindViewHolders(RecyclerView.ViewHolder holder, int position);
}
