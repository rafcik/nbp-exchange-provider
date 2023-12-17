package com.rafcik.nbpexchangeprovider;

import org.javamoney.moneta.spi.AbstractRateProvider;
import org.javamoney.moneta.spi.loader.LoaderService;

import javax.money.convert.ConversionQuery;
import javax.money.convert.ExchangeRate;
import javax.money.convert.ProviderContext;
import java.io.InputStream;

public class NBPRateProvider extends AbstractRateProvider implements LoaderService.LoaderListener {

    public NBPRateProvider(ProviderContext providerContext) {
        super(providerContext);
    }

    @Override
    public ExchangeRate getExchangeRate(ConversionQuery conversionQuery) {
        return null;
    }

    @Override
    public void newDataLoaded(String s, InputStream inputStream) {

    }

}
