package com.souher.sdk.interfaces;

import com.souher.sdk.database.DataModel;
import com.souher.sdk.iApp;

public interface iOnDataSaved<T extends DataModel>
{
    void onDataSaved(T model);

}
