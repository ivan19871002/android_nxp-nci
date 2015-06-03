 /******************************************************************************
 *
 *  The original Work has been changed by NXP Semiconductors.
 *
 *  Copyright (C) 2013 NXP Semiconductors
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
 ******************************************************************************/
package com.android.nfc.cardemulation;

import android.util.Log;

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Iterator;
import com.android.nfc.NfcService;

public class AidRoutingCache {
    final private static boolean DBG = true;
    final private static String TAG = "AidRoutingCache";

    final private static int CAPACITY = 32;

    // Cached routing table
    final private static Hashtable<String, AidElement> mRouteCache = new Hashtable<String, AidElement>(CAPACITY);

    AidRoutingCache() {
    }

    boolean addAid(String aid, boolean isDefault, int route, int power) {
        AidElement elem = new AidElement(aid, isDefault, route, power);
        if (mRouteCache.size() >= CAPACITY) {
            return false;
        }
        mRouteCache.put(aid.toUpperCase(), elem);
        return true;
    }

    boolean removeAid(String aid) {
        return (mRouteCache.remove(aid) != null);
    }

    boolean isDefault(String aid) {
        AidElement elem = (AidElement)mRouteCache.get(aid);

        return elem!= null && elem.isDefault();
    }

    void clear() {
        mRouteCache.clear();
    }

    void commit() {
        List<AidElement> list = Collections.list(mRouteCache.elements());
        Collections.sort(list);
        Iterator<AidElement> it = list.iterator();

        NfcService.getInstance().clearRouting();

        while(it.hasNext()){
            AidElement element =it.next();
            if (DBG) Log.d (TAG, element.toString());
            NfcService.getInstance().routeAids(
                    element.getAid(),
                    element.getRouteLocation(),
                    element.getPowerState()
                    );
        }
    }
}

class AidElement implements Comparable {

    private String mAid;
    private boolean mIsDefault;
    private int mRouteLocation;
    private int mPowerState;

    public AidElement(String aid, boolean isDefault, int route, int power) {
        mAid = aid;
        mIsDefault = isDefault;
        mRouteLocation = route;
        mPowerState = power;
    }

    public boolean isDefault() {
        return mIsDefault;
    }

    public String getAid() {
        return mAid;
    }

    public int getRouteLocation() {
        return mRouteLocation;
    }

    public int getPowerState() {
        return mPowerState;
    }

    @Override
    public int compareTo(Object o) {

        AidElement elem = (AidElement)o;

        if (mIsDefault && !elem.isDefault()) {
            return -1;
        }
        else if (!mIsDefault && elem.isDefault()) {
            return 1;
        }
        return elem.getAid().length() - mAid.length();
    }

    @Override
    public String toString() {
        return "aid: " + mAid + ", location: " + mRouteLocation
                    + ", power: " + mPowerState + ",isDefault: " + mIsDefault;
    }
}
