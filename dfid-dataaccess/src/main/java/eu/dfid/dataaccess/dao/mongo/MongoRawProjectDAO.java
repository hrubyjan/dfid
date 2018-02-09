package eu.dfid.dataaccess.dao.mongo;

import eu.dfid.dataaccess.dao.RawProjectDAO;
import eu.dl.dataaccess.dao.mongo.MongoRawDataDAO;
import eu.dl.dataaccess.dto.raw.RawData;

/**
 * Raw Project DAO implementation for MongoDB.
 */
public class MongoRawProjectDAO extends MongoRawDataDAO implements RawProjectDAO<RawData> {
    private static final String RAW_PROJECT_COLLECTION_NAME = "rawProject";

    @Override
    protected final String getCollectionName() {
        return RAW_PROJECT_COLLECTION_NAME;
    }
}
