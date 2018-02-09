package eu.dfid.dataaccess.dao.mongo;

import eu.dfid.dataaccess.dto.parsed.DFIDParsedTender;
import eu.dl.dataaccess.dao.ParsedTenderDAO;
import eu.dl.dataaccess.dao.mongo.GenericMongoDAO;

/**
 * Parsed Tender DAO implementation for MongoDB.
 */
class MongoParsedTenderDAO extends GenericMongoDAO<DFIDParsedTender> implements ParsedTenderDAO<DFIDParsedTender> {
    private static final String PARSED_TENDER_COLLECTION_NAME = "parsedTender";

    @Override
    protected final Class<DFIDParsedTender> getDTOClass() {
        return DFIDParsedTender.class;
    }

    @Override
    protected final String getCollectionName() {
        return PARSED_TENDER_COLLECTION_NAME;
    }

    @Override
    public final DFIDParsedTender getEmptyInstance() {
        return new DFIDParsedTender();
    }
}
