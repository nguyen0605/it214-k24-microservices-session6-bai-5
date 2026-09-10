package com.vietmart.dashboard.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "productClient", url = "${services.product-service.url}")
public interface ProductClient {

    @GetMapping("/api/products/active-count")
    Long getActiveCount();
}
