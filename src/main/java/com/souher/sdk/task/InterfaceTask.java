package com.souher.sdk.task;

import com.souher.sdk.extend.InterfaceExecutor;
import com.souher.sdk.iApp;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class InterfaceTask implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
       // Reporter.Current.debug("InterfaceTask start!");
        dataMap.forEach(( key, val)->{

            try {
                Class clas=Class.forName(key);
                Long tick=System.currentTimeMillis();
                InterfaceExecutor.Current.executorOrderly(clas,tick);
            } catch (ClassNotFoundException e) {
                iApp.error(e);
            }
        });
    }
}
