package com.souher.sdk.interfaces;

import com.souher.sdk.database.DataModel;
import com.souher.sdk.iApp;

import java.util.concurrent.ConcurrentHashMap;

public interface iOnDataUpdated<T extends DataModel>
{
    void onDataUpdated(T model);

}
