/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 *  The original Work has been changed by NXP Semiconductors.
 *
 *  Copyright (C) 2013-2014 NXP Semiconductors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.android.nfc.cardemulation;

import android.util.Log;
import android.util.SparseArray;

import com.android.nfc.NfcService;
import android.nfc.cardemulation.ApduServiceInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

public class AidRoutingManager {
    static final String TAG = "AidRoutingManager";

    static final boolean DBG = true;

    // This is the default IsoDep protocol route; it means
    // that for any AID that needs to be routed to this
    // destination, we won't need to add a rule to the routing
    // table, because this destination is already the default route.
    //
    // For Nexus devices, the default route is always 0x00.
    public static final int DEFAULT_ROUTE = 0x00;

    // For Nexus devices, just a static route to the eSE
    // OEMs/Carriers could manually map off-host AIDs
    // to the correct eSE/UICC based on state they keep.
    public static final int DEFAULT_OFFHOST_ROUTE = ApduServiceInfo.SECURE_ELEMENT_ROUTE_UICC;

    final Object mLock = new Object();

    // mAidRoutingTable contains the current routing table. The index is the route ID.
    // The route can include routes to a eSE/UICC.
    final SparseArray<Set<String>> mAidRoutingTable = new SparseArray<Set<String>>();

    // Easy look-up what the route is for a certain AID
    final HashMap<String, Integer> mRouteForAid = new HashMap<String, Integer>();

    // Whether the routing table is dirty
    boolean mDirty;

    //default secure element id if not specified
    int mDefaultSeId;

    // Store final sorted outing table
    final AidRoutingCache mRoutnigCache;
    final VzwRoutingCache mVzwRoutingCache;

    public AidRoutingManager() {
        mDefaultSeId = -1;
        mRoutnigCache = new AidRoutingCache();
      mVzwRoutingCache = new VzwRoutingCache();
    }

    public boolean aidsRoutedToHost() {
        synchronized(mLock) {
            Set<String> aidsToHost = mAidRoutingTable.get(0);
            return aidsToHost != null && aidsToHost.size() > 0;
        }
    }

    public Set<String> getRoutedAids() {
        Set<String> routedAids = new HashSet<String>();
        synchronized (mLock) {
            for (Map.Entry<String, Integer> aidEntry : mRouteForAid.entrySet()) {
                routedAids.add(aidEntry.getKey());
            }
        }
        return routedAids;
    }

    public boolean setRouteForAid(String aid, boolean onHost, int route, int power, boolean isDefaultApp) {
        boolean hceEnabled = aidsRoutedToHost();
        synchronized (mLock) {
            int currentRoute = getRouteForAidLocked(aid);
            boolean currentDefault = mRoutnigCache.isDefault(aid);
            if (DBG) Log.d(TAG, "Set route for AID: " + aid + ", host: " + onHost + " , route: " + route +
                    ", power: " + power + ", current: 0x" + Integer.toHexString(currentRoute) + " , isDefaultApp: " + isDefaultApp);
           //int defaultRouteLocMask= NfcService.getInstance().GetDefaultRouteLocMask();
            int defaultRoute = NfcService.getInstance().GetDefaultRouteLoc();

            if (DBG) Log.d(TAG, "defaultRoute: aids:" + defaultRoute);

            if (onHost) route = DEFAULT_ROUTE;
            if (!onHost && route == -1) {
                if (DBG)
                    Log.d(TAG, "set route to UICC");
                route = DEFAULT_OFFHOST_ROUTE;
            }

            if (route == currentRoute && isDefaultApp == currentDefault) {
                return true;
            }
           // if ((route != currentRoute)
                   // || (onHost && currentDefault != isDefaultApp)) {
               // removeAid(aid);
           // }

            if((route != currentRoute) || (defaultRoute == route && currentDefault != isDefaultApp ) )
               removeAid(aid);

            Set<String> aids = mAidRoutingTable.get(route);
            if (DBG) Log.d(TAG, "setRouteForAid(): aids:" + aids);
            if (aids == null) {
               aids = new HashSet<String>();
               mAidRoutingTable.put(route, aids);
            }
            aids.add(aid);
            mRouteForAid.put(aid, route);
            if (!hceEnabled && onHost) {
                /* enable HCE when the first app installed. */
                mDirty = true;
            }
            //if (!onHost || isDefaultApp) {
                //if (DBG) Log.d(TAG, "routeAids(): aid:" + aid + ", route=" + route);
                //mRoutnigCache.addAid(aid, isDefaultApp, route, power);
                //mDirty = true;
            //}
            if (route != defaultRoute || isDefaultApp) {
                if (DBG) Log.d(TAG, "routeAids(): aid:" + aid + ", route=" + route);
                mRoutnigCache.addAid(aid, isDefaultApp, route, power);
                mDirty = true;
            }
        }
        return true;
    }

    /**
     * This notifies that the AID routing table in the controller
     * has been cleared (usually due to NFC being turned off).
     */
    public void onNfccRoutingTableCleared() {
        // The routing table in the controller was cleared
        // To stay in sync, clear our own tables.
        synchronized (mLock) {
            mAidRoutingTable.clear();
            mRouteForAid.clear();
            mRoutnigCache.clear();
        }
    }

    public boolean UpdateVzwCache(byte[] aid,int route,int power,boolean isAllowed){
       mVzwRoutingCache.addAid(aid,route,power,isAllowed);
       mDirty = true;
       return true;
    }

    public VzwRoutingCache GetVzwCache(){
       return mVzwRoutingCache;
    }

    public void ClearVzwCache() {
        mVzwRoutingCache.clear();
    }

    public boolean removeAid(String aid) {
        if (DBG) Log.d(TAG, "removeAid(String aid): aid:" + aid);
        boolean hceEnabled = aidsRoutedToHost();
        synchronized (mLock) {
            int currentRoute = getRouteForAidLocked(aid);
            if (currentRoute == -1) {
               if (DBG) Log.d(TAG, "removeAid(): No existing route for " + aid);
               return false;
            }
            Set<String> aids = mAidRoutingTable.get(currentRoute);
            if (aids == null) return false;
            aids.remove(aid);
            mRouteForAid.remove(aid);
            if (hceEnabled && !aidsRoutedToHost()) {
                /* disable HCE when the first app installed. */
                mDirty = true;
            }

            if (DBG) Log.d(TAG, "removeAid(): aid:" + aid + ", currentRoute="+currentRoute);
            if (mRoutnigCache.removeAid(aid)) {
                mDirty = true;
            }
        }
        return true;
    }

    public void commitRouting() {
        synchronized (mLock) {
            if (mDirty || true == NfcService.getInstance().mIsRouteForced) {
                mRoutnigCache.commit();
                NfcService.getInstance().commitRouting();
                mDirty = false;
            } else {
                if (DBG) Log.d(TAG, "Not committing routing because table not dirty.");
            }
        }
    }

    int getRouteForAidLocked(String aid) {
        Integer route = mRouteForAid.get(aid);
        return route == null ? -1 : route;
    }
}
