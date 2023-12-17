package com.rafcik.nbpexchangeprovider;

import org.javamoney.moneta.spi.AbstractRateProvider;
import org.javamoney.moneta.spi.loader.LoaderService;

import javax.money.convert.ConversionQuery;
import javax.money.convert.ExchangeRate;
import javax.money.convert.ProviderContext;
import java.io.InputStream;

public class NBPRateProvider extends AbstractRateProvider implements LoaderService.LoaderListener {

    private final NBPExchangeRateContainer nbpExchangeRateContainer;

    public NBPRateProvider(ProviderContext providerContext) {
        super(providerContext);

        nbpExchangeRateContainer = new NBPExchangeRateContainer();
    }

    @Override
    public ExchangeRate getExchangeRate(ConversionQuery conversionQuery) {
        return nbpExchangeRateContainer.getExchangeRate(conversionQuery);
    }

    @Override
    public void newDataLoaded(String s, InputStream inputStream) {

    }

}
