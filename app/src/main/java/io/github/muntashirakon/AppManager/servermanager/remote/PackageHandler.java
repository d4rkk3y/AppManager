/*
 * Copyright (C) 2020 Muntashir Al-Islam
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.muntashirakon.AppManager.servermanager.remote;

import android.app.ActivityThread;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import io.github.muntashirakon.AppManager.server.common.ClassCallerProcessor;

public class PackageHandler extends ClassCallerProcessor {
    public static final String ARG_PACKAGE_NAME = "pkg";
    public static final String ARG_COMPONENT_NAME = "comp";
    public static final String ARG_COMPONENT_STATE = "comp_state";
    public static final String ARG_USER_HANDLE = "user";
    public static final String ARG_FLAGS = "flags";
    public static final String ARG_ACTION = "action";

    public static final int ACTION_PACKAGE_INFO = 1;
    public static final int ACTION_COMPONENT_SETTING = 2;
    public static final int ACTION_APPLICATION_INFO = 3;

    public PackageHandler(Context mPackageContext, Context mSystemContext, byte[] bytes) {
        super(mPackageContext, mSystemContext, bytes);
    }

    @NonNull
    @Override
    public Bundle proxyInvoke(@NonNull Bundle args) throws Throwable {
        int action = args.getInt(ARG_ACTION, ACTION_PACKAGE_INFO);
        String packageName = args.getString(ARG_PACKAGE_NAME);
        if (packageName == null) {
            throw new IllegalArgumentException("Component name cannot be null");
        }
        int userHandle = args.getInt(ARG_USER_HANDLE);
        int flags = args.getInt(ARG_FLAGS, 0);
        IPackageManager pm = ActivityThread.getPackageManager();
        if (pm == null) throw new Exception("IPackageManager cannot be null");
        if (action == ACTION_PACKAGE_INFO) {
            PackageInfo packageInfo = pm.getPackageInfo(packageName, flags, userHandle);
            if (packageInfo == null) {
                throw new PackageManager.NameNotFoundException("Package doesn't exist.");
            }
            args.clear();
            args.putParcelable("return", packageInfo);
        } else if (action == ACTION_COMPONENT_SETTING) {
            String componentName = args.getString(ARG_COMPONENT_NAME);
            if (componentName == null)
                throw new IllegalArgumentException("Component name cannot be null");
            int state = args.getInt(ARG_COMPONENT_STATE, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
            pm.setComponentEnabledSetting(new ComponentName(packageName, componentName), state, flags, userHandle);
        } else if (action == ACTION_APPLICATION_INFO) {
            ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, flags, userHandle);
            if (applicationInfo == null) {
                throw new PackageManager.NameNotFoundException("Package doesn't exist.");
            }
            args.clear();
            args.putParcelable("return", applicationInfo);
        }
        return args;
    }
}
