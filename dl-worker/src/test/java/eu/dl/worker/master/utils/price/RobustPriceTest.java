package eu.dl.worker.master.utils.price;

import eu.dl.dataaccess.dto.generic.Price;
import eu.dl.dataaccess.dto.master.MasterBid;
import eu.dl.dataaccess.dto.master.MasterTender;
import eu.dl.dataaccess.dto.master.MasterTenderLot;
import eu.dl.dataaccess.dto.matched.MatchedBid;
import eu.dl.dataaccess.dto.matched.MatchedTender;
import eu.dl.dataaccess.dto.matched.MatchedTenderLot;
import eu.dl.dataaccess.dto.matched.StructuredBidId;
import eu.dl.dataaccess.dto.matched.StructuredLotId;
import eu.dl.worker.master.plugin.specific.RobustPricePlugin;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Marek Mikes
 */
public final class RobustPriceTest {
    private static final String TENDER1_ID = "tender1ID";
    private static final String TENDER2_ID = "tender2ID";
    private static final String TENDER3_ID = "tender3ID";
    private static final String TENDER4_ID = "tender4ID";
    private static final String TENDER5_ID = "tender5ID";
    private static final double TOLERANCE = Math.pow(10, -RobustPricePlugin.DIVISION_PRECISION);

    // correctly fulfilled tenders:
    private final MatchedTender tender1 = new MatchedTender()
            .setEstimatedPrice(new Price()
                    .setVat(new BigDecimal(15))
                    .setNetAmount(new BigDecimal(1500)))
            .addLot(new MatchedTenderLot()
                    .setLotId("tender1Lot1ID")
                    .setEstimatedPrice(new Price()
                            .setVat(new BigDecimal(15))
                            .setNetAmount(new BigDecimal(600))))
            .addLot(new MatchedTenderLot()
                    .setLotId("tender1Lot2ID")
                    .setEstimatedPrice(new Price()
                            .setVat(new BigDecimal(15))
                            .setNetAmount(new BigDecimal(900))))
            .setFinalPrice(new Price()
                    .setNetAmount(new BigDecimal(1500)));
    private final MatchedTender tender2 = new MatchedTender()
            .setEstimatedPrice(new Price()
                    .setVat(new BigDecimal(21))
                    .setNetAmount(new BigDecimal(1500)))
            .addLot(new MatchedTenderLot()
                    .setLotId("tender2Lot1ID")
                    .setEstimatedPrice(new Price()
                            .setVat(new BigDecimal(15))
                            .setNetAmount(new BigDecimal(700)))
                    .setBids(Arrays.asList(new MatchedBid()
                            .setBidId("tender2Lot1Bid1ID")
                            .setIsWinning(true)
                            .setPrice(new Price()
                                    .setNetAmount(new BigDecimal(650))))))
            .setFinalPrice(new Price()
                    .setNetAmount(new BigDecimal(1500)));
    private final MasterTender masterTender1 = new MasterTender()
            .setEstimatedPrice(new Price()
                    .setNetAmount(new BigDecimal(1500)))
            .addLot(new MasterTenderLot()
                    .setEstimatedPrice(new Price()
                            .setNetAmount(new BigDecimal(650)))
                    .setBids(Arrays.asList(new MasterBid()
                            .setIsWinning(true)
                            .setPrice(new Price()
                                    .setNetAmount(new BigDecimal(650)))
                            .setSourceBidIds(Arrays.asList((StructuredBidId) (new StructuredBidId()
                                    .setBidId(tender2.getLots().get(0).getBids().get(0).getBidId())
                                    .setTenderId(TENDER2_ID)
                                    .setLotId(tender2.getLots().get(0).getLotId()))))))
                    .setSourceLotIds(Arrays.asList(
                            new StructuredLotId()
                                    .setTenderId(TENDER1_ID)
                                    .setLotId(tender1.getLots().get(0).getLotId()),
                            new StructuredLotId()
                                    .setTenderId(TENDER2_ID)
                                    .setLotId(tender2.getLots().get(0).getLotId()))))
            .addLot(new MasterTenderLot()
                    .setEstimatedPrice(new Price()
                            .setNetAmount(new BigDecimal(900)))
                    .setSourceLotIds(Arrays.asList(
                            new StructuredLotId()
                                    .setTenderId(TENDER1_ID)
                                    .setLotId(tender1.getLots().get(1).getLotId()))));

    // incorrectly fulfilled tenders:
    private final MatchedTender tender3 = new MatchedTender();
    private final MatchedTender tender4 = new MatchedTender()
            .setEstimatedPrice(new Price()
                    .setVat(new BigDecimal(0))
                    .setNetAmount(new BigDecimal(200)))
            .addLot(new MatchedTenderLot()
                    .setLotId("tender4Lot1ID")
                    .setEstimatedPrice(new Price()
                            .setVat(new BigDecimal(21))
                            .setNetAmount(new BigDecimal(0))))
            .addLot(new MatchedTenderLot()
                    .setLotId("tender4Lot2ID")
                    .setEstimatedPrice(new Price()
                            .setVat(new BigDecimal(0))
                            .setNetAmount(new BigDecimal(0))))
            .setFinalPrice(new Price()
                    .setNetAmount(new BigDecimal(0)));
    private final MatchedTender tender5 = new MatchedTender()
            .addLot(new MatchedTenderLot()
                    .setLotId("tender5Lot1ID")
                    .setEstimatedPrice(new Price()
                            .setVat(new BigDecimal(21))
                            .setNetAmount(new BigDecimal(50))))
            .addLot(new MatchedTenderLot()
                    .setLotId("tender5Lot2ID")
                    .setEstimatedPrice(new Price()
                            .setVat(new BigDecimal(0))
                            .setNetAmount(new BigDecimal(0))))
            .setFinalPrice(new Price()
                    .setNetAmount(new BigDecimal(0)));
    private final MasterTender masterTender2 = new MasterTender()
            .setEstimatedPrice(new Price()
                    .setNetAmount(new BigDecimal(0)))
            .addLot(new MasterTenderLot()
                    .setEstimatedPrice(new Price()
                            .setNetAmount(new BigDecimal(0)))
                    .setSourceLotIds(Arrays.asList(
                            new StructuredLotId()
                                    .setTenderId(TENDER4_ID)
                                    .setLotId(tender4.getLots().get(0).getLotId()),
                            new StructuredLotId()
                                    .setTenderId(TENDER5_ID)
                                    .setLotId(tender5.getLots().get(0).getLotId()))))
            .addLot(new MasterTenderLot()
                    .setEstimatedPrice(new Price()
                            .setNetAmount(new BigDecimal(0)))
                    .setSourceLotIds(Arrays.asList(
                            new StructuredLotId()
                                    .setTenderId(TENDER4_ID)
                                    .setLotId(tender4.getLots().get(1).getLotId()),
                            new StructuredLotId()
                                    .setTenderId(TENDER5_ID)
                                    .setLotId(tender5.getLots().get(1).getLotId()))));

    /**
     * Default constructor to set tender IDs. The setter does not support fluent interface, so we call it here.
     */
    public RobustPriceTest() {
        tender1.setId(TENDER1_ID);
        tender2.setId(TENDER2_ID);
        tender3.setId(TENDER3_ID);
        tender4.setId(TENDER4_ID);
        tender5.setId(TENDER5_ID);
    }

    /**
     * Test of correct robust price when tenders are correctly fulfilled.
     */
    @Test
    public void correctPriceTest() {
        RobustPricePlugin robustPricePlugin = new RobustPricePlugin();
        List<MatchedTender> tenders = Arrays.asList(tender1, tender2);
        robustPricePlugin.master(tenders, masterTender1, tenders);

        // 1. lot:
        // 600 700 750 750 -> ME = 700
        // 600 700   750 750 -> winner is 600
        List<MasterTenderLot> lots = masterTender1.getLots();
        assertTrue(lots.get(0).getRobustEstimatedPrice().getNetAmount().equals(new BigDecimal(600)));
        assertTrue(lots.get(0).getRobustEstimatedPrice().getVat().equals(new BigDecimal(15)));
        // M = (600 + 700 + 750 + 750) / 4 = 700
        // min =  600/700=0,857142857   700/600=1,166666667
        assertTrue(
                Math.abs(lots.get(0).getRobustEstimatedPrice().getReliability() - ((4.0 - 0.5) * 0.857142857) / 4.0)
                        < TOLERANCE);

        // 2. lot:
        // 750 750 900 -> ME = 750
        // 900   750 750 -> winner is 900
        assertTrue(lots.get(1).getRobustEstimatedPrice().getNetAmount().equals(new BigDecimal(900)));
        assertTrue(lots.get(1).getRobustEstimatedPrice().getVat().equals(new BigDecimal(15)));
        // M = (750 + 750 + 900) / 3 = 800
        // min =  800/900=0,888888889   900/800=1,125
        assertTrue(
                Math.abs(lots.get(1).getRobustEstimatedPrice().getReliability() - ((3.0 - 0.5) * 0.888888889) / 3.0)
                        < TOLERANCE);

        // The bid:
        // 650 700 750 750 750 750 -> ME 750
        // 650   750 750   700   750 750 -> winner is 650
        MasterBid bid = masterTender1.getLots().get(0).getBids().get(0);
        assertTrue(bid.getRobustPrice().getNetAmount().equals(new BigDecimal(650)));
        assertTrue(bid.getRobustPrice().getVat().equals(new BigDecimal(15)));
        // M = (650 + 700 + 750 + 750 + 750 + 750) / 6 = 725
        // min =  650/725=0,896551724   725/650=1,115384615
        assertTrue(Math.abs(bid.getRobustPrice().getReliability() - ((6.0 - 0.5) * 0.896551724) / 6.0) < TOLERANCE);
    }

    /**
     * Test of correct robust price when tenders are incorrectly fulfilled.
     */
    @Test
    public void incorrectPriceTest() {
        RobustPricePlugin robustPricePlugin = new RobustPricePlugin();
        List<MatchedTender> tenders = Arrays.asList(tender3, tender4, tender5);
        robustPricePlugin.master(tenders, masterTender2, tenders);

        List<MasterTenderLot> lots = masterTender2.getLots();

        // 1. lot:
        assertTrue(lots.get(0).getRobustEstimatedPrice().getNetAmount().equals(new BigDecimal(50)));
        assertTrue(lots.get(0).getRobustEstimatedPrice().getVat().equals(new BigDecimal(21)));
        // M = (0 + 50 + 100) / 3 = 50
        // min =  50/50=1   50/50=1
        assertTrue(Math.abs(lots.get(0).getRobustEstimatedPrice().getReliability() - ((3.0 - 0.5) * 1.0) / 3.0)
                < TOLERANCE);

        // 2. lot:
        assertTrue(lots.get(1).getRobustEstimatedPrice().getNetAmount().equals(new BigDecimal(0)));
        assertTrue(lots.get(1).getRobustEstimatedPrice().getVat().equals(new BigDecimal(0)));
        // N = 3
        // n = 1
        assertTrue(Math.abs(lots.get(1).getRobustEstimatedPrice().getReliability() - (3.0 - 0.5 - 1.0) / 3.0)
                < TOLERANCE);
    }
}
