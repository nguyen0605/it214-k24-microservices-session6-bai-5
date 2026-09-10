package com.vietmart.dashboard.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "orderClient", url = "${services.order-service.url}")
public interface OrderClient {
    
    @GetMapping("/api/orders/count")
    Long getTotalCount();

    @GetMapping("/api/orders/revenue/week")
    Long getWeekRevenue();
}
