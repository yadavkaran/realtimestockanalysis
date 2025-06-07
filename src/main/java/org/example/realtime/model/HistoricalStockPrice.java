package org.example.realtime.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "historical_stock_prices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalStockPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private double volume;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public HistoricalStockPrice(String symbol, double price) {
        this.symbol = symbol;
        this.price = price;
        this.volume = 0.0;
        this.timestamp = LocalDateTime.now();
    }

    public HistoricalStockPrice(String symbol, double price, double volume) {
        this.symbol = symbol;
        this.price = price;
        this.volume = volume;
        this.timestamp = LocalDateTime.now();
    }
} 