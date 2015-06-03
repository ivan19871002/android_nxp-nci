/*
 * Copyright (C) 2013-2014 NXP Semiconductors
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

package com.vzw.nfc;

import java.util.ArrayList;

import android.nfc.INfcAdapter;
import android.nfc.cardemulation.ApduServiceInfo;
import android.os.IBinder;
import android.os.ServiceManager;
import android.os.RemoteException;

import com.vzw.nfc.dos.AidMaskDo;
import com.vzw.nfc.dos.AidRangeDo;
import com.vzw.nfc.dos.ClfFilterDoList;
import com.vzw.nfc.dos.ClfFilterDo;
import com.vzw.nfc.dos.FilterConditionTagDo;
import com.vzw.nfc.dos.FilterEntryDo;
import com.vzw.nfc.dos.DoParserException;
import android.util.Log;

public final class AidFilter {

    public final int DEFAULT_ROUTE_LOCATION = ApduServiceInfo.SECURE_ELEMENT_ROUTE_UICC;

    /**
     * Set the filter list based on the uTLV.
     * @param filterList TLV per CLF Routing/Filtering Requirements.
     * @return true on success, false on failure
     */
    public boolean setFilterList(byte[] filterList) {
        boolean status = true;
        ClfFilterDoList all_CLF_FILTER_DO = new ClfFilterDoList(filterList, 0,
                filterList.length);
        try {
            all_CLF_FILTER_DO.translate();
        } catch (DoParserException e) {
            e.printStackTrace();
            return false;
        }
        ArrayList<RouteEntry> entries = new ArrayList<RouteEntry>();

        prepareRouteInfo(all_CLF_FILTER_DO, entries);
        try {
            // Call NfcService to push AID entries.
            status = getServiceInterface().setVzwAidList(
                    entries.toArray(new RouteEntry[entries.size()]));
        } catch (RemoteException e) {
            status = false;
        }

        return status;
    }



    /**
      * Enable the filterConditionTag immediately.
      * @param filterConditionTag tag of the filter condition to be enabled.
      * @return true on success, false on failure
      */
     public boolean enableFilterCondition(byte filterConditionTag) {
         boolean status = true;
         if(FilterConditionTagDo.SCREEN_OFF_TAG == filterConditionTag) {
             try {
                 getServiceInterface().setScreenOffCondition(true);
             } catch (RemoteException e) {
                 status = false;
             }
         }
         return status;
     }
     /**
      * Disable the filterConditionTag immediately.
      * @param filterConditionTag tag of the filter condition to be disabled.
      * @return true on success, false on failure
      */
     public boolean disableFilterCondition(byte filterConditionTag) {
         boolean status = true;
         if(FilterConditionTagDo.SCREEN_OFF_TAG == filterConditionTag) {
             try {
                 getServiceInterface().setScreenOffCondition(false);
             } catch (RemoteException e) {
                 status = false;
             }
         }
         return status;
     }

    private void prepareRouteInfo(ClfFilterDoList all_CLF_FILTER_DO,
            ArrayList<RouteEntry> entries) {

        ArrayList<ClfFilterDo> clf_FILTER_DOs = all_CLF_FILTER_DO
                .getClfFilterDos();
        for (ClfFilterDo clf_FILTER_DO : clf_FILTER_DOs) {

            byte[] aid = getAid(clf_FILTER_DO.getFilterEntryDo()
                    .getAidRangeDo(), clf_FILTER_DO.getFilterEntryDo()
                    .getAidMaskDo());

            int powerState = getPowerState(clf_FILTER_DO.getFilterEntryDo());
            Log.d("AidFilter", "prepareRouteInfo powerState" + powerState);
            RouteEntry entry = new RouteEntry(aid, powerState,
                    DEFAULT_ROUTE_LOCATION, clf_FILTER_DO.getFilterEntryDo()
                            .getVzwArDo().isVzwAllowed());
            entries.add(entry);
        }
    }

    private byte[] getAid(AidRangeDo aid_range, AidMaskDo aid_mask) {
        byte[] aid = null;
        byte[] barr_aid_mask = aid_mask.getAidMask();
        int count = 0;

        while (count < barr_aid_mask.length) {
            if (barr_aid_mask[count] != (byte) 0xFF) {
                break;
            }
            count++;
        }

        if (count != 0) {
            aid = new byte[count];
            System.arraycopy(aid_range.getAidRange(), 0, aid, 0, count);
        }
        return aid;
    }

    private int getPowerState(FilterEntryDo filter_entry_do) {
        int powerState = 0x00;
        int routeInfo = filter_entry_do.getRoutingModeDo().getRoutingInfo();
        FilterConditionTagDo conditionTagDo = filter_entry_do
                .getFilterConditionTagDo();
        if ((conditionTagDo != null && conditionTagDo.getFilterConditionTag() == FilterConditionTagDo.SCREEN_OFF_TAG)) {
            /*
             * It is a payment AID, only enable in Screen on and FULL Power
             * mode.
             */
            powerState = 0x01;// only full power mode
        } else if (routeInfo != 0x00) {
            /*
             * It is a non payment AID, can be enable in Screen off and F|L|N
             * Power mode.
             */
            powerState = (routeInfo | 0x80);
        }
        Log.d("AidFilter", "getPowerState" + powerState);
        return powerState;
    }

    /** get handle to NFC service interface */
    private static INfcAdapter getServiceInterface() {

        if (mNfcAdapter != null) {
            return mNfcAdapter;
        }

        /* get a handle to NFC service */
        IBinder b = ServiceManager.getService("nfc");
        if (b == null) {
            return null;
        }

        mNfcAdapter = INfcAdapter.Stub.asInterface(b);
        return mNfcAdapter;
    }

    private static INfcAdapter mNfcAdapter;
}
