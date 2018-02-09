package eu.dfid.dataaccess.dao.mongo;

import eu.dfid.dataaccess.dao.ParsedProjectDAO;
import eu.dfid.dataaccess.dto.parsed.ParsedProject;
import eu.dl.dataaccess.dao.mongo.GenericMongoDAO;

/**
 * Parsed Project DAO implementation for MongoDB.
 */
class MongoParsedProjectDAO extends GenericMongoDAO<ParsedProject> implements ParsedProjectDAO<ParsedProject> {
    private static final String PARSED_PROJECT_COLLECTION_NAME = "parsedProject";

    @Override
    protected final Class<ParsedProject> getDTOClass() {
        return ParsedProject.class;
    }

    @Override
    protected final String getCollectionName() {
        return PARSED_PROJECT_COLLECTION_NAME;
    }

    @Override
    public ParsedProject getEmptyInstance() {
        return new ParsedProject();
    }
}
