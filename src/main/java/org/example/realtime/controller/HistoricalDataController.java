package org.example.realtime.controller;

import lombok.RequiredArgsConstructor;
import org.example.realtime.model.HistoricalStockPrice;
import org.example.realtime.service.HistoricalDataService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/historical")
@RequiredArgsConstructor
public class HistoricalDataController {

    private final HistoricalDataService historicalDataService;

    @GetMapping("/{symbol}/recent")
    public ResponseEntity<List<HistoricalStockPrice>> getRecentPrices(@PathVariable String symbol) {
        return ResponseEntity.ok(historicalDataService.getRecentPrices(symbol));
    }

    @GetMapping("/{symbol}/range")
    public ResponseEntity<List<HistoricalStockPrice>> getPricesInRange(
            @PathVariable String symbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(historicalDataService.getPricesInRange(symbol, start, end));
    }

    @GetMapping("/{symbol}/statistics")
    public ResponseEntity<Map<String, Double>> getPriceStatistics(
            @PathVariable String symbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        return ResponseEntity.ok(historicalDataService.getPriceStatistics(symbol, since));
    }
} 