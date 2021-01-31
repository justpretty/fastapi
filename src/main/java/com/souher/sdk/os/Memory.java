package com.souher.sdk.os;


import com.souher.sdk.io.RealTimeFileContentService;

public class Memory {

    public static Memory Default=new Memory();

    private RealTimeFileContentService service;
    public Memory()
    {
        service=RealTimeFileContentService.RealTimeMemFreeContentService;
        service.start();
    }

    private double value=100;

    public double getFreeRatio()
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
