# realtimestockanalysis

# Real-Time Stock Analysis Dashboard 📈

A comprehensive stock analysis platform that provides real-time insights and technical analysis for stock market data. Built with React, Spring Boot, and Apache Kafka.

## 🌟 Features

### Frontend
- Modern, responsive dashboard built with React and TypeScript
- Beautiful UI using Material-UI with dark mode support
- Real-time data updates and analysis
- Interactive charts and visualizations
- Multiple timeframe analysis (1D, 1W, 1M)
- Technical indicators display
- Volume analysis
- Trend strength visualization

### Backend
- Real-time stock data processing using Kafka
- Advanced technical analysis:
  - Trend analysis with strength indicators
  - Volatility calculations and risk assessment
  - Moving averages (short and long-term)
  - Volume analysis with trend detection
  - Technical indicators (RSI, MACD)
- Historical data storage and analysis
- Rate-limited API integration with Yahoo Finance
- Secure API endpoints with proper error handling

## 🛠️ Tech Stack

### Frontend
- React with TypeScript
- Material-UI for component library
- Axios for API communication
- Recharts for data visualization

### Backend
- Spring Boot (Java)
- Apache Kafka for real-time data streaming
- JPA/Hibernate for data persistence
- RESTful API architecture
- Yahoo Finance API integration

### Database
- PostgreSQL for historical data storage
- In-memory caching for performance

### DevOps
- Maven for dependency management
- Git for version control

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Node.js 16 or higher
- PostgreSQL
- Apache Kafka
- Maven

### Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/realtime-stock-pipeline.git
cd realtime-stock-pipeline
```

2. Start Kafka and Zookeeper:
```bash
# Start Zookeeper
cd kafka_2.13-3.6.1
.\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties

# In a new terminal, start Kafka
.\bin\windows\kafka-server-start.bat .\config\server.properties
```

3. Configure the backend:
- Update `application.properties` with your database credentials
- Set up your Yahoo Finance API key (if needed)

4. Build and run the backend:
```bash
mvn clean install
mvn spring-boot:run
```

5. Set up the frontend:
```bash
cd stock-frontend
npm install
npm start
```

The application will be available at:
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080

## 📊 API Endpoints

### Stock Analysis
- `GET /api/v1/analysis/trend/{symbol}` - Get trend analysis
- `GET /api/v1/analysis/volatility/{symbol}` - Get volatility analysis
- `GET /api/v1/analysis/moving-average/{symbol}` - Get moving averages
- `GET /api/v1/analysis/volume-analysis/{symbol}` - Get volume analysis
- `GET /api/v1/analysis/technical-indicators/{symbol}` - Get technical indicators

### Historical Data
- `GET /api/historical/{symbol}/recent` - Get recent prices
- `GET /api/historical/{symbol}/range` - Get prices in date range
- `GET /api/historical/{symbol}/statistics` - Get price statistics

## 🔧 Configuration

### Backend Configuration
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/stockdb
spring.datasource.username=your_username
spring.datasource.password=your_password

# Kafka
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.topic=stock-prices

# Yahoo Finance API
yahoo.finance.api.key=your_api_key
```

### Frontend Configuration
```typescript
// src/services/stockService.ts
const API_BASE_URL = 'http://localhost:8080/api/v1';
```

## 📈 Usage

1. Enter a stock symbol (e.g., AAPL, GOOGL, MSFT)
2. Select the desired timeframe
3. View real-time analysis including:
   - Price trends
   - Volume analysis
   - Technical indicators
   - Moving averages
   - Volatility metrics

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Yahoo Finance API for stock data
- Material-UI for the component library
- Apache Kafka for real-time data streaming
- Spring Boot team for the amazing framework

## 📧 Contact

Your Name - [@yourtwitter](https://twitter.com/yourtwitter) - email@example.com

Project Link: [https://github.com/yourusername/realtime-stock-pipeline](https://github.com/yourusername/realtime-stock-pipeline) 
