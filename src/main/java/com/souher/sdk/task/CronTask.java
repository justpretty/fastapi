package com.souher.sdk.task;

import com.souher.sdk.iApp;

import com.souher.sdk.interfaces.*;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.text.SimpleDateFormat;

import static org.quartz.JobBuilder.newJob;

public class CronTask
{

    public static CronTask Current = new CronTask();

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static SchedulerFactory schedulerFactory= new StdSchedulerFactory();
    private static Scheduler scheduler;

    static
    {
        //Reporter.Default.debug("OnceTaskInvoker construct ");
        try {
            scheduler = schedulerFactory.getScheduler();
            scheduler.start();
        } catch (SchedulerException e) {
            iApp.error(e);
        }
    }

    public static Scheduler currentScheduler()
    {
        return scheduler;
    }
    public void init() {
        createInterfaceTask(iOnEveryMinute.class,"10 * * * * ?");
        createInterfaceTask(iOnEveryTenSeconds.class,"5/10 * * * * ?");
        createInterfaceTask(iOnEveryHour.class,"20 0 * * * ?");
        createInterfaceTask(iOnEveryDay.class,"40 0 0 * * ?");
    }
    public void initForAll() {
        createInterfaceTask(iOnEveryMinuteForAll.class,"11 * * * * ?");
        createInterfaceTask(iOnEveryTenSecondsForAll.class,"6/10 * * * * ?");
        createInterfaceTask(iOnEveryHourForAll.class,"21 0 * * * ?");
        createInterfaceTask(iOnEveryDayForAll.class,"41 0 0 * * ?");
    }

    private void createInterfaceTask(Class interfaceClass,String cron)
    {
        String key= interfaceClass.getName();
        JobDetail jobDetail=newJob(InterfaceTask.class)
                .usingJobData(key,"")
                .withIdentity(key)
                .build();
        Trigger trigger=(Trigger) TriggerBuilder.newTrigger()
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .withIdentity(key)
                .build();
        try {
            scheduler.scheduleJob(jobDetail,trigger);
        } catch (SchedulerException e) {
            iApp.error(e);
        }
    }
}
