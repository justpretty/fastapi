package com.souher.sdk.business;

import com.souher.sdk.database.DataModel;
import com.souher.sdk.database.Database;
import com.souher.sdk.iApp;
import com.souher.sdk.iFile;
import com.souher.sdk.interfaces.iOnEveryMinute;
import com.souher.sdk.interfaces.iOnEveryMinuteForAll;
import com.souher.sdk.model.DatabaseStatus;

import java.io.File;

public class Monitor implements iOnEveryMinuteForAll
{
    @Override
    public void onEveryMinuteForAll(Long tick) throws Exception
    {
        DatabaseStatus databaseStatus=new DatabaseStatus();
        databaseStatus.tag=iApp.debugHead.get();
        databaseStatus.first();
        databaseStatus.active_count= DataModel.getDatabase().dataSource().getActiveCount();
        databaseStatus.pool_count=DataModel.getDatabase().dataSource().getPoolingCount();
        databaseStatus.save();
    }
}
