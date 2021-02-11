package com.souher.sdk.interfaces;

import com.souher.sdk.database.DataModel;
import com.souher.sdk.database.DataResult;

public interface iHasOtherCacheTriggerModels
{
    default DataResult<? extends DataModel> triggerUpdateModelsOnDeleted()
    {
        return new DataResult<>();
    }

    default DataResult<? extends DataModel> triggerUpdateModelsOnInserted()
    {
        return new DataResult<>();
    }
    default DataResult<? extends DataModel> triggerUpdateModelsOnUpdated()
    {
        return new DataResult<>();
    }
}
