package eu.dfid.worker.clean.plugin.project;

import eu.dfid.dataaccess.dto.clean.DFIDCleanProject;
import eu.dfid.dataaccess.dto.parsed.ParsedProject;
import eu.dl.worker.clean.plugin.BaseDateTimePlugin;
import eu.dl.worker.clean.plugin.DatePlugin;
import eu.dl.worker.utils.ArrayUtils;
import eu.dl.worker.clean.utils.DateUtils;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Date cleaning plugin for DFID Projects.
 *
 * @author Michal Riha
 *
 * @param <T> parsedProject
 * @param <U> cleanProject
 */
public class DFIDProjectDatePlugin<T extends ParsedProject, U extends DFIDCleanProject>
        extends BaseDateTimePlugin<DatePlugin, T, U> {
    /**
     * DatePlugin should be initialised with the pattern of the date.
     *
     * @param formatter
     *            formatter used to parse the date/datetime fields
     */
    public DFIDProjectDatePlugin(final DateTimeFormatter formatter) {
        super(formatter);
    }

    /**
     * Plugin constructor with configuration.
     *
     * @param formatters
     *       list of datetime formatters
     */
    public DFIDProjectDatePlugin(final List<DateTimeFormatter> formatters) {
        super(formatters);
    }

    /**
     * Cleans date and date fields.
     *
     * @param parsedProject
     *            project with source data
     * @param cleanProject
     *            project with clean data
     *
     * @return project with cleaned data
     */
    @Override
    public final DFIDCleanProject clean(final ParsedProject parsedProject, final DFIDCleanProject cleanProject) {
        cleanProject.setSignatureDates(ArrayUtils.walk(parsedProject.getSignatureDates(),
            signatureDate -> DateUtils.cleanDate(signatureDate, formatters)));

        cleanProject.setApprovalDate(DateUtils.cleanDate(parsedProject.getApprovalDate(), formatters));

        cleanProject.setClosingDate(DateUtils.cleanDate(parsedProject.getClosingDate(), formatters));

        cleanProject.setDeactivationDate(DateUtils.cleanDate(parsedProject.getDeactivationDate(), formatters));

        return cleanProject;
    }


}
