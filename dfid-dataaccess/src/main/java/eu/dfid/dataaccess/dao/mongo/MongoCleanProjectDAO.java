package eu.dfid.dataaccess.dao.mongo;

import eu.dfid.dataaccess.dao.CleanProjectDAO;
import eu.dfid.dataaccess.dto.clean.DFIDCleanProject;
import eu.dl.dataaccess.dao.mongo.GenericMongoDAO;

/**
 * Clean Project DAO implementation for MongoDB.
 */
public class MongoCleanProjectDAO extends GenericMongoDAO<DFIDCleanProject>
    implements CleanProjectDAO<DFIDCleanProject> {

    private static final String CLEAN_PROJECT_COLLECTION_NAME = "cleanProject";

    @Override
    protected final Class<DFIDCleanProject> getDTOClass() {
        return DFIDCleanProject.class;
    }

    @Override
    protected final String getCollectionName() {
        return CLEAN_PROJECT_COLLECTION_NAME;
    }

    @Override
    public final DFIDCleanProject getEmptyInstance() {
        return new DFIDCleanProject();
    }
}
