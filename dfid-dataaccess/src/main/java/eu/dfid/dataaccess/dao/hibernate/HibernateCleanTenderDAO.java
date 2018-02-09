package eu.dfid.dataaccess.dao.hibernate;

import eu.dfid.dataaccess.dto.clean.DFIDCleanTender;
import eu.dl.dataaccess.dao.CleanTenderDAO;
import eu.dl.dataaccess.dao.hibernate.GenericHibernateDAO;

import java.time.LocalDate;
import java.util.List;

/**
 * Hibernate DAO implementation for tenders.
 */
public class HibernateCleanTenderDAO extends GenericHibernateDAO<DFIDCleanTender>
        implements CleanTenderDAO<DFIDCleanTender> {

    @Override
    protected final Class<DFIDCleanTender> getDTOClass() {
        return DFIDCleanTender.class;
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
