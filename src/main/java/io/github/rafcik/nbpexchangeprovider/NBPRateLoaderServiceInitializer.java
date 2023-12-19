package io.github.rafcik.nbpexchangeprovider;


import org.javamoney.moneta.spi.loader.LoadDataInformation;
import org.javamoney.moneta.spi.loader.LoadDataInformationBuilder;
import org.javamoney.moneta.spi.loader.LoaderService;

import javax.money.spi.Bootstrap;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
            .filter((LoadDataInformation loadDataInformation) -> loadDataInformation.getUpdatePolicy().equals(LoaderService.UpdatePolicy.NEVER))
            .forEach(loader::registerData);

        loadDataInformations
            .stream()
            .filter((LoadDataInformation loadDataInformation) -> !loader.isResourceRegistered(loadDataInformation.getResourceId()))
            .filter((LoadDataInformation loadDataInformation) -> !loadDataInformation.getUpdatePolicy().equals(LoaderService.UpdatePolicy.NEVER))
            .forEach(loader::registerAndLoadData);
    }

    public NBPRateLoaderServiceInitializer withCurrent() {
        LoadDataInformation loadDataInformation = new LoadDataInformationBuilder()
            .withResourceId("nbp-rate-provider-current")
            .withUpdatePolicy(LoaderService.UpdatePolicy.SCHEDULED)
            .withProperties(Map.of("period", "03:00"))
            .withLoaderListener(loaderListener)
            .withResourceLocations(URI.create("https://api.nbp.pl/api/exchangerates/tables/A"))
            .withStartRemote(false)
            .build();

        loadDataInformations.add(loadDataInformation);

        return this;
    }

    public NBPRateLoaderServiceInitializer withHistoricSinceBeginning() {
        LocalDate beginning = LocalDate.of(2002, Month.of(1), 1);

        return withHistoricSince(beginning);
    }

    public NBPRateLoaderServiceInitializer withHistoricSince(LocalDate date) {
        LocalDate fromDate = LocalDate.from(date);

        while (fromDate.isBefore(LocalDate.now())) {
            LocalDate toDate = createToDate(fromDate);

            LoadDataInformation result = createForSingleHistoric(fromDate, toDate);
            loadDataInformations.add(result);

            fromDate = toDate.plusDays(1);
        }

        return this;
    }

    private LocalDate createToDate(LocalDate fromDate) {
        LocalDate toDate = fromDate.plusMonths(3).minusDays(1);

        if (toDate.isAfter(LocalDate.now())) {
            return LocalDate.now();
        }

        return toDate;
    }

    private LoadDataInformation createForSingleHistoric(LocalDate from, LocalDate to) {
        String resourceId = String.format("nbp-rate-provider-historic-%tY-%tm-%td-%tY-%tm-%td", from, from, from, to, to, to);
        String url = String.format("https://api.nbp.pl/api/exchangerates/tables/A/%tY-%tm-%td/%tY-%tm-%td", from, from, from, to, to, to);

        String backupResourceUrl = String.format("src/main/resources/fallback//%s.dat", resourceId);
        File backupResourceFile = new File(backupResourceUrl);

        return new LoadDataInformationBuilder()
            .withResourceId(resourceId)
            .withUpdatePolicy(createUpdatePolicy(backupResourceFile))
            .withProperties(Map.of())
            .withLoaderListener(loaderListener)
            .withResourceLocations(URI.create(url))
            .withBackupResource(backupResourceFile.toURI())
            .withStartRemote(false)
            .build();
    }

    private LoaderService.UpdatePolicy createUpdatePolicy(File backupResourceFile) {
        if (backupResourceFile.exists()) {
            return LoaderService.UpdatePolicy.NEVER;
        }

        return LoaderService.UpdatePolicy.ONSTARTUP;
    }

}
