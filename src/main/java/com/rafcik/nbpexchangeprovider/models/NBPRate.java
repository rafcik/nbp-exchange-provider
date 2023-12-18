package com.rafcik.nbpexchangeprovider.models;

import java.math.BigDecimal;

public record NBPRate(String country, String symbol, String currency, String code, BigDecimal mid) {
}
