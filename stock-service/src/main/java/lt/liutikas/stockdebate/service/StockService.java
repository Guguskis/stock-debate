package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.model.FullStockDetail;
import lt.liutikas.stockdebate.model.IsStock;
import lt.liutikas.stockdebate.model.SimpleStockDetail;
import lt.liutikas.stockdebate.model.Stock;
import lt.liutikas.stockdebate.repository.InformationRepository;
import lt.liutikas.stockdebate.repository.IsStockRepository;
import lt.liutikas.stockdebate.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StockService {

    private final Logger LOG = LoggerFactory.getLogger(StockService.class);

    private final StockRepository stockRepository;
    private final IsStockRepository isStockRepository;
    private final InformationRepository informationRepository;

    public StockService(StockRepository stockRepository, IsStockRepository isStockRepository, InformationRepository informationRepository) {
        this.stockRepository = stockRepository;
        this.isStockRepository = isStockRepository;
        this.informationRepository = informationRepository;
    }

    public ResponseEntity getStock(String symbol) {
        Stock liveStock = informationRepository.getStock(symbol);

        if (liveStock == null) {
            return ResponseEntity.notFound().build();
        }

        Stock databaseStock = stockRepository.findBySymbolIgnoreCase(symbol);
        if (databaseStock == null) {
            liveStock.setLogoUrl(getLogoUrl(symbol));
            stockRepository.save(liveStock);
        } else {
            liveStock.setLogoUrl(databaseStock.getLogoUrl());
        }

        LOG.info(String.format("Retrieved stock '%s'", symbol));
        return ResponseEntity.ok(liveStock);
    }

    private String getLogoUrl(String symbol) {
        return "https://external-content.duckduckgo.com/iu/?u=http%3A%2F%2Fwww.norrislakemarinas.org%2Fwp-content%2Fthemes%2Fnorris%2Fimg%2Flogo_placeholder.png&f=1&nofb=1";
    }

    public ResponseEntity getStockPrice(String symbol, String dateString) {

        List<FullStockDetail> fullStockDetails = informationRepository.getFullStockDetails(symbol, dateString);

        if (fullStockDetails == null) {
            return ResponseEntity.badRequest().build();
        }

        if (fullStockDetails.size() == 0) {
            String message = String.format("Stock price for day '%s' is unavailable, maybe holiday?", dateString);
            LOG.warn(message);
            return ResponseEntity.badRequest().body(message);
        }

        FullStockDetail fullStockDetail = fullStockDetails.get(0);
        SimpleStockDetail simpleStockDetail = mapToSimpleStockDetail(fullStockDetail, symbol);
        LOG.info(String.format("Retrieved details for stock '%s' at date '%s'", symbol, dateString));
        return ResponseEntity.ok(simpleStockDetail);
    }

    private SimpleStockDetail mapToSimpleStockDetail(FullStockDetail fullStockDetail, String symbol) {
        SimpleStockDetail simpleStockDetail = new SimpleStockDetail();
        simpleStockDetail.setSymbol(symbol.toUpperCase());
        simpleStockDetail.setPrice(fullStockDetail.getClose());
        simpleStockDetail.setDate(fullStockDetail.getDate());
        return simpleStockDetail;
    }

    public ResponseEntity isStockExists(String symbol) {
        IsStock isStock = isStockRepository.findByStockSymbol(symbol);

        if (isStock == null) {
            Stock stock = informationRepository.getStock(symbol);
            IsStock newIsStock = new IsStock();

            newIsStock.setStockSymbol(symbol);
            newIsStock.setStock(stock != null);

            isStock = isStockRepository.save(newIsStock);
        }

        return ResponseEntity.ok(isStock);
    }
}
