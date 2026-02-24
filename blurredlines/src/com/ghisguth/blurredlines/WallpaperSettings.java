package com.ghisguth.blurredlines;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import cxa.lineswallpaper.R;

public class WallpaperSettings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat
            implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                getPreferenceManager().setStorageDeviceProtected();
            }
            getPreferenceManager().setSharedPreferencesName(Wallpaper.SHARED_PREF_NAME);
            setPreferencesFromResource(R.xml.settings, rootKey);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen()
                    .getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen()
                    .getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {}

        @SuppressWarnings("deprecation")
        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            if (preference instanceof com.ghisguth.ux.ColorPickerPreference) {
                DialogFragment dialogFragment =
                        com.ghisguth.ux.ColorPickerPreferenceFragmentCompat.newInstance(
                                preference.getKey());
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(
                        getParentFragmentManager(),
                        "androidx.preference.PreferenceFragment.DIALOG");
            } else {
                super.onDisplayPreferenceDialog(preference);
            }
        }
    }
}
