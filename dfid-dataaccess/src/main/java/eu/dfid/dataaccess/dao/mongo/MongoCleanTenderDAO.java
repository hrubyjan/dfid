package eu.dfid.dataaccess.dao.mongo;

import eu.dfid.dataaccess.dto.clean.DFIDCleanTender;
import eu.dl.dataaccess.dao.CleanTenderDAO;
import eu.dl.dataaccess.dao.mongo.GenericMongoDAO;

import java.time.LocalDate;
import java.util.List;

/**
 * Clean Tender DAO implementation for MongoDB.
 */
public class MongoCleanTenderDAO extends GenericMongoDAO<DFIDCleanTender> implements CleanTenderDAO<DFIDCleanTender> {
    private static final String CLEAN_TENDER_COLLECTION_NAME = "cleanTender";

    @Override
    protected final Class<DFIDCleanTender> getDTOClass() {
        return DFIDCleanTender.class;
    }

    @Override
    protected final String getCollectionName() {
        return CLEAN_TENDER_COLLECTION_NAME;
    }

    @Override
    public final DFIDCleanTender getEmptyInstance() {
        return new DFIDCleanTender();
    }

    @Override
    public final List<DFIDCleanTender> getByCountry(final String countryCode, final Integer page) {
        return null;
    }

    @Override
    public final List<String> getIncludedPublicationSourceIds(final LocalDate date) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
