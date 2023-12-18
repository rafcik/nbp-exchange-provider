package com.rafcik.nbpexchangeprovider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rafcik.nbpexchangeprovider.models.NBPRate;
import com.rafcik.nbpexchangeprovider.models.NBPRates;
import com.rafcik.nbpexchangeprovider.models.NBPRatesTable;
import org.javamoney.moneta.convert.ExchangeRateBuilder;
import org.javamoney.moneta.spi.AbstractRateProvider;
import org.javamoney.moneta.spi.DefaultNumberValue;
import org.javamoney.moneta.spi.loader.LoaderService;

import javax.money.MonetaryException;
import javax.money.convert.*;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NBPRateProvider extends AbstractRateProvider implements LoaderService.LoaderListener {

    private static final Logger LOG = Logger.getLogger(NBPRateProvider.class.getName());
    private static final String BASE_CURRENCY_CODE = "PLN";
    private final static ProviderContext CONTEXT = ProviderContextBuilder
        .of("NBP", RateType.ANY)
        .setRateTypes(RateType.HISTORIC, RateType.DEFERRED)
        .set("providerDescription", "Narodowy Bank Polski")
        .build();

    private volatile CountDownLatch loadLock = new CountDownLatch(1);

    private final NBPRateContainer rateContainer;
    private final ObjectMapper mapper;


    public NBPRateProvider() {
        super(CONTEXT);

        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        rateContainer = new NBPRateContainer();

        NBPRateLoaderServiceInitializer
            .create(this)
            .withCurrent()
            .withTestHistoric()
            .initialize();
    }

    @Override
    public void newDataLoaded(String s, InputStream inputStream) {
        try {
            NBPRates nbpRates = mapper.readValue(inputStream, new TypeReference<>() {
            });
            addNBPRates(nbpRates);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error during data load.", e);
        } finally {
            loadLock.countDown();
        }
    }

    public void addNBPRates(NBPRates nbpRates) {
        for (NBPRatesTable nbpRatesTable : nbpRates) {
            for (NBPRate nbpRate : nbpRatesTable.rates()) {
                LocalDate rateDate = nbpRatesTable.effectiveDate();
                String targetCurrencyCode = nbpRate.code();

                rateContainer.addRate(rateDate, targetCurrencyCode, nbpRate.mid());
            }
        }
    }

    @Override
    public ExchangeRate getExchangeRate(ConversionQuery conversionQuery) {
        Objects.requireNonNull(conversionQuery);
        try {
            if (loadLock.await(30, TimeUnit.SECONDS)) {
                if (areBothBaseCurrencies(conversionQuery)) {
                    return createBaseExchangeRateBuilder(conversionQuery)
                        .setFactor(DefaultNumberValue.ONE)
                        .build();
                } else {
                    return getExchangeRateForDifferentCurrencies(conversionQuery);
                }
            } else {
                throw new MonetaryException("Failed to load currency conversion data: ");
            }
        } catch (InterruptedException e) {
            throw new MonetaryException("Failed to load currency conversion data: Load task has been interrupted.", e);
        }
    }

    private ExchangeRate getExchangeRateForDifferentCurrencies(ConversionQuery conversionQuery) {
        if (BASE_CURRENCY_CODE.equals(conversionQuery.getBaseCurrency().getCurrencyCode())) {
            BigDecimal factor = getInvertedFactor(getQueryDates(conversionQuery), conversionQuery.getCurrency().getCurrencyCode());

            return createBaseExchangeRateBuilder(conversionQuery)
                .setFactor(DefaultNumberValue.of(factor))
                .build();
        } else if (BASE_CURRENCY_CODE.equals(conversionQuery.getCurrency().getCurrencyCode())) {
            BigDecimal factor = getFactor(getQueryDates(conversionQuery), conversionQuery.getBaseCurrency().getCurrencyCode());

            return createBaseExchangeRateBuilder(conversionQuery)
                .setFactor(DefaultNumberValue.of(factor))
                .build();
        } else {
            BigDecimal baseCurrencyFactor = getFactor(getQueryDates(conversionQuery), conversionQuery.getBaseCurrency().getCurrencyCode());
            BigDecimal currencyFactor = getFactor(getQueryDates(conversionQuery), conversionQuery.getCurrency().getCurrencyCode());

            BigDecimal factor = baseCurrencyFactor.divide(currencyFactor, MathContext.DECIMAL64);

            return createBaseExchangeRateBuilder(conversionQuery)
                .setFactor(DefaultNumberValue.of(factor))
                .build();
        }
    }

    private BigDecimal getFactor(LocalDate[] dates, String currencyCode) {
        return rateContainer
            .getRate(dates, currencyCode)
            .orElseThrow(() -> new MonetaryException("NBPRateProvider - can't find rate for given currency: " + currencyCode));
    }

    private BigDecimal getInvertedFactor(LocalDate[] dates, String currencyCode) {
        BigDecimal factor = getFactor(dates, currencyCode);

        return BigDecimal.ONE.divide(factor, MathContext.DECIMAL64);
    }

    private boolean areBothBaseCurrencies(ConversionQuery query) {
        return BASE_CURRENCY_CODE.equals(query.getBaseCurrency().getCurrencyCode()) &&
            BASE_CURRENCY_CODE.equals(query.getCurrency().getCurrencyCode());
    }

    private ExchangeRateBuilder createBaseExchangeRateBuilder(ConversionQuery query) {
        ConversionContext conversionContext = createBaseConversionContext(query);

        ExchangeRateBuilder builder = new ExchangeRateBuilder(conversionContext);
        builder.setBase(query.getBaseCurrency());
        builder.setTerm(query.getCurrency());

        return builder;
    }

    private ConversionContext createBaseConversionContext(ConversionQuery query) {
        return ConversionContextBuilder
            .create(CONTEXT, RateType.HISTORIC)
            .set(getQueryDates(query))
            .build();
    }

}
