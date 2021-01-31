package com.souher.sdk.interfaces;

public interface iApi
{
    boolean hasPost();
    boolean hasGet();
    String urlPath();
    String handle(spark.Request request, spark.Response response);
}
