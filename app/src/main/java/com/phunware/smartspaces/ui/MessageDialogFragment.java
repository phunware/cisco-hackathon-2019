package com.phunware.smartspaces.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.phunware.smartspaces.R;
import com.phunware.smartspaces.mapping.NavigateToPoiActivity;

public class MessageDialogFragment extends DialogFragment {

    private static final String ARG_TAG = "arg_tag";
    public static final String ARG_TITLE = "arg_title";
    public static final String ARG_MESSAGE = "arg_message";
    private String tag;

    public interface OnNavigateClickedListener {
        void onNavigate(@Nullable String tag);
    }

    public static MessageDialogFragment newInstance(@Nullable String title, @NonNull String message) {

        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);

        MessageDialogFragment fragment = new MessageDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        tag = getArguments().getString(ARG_TAG);
        final String title = getArguments().getString(ARG_TITLE);
        final String message = getArguments().getString(ARG_MESSAGE);

        if(title != null) {
            builder.setTitle(title);
        }

        builder.setMessage(message);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //navigateToRoom();
            }
        });

        /*builder.setNegativeButton(R.string.cancel, ((dialog, which) -> {
            dialog.dismiss();
        }));*/

        return builder.show();
    }

    private void navigateToRoom(){
        Intent navigationIntent = new Intent(getContext(), NavigateToPoiActivity.class);
        startActivity(navigationIntent);
    }


}
