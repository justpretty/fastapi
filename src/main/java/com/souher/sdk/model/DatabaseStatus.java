package com.souher.sdk.model;

import com.souher.sdk.database.DataModel;
import com.souher.sdk.interfaces.iRedisModel;

public class DatabaseStatus extends DataModel implements iRedisModel
{
    public Integer id;
    public String tag;
    public Integer active_count;
    public Integer pool_count;

    @Override
    public String[] keyColumn()
    {
        return new String[]{"tag"};
    }
}
