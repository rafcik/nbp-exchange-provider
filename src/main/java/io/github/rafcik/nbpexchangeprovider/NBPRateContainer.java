package io.github.rafcik.nbpexchangeprovider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class NBPRateContainer {

    private final Map<LocalDate, Map<String, BigDecimal>> rates = new ConcurrentHashMap<>();

    public void addRate(LocalDate date, String currencyCode, BigDecimal rate) {
        if (!rates.containsKey(date)) {
            synchronized (rates) {
                if (!rates.containsKey(date)) {
                    rates.put(date, new ConcurrentHashMap<>());
                }
            }
        }

        Map<String, BigDecimal> dayRates = rates.get(date);
        dayRates.put(currencyCode, rate);
    }

    public Optional<BigDecimal> getRate(LocalDate[] dates, String currencyCode) {
        for (LocalDate date : dates) {
            Optional<BigDecimal> rate = getRate(date, currencyCode);

            if (rate.isPresent()) {
                return rate;
            }
        }

        return Optional.empty();
    }

    public Optional<BigDecimal> getRate(LocalDate date, String currencyCode) {
        return Optional
            .ofNullable(rates.get(date))
            .map(dayRates -> dayRates.get(currencyCode));
    }

    public Optional<BigDecimal> getRate(String currencyCode) {
        return rates
            .keySet()
            .stream()
            .max(Comparator.naturalOrder())
            .flatMap(date -> getRate(date, currencyCode));
    }

}
