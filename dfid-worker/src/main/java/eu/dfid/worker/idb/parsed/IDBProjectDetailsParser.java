package eu.dfid.worker.idb.parsed;

import eu.dfid.dataaccess.dto.codetables.PublicationSources;
import eu.dfid.dataaccess.dto.parsed.DFIDParsedFinancingSummaryItem;
import eu.dfid.dataaccess.dto.parsed.DFIDParsedProjectOperation;
import eu.dfid.dataaccess.dto.parsed.DFIDParsedWeightedAttribute;
import eu.dfid.dataaccess.dto.parsed.ParsedProject;
import eu.dfid.worker.parser.BaseDFIDProjectParser;
import eu.dfid.worker.parser.ParserUtils;
import eu.dl.core.UnrecoverableException;
import eu.dl.dataaccess.dto.parsed.ParsedFunding;
import eu.dl.dataaccess.dto.parsed.ParsedPrice;
import eu.dl.dataaccess.dto.parsed.ParsedPublication;
import eu.dl.dataaccess.dto.raw.RawData;
import eu.dl.worker.utils.jsoup.JsoupUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Project details parser for Inter-American Development Bank (IDB).
 *
 * @author Tomas Mrazek
 */
public class IDBProjectDetailsParser extends BaseDFIDProjectParser {
    private static final String VERSION = "1";

    /**
     * IDB financing price is shown in USD millions.
     */
    private static final double IDB_FINANCING_MULTIPLIER = 1000000;

    private static final Locale LOCALE = Locale.US;

    /**
     * IDB financing number format.
     */
    private static final NumberFormat NUMBER_FORMAT;

    static {
        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(LOCALE);
        formatSymbols.setDecimalSeparator('.');
        formatSymbols.setGroupingSeparator(',');
        NUMBER_FORMAT = new DecimalFormat("#,##0.###", formatSymbols);
    }

    @Override
    public final List<ParsedProject> parse(final RawData rawProject) {
        ParsedProject parsedProject = new ParsedProject();

        final Document document = Jsoup.parse(rawProject.getSourceData());
        final Element originNode = JsoupUtils.selectFirst(".fondoBlanco", document);

        if (originNode == null) {
            logger.error("HTML node with class 'fondoBlanco' which includes project data is missing");
            throw new UnrecoverableException("HTML node with project data is missing");
        }

        final Element headerNode = JsoupUtils.selectFirst(".headerTitle", originNode);
        final Element projectDetailNode = JsoupUtils.selectFirst(".upProjectDetails", originNode);
        final Element projectDetailLftTblNode = JsoupUtils.selectFirst(".pdLeft table", projectDetailNode);
        final Element projectDetailRgtTblNode = JsoupUtils.selectFirst(".pdRight table", projectDetailNode);
        final Element projectInformation = JsoupUtils.selectFirst(".downProjectDetails", originNode);

        //this currency is used for every price from project detail except IDB financing (this is shown in
        //US$ millions) and final cost (this is shown in US$)
        final String currency = parseCurrency(projectInformation);

        parsedProject
                .setName(JsoupUtils.selectText("h1", headerNode))
                .setStatus(JsoupUtils.getFirstValueByLabel(headerNode, "(?i)project status"))
                .setDescription(JsoupUtils.selectText(".textVideo", originNode))
                .setProjectId(JsoupUtils.getFirstValueByLabel(projectDetailLftTblNode, "(?i)project number"))
                .setOperations(parseOperations(projectDetailLftTblNode))
                .setCountry(JsoupUtils.getFirstValueByLabel(projectDetailLftTblNode, "(?i)country"))
                .addMajorSector(new DFIDParsedWeightedAttribute()
                        .setName(JsoupUtils.getFirstValueByLabel(projectDetailLftTblNode, "(?i)sector")))
                .addSector(new DFIDParsedWeightedAttribute()
                        .setName(JsoupUtils.getFirstValueByLabel(projectDetailLftTblNode, "(?i)subsector")))
                .setLendingInstrument(parseProjectType(Arrays.asList(projectDetailLftTblNode, projectDetailRgtTblNode)))
                .setEnvironmentalAndSocialCategory(JsoupUtils.getFirstValueByLabel(projectDetailRgtTblNode,
                        "(?i)environmental and social impact category"))
                .setSignatureDates(parseSignatureDates(projectDetailRgtTblNode))
                .setApprovalDate(parseApprovalDate(projectDetailRgtTblNode))
                .setDonorFinancings(parseDonorFinancings(projectInformation, currency))
                .addDonorFinancing(praseIDBFinancing(projectDetailRgtTblNode))
                .setEstimatedCost(parsePrice(projectInformation, "(?i)estimated total cost", currency))
                .setEstimatedBorrowerFinancing(
                        parsePrice(projectInformation, "(?i)estimated country counterpart financing", currency))
                .addFinancingSummaryItem(parseFinancingSummary(projectInformation, currency))
                .setLendingInstrumentTypes(JsoupUtils.getAllValuesByLabel(projectInformation, "(?i)financing type"))
                .setFundings(parseFundings(projectInformation))
                .setFinalCost(parsePrice(projectInformation, "(?i)total cost", "USD"))
                .setBorrowerFinancing(parsePrice(projectInformation, "(?i)country counterpart financing", currency))
                .addPublication(new ParsedPublication()
                        .setLastUpdate(parseLastUpdate(projectInformation))
                        .setIsIncluded(true)
                        .setSource(PublicationSources.IDB)
                        .setHumanReadableUrl(rawProject.getSourceUrl().toString()));

        /*
        project.financingSummary.incomeCollected.netAmount
        getFirstTableValueByLabel(projectInformation, "income collected")
        */

        return Arrays.asList(parsedProject);
    }

    /**
     * Parse last update.
     *
     * @param projectInformation element to parse from
     *
     * @return String or null
     */
    private String parseLastUpdate(final Element projectInformation) {
        return JsoupUtils.getFirstValueByLabel(projectInformation, "(?i)reporting date");
    }

    /**
     * Parse approval date.
     *
     * @param projectDetailRgtTblNode element to parse from
     *
     * @return String or null
     */
    private String parseApprovalDate(final Element projectDetailRgtTblNode) {
        return JsoupUtils.getFirstValueByLabel(projectDetailRgtTblNode, "(?i)approval date");
    }

    /**
     * @param projectInformationNode node with project informations
     * @param currency               currency of prices
     *
     * @return list of donor financings
     */
    private List<ParsedPrice> parseDonorFinancings(final Element projectInformationNode, final String currency) {
        final List<String> values = JsoupUtils.getAllValuesByLabel(projectInformationNode, "(?i)^amount$");
        if (values == null) {
            return null;
        }

        return values.stream()
                .map((amount) -> new ParsedPrice().setNetAmount(removeCurrencyFromAmount(amount)).setCurrency(currency))
                .collect(Collectors.toList());
    }

    /**
     * @param projectInformationNode node with project informations
     *
     * @return list of fundings
     */
    private List<ParsedFunding> parseFundings(final Element projectInformationNode) {
        final List<String> values = JsoupUtils.getAllValuesByLabel(projectInformationNode, "(?i)fund");
        if (values == null) {
            return null;
        }

        return values.stream()
                .map((fund) -> new ParsedFunding().setSource(fund))
                .collect(Collectors.toList());
    }

    /**
     * Parses financing summary.
     *
     * @param context  node which includes financing summary data
     * @param currency currency of parsed prices
     *
     * @return financing summary or null in case that no values were parsed
     */
    private DFIDParsedFinancingSummaryItem parseFinancingSummary(final Element context, final String currency) {

        final DFIDParsedFinancingSummaryItem summary = new DFIDParsedFinancingSummaryItem();
        boolean isSummaryEmpty = true;

        final ParsedPrice cancelledPrice = parsePrice(context, "(?i)cancelled amount", currency);
        if (cancelledPrice != null) {
            summary.setCancelledAmount(cancelledPrice);
            isSummaryEmpty = false;
        }

        final ParsedPrice disbursedPrice = parsePrice(context, "(?i)isbursed to date", currency);
        if (disbursedPrice != null) {
            summary.setDisbursed(disbursedPrice);
            isSummaryEmpty = false;
        }

        final ParsedPrice repaymentsPrice = parsePrice(context, "(?i)repayments", currency);
        if (repaymentsPrice != null) {
            summary.setRepayments(repaymentsPrice);
            isSummaryEmpty = false;
        }

        return (isSummaryEmpty ? null : summary);
    }

    /**
     * Parses signature dates of the project.
     *
     * @param context node which includes signature dates data
     *
     * @return list of signature dates
     */
    private List<String> parseSignatureDates(final Element context) {
        final List<Element> signatureDateNodes = JsoupUtils.getLabeledValueNodes(context,
                "(?i)contract signature date");

        if (signatureDateNodes == null) {
            return null;
        }

        final List<String> signatureDates = new ArrayList<>();
        //only first signature date is labeled
        final Element firstSignatureDateNode = signatureDateNodes.get(0);
        signatureDates.add(firstSignatureDateNode.text());

        //every next signature date is within the following not labeled row
        Element nextRow = firstSignatureDateNode.parent().nextElementSibling();
        while (nextRow != null && !nextRow.child(0).hasText()) {
            signatureDates.add(nextRow.child(1).text());
            nextRow = nextRow.nextElementSibling();
        }

        return signatureDates;

    }

    /**
     * Parses project type. The first found wins.
     *
     * @param contexts contexts where project type can be found
     *
     * @return project type
     */
    private String parseProjectType(final List<Element> contexts) {
        for (Element table : contexts) {
            final String projectType = JsoupUtils.getFirstValueByLabel(table, "(?i)project type");
            if (projectType != null) {
                return projectType;
            }
        }

        return null;
    }

    /**
     * Parses project operations.
     *
     * @param context node which includes operations data
     *
     * @return list of operations
     */
    private List<DFIDParsedProjectOperation> parseOperations(final Element context) {
        final List<String> operationNumbers = JsoupUtils.getAllValuesByLabel(context, "(?i)operation number");

        if (operationNumbers == null) {
            return null;
        }

        return operationNumbers.stream()
                .map((number) -> new DFIDParsedProjectOperation().setOperationNumber(number))
                .collect(Collectors.toList());
    }

    /**
     * @param context  node which includes price data
     * @param label    label of the price field
     * @param currency currency of the price
     *
     * @return parsed price
     */
    private ParsedPrice parsePrice(final Element context, final String label, final String currency) {
        String netAmount = JsoupUtils.getFirstValueByLabel(context, label);
        if (netAmount == null) {
            return null;
        }

        netAmount = removeCurrencyFromAmount(netAmount);

        return new ParsedPrice().setNetAmount(netAmount).setCurrency(currency);
    }

    /**
     * Parse currency.
     *
     * @param projectInformation element to parse from
     *
     * @return String or null
     */
    private String parseCurrency(final Element projectInformation) {
        String currency = JsoupUtils.getFirstValueByLabel(projectInformation, "(?i)reporting currency");
        return currency == null || currency.length() < 3
                ? null
                : currency.substring(0, 3);
    }

    /**
     * Removes currency from the string where currency and amount value is and returns just the amount value.
     *
     * @param currencyAndAmount string where currency and amount value is
     *
     * @return String or null
     */
    private String removeCurrencyFromAmount(final String currencyAndAmount) {
        // the amount is value without spaces or it is something like "USD 3,417,240"
        if (currencyAndAmount.contains(" ")) {
            assert currencyAndAmount.indexOf(" ") == 3
                    : "The value of amount should start with three letters of currency and space";
            return currencyAndAmount.substring(currencyAndAmount.indexOf(" ") + 1);
        } else {
            return currencyAndAmount;
        }
    }

    /**
     * Parses IDB financing price. IDB financing is shown in US$ millions.
     *
     * @param context element that includes IDB financing data
     *
     * @return parsed price
     */
    private ParsedPrice praseIDBFinancing(final Element context) {
        ParsedPrice price = parsePrice(context, "(?i)idb financing", "USD");
        if (price == null) {
            return null;
        }

        String multipliedAmount = ParserUtils.multiply(price.getNetAmount(), IDB_FINANCING_MULTIPLIER, NUMBER_FORMAT);

        if (!multipliedAmount.equals(price.getNetAmount())) {
            price.setNetAmount(multipliedAmount);
        } else {
            return price;
        }

        return price;
    }

    @Override
    public final String getVersion() {
        return VERSION;
    }
}
