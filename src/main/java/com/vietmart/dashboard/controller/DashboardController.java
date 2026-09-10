package com.vietmart.dashboard.controller;

import com.vietmart.dashboard.client.OrderClient;
import com.vietmart.dashboard.client.ProductClient;
import com.vietmart.dashboard.client.UserClient;
import com.vietmart.dashboard.dto.DashboardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DashboardController {

    private final OrderClient orderClient;
    private final ProductClient productClient;
    private final UserClient userClient;
    private final Executor dashboardExecutor;

    private static final Long DEFAULT_FALLBACK_VALUE = 0L;

    @GetMapping("/dashboard")
    public DashboardResponse getDashboard() {
        log.info("Starting parallel aggregation for Dashboard data");
        long startTime = System.currentTimeMillis();

        // 1. Kick off parallel calls with exception handling & fallbacks
        CompletableFuture<Long> totalOrdersFuture = CompletableFuture
                .supplyAsync(orderClient::getTotalCount, dashboardExecutor)
                .exceptionally(ex -> {
                    log.error("Error fetching total order count: {}", ex.getMessage());
                    return DEFAULT_FALLBACK_VALUE;
                });

        CompletableFuture<Long> weekRevenueFuture = CompletableFuture
                .supplyAsync(orderClient::getWeekRevenue, dashboardExecutor)
                .exceptionally(ex -> {
                    log.error("Error fetching weekly revenue: {}", ex.getMessage());
                    return DEFAULT_FALLBACK_VALUE;
                });

        CompletableFuture<Long> activeProdsFuture = CompletableFuture
                .supplyAsync(productClient::getActiveCount, dashboardExecutor)
                .exceptionally(ex -> {
                    log.error("Error fetching active products count: {}", ex.getMessage());
                    return DEFAULT_FALLBACK_VALUE;
                });

        CompletableFuture<Long> newUsersFuture = CompletableFuture
                .supplyAsync(userClient::getNewUsersThisMonth, dashboardExecutor)
                .exceptionally(ex -> {
                    log.error("Error fetching new users this month: {}", ex.getMessage());
                    return DEFAULT_FALLBACK_VALUE;
                });

        // 2. Combine all futures and wait with a global timeout of 3 seconds
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                totalOrdersFuture,
                weekRevenueFuture,
                activeProdsFuture,
                newUsersFuture
        );

        try {
            allFutures.get(3, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.warn("Dashboard aggregation reached 3s global timeout. Partial data will be used.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Dashboard thread interrupted: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error waiting for dashboard futures: {}", e.getMessage());
        }

        // 3. Extract results safely using getNow()
        Long totalOrders = totalOrdersFuture.getNow(DEFAULT_FALLBACK_VALUE);
        Long weekRevenue = weekRevenueFuture.getNow(DEFAULT_FALLBACK_VALUE);
        Long activeProds = activeProdsFuture.getNow(DEFAULT_FALLBACK_VALUE);
        Long newUsers = newUsersFuture.getNow(DEFAULT_FALLBACK_VALUE);

        long elapsedTime = System.currentTimeMillis() - startTime;
        log.info("Dashboard aggregated in {} ms", elapsedTime);

        return new DashboardResponse(totalOrders, weekRevenue, activeProds, newUsers);
    }
}
