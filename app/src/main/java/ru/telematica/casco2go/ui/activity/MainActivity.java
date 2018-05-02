package ru.telematica.casco2go.ui.activity;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import ru.telematica.casco2go.R;
import ru.telematica.casco2go.ui.base.MainActivityView;
import ru.telematica.casco2go.ui.base.BaseView;
import ru.telematica.casco2go.ui.presenters.MainPresenter;

public class MainActivity extends MainActivityView implements BaseView {

    private static final int PERMISSION_LOCATION_REQUEST_CODE = 644;

    private MainPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Не паримся с восстановлением контекста при рестартах
        super.onCreate(null);
        setContentView(R.layout.activity_main);

        presenter = new MainPresenter(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.onStartActivity();
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
    public void showError(String message) {
        //AlertDialog dialog = new AlertDialog.Builder(this, R.style.AppDialog)
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

}
