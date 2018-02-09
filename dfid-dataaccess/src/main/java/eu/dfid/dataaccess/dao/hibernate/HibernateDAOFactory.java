package eu.dfid.dataaccess.dao.hibernate;

import eu.dfid.dataaccess.dao.CleanProjectDAO;
import eu.dfid.dataaccess.dao.DAOFactory;
import eu.dfid.dataaccess.dao.ParsedProjectDAO;
import eu.dfid.dataaccess.dao.RawProjectDAO;
import eu.dl.dataaccess.dao.CleanTenderDAO;
import eu.dl.dataaccess.dao.CrawlerAuditDAO;
import eu.dl.dataaccess.dao.ParsedTenderDAO;
import eu.dl.dataaccess.dao.RawDataDAO;
import eu.dl.dataaccess.dao.TransactionUtils;
import eu.dl.dataaccess.dao.hibernate.HibernateCrawlerAuditDAO;
import eu.dl.dataaccess.dao.hibernate.HibernateRawDataDAO;
import eu.dl.dataaccess.dao.hibernate.HibernateTransactionUtils;


/**
 * DAO factory implementation for Hibernate data sources.
 */
public final class HibernateDAOFactory extends DAOFactory {
     @Override
    public CrawlerAuditDAO getCrawlerAuditDAO(final String workerName, final String workerVersion) {
        return (CrawlerAuditDAO) new HibernateCrawlerAuditDAO().populateWithWorkerMetadata(workerName, workerVersion);
    }

    // TENDER DAOs

    @Override
    public RawDataDAO getRawTenderDAO(final String workerName, final String workerVersion) {
        return (RawDataDAO) new HibernateRawDataDAO().populateWithWorkerMetadata(workerName, workerVersion);
    }

    @Override
    public ParsedTenderDAO getParsedTenderDAO(final String workerName, final String workerVersion) {
        return (ParsedTenderDAO) new HibernateParsedTenderDAO().populateWithWorkerMetadata(workerName, workerVersion);
    }

    @Override
    public CleanTenderDAO getCleanTenderDAO(final String workerName, final String workerVersion) {
        return (CleanTenderDAO) new HibernateCleanTenderDAO().populateWithWorkerMetadata(workerName, workerVersion);
    }

    // PROJECT DAOs

    @Override
    public RawProjectDAO getRawProjectDAO(final String workerName, final String workerVersion) {
        return (RawProjectDAO) new HibernateRawProjectDAO().populateWithWorkerMetadata(workerName, workerVersion);
    }

    @Override
    public ParsedProjectDAO getParsedProjectDAO(final String workerName, final String workerVersion) {
        return (ParsedProjectDAO) new HibernateParsedProjectDAO().populateWithWorkerMetadata(workerName, workerVersion);
    }

    @Override
    public CleanProjectDAO getCleanProjectDAO(final String workerName, final String workerVersion) {
        return (CleanProjectDAO) new HibernateCleanProjectDAO().populateWithWorkerMetadata(workerName, workerVersion);
    }

    @Override
    public TransactionUtils getTransactionUtils() {
        return HibernateTransactionUtils.getInstance();
    }
}
