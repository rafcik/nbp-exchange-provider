package com.rafcik.nbpexchangeprovider;

import javax.money.MonetaryException;
import javax.money.convert.ConversionQuery;
import javax.money.convert.ExchangeRate;

public class NBPExchangeRateContainer {

    public ExchangeRate getExchangeRate(ConversionQuery conversionQuery) {
        throw new MonetaryException("NBPExchangeRateContainer.getExchangeRate is not implemented yet");
    }

}
