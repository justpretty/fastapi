package com.souher.sdk.interfaces;

import com.souher.sdk.database.DataModel;

public interface iOnDataInserting<T extends DataModel>
{
    void onDataInserting(T model);
}
