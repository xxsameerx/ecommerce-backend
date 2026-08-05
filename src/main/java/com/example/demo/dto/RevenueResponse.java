package com.example.demo.dto;

public class RevenueResponse {
    private Double totalRevenue;
    private Long totalOrders;
    private String period;

    public RevenueResponse(Double totalRevenue, Long totalOrders, String period) {
        this.totalRevenue = totalRevenue;
        this.totalOrders = totalOrders;
        this.period = period;
    }

    public Double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }
}