package com.rafcik.nbpexchangeprovider.models;

import java.math.BigDecimal;

public record NBPRate(String currency, String code, BigDecimal mid) {
}
