package eu.dfid.dataaccess.dao.hibernate;

import eu.dfid.dataaccess.dao.ParsedProjectDAO;
import eu.dfid.dataaccess.dto.parsed.ParsedProject;
import eu.dl.dataaccess.dao.hibernate.GenericHibernateDAO;

/**
 * Parsed Project DAO implementation for MongoDB.
 */
class HibernateParsedProjectDAO extends GenericHibernateDAO<ParsedProject> implements ParsedProjectDAO<ParsedProject> {
    
    @Override
    protected final Class<ParsedProject> getDTOClass() {
        return ParsedProject.class;
    }

    @Override
    public ParsedProject getEmptyInstance() {
        return new ParsedProject();
    }
}
