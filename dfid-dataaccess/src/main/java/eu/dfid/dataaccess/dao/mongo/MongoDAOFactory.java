package eu.dfid.dataaccess.dao.mongo;

import eu.dfid.dataaccess.dao.CleanProjectDAO;
import eu.dfid.dataaccess.dao.DAOFactory;
import eu.dfid.dataaccess.dao.ParsedProjectDAO;
import eu.dfid.dataaccess.dao.RawProjectDAO;
import eu.dl.dataaccess.dao.CleanTenderDAO;
import eu.dl.dataaccess.dao.CrawlerAuditDAO;
import eu.dl.dataaccess.dao.ParsedTenderDAO;
import eu.dl.dataaccess.dao.RawDataDAO;
import eu.dl.dataaccess.dao.TransactionUtils;
import eu.dl.dataaccess.dao.mongo.MongoCrawlerAuditDAO;
import eu.dl.dataaccess.dao.mongo.MongoTransactionUtils;

/**
 * DAO factory implementation for MongoDB database.
 */
public final class MongoDAOFactory extends DAOFactory {

    @Override
    public CrawlerAuditDAO getCrawlerAuditDAO(final String workerName, final String workerVersion) {
        return (CrawlerAuditDAO) new MongoCrawlerAuditDAO().populateWithWorkerMetadata(workerName, workerVersion);
    }

    // TENDER DAOs

    @Override
    public RawDataDAO getRawTenderDAO(final String workerName, final String workerVersion) {
        return (RawDataDAO) new MongoRawTenderDAO().populateWithWorkerMetadata(workerName, workerVersion);
    }

    @Override
    public ParsedTenderDAO getParsedTenderDAO(final String workerName, final String workerVersion) {
        return (ParsedTenderDAO) new MongoParsedTenderDAO().populateWithWorkerMetadata(workerName, workerVersion);
    }

    @Override
    public CleanTenderDAO getCleanTenderDAO(final String workerName, final String workerVersion) {
        return (CleanTenderDAO) new MongoCleanTenderDAO().populateWithWorkerMetadata(workerName, workerVersion);
    }

    // PROJECT DAOs

    @Override
    public RawProjectDAO getRawProjectDAO(final String workerName, final String workerVersion) {
        return (RawProjectDAO) new MongoRawProjectDAO().populateWithWorkerMetadata(workerName, workerVersion);
    }

    @Override
    public ParsedProjectDAO getParsedProjectDAO(final String workerName, final String workerVersion) {
        return (ParsedProjectDAO) new MongoParsedProjectDAO().populateWithWorkerMetadata(workerName, workerVersion);
    }

    @Override
    public CleanProjectDAO getCleanProjectDAO(final String workerName, final String workerVersion) {
        return (CleanProjectDAO) new MongoCleanProjectDAO().populateWithWorkerMetadata(workerName, workerVersion);
    }

    @Override
    public TransactionUtils getTransactionUtils() {
        return MongoTransactionUtils.getInstance();
    }
}
