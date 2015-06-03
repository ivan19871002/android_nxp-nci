/*
 * Copyright (C) 2010 The Android Open Source Project
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

/******************************************************************************
 *
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
 ******************************************************************************/

package android.nfc;

import android.app.PendingIntent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.Tag;
import android.nfc.TechListParcel;
import android.nfc.IAppCallback;
import android.nfc.INfcAdapterExtras;
import android.nfc.INfcTag;
import android.nfc.INfcCardEmulation;
import android.os.Bundle;
import android.nfc.INfcAccessExtras;
import android.nfc.INfcAla;
import com.vzw.nfc.RouteEntry;

/**
 * @hide
 */
interface INfcAdapter
{
    INfcTag getNfcTagInterface();
    INfcCardEmulation getNfcCardEmulationInterface();
    INfcAdapterExtras getNfcAdapterExtrasInterface(in String pkg);
    INfcAccessExtras getNfcAccessExtrasInterface(in String pkg);
    INfcAla getNfcAlaInterface();

    int getState();
    boolean disable(boolean saveState);
    boolean enable();
    boolean enableNdefPush();
    boolean disableNdefPush();
    boolean isNdefPushEnabled();

    void setForegroundDispatch(in PendingIntent intent,
            in IntentFilter[] filters, in TechListParcel techLists);
    void setAppCallback(in IAppCallback callback);

    void dispatch(in Tag tag);

    void setReaderMode (IBinder b, IAppCallback callback, int flags, in Bundle extras);
    void setP2pModes(int initatorModes, int targetModes);
    int[] getSecureElementList(String pkg);
    int getSelectedSecureElement(String pkg);
    int selectSecureElement(String pkg,int seId);
    int deselectSecureElement(String pkg);
    void storeSePreference(int seId);
    int setEmvCoPollProfile(boolean enable, int route);
    boolean setVzwAidList(in RouteEntry[] entries);
    void setScreenOffCondition(in boolean enable);
    void MifareDesfireRouteSet(int routeLoc, boolean fullPower, boolean lowPower, boolean noPower);
    void DefaultRouteSet(int routeLoc, boolean fullPower, boolean lowPower, boolean noPower);
    void MifareCLTRouteSet(int routeLoc, boolean fullPower, boolean lowPower, boolean noPower);
    boolean snepDtaCmd(String cmdType, String serviceName, int serviceSap, int miu, int rwSize, int testCaseId);
}
