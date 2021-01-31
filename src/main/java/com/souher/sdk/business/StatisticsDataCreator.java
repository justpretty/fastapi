package com.souher.sdk.business;

import com.souher.sdk.extend.InterfaceExecutor;
import com.souher.sdk.interfaces.iOnEveryDay;
import com.souher.sdk.interfaces.iOnEveryHour;
import com.souher.sdk.interfaces.iStatisticsDataCreatorForFourDimensional;
import com.souher.sdk.interfaces.iStatisticsDataCreatorForTwoDimensional;

public class StatisticsDataCreator implements iOnEveryDay
{
    @Override
    public void onEveryDay(Long tick) throws Exception
    {
       InterfaceExecutor.Current.executorMethodOrderly(iStatisticsDataCreatorForTwoDimensional.class,"handleStatisticsOnEveryHour",tick);
        InterfaceExecutor.Current.executorMethodOrderly(iStatisticsDataCreatorForFourDimensional.class,"handleStatisticsOnEveryHour",tick);
    }
}
