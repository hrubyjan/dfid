package eu.dfid.worker.idb.parsed;

import eu.dfid.dataaccess.dto.codetables.PublicationSources;
import eu.dfid.dataaccess.dto.parsed.DFIDParsedProjectOperation;
import eu.dfid.dataaccess.dto.parsed.DFIDParsedTender;
import eu.dfid.dataaccess.dto.parsed.ParsedProject;
import eu.dfid.worker.parser.BaseDFIDTenderParser;
import eu.dl.dataaccess.dto.parsed.ParsedAddress;
import eu.dl.dataaccess.dto.parsed.ParsedBid;
import eu.dl.dataaccess.dto.parsed.ParsedBody;
import eu.dl.dataaccess.dto.parsed.ParsedPrice;
import eu.dl.dataaccess.dto.parsed.ParsedPublication;
import eu.dl.dataaccess.dto.parsed.ParsedTender;
import eu.dl.dataaccess.dto.parsed.ParsedTenderLot;
import eu.dl.dataaccess.dto.raw.RawData;
import eu.dl.worker.utils.jsoup.JsoupUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Awarded contracts (consulting services) parser for Inter-American Development Bank (IDB).
 *
 * @author Marek Mikes
 */
public class IDBAwardedContractsConsultingServicesParser extends BaseDFIDTenderParser {
    private static final String VERSION = "2";

    @Override
    public final List<ParsedTender> parse(final RawData rawTender) {
        List<ParsedTender> parsedTenders = new ArrayList<>();

        final Document resultPage = Jsoup.parse(rawTender.getSourceData());

        final Elements rows = JsoupUtils.select("table[id=searchResults] > tbody > tr", resultPage);
        assert rows.size() % 3 == 0 : "One record is extended through 3 rows so number of rows should be divided by 3";
        for (int i = 0; i < rows.size(); i += 3) {
            DFIDParsedTender parsedTender = new DFIDParsedTender();

            final Element titleRow = rows.get(i);
            final Element contentRow = rows.get(i + 2);

            // parse DFID attributes
            parsedTender
                    .setProcurementType(parseTenderProcurementType(titleRow))
                    .setProject(new ParsedProject()
                            .addOperation(new DFIDParsedProjectOperation()
                                    .setOperationNumber(parseProjectOperationNumber(contentRow))))
                    .addMajorSector(parseTenderMajorSector(contentRow));

            // parse common attributes
            parsedTender
                    .setSupplyType("CONSULTING SERVICES")
                    .addBuyer(new ParsedBody()
                            .setAddress(new ParsedAddress()
                                    .setCountry(parseBuyerCountry(titleRow))))
                    .addPublication(new ParsedPublication()
                            .setSourceId(parsePublicationSourceId(titleRow))
                            .setIsIncluded(true)
                            .setSource(PublicationSources.IDB)
                            .setHumanReadableUrl(rawTender.getSourceUrl().toString()))
                    .setContractSignatureDate(parseContractSignatureDate(titleRow))
                    .addLot(new ParsedTenderLot()
                            .addBid(new ParsedBid()
                                    .setIsWinning(Boolean.TRUE.toString())
                                    .addBidder(new ParsedBody()
                                            .setName(parseBidderName(contentRow))
                                            .setAddress(parseBidderAddress(contentRow)))))
                    .setFinalPrice(new ParsedPrice()
                            .setCurrency("USD")
                            .setNetAmount(parseTenderFinalNetAmount(contentRow)))
                    .setEstimatedCompletionDate(parseTenderEstimatedCompletionDate(contentRow));

            parsedTenders.add(parsedTender);
        }

        return parsedTenders;
    }

    @Override
    public final String getVersion() {
        return VERSION;
    }

    /**
     * Parse buyer country value from row representing title of record.
     *
     * @param titleRow
     *         row representing title of record to be parsed
     *
     * @return String or Null
     */
    private static String parseBuyerCountry(final Element titleRow) {
        return JsoupUtils.selectText("td:nth-child(2)", titleRow);
    }

    /**
     * Parse tender procurement type value from row representing title of record.
     *
     * @param titleRow
     *         row representing title of record to be parsed
     *
     * @return String or Null
     */
    private static String parseTenderProcurementType(final Element titleRow) {
        return JsoupUtils.selectText("td:nth-child(3)", titleRow);
    }

    /**
     * Parse publication source ID value from row representing title of record.
     *
     * @param titleRow
     *         row representing title of record to be parsed
     *
     * @return String or Null
     */
    private static String parsePublicationSourceId(final Element titleRow) {
        return JsoupUtils.selectText("td:nth-child(4)", titleRow);
    }

    /**
     * Parse contract signature date value from row representing title of record.
     *
     * @param titleRow
     *         row representing title of record to be parsed
     *
     * @return String or Null
     */
    private static String parseContractSignatureDate(final Element titleRow) {
        return JsoupUtils.selectText("td:nth-child(5)", titleRow);
    }

    /**
     * Parse project operation number value from row representing content of record.
     *
     * @param contentRow
     *         row representing content of record to be parsed
     *
     * @return String or Null
     */
    private static String parseProjectOperationNumber(final Element contentRow) {
        return JsoupUtils.selectText("table > tbody > tr:nth-child(1) > td:nth-child(2)", contentRow);
    }

    /**
     * Parse bidder name value from row representing content of record.
     *
     * @param contentRow
     *         row representing content of record to be parsed
     *
     * @return String or Null
     */
    private static String parseBidderName(final Element contentRow) {
        return JsoupUtils.selectText("table > tbody > tr:nth-child(1) > td:nth-child(4)", contentRow);
    }

    /**
     * Parse tender final net amount from row representing content of record.
     *
     * @param contentRow
     *         row representing content of record to be parsed
     *
     * @return String or Null
     */
    private static String parseTenderFinalNetAmount(final Element contentRow) {
        return JsoupUtils.selectText("table > tbody > tr:nth-child(2) > td:nth-child(2)", contentRow);
    }

    /**
     * Parse bidder address value from row representing content of record.
     *
     * @param contentRow
     *         row representing content of record to be parsed
     *
     * @return Parsed address or Null
     */
    private static ParsedAddress parseBidderAddress(final Element contentRow) {
        final String countryAndCityString =
                JsoupUtils.selectText("table > tbody > tr:nth-child(2) > td:nth-child(4)", contentRow);

        if (countryAndCityString == null) {
            return null;
        }

        int commaIndex = countryAndCityString.indexOf(",");
        assert commaIndex != -1
                : "Comma should be filled always. Even if there is only country (comma is last character)";
        return new ParsedAddress()
                .setCountry(countryAndCityString.substring(0, commaIndex).trim())
                .setCity(countryAndCityString.length() - 1 == commaIndex
                        ? null
                        : countryAndCityString.substring(commaIndex + 1).trim());
    }

    /**
     * Parse tender estimated completion date value from row representing content of record.
     *
     * @param contentRow
     *         row representing content of record to be parsed
     *
     * @return String or Null
     */
    private static String parseTenderEstimatedCompletionDate(final Element contentRow) {
        return JsoupUtils.selectText("table > tbody > tr:nth-child(3) > td:nth-child(4)", contentRow);
    }

    /**
     * Parse tender major sector value from row representing content of record.
     *
     * @param contentRow
     *         row representing content of record to be parsed
     *
     * @return String or Null
     */
    private static String parseTenderMajorSector(final Element contentRow) {
        return JsoupUtils.selectText("table > tbody > tr:nth-child(4) > td:nth-child(2)", contentRow);
    }

    @Override
    protected final String countryOfOrigin(final ParsedTender parsed, final RawData raw){
        return null;
    }
}
