package com.vietmart.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponse {
    private Long totalOrders;
    private Long weekRevenue;
    private Long activeProducts;
    private Long newUsers;
}
