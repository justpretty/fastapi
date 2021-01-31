package com.souher.sdk.extend;


import com.souher.sdk.iApp;
import com.souher.sdk.os.Cpu;
import com.souher.sdk.os.Memory;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ExtendThread {

    private Runnable runnable;

    private static final double MinCpuIdleRatio =10;
    private static final double MinMemFreeRatio =5;
    private static final int CheckIntervalMiniseconds =2000;
    private static Cpu DefaultCpu =Cpu.Default;
    private static Memory DefaultMemory =Memory.Default;
    private static ConcurrentLinkedQueue<Runnable> Queue = new ConcurrentLinkedQueue();
    private static int CountPerRun=100;
    private static boolean isClean=false;
    private static Thread MainThread = new Thread(() -> {
        while(true)
        {
            if(isClean && CheckCanRun())
            {
                CountPerRun+=20;
            }
            else if(isClean &&!CheckCanRun())
            {
                if(CountPerRun<1)
                {
                    CountPerRun=1;
                }
                else
                {
                    CountPerRun/=2;
                }
            }
            if(Queue.size()>0&&CheckCanRun())
            {
                int count=CountPerRun;
                while (Queue.size()>0)
                {
                    Runnable a=Queue.poll();
                    if(a!=null)
                    {
                        new Thread(a).start();
                    }
                    count--;
                    if(count<=0)
                    {
                        break;
                    }
                }
                if(count==0)
                {
                    isClean=true;
                }
                else {
                    isClean=false;
                }
            }
            if(Queue.size()==0)
            {
                isClean=false;
            }
            try {
                Thread.sleep(CheckIntervalMiniseconds);
            } catch (InterruptedException e) {
                iApp.error(e);
            }
        }
    });
    static{
        MainThread.start();
    }

    public ExtendThread(Runnable runnable)
    {
        this.runnable=runnable;
    }

    private static boolean CheckCanRun()
    {
        double a;
        a= DefaultCpu.getIdleRatio();
        if(a==-1){return true;}
        if(a<=1)
        {
            iApp.warning("CPU过高");
        }
        if(a< MinCpuIdleRatio)
        {
            return false;
        }

        a= DefaultMemory.getFreeRatio();
        if(a==-1){return true;}
        if(a< MinMemFreeRatio)
        {
            iApp.warning("内存过高");
            return false;
        }
        return true;
    }

    public void start()
    {
        if(Queue.size()>0) {

        }
        if(Queue.size()>0)
        {
            Queue.offer(this.runnable);
            return;
        }
        if(CheckCanRun())
        {
            new Thread(runnable).start();
        }
        else
        {

            Queue.offer(this.runnable);
            return;
        }
    }
}
