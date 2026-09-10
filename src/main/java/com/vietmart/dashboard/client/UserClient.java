package com.vietmart.dashboard.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "userClient", url = "${services.user-service.url}")
public interface UserClient {

    @GetMapping("/api/users/new-this-month")
    Long getNewUsersThisMonth();
}
