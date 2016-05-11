package com.ibuildapp.romanblack.CataloguePlugin.api;


import com.ibuildapp.romanblack.CataloguePlugin.api.model.ApiResponse;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface CatalogApi {
    @GET("/endpoint/payment.php")
    Observable<ApiResponse> sendOrder(@Query("app_id") String appId, @Query("widget_id") String widgetId,
                                     @Query("order_info") String orderInfo, @Query("items") String items);
}
