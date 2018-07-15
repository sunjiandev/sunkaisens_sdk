/* Copyright (C) 2012, Doubango Telecom <http://www.doubango.org>
*	
* This file is part of imsdroid Project (http://code.google.com/p/imsdroid)
*
* imsdroid is free software: you can redistribute it and/or modify it under the terms of 
* the GNU General Public License as published by the Free Software Foundation, either version 3 
* of the License, or (at your option) any later version.
*	
* imsdroid is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
* See the GNU General Public License for more details.
*	
* You should have received a copy of the GNU General Public License along 
* with this program; if not, write to the Free Software Foundation, Inc., 
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package org.doubango.ngn.media;

import android.content.Context;
import android.view.View;
import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.tinyWRAP.ProxyPlugin;
import org.doubango.tinyWRAP.ProxyVideoConsumer;

import java.math.BigInteger;

public abstract class NgnProxyVideoConsumer extends NgnProxyPlugin {
    protected boolean mFullScreenRequired;

    public NgnProxyVideoConsumer(BigInteger id, ProxyPlugin plugin) {
        super(id, plugin);

        mFullScreenRequired = NgnEngine.getInstance().getConfigurationService().getBoolean(
                NgnConfigurationEntry.GENERAL_FULL_SCREEN_VIDEO,
                NgnConfigurationEntry.DEFAULT_GENERAL_FULL_SCREEN_VIDEO);
    }

    public static NgnProxyVideoConsumer createInstance(BigInteger id, ProxyVideoConsumer consumer) {
        if (NgnApplication.isGlEs2Supported()) {
            boolean isUseBypassDecoding = NgnEngine.getInstance().getConfigurationService().getBoolean(NgnConfigurationEntry.GENERAL_BYPASS_DECODING,
                    NgnConfigurationEntry.DEFAULT_GENERAL_BYPASS_DECODING);
            if (isUseBypassDecoding) {
                //return new NgnProxyVideoConsumerGL_MC(id, consumer);
                //if(NgnApplication.isBh04() || NgnApplication.isYijia())
                //if(NgnApplication.getSDKVersion() <19 && NgnApplication.getSDKVersion() >=17)

                if (NgnApplication.getSDKVersion() >= 19)
                    return new NgnProxyVideoConsumerGL_MC(id, consumer);
                else
                    return new NgnProxyVideoConsumerGL_MC_Surface(id, consumer);
            } else
                return new NgnProxyVideoConsumerGL(id, consumer);
        } else
            return new NgnProxyVideoConsumerSV(id, consumer);
    }

    public abstract void setContext(Context context);

    public abstract View startPreview(Context context);

    public abstract View startPreview();
}