package com.souher.sdk.os;


import com.souher.sdk.io.RealTimeFileContentService;

public class Cpu {

    public static Cpu Default=new Cpu();
    private RealTimeFileContentService service;
    public Cpu()
    {
        service=RealTimeFileContentService.RealTimeCpuIdleContentService;
        service.start();
    }

    private double value=100;

    public double getIdleRatio()
    {
        double a=service.getDoubleContent();
        if(a>0)
        {
            value=a;
            return a;
        }
        return value;
    }
}
