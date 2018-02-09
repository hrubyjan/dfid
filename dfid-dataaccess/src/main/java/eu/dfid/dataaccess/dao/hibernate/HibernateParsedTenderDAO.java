package eu.dfid.dataaccess.dao.hibernate;

import eu.dfid.dataaccess.dto.parsed.DFIDParsedTender;
import eu.dl.dataaccess.dao.ParsedTenderDAO;
import eu.dl.dataaccess.dao.hibernate.GenericHibernateDAO;

/**
 * Hibernate DAO implementation for tenders.
 */
public class HibernateParsedTenderDAO extends GenericHibernateDAO<DFIDParsedTender>
        implements ParsedTenderDAO<DFIDParsedTender> {

    @Override
    protected final Class<DFIDParsedTender> getDTOClass() {
        return DFIDParsedTender.class;
    }

    @Override
    public final DFIDParsedTender getEmptyInstance() {
        return new DFIDParsedTender();
    }
}
