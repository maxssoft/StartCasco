package ru.telematica.casco2go.ui.activity;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.greenrobot.eventbus.EventBus;

import ru.telematica.casco2go.R;
import ru.telematica.casco2go.model.eventbus.KeyboardShowEvent;
import ru.telematica.casco2go.ui.base.MainActivityView;
import ru.telematica.casco2go.ui.base.BaseView;
import ru.telematica.casco2go.ui.presenters.MainPresenter;

public class MainActivity extends MainActivityView implements BaseView {

    private static final int PERMISSION_LOCATION_REQUEST_CODE = 644;

    private View rootView;
    private boolean startOnCreate = false;
    private MainPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Не паримся с восстановлением контекста при рестартах
        super.onCreate(null);
        setContentView(R.layout.activity_main);

        rootView = findViewById(R.id.activity_root);
        setKeyboardObserver();

        presenter = new MainPresenter(this);
        startOnCreate = true;
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.onStartActivity();
        if (startOnCreate) {
            startOnCreate = false;
            presenter.restoreFragment();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.onStopActivity();
    }

    @Override
    protected void onDestroy() {
        presenter.releaseActivity();
        super.onDestroy();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // Прячем клавиатуру, если тапнул по области за пределами EditText и текущий контрол в фокусе EditText
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View currentFocus = getCurrentFocus();
            if (currentFocus != null && currentFocus instanceof EditText) {
                hideKeyboard();
            }
        }

        return super.dispatchTouchEvent(event);
    }

    @Override
    public void showError(String message, Throwable error) {
        //AlertDialog dialog = new AlertDialog.Builder(this, R.style.AppDialog)
        if (error != null && error.getMessage() != null && !error.getMessage().isEmpty()) {
            message = message + "\n\n" + error.getMessage();
        }

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.AppDialog)
                .setTitle(R.string.error_title)
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton(R.string.ok, (dialog1, which) -> {
                    dialog1.dismiss();
                })
                .create();
        dialog.show();
    }

    @Override
    public void hideKeyboard() {
        View currentFocus = getCurrentFocus();
        if (currentFocus == null) {
            currentFocus = getWindow().getDecorView();
        }
        if (currentFocus != null) {
            InputMethodManager mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            mImm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    private void setKeyboardObserver() {
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout () {
                Rect rect = new Rect();
                rootView.getWindowVisibleDisplayFrame(rect);
                int heightDiff = rootView.getRootView().getHeight() - (rect.bottom - rect.top);
                if (heightDiff > 200) {
                    EventBus.getDefault().post(new KeyboardShowEvent(true));
                } else {
                    EventBus.getDefault().post(new KeyboardShowEvent(false));
                }
            }
        });
    }

}
