package org.example.realtime.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StockPrice {
    private String symbol;
    private double price;
    private double volume;
    private double change;
    private double changePercent;
    private LocalDateTime timestamp;
    private double high;
    private double low;
    private double open;
    private double previousClose;

    public StockPrice() {
        this.timestamp = LocalDateTime.now();
    }

    public StockPrice(String symbol, double price, double volume, double change, 
                     double changePercent, double high, double low, double open, 
                     double previousClose) {
        this.symbol = symbol;
        this.price = price;
        this.volume = volume;
        this.change = change;
        this.changePercent = changePercent;
        this.high = high;
        this.low = low;
        this.open = open;
        this.previousClose = previousClose;
        this.timestamp = LocalDateTime.now();
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return symbol + " -> " + price;
    }
}
