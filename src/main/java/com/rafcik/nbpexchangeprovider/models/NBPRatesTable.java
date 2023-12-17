package com.rafcik.nbpexchangeprovider.models;

import java.time.LocalDate;
import java.util.List;

public record NBPRatesTable(String table, String no, LocalDate effectiveDate, List<NBPRate> rates) {
}
