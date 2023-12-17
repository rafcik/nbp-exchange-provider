package com.rafcik.nbpexchangeprovider;


import org.javamoney.moneta.spi.loader.LoadDataInformation;
import org.javamoney.moneta.spi.loader.LoadDataInformationBuilder;
import org.javamoney.moneta.spi.loader.LoaderService;

import javax.money.spi.Bootstrap;
import java.net.URI;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class NBPRateLoaderServiceInitializer {

    private final List<LoadDataInformation> loadDataInformations = new ArrayList<>();
    private final LoaderService.LoaderListener loaderListener;

    private NBPRateLoaderServiceInitializer(LoaderService.LoaderListener loaderListener) {
        this.loaderListener = loaderListener;
    }

    public static NBPRateLoaderServiceInitializer create(LoaderService.LoaderListener loaderListener) {
        return new NBPRateLoaderServiceInitializer(loaderListener);
    }

    public void initialize() {
        LoaderService loader = Bootstrap.getService(LoaderService.class);

        loadDataInformations
            .stream()
            .filter((LoadDataInformation loadDataInformation) -> !loader.isResourceRegistered(loadDataInformation.getResourceId()))
            .forEach(loader::registerAndLoadData);
    }

    public NBPRateLoaderServiceInitializer withCurrent() {
        LoadDataInformation loadDataInformation = new LoadDataInformationBuilder()
            .withResourceId("nbp-rate-provider-current")
            .withUpdatePolicy(LoaderService.UpdatePolicy.SCHEDULED)
            .withProperties(Map.of("period", "03:00"))
            .withLoaderListener(loaderListener)
//            .withBackupResource(URI.create(ECB_CURRENT_FALLBACK_PATH))
            .withResourceLocations(URI.create("https://api.nbp.pl/api/exchangerates/tables/A"))
            .withStartRemote(false)
            .build();

        loadDataInformations.add(loadDataInformation);

        return this;
    }

//    public NBPRateLoaderServiceInitializer withHistoric(LocalDate from, LocalDate to) {
//
//
//        loadDataInformations.add(loadDataInformation);
//
//        return this;
//    }

    public NBPRateLoaderServiceInitializer withTestHistoric() {
        List<LoadDataInformation> test = List.of(
            createForSingleHistoric(LocalDate.of(2020, Month.of(1), 1), LocalDate.of(2020, Month.of(3), 31)),
            createForSingleHistoric(LocalDate.of(2020, Month.of(4), 1), LocalDate.of(2020, Month.of(6), 30)),
            createForSingleHistoric(LocalDate.of(2020, Month.of(7), 1), LocalDate.of(2020, Month.of(9), 30)),
            createForSingleHistoric(LocalDate.of(2020, Month.of(10), 1), LocalDate.of(2020, Month.of(12), 31)),

            createForSingleHistoric(LocalDate.of(2021, Month.of(1), 1), LocalDate.of(2021, Month.of(3), 31)),
            createForSingleHistoric(LocalDate.of(2021, Month.of(4), 1), LocalDate.of(2021, Month.of(6), 30)),
            createForSingleHistoric(LocalDate.of(2021, Month.of(7), 1), LocalDate.of(2021, Month.of(9), 30)),
            createForSingleHistoric(LocalDate.of(2021, Month.of(10), 1), LocalDate.of(2021, Month.of(12), 31)),

            createForSingleHistoric(LocalDate.of(2022, Month.of(1), 1), LocalDate.of(2022, Month.of(3), 31)),
            createForSingleHistoric(LocalDate.of(2022, Month.of(4), 1), LocalDate.of(2022, Month.of(6), 30)),
            createForSingleHistoric(LocalDate.of(2022, Month.of(7), 1), LocalDate.of(2022, Month.of(9), 30)),
            createForSingleHistoric(LocalDate.of(2022, Month.of(10), 1), LocalDate.of(2022, Month.of(12), 31)),

            createForSingleHistoric(LocalDate.of(2023, Month.of(1), 1), LocalDate.of(2023, Month.of(3), 31)),
            createForSingleHistoric(LocalDate.of(2023, Month.of(4), 1), LocalDate.of(2023, Month.of(6), 30)),
            createForSingleHistoric(LocalDate.of(2023, Month.of(7), 1), LocalDate.of(2023, Month.of(9), 30)),
            createForSingleHistoric(LocalDate.of(2023, Month.of(10), 1), LocalDate.now())
        );

        loadDataInformations.addAll(test);

        return this;
    }

    private LoadDataInformation createForSingleHistoric(LocalDate from, LocalDate to) {
        String resourceId = String.format("nbp-rate-provider-historic-%tY-%tm-%td-%tY-%tm-%td", from, from, from, to, to, to);
        String url = String.format("https://api.nbp.pl/api/exchangerates/tables/A/%tY-%tm-%td/%tY-%tm-%td", from, from, from, to, to, to);

        return new LoadDataInformationBuilder()
            .withResourceId(resourceId)
            .withUpdatePolicy(LoaderService.UpdatePolicy.ONSTARTUP)
            .withProperties(Map.of())
            .withLoaderListener(loaderListener)
//            .withBackupResource(URI.create(ECB_CURRENT_FALLBACK_PATH))
            .withResourceLocations(URI.create(url))
            .withStartRemote(false)
            .build();
    }

}
