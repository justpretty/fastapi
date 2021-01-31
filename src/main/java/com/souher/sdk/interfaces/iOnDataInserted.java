package com.souher.sdk.interfaces;

import com.souher.sdk.database.DataModel;
import com.souher.sdk.iApp;

public interface iOnDataInserted<T extends DataModel>
{
    void onDataInserted(T model);
}
