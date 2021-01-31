package com.souher.sdk.interfaces;

import com.souher.sdk.database.DataModel;
import com.souher.sdk.iApp;

public interface iOnDataDeleted<T extends DataModel>
{
    void onDataDeleted(T model);


}
