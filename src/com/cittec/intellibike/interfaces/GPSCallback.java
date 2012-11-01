package com.cittec.intellibike.interfaces;

import android.location.Location;

public interface GPSCallback
{
        public abstract void onGPSUpdate(Location location);
        public abstract void onGPSStatusChange();
}