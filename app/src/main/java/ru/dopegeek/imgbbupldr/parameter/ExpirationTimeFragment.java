package ru.dopegeek.imgbbupldr.parameter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

public class ExpirationTimeFragment extends DialogFragment {

    public static Long expTimeValue = 2592000L;
    public final ArrayList<Pair<String, Long>> expirationVariants = new ArrayList<>();
    final String TAG = "Time";
    private final String SAVED_EXP_TIME = "expKey";
    SharedPreferences mSettings;

    {
        expirationVariants.add(new Pair<>("5 minutes", 300L));
        expirationVariants.add(new Pair<>("30 minutes", 1800L));
        expirationVariants.add(new Pair<>("1 hour", 3600L));
        expirationVariants.add(new Pair<>("6 hours", 21600L));
        expirationVariants.add(new Pair<>("1 day", 86400L));
        expirationVariants.add(new Pair<>("3 days", 259200L));
        expirationVariants.add(new Pair<>("1 week", 604800L));
        expirationVariants.add(new Pair<>("1 month", 2592000L));
    }


    protected void saveActivityPreferences() {
        mSettings = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putLong(SAVED_EXP_TIME, expTimeValue);
        editor.apply();
    }

    protected void loadActivityPreferences() {
        mSettings = getActivity().getPreferences(Context.MODE_PRIVATE);
        expTimeValue = mSettings.getLong(SAVED_EXP_TIME, 77L);
    }

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        loadActivityPreferences();
        if (expTimeValue == 77L) {
            expTimeValue = 2592000L;
        }

        final String[] expVariants = new String[expirationVariants.size()];
        int abc = 0;
        for (Pair<String, Long> p : expirationVariants) {
            expVariants[abc] = p.first;
            abc++;
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Exp.time of uploading images:").setItems(expVariants, (DialogInterface dialogInterface, int which) -> {
            expTimeValue = expirationVariants.get(which).second;
            saveActivityPreferences();
            Toast.makeText(getActivity(), "Uploading images will be autodelete in " + expVariants[which], Toast.LENGTH_SHORT).show();
            Log.i(TAG, "ExpTimeValue should be set " + expTimeValue);
        });
        return builder.create();
    }

}
