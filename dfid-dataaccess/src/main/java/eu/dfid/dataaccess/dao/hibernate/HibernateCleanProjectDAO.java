package eu.dfid.dataaccess.dao.hibernate;

import eu.dfid.dataaccess.dao.CleanProjectDAO;
import eu.dfid.dataaccess.dto.clean.DFIDCleanProject;
import eu.dl.dataaccess.dao.hibernate.GenericHibernateDAO;

/**
 * Hibernate DAO implementation for tenders.
 */
public class HibernateCleanProjectDAO extends GenericHibernateDAO<DFIDCleanProject>
        implements CleanProjectDAO<DFIDCleanProject> {

    @Override
    protected final Class<DFIDCleanProject> getDTOClass() {
        return DFIDCleanProject.class;
    }

    @Override
    public final DFIDCleanProject getEmptyInstance() {
        return new DFIDCleanProject();
    }
}
