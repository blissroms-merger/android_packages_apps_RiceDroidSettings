/*
 * Copyright (C) 2016-2022 riceDroid Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rice.settings.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.view.View;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.rice.settings.preferences.SystemSettingListPreference;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.rice.settings.fragments.statusbar.BatteryBar;
import com.rice.settings.fragments.statusbar.Clock;
import com.rice.settings.fragments.statusbar.NetworkTrafficSettings;
import com.rice.settings.preferences.CustomSeekBarPreference;
import com.rice.settings.preferences.SystemSettingSeekBarPreference;
import com.rice.settings.utils.DeviceUtils;
import com.rice.settings.utils.TelephonyUtils;

import lineageos.preference.LineageSystemSettingListPreference;
import lineageos.providers.LineageSettings;

import java.util.List;

@SearchIndexable
public class StatusBar extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String TAG = "StatusBar";

    private static final String STATUS_BAR_CLOCK_STYLE = "status_bar_clock";
    private static final String QUICK_PULLDOWN = "qs_quick_pulldown";
    private static final String SMART_PULLDOWN = "qs_smart_pulldown";
    private static final String KEY_VOLTE_ICON_STYLE = "volte_icon_style";
    private static final String KEY_VOWIFI_ICON_STYLE = "vowifi_icon_style";
    private static final String KEY_VOLTE_VOWIFI_OVERRIDE = "volte_vowifi_override";
    private static final String KEY_SHOW_ROAMING = "roaming_indicator_icon";
    private static final String KEY_SHOW_FOURG = "show_fourg_icon";
    private static final String KEY_SHOW_DATA_DISABLED = "data_disabled_icon";
    private static final String KEY_COMBINED_ICONS = "combined_status_bar_signal_icons";
    private static final String KEY_USE_OLD_MOBILETYPE = "use_old_mobiletype";
    private static final String KEY_STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";
    private static final String KEY_STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String KEY_STATUS_BAR_BATTERY_TEXT_CHARGING = "status_bar_battery_text_charging";
    private static final String KEY_STATUS_BAR_CLOCK_SIZE  = "status_bar_clock_size";
    private static final String PREF_CLOCK_BG = "statusbar_clock_chip";

    private static final int PULLDOWN_DIR_NONE = 0;
    private static final int PULLDOWN_DIR_RIGHT = 1;
    private static final int PULLDOWN_DIR_LEFT = 2;
    private static final int PULLDOWN_DIR_ALWAYS = 3;

    private static final int BATTERY_STYLE_PORTRAIT = 0;
    private static final int BATTERY_STYLE_TEXT = 4;
    private static final int BATTERY_STYLE_HIDDEN = 5;

    private LineageSystemSettingListPreference mStatusBarClock;
    private LineageSystemSettingListPreference mQuickPulldown;
    private SystemSettingListPreference mBatteryPercent;
    private SystemSettingListPreference mBatteryStyle;
    private SystemSettingListPreference mSmartPulldown;
    private SystemSettingSeekBarPreference mVolteIconStyle;
    private SystemSettingSeekBarPreference mVowifiIconStyle;
    private SwitchPreference mOverride;
    private SwitchPreference mShowRoaming;
    private SwitchPreference mShowFourg;
    private SwitchPreference mDataDisabled;
    private SwitchPreference mCombinedIcons;
    private SwitchPreference mOldMobileType;
    private SwitchPreference mBatteryTextCharging;
    private CustomSeekBarPreference mClockSize;
    private SwitchPreference mStatusBarClockBG;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.rice_settings_statusbar);

        ContentResolver resolver = getActivity().getContentResolver();
        Context mContext = getActivity().getApplicationContext();

        final PreferenceScreen prefScreen = getPreferenceScreen();

        mStatusBarClock =
                (LineageSystemSettingListPreference) findPreference(STATUS_BAR_CLOCK_STYLE);

        // Adjust status bar preferences for RTL
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            if (DeviceUtils.hasCenteredCutout(mContext)) {
                mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_notch_rtl);
                mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_notch_rtl);
            } else {
                mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_rtl);
                mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_rtl);
            }
        } else if (DeviceUtils.hasCenteredCutout(mContext)) {
            mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_notch);
            mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_notch);
        }

        mVolteIconStyle = (SystemSettingSeekBarPreference) findPreference(KEY_VOLTE_ICON_STYLE);
        mVowifiIconStyle = (SystemSettingSeekBarPreference) findPreference(KEY_VOWIFI_ICON_STYLE);
        mOverride = (SwitchPreference) findPreference(KEY_VOLTE_VOWIFI_OVERRIDE);
        mShowRoaming = (SwitchPreference) findPreference(KEY_SHOW_ROAMING);
        mShowFourg = (SwitchPreference) findPreference(KEY_SHOW_FOURG);
        mDataDisabled = (SwitchPreference) findPreference(KEY_SHOW_DATA_DISABLED);
        mCombinedIcons = (SwitchPreference) findPreference(KEY_COMBINED_ICONS);
        mOldMobileType = (SwitchPreference) findPreference(KEY_USE_OLD_MOBILETYPE);

        if (!TelephonyUtils.isVoiceCapable(getActivity())) {
            prefScreen.removePreference(mVolteIconStyle);
            prefScreen.removePreference(mVowifiIconStyle);
            prefScreen.removePreference(mOverride);
            prefScreen.removePreference(mShowRoaming);
            prefScreen.removePreference(mShowFourg);
            prefScreen.removePreference(mDataDisabled);
            prefScreen.removePreference(mCombinedIcons);
            prefScreen.removePreference(mOldMobileType);
        } else {
            boolean mConfigUseOldMobileType = mContext.getResources().getBoolean(
                    com.android.internal.R.bool.config_useOldMobileIcons);
            boolean showing = Settings.System.getIntForUser(resolver,
                    Settings.System.USE_OLD_MOBILETYPE,
                    mConfigUseOldMobileType ? 1 : 0, UserHandle.USER_CURRENT) != 0;
            mOldMobileType.setChecked(showing);
        }

        int batterystyle = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_BATTERY_STYLE, BATTERY_STYLE_PORTRAIT, UserHandle.USER_CURRENT);
        int batterypercent = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, 0, UserHandle.USER_CURRENT);

        mBatteryStyle = (SystemSettingListPreference) findPreference(KEY_STATUS_BAR_BATTERY_STYLE);
        mBatteryStyle.setOnPreferenceChangeListener(this);

        mBatteryPercent = (SystemSettingListPreference) findPreference(KEY_STATUS_BAR_SHOW_BATTERY_PERCENT);
        mBatteryPercent.setEnabled(
                batterystyle != BATTERY_STYLE_TEXT && batterystyle != BATTERY_STYLE_HIDDEN);
        mBatteryPercent.setOnPreferenceChangeListener(this);

        mBatteryTextCharging = (SwitchPreference) findPreference(KEY_STATUS_BAR_BATTERY_TEXT_CHARGING);
        mBatteryTextCharging.setEnabled(batterystyle == BATTERY_STYLE_HIDDEN ||
                (batterystyle != BATTERY_STYLE_TEXT && batterypercent != 2));

        mQuickPulldown =
                (LineageSystemSettingListPreference) findPreference(QUICK_PULLDOWN);
        mQuickPulldown.setOnPreferenceChangeListener(this);
        updateQuickPulldownSummary(mQuickPulldown.getIntValue(0));

        mSmartPulldown = (SystemSettingListPreference) findPreference(SMART_PULLDOWN);
        mSmartPulldown.setOnPreferenceChangeListener(this);
        updateSmartPulldownSummary(mSmartPulldown.getIntValue(0));

        // Adjust status bar preferences for RTL
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            mQuickPulldown.setEntries(R.array.status_bar_quick_qs_pulldown_entries_rtl);
            mQuickPulldown.setEntryValues(R.array.status_bar_quick_qs_pulldown_values_rtl);
        }

        mClockSize = (CustomSeekBarPreference) findPreference(KEY_STATUS_BAR_CLOCK_SIZE);
        int clockSize = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.STATUS_BAR_CLOCK_SIZE, 14);
        mClockSize.setValue(clockSize / 1);
        mClockSize.setOnPreferenceChangeListener(this);

        mStatusBarClockBG = (SwitchPreference) findPreference(PREF_CLOCK_BG);
        mStatusBarClockBG.setChecked((Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_CLOCK_CHIP, 1) == 1));
        mStatusBarClockBG.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mBatteryStyle) {
            int value = Integer.parseInt((String) newValue);
            int batterypercent = Settings.System.getIntForUser(getContentResolver(),
                    Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, 0, UserHandle.USER_CURRENT);
            mBatteryPercent.setEnabled(
                    value != BATTERY_STYLE_TEXT && value != BATTERY_STYLE_HIDDEN);
            mBatteryTextCharging.setEnabled(value == BATTERY_STYLE_HIDDEN ||
                    (value != BATTERY_STYLE_TEXT && batterypercent != 2));
            return true;
        } else if (preference == mBatteryPercent) {
            int value = Integer.parseInt((String) newValue);
            int batterystyle = Settings.System.getIntForUser(getContentResolver(),
                    Settings.System.STATUS_BAR_BATTERY_STYLE, BATTERY_STYLE_PORTRAIT, UserHandle.USER_CURRENT);
            mBatteryTextCharging.setEnabled(batterystyle == BATTERY_STYLE_HIDDEN ||
                    (batterystyle != BATTERY_STYLE_TEXT && value != 2));
            return true;
        } else if (preference == mQuickPulldown) {
            int value = Integer.parseInt((String) newValue);
            updateQuickPulldownSummary(value);
            return true;
        } else if (preference == mSmartPulldown) {
            int value = Integer.parseInt((String) newValue);
            updateSmartPulldownSummary(value);
            return true;
        } else if (preference == mClockSize) {
            int width = ((Integer)newValue).intValue();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_CLOCK_SIZE, width);
            return true;
        } else if (preference == mStatusBarClockBG) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_CHIP, value ? 1 : 0);
            return true;
        }
        return false;
    }

    private void updateQuickPulldownSummary(int value) {
        String summary="";
        switch (value) {
            case PULLDOWN_DIR_NONE:
                summary = getResources().getString(
                    R.string.status_bar_quick_qs_pulldown_off);
                break;
            case PULLDOWN_DIR_ALWAYS:
                summary = getResources().getString(
                    R.string.status_bar_quick_qs_pulldown_always);
                break;
            case PULLDOWN_DIR_LEFT:
            case PULLDOWN_DIR_RIGHT:
                summary = getResources().getString(
                    R.string.status_bar_quick_qs_pulldown_summary,
                    getResources().getString(value == PULLDOWN_DIR_LEFT
                        ? R.string.status_bar_quick_qs_pulldown_summary_left
                        : R.string.status_bar_quick_qs_pulldown_summary_right));
                break;
        }
        mQuickPulldown.setSummary(summary);
    }

    private void updateSmartPulldownSummary(int value) {
        Resources res = getResources();

        if (value == 0) {
            // Smart pulldown deactivated
            mSmartPulldown.setSummary(res.getString(R.string.smart_pulldown_off));
        } else if (value == 3) {
            mSmartPulldown.setSummary(res.getString(R.string.smart_pulldown_none_summary));
        } else {
            String type = res.getString(value == 1
                    ? R.string.smart_pulldown_dismissable
                    : R.string.smart_pulldown_ongoing);
            mSmartPulldown.setSummary(res.getString(R.string.smart_pulldown_summary, type));
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CRDROID_SETTINGS;
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.rice_settings_statusbar) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);

                    if (!TelephonyUtils.isVoiceCapable(context)) {
                        keys.add(KEY_VOLTE_ICON_STYLE);
                        keys.add(KEY_VOWIFI_ICON_STYLE);
                        keys.add(KEY_VOLTE_VOWIFI_OVERRIDE);
                        keys.add(KEY_SHOW_ROAMING);
                        keys.add(KEY_SHOW_FOURG);
                        keys.add(KEY_SHOW_DATA_DISABLED);
                        keys.add(KEY_COMBINED_ICONS);
                        keys.add(KEY_USE_OLD_MOBILETYPE);
                    }

                    return keys;
                }
            };
}
