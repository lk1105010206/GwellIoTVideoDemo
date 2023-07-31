package com.tencentcs.iotvideodemo.base;

import android.content.pm.PackageManager;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.tencentcs.iotvideo.utils.LogUtils;
import com.tencentcs.iotvideodemo.R;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class BaseFragment extends Fragment {
    private static final String TAG = "BaseFragment";

    /**
     * 提示弹窗
     */
    public AlertDialog normalDialog;

    private RxPermissions mRxPermissions;

    private Disposable mPermissionDisposable;

    protected void requestPermissions(final OnPermissionsListener listener, String... permissions) {
        if (isPermissionsGranted(permissions)) {
            if (listener != null) {
                listener.OnPermissions(true);
                return;
            }
        }
        if (mRxPermissions == null) {
            mRxPermissions = new RxPermissions(this);
        }
        if (mPermissionDisposable != null && !mPermissionDisposable.isDisposed()) {
            mPermissionDisposable.isDisposed();
            LogUtils.e(TAG, "stop last permissions request");
        }
        mPermissionDisposable = mRxPermissions
                .request(permissions)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean granted) throws Exception {
                        if (listener != null) {
                            listener.OnPermissions(granted);
                        }
                    }
                });
    }

    protected boolean isPermissionsGranted(String[] permissions) {
        if (getActivity() == null) {
            return false;
        }
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    protected boolean shouldShowRequestPermissionRationale(String[] permissions) {
        if (getActivity() == null) {
            return false;
        }
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission)) {
                return false;
            }
        }
        return true;
    }

    protected interface OnPermissionsListener {
        void OnPermissions(boolean granted);
    }

    /**
     * 弹窗取消或确认监听
     */
    public interface CancelOrConfirmListener{
        /**
         * 弹窗确认
         */
        void onConfirm();

        /**
         * 弹窗取消
         */
        void onCancel();
    }

    /**
     * 显示提示弹窗
     */
    public void showTipDialog(String title, String message, CancelOrConfirmListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(getResources().getString(R.string.confirm),
                (dialog, which) -> {
                    if (null != listener) {
                        listener.onConfirm();
                    }
                });
        builder.setNegativeButton(getResources().getString(R.string.cancel),
                (dialog, which) -> {
                    if (null != listener) {
                        listener.onCancel();
                    }
                });
        if (null == normalDialog) {
            normalDialog = builder.create();
        }
        if (!normalDialog.isShowing()) {
            normalDialog.show();
        }
    }

    /**
     * 隐藏删除设备弹出框
     */
    public void dismissTipDialog() {
        if (null != normalDialog && normalDialog.isShowing()) {
            normalDialog.dismiss();
        }
    }
}
