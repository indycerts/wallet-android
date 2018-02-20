package com.learningmachine.android.app.ui.settings.passphrase;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.learningmachine.android.app.R;
import com.learningmachine.android.app.data.bitcoin.BitcoinManager;
import com.learningmachine.android.app.data.inject.Injector;
import com.learningmachine.android.app.databinding.FragmentRevealPassphraseBinding;
import com.learningmachine.android.app.dialog.AlertDialogFragment;
import com.learningmachine.android.app.ui.LMActivity;
import com.learningmachine.android.app.ui.LMFragment;
import com.learningmachine.android.app.ui.onboarding.OnboardingActivity;
import com.learningmachine.android.app.util.DialogUtils;
import com.smallplanet.labalib.Laba;

import javax.inject.Inject;

public class RevealPassphraseFragment extends LMFragment {

    @Inject BitcoinManager mBitcoinManager;
    private FragmentRevealPassphraseBinding mBinding;

    public static Fragment newInstance() {
        return new RevealPassphraseFragment();
    }

    private String mPassphrase;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.obtain(getContext())
                .inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_reveal_passphrase,
                container,
                false);

        mBitcoinManager.getPassphrase()
                .compose(bindToMainThread())
                .subscribe(this::configureCurrentPassphraseTextView);


        mBinding.onboardingEmailButton.setOnClickListener(view -> onEmail());
        mBinding.onboardingSaveButton.setOnClickListener(view -> onSave());
        mBinding.onboardingWriteButton.setOnClickListener(view -> onWrite());

        mBinding.onboardingSaveCheckmark.setVisibility(View.INVISIBLE);
        mBinding.onboardingEmailCheckmark.setVisibility(View.INVISIBLE);
        mBinding.onboardingWriteCheckmark.setVisibility(View.INVISIBLE);

        return mBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    private void configureCurrentPassphraseTextView(String currentPassphrase) {
        if (mBinding == null) {
            return;
        }

        mPassphrase = currentPassphrase;

        mBinding.onboardingPassphraseContent.setText(currentPassphrase);
    }



    protected void onSave() {
        ((LMActivity)getActivity()).askToSavePassphraseToDevice(mPassphrase, (passphrase) -> {

            if(passphrase == null) {

                DialogUtils.showAlertDialog(getContext(), this,
                        R.drawable.ic_dialog_failure,
                        getResources().getString(R.string.onboarding_passphrase_permissions_error_title),
                        getResources().getString(R.string.onboarding_passphrase_permissions_error),
                        getResources().getString(R.string.onboarding_passphrase_ok),
                        null,
                        (btnIdx) -> {
                            HandleBackupOptionCompleted(null);
                            return null;
                        });
                return null;
            }

            DialogUtils.showAlertDialog(getContext(), this,
                    R.drawable.ic_dialog_success,
                    getResources().getString(R.string.onboarding_passphrase_complete_title),
                    getResources().getString(R.string.onboarding_passphrase_save_complete),
                    getResources().getString(R.string.onboarding_passphrase_ok),
                    null,
                    (btnIdx) -> {
                        if(mBinding != null) {
                            HandleBackupOptionCompleted(mBinding.onboardingSaveCheckmark);
                        }
                        return null;
                    });

            return null;
        });
    }

    protected void onEmail() {
        DialogUtils.showAlertDialog(getContext(), this,
                0,
                getResources().getString(R.string.onboarding_passphrase_email_before_title),
                getResources().getString(R.string.onboarding_passphrase_email_before),
                getResources().getString(R.string.onboarding_passphrase_cancel),
                getResources().getString(R.string.onboarding_passphrase_ok),
                (btnIdx) -> {

                    if((int)btnIdx == 0) {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_SUBJECT, "BlockCerts Backup");
                        intent.putExtra(Intent.EXTRA_TEXT, mPassphrase);
                        Intent mailer = Intent.createChooser(intent, null);
                        startActivity(mailer);

                        if(mBinding != null) {
                            HandleBackupOptionCompleted(mBinding.onboardingEmailCheckmark);
                        }
                    }
                    return null;
                });
    }

    protected void onWrite() {
        AlertDialogFragment fragment = DialogUtils.showCustomDialog(getContext(), this,
                R.layout.dialog_write_passphrase,
                R.drawable.ic_writedown,
                getResources().getString(R.string.onboarding_passphrase_write_title),
                getResources().getString(R.string.onboarding_passphrase_write_message),
                getResources().getString(R.string.onboarding_passphrase_write_confirmation),
                null,
                (btnIdx) -> {
                    if(mBinding != null) {
                        HandleBackupOptionCompleted(mBinding.onboardingWriteCheckmark);
                    }
                    return null;
                },
                (dialogContent) -> {

                    // Set the content of the passphrase text field
                    View view = (View)dialogContent;
                    TextView passphraseView = (TextView)view.findViewById(R.id.onboarding_passphrase_content);
                    passphraseView.setText(mPassphrase);

                    // For this dialog, we want to fill the whole screen regardless of the size of the content
                    // 1) Dialog width should be 80% of the width of the screen
                    Display display = getActivity().getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);

                    float idealDialogWidth = size.x * 0.8f;
                    float idealDialogHeight = size.y * 0.8f;

                    view.setLayoutParams(new FrameLayout.LayoutParams((int) idealDialogWidth, (int) idealDialogHeight));

                    return null;
                });


    }

    public void HandleBackupOptionCompleted(View view) {
        if(view != null) {
            Laba.Animate(view, "!s!f!>", () -> { return null; });
            view.setVisibility(View.VISIBLE);
        }
    }

}
