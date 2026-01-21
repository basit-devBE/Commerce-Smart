package com.example.Commerce.Controllers;

import com.example.Commerce.Aspects.PerformanceMonitoringAspect;
import com.example.Commerce.Config.RequiresRole;
import com.example.Commerce.DTOs.ApiResponse;
import com.example.Commerce.Enums.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Performance Monitoring", description = "APIs for monitoring database performance")
@RestController
@RequestMapping("/api/performance")
public class PerformanceController {
    
    private final PerformanceMonitoringAspect performanceAspect;

    public PerformanceController(PerformanceMonitoringAspect performanceAspect) {
        this.performanceAspect = performanceAspect;
    }

    @Operation(summary = "Get database fetch times", description = "Retrieves all recorded database query execution times. Requires ADMIN role.")
    @RequiresRole(UserRole.ADMIN)
    @GetMapping("/db-metrics")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getDbMetrics() {
        Map<String, Long> metrics = performanceAspect.getDbFetchTimes();
        ApiResponse<Map<String, Long>> response = new ApiResponse<>(HttpStatus.OK.value(), "Performance metrics retrieved successfully", metrics);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Clear performance metrics", description = "Clears all recorded performance metrics. Requires ADMIN role.")
    @RequiresRole(UserRole.ADMIN)
    @DeleteMapping("/clear-metrics")
    public ResponseEntity<ApiResponse<Void>> clearMetrics() {
        performanceAspect.clearMetrics();
        ApiResponse<Void> response = new ApiResponse<>(HttpStatus.OK.value(), "Performance metrics cleared successfully", null);
        return ResponseEntity.ok(response);
    }
}