package com.rafcik.nbpexchangeprovider;

import org.javamoney.moneta.Money;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.convert.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Month;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

class NBPRateProviderTest {

    private static final CurrencyUnit EUR = Monetary.getCurrency("EUR");
    private static final CurrencyUnit USD = Monetary.getCurrency("USD");
    private static final CurrencyUnit PLN = Monetary.getCurrency("PLN");
    private static final BigDecimal USD_TO_PLN_RATE = BigDecimal.valueOf(3.9910);
    private static final BigDecimal EUR_TO_PLN_RATE = BigDecimal.valueOf(4.3494);
    private static final BigDecimal USD_TO_EUR_RATE = USD_TO_PLN_RATE.divide(EUR_TO_PLN_RATE, MathContext.DECIMAL64);
    private static final MonetaryAmount TEN_PLN = Money.of(BigDecimal.TEN, PLN);
    private static final MonetaryAmount TEN_USD = Money.of(BigDecimal.TEN, USD);
    private static final MonetaryAmount TEN_EUR = Money.of(BigDecimal.TEN, EUR);
    private static final LocalDate DATE = LocalDate.of(2023, Month.DECEMBER, 1);

    private ExchangeRateProvider provider;

    @BeforeTest
    public void setup() {
        provider = MonetaryConversions.getExchangeRateProvider("NBP");
    }

    @Test
    public void shouldReturnNBPRateProvider() {
        assertNotNull(provider);
        assertEquals(provider.getClass(), NBPRateProvider.class);
    }

    @Test
    public void shouldReturnSameUSDValue() {
        CurrencyConversion currencyConversion = provider.getCurrencyConversion(USD);

        MonetaryAmount result = currencyConversion.apply(TEN_USD);

        assertEquals(result.getCurrency(), USD);
        assertEquals(result.getNumber().numberValue(BigDecimal.class), BigDecimal.TEN);
    }

    @Test
    public void shouldReturnSamePLNValue() {
        CurrencyConversion currencyConversion = provider.getCurrencyConversion(PLN);

        MonetaryAmount result = currencyConversion.apply(TEN_PLN);

        assertEquals(result.getCurrency(), PLN);
        assertEquals(result.getNumber().numberValue(BigDecimal.class), BigDecimal.TEN);
    }

    @Test
    public void shouldReturnSameEURValue() {
        CurrencyConversion currencyConversion = provider.getCurrencyConversion(EUR);

        MonetaryAmount result = currencyConversion.apply(TEN_EUR);

        assertEquals(result.getCurrency(), EUR);
        assertEquals(result.getNumber().numberValue(BigDecimal.class),BigDecimal.TEN);
    }

    @Test
    public void shouldConvertPLNToUSD() {
        ConversionQuery conversionQuery = ConversionQueryBuilder.of()
            .setBaseCurrency(PLN)
            .setTermCurrency(USD)
            .set(DATE)
            .build();
        CurrencyConversion currencyConversion = provider.getCurrencyConversion(conversionQuery);

        MonetaryAmount result = currencyConversion.apply(TEN_PLN);

        assertEquals(result.getCurrency(), USD);
        assertEquals(result.getNumber().doubleValue(), BigDecimal.TEN.divide(USD_TO_PLN_RATE, MathContext.DECIMAL64).doubleValue());
    }

    @Test
    public void shouldConvertUSDToPLN() {
        ConversionQuery conversionQuery = ConversionQueryBuilder.of()
            .setBaseCurrency(USD)
            .setTermCurrency(PLN)
            .set(DATE)
            .build();
        CurrencyConversion currencyConversion = provider.getCurrencyConversion(conversionQuery);

        MonetaryAmount result = currencyConversion.apply(TEN_USD);

        assertEquals(result.getCurrency(), PLN);
        assertEquals(result.getNumber().doubleValue(), BigDecimal.TEN.multiply(USD_TO_PLN_RATE).doubleValue());
    }

    @Test
    public void shouldConvertPLNToEUR() {
        ConversionQuery conversionQuery = ConversionQueryBuilder.of()
            .setBaseCurrency(PLN)
            .setTermCurrency(EUR)
            .set(DATE)
            .build();
        CurrencyConversion currencyConversion = provider.getCurrencyConversion(conversionQuery);

        MonetaryAmount result = currencyConversion.apply(TEN_PLN);

        assertEquals(result.getCurrency(), EUR);
        assertEquals(result.getNumber().doubleValue(), BigDecimal.TEN.divide(EUR_TO_PLN_RATE, MathContext.DECIMAL64).doubleValue());
    }

    @Test
    public void shouldConvertEURToPLN() {
        ConversionQuery conversionQuery = ConversionQueryBuilder.of()
            .setBaseCurrency(EUR)
            .setTermCurrency(PLN)
            .set(DATE)
            .build();
        CurrencyConversion currencyConversion = provider.getCurrencyConversion(conversionQuery);

        MonetaryAmount result = currencyConversion.apply(TEN_EUR);

        assertEquals(result.getCurrency(), PLN);
        assertEquals(result.getNumber().doubleValue(), BigDecimal.TEN.multiply(EUR_TO_PLN_RATE).doubleValue());
    }

    @Test
    public void shouldConvertEURToUSD() {
        ConversionQuery conversionQuery = ConversionQueryBuilder.of()
            .setBaseCurrency(EUR)
            .setTermCurrency(USD)
            .set(DATE)
            .build();
        CurrencyConversion currencyConversion = provider.getCurrencyConversion(conversionQuery);

        MonetaryAmount result = currencyConversion.apply(TEN_EUR);

        assertEquals(result.getCurrency(), USD);
        assertEquals(result.getNumber().doubleValue(), BigDecimal.TEN.divide(USD_TO_EUR_RATE, MathContext.DECIMAL64).doubleValue());
    }

    @Test
    public void shouldConvertUSDToEUR() {
        ConversionQuery conversionQuery = ConversionQueryBuilder.of()
            .setBaseCurrency(USD)
            .setTermCurrency(EUR)
            .set(DATE)
            .build();
        CurrencyConversion currencyConversion = provider.getCurrencyConversion(conversionQuery);

        MonetaryAmount result = currencyConversion.apply(TEN_USD);

        assertEquals(result.getCurrency(), EUR);
        assertEquals(result.getNumber().doubleValue(), BigDecimal.TEN.multiply(USD_TO_EUR_RATE).doubleValue());
    }

}