package eu.dfid.dataaccess.dao.hibernate;

import eu.dfid.dataaccess.dao.RawProjectDAO;
import eu.dl.dataaccess.dao.hibernate.HibernateRawDataDAO;
import eu.dl.dataaccess.dto.raw.RawData;

/**
 * Raw Project DAO implementation for Hibernate.
 */
public class HibernateRawProjectDAO extends HibernateRawDataDAO implements RawProjectDAO<RawData> {
}
