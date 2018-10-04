package eu.dl.worker.master.plugin.specific;

import eu.dl.dataaccess.dto.generic.Price;
import eu.dl.dataaccess.dto.master.MasterBid;
import eu.dl.dataaccess.dto.master.MasterTender;
import eu.dl.dataaccess.dto.master.MasterTenderLot;
import eu.dl.dataaccess.dto.matched.MatchedBid;
import eu.dl.dataaccess.dto.matched.MatchedTender;
import eu.dl.dataaccess.dto.matched.MatchedTenderLot;
import eu.dl.dataaccess.dto.matched.StructuredBidId;
import eu.dl.dataaccess.dto.matched.StructuredLotId;
import eu.dl.dataaccess.dto.utils.DTOUtils;
import eu.dl.worker.master.plugin.MasterPlugin;
import eu.dl.worker.utils.BasePlugin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This plugin sets robust prices and works with price without VAT.
 */
public class RobustPricePlugin extends BasePlugin implements MasterPlugin<MatchedTender, MasterTender, MatchedTender> {

    /**
     * Precision used during division od prices.
     */
    public static final int DIVISION_PRECISION = 6;
    private static final RoundingMode DIVISION_ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * Struct which contains price value and its whole price object. The price value is not always the same as value in
     * the price object because sometimes we handle tender price divided by number of lots.
     */
    private class PriceValueAndObject {
        private final BigDecimal priceValue;
        private final Price priceObject;

        /**
         * @param priceValue
         *         priceValue
         * @param priceObject
         *         priceObject
         */
        PriceValueAndObject(final BigDecimal priceValue, final Price priceObject) {
            this.priceValue = priceValue;
            this.priceObject = priceObject;
        }

        /**
         * @return priceValue
         */
        BigDecimal getPriceValue() {
            return priceValue;
        }

        /**
         * @return price object where the price value originally was
         */
        Price getPriceObject() {
            return priceObject;
        }
    }

    /**
     * This comparator helps to order items in the price value order.
     */
    final class PriceComparator implements Comparator<PriceValueAndObject> {
        @Override
        public int compare(final PriceValueAndObject o1, final PriceValueAndObject o2) {
            assert o1.getPriceValue() != null && o2.getPriceValue() != null;
            return o1.getPriceValue().compareTo(o2.getPriceValue());
        }
    }

    /**
     * This comparator helps to order items in one group. Group contains prices of one type (e.g. bid prices).
     * We order it using deviation min(P/ME,ME/P) where P is given price.
     */
    final class GroupPriceComparator implements Comparator<PriceValueAndObject> {
        private BigDecimal median;

        /**
         * @param median
         *         median
         */
        GroupPriceComparator(final BigDecimal median) {
            assert median.compareTo(BigDecimal.ZERO) == 1 : "Median has to be positive.";
            this.median = median;
        }

        @Override
        public int compare(final PriceValueAndObject o1, final PriceValueAndObject o2) {
            if (o1.getPriceValue() == null && o2.getPriceValue() == null) {
                return 0;
            }

            if (o1.getPriceValue() == null && o2.getPriceValue() != null) {
                return -1;
            }

            if (o1.getPriceValue() != null && o2.getPriceValue() == null) {
                return 1;
            }

            BigDecimal min1 = o1.getPriceValue()
                    .divide(median, DIVISION_PRECISION, DIVISION_ROUNDING_MODE)
                    .min(median.divide(o1.getPriceValue(), DIVISION_PRECISION, DIVISION_ROUNDING_MODE));
            BigDecimal min2 = o2.getPriceValue()
                    .divide(median, DIVISION_PRECISION, DIVISION_ROUNDING_MODE)
                    .min(median.divide(o2.getPriceValue(), DIVISION_PRECISION, DIVISION_ROUNDING_MODE));
            return min1.compareTo(min2);
        }
    }

    @Override
    public final MasterTender master(final List<MatchedTender> items, final MasterTender finalItem,
            final List<MatchedTender> context) {
        if (finalItem.getLots() == null) {
            return finalItem;
        }

        for (MasterTenderLot masterTenderLot : finalItem.getLots()) {
            masterTenderLot.setRobustEstimatedPrice(getLotRobustEstimatedPrice(masterTenderLot, items, finalItem));
            if (masterTenderLot.getBids() == null) {
                continue;
            }
            for (MasterBid masterBid : masterTenderLot.getBids()) {
                if (Objects.equals(masterBid.getIsWinning(), Boolean.TRUE)) {
                    masterBid.setRobustPrice(getBidRobustPrice(masterBid, items, finalItem));
                    break;
                }
            }
        }

        return finalItem;
    }

    /**
     * Method creates robust estimated price of some master lot which is passed as first parameter.
     *
     * @param masterTenderLot
     *         master tender lot of final tender. We want it to get all matched tender lots from which it is mastered
     * @param items
     *         matched tenders from which final master tender is created
     * @param finalItem
     *         final master tender
     *
     * @return robust estimated price of the lot
     */
    private Price getLotRobustEstimatedPrice(final MasterTenderLot masterTenderLot, final List<MatchedTender> items,
            final MasterTender finalItem) {
        List<StructuredLotId> sourceLotIds = masterTenderLot.getSourceLotIds();
        List<MatchedTenderLot> sourceLots = DTOUtils.getSourceLotsByStructuredIds(items, sourceLotIds);

        // get group of all lot estimated prices
        List<PriceValueAndObject> lotEstimatedPrices = sourceLots.stream()
                .filter(lot -> lot.getEstimatedPrice() != null)
                .filter(lot -> lot.getEstimatedPrice().getNetAmount() != null)
                .map(lot -> new PriceValueAndObject(lot.getEstimatedPrice().getNetAmount(), lot.getEstimatedPrice()))
                .collect(Collectors.toList());

        // get group of all tender estimated prices divided by number of lots
        BigDecimal numberOfLots = new BigDecimal(finalItem.getLots().size());
        List<PriceValueAndObject> tenderEstimatedPrices = items.stream()
                .filter(tender -> tender.getEstimatedPrice() != null)
                .filter(tender -> tender.getEstimatedPrice().getNetAmount() != null)
                .map(tender -> new PriceValueAndObject(tender.getEstimatedPrice()
                        .getNetAmount()
                        .divide(numberOfLots, DIVISION_PRECISION, DIVISION_ROUNDING_MODE), tender.getEstimatedPrice()))
                .collect(Collectors.toList());

        return getRobustPrice(Arrays.asList(lotEstimatedPrices, tenderEstimatedPrices), finalItem);
    }

    /**
     * Method creates robust price of some master bid which is passed as first parameter.
     *
     * @param masterBid
     *         master bid of final tender lot. We want it to get all matched bids and lots from which it is mastered
     * @param items
     *         matched tenders from which final master tender is created
     * @param finalItem
     *         final master tender
     *
     * @return robust price of the lot
     */
    private Price getBidRobustPrice(final MasterBid masterBid, final List<MatchedTender> items,
            final MasterTender finalItem) {
        List<StructuredBidId> sourceBidIds = masterBid.getSourceBidIds();
        List<MatchedTenderLot> sourceLots = DTOUtils.getSourceLotsByStructuredBidIds(items, sourceBidIds);
        List<MatchedBid> sourceBids = DTOUtils.getSourceBidsByStructuredIds(items, sourceBidIds);

        // get group of all bid prices
        List<PriceValueAndObject> bidPrices = sourceBids.stream()
                .filter(bid -> bid.getPrice() != null)
                .filter(bid -> bid.getPrice().getNetAmount() != null)
                .map(bid -> new PriceValueAndObject(bid.getPrice().getNetAmount(), bid.getPrice()))
                .collect(Collectors.toList());

        // get group of all tender final prices divided by number of lots
        BigDecimal numberOfLots = new BigDecimal(finalItem.getLots().size());
        List<PriceValueAndObject> tenderFinalPrices = items.stream()
                .filter(tender -> tender.getFinalPrice() != null)
                .filter(tender -> tender.getFinalPrice().getNetAmount() != null)
                .map(tender -> new PriceValueAndObject(tender.getFinalPrice()
                        .getNetAmount()
                        .divide(numberOfLots, DIVISION_PRECISION, DIVISION_ROUNDING_MODE), tender.getFinalPrice()))
                .collect(Collectors.toList());

        // get group of all lot estimated prices
        List<PriceValueAndObject> lotEstimatedPrices = sourceLots.stream()
                .filter(lot -> lot.getEstimatedPrice() != null)
                .filter(lot -> lot.getEstimatedPrice().getNetAmount() != null)
                .map(lot -> new PriceValueAndObject(lot.getEstimatedPrice().getNetAmount(), lot.getEstimatedPrice()))
                .collect(Collectors.toList());

        // get group of all tender estimated prices divided by number of lots
        List<PriceValueAndObject> tenderEstimatedPrices = items.stream()
                .filter(tender -> tender.getEstimatedPrice() != null)
                .filter(tender -> tender.getEstimatedPrice().getNetAmount() != null)
                .map(tender -> new PriceValueAndObject(tender.getEstimatedPrice()
                        .getNetAmount()
                        .divide(numberOfLots, DIVISION_PRECISION, DIVISION_ROUNDING_MODE), tender.getEstimatedPrice()))
                .collect(Collectors.toList());

        return getRobustPrice(Arrays.asList(bidPrices, tenderFinalPrices, lotEstimatedPrices, tenderEstimatedPrices),
                finalItem);
    }

    /**
     * Method creates robust estimated price of some master lot or robust price of some master bid. This part is the
     * same for both prices.
     *
     * @param priceGroups
     *         price groups. Order of groups is clear and described on wiki. Each group is unordered.
     * @param finalItem
     *         final master tender
     *
     * @return robust price of lot/bid
     */
    private Price getRobustPrice(final List<List<PriceValueAndObject>> priceGroups, final MasterTender finalItem) {
        List<PriceValueAndObject> allPrices = new ArrayList<>();
        for (List<PriceValueAndObject> priceGroup : priceGroups) {
            allPrices.addAll(priceGroup);
        }

        if (allPrices.size() < 1) {
            return null;
        }
        
        // sort prices in the price value order.
        allPrices = allPrices.stream().sorted(new PriceComparator()).collect(Collectors.toList());

        // get median
        int middle = allPrices.size() / 2;
        PriceValueAndObject median = allPrices.size() % 2 == 0 ? allPrices.get(middle - 1) : allPrices.get(middle);

        // reverse order of groups if tender/lot is awarded as superior framework agreement
        if (finalItem.getIsFrameworkAgreement() != null && finalItem.getIsFrameworkAgreement()) {
            Collections.reverse(priceGroups);
        }

        PriceValueAndObject robustPrice = getRobustPriceObject(priceGroups, median);

        if (robustPrice == null) {
            return null;
        } else {
            // create copy because we do not want to modify the chosen priceValue!
            Price robustPriceCopy = new Price()
                    .setNetAmount(robustPrice.getPriceValue())                 
                    .setCurrency(robustPrice.getPriceObject().getCurrency())
                    .setVat(getVat(allPrices))
                    .setReliability(getReliability(allPrices, robustPrice));
            return robustPriceCopy;
        }
    }

    /**
     * Method gets desired robust price saved in our struct which contains robust price value and the price object.
     * It tries group by group, sorts it and returns the price as the first price P that meets min(P/ME;ME/P)>0,15
     * condition.
     *
     * @param priceGroups
     *         price groups. Order of groups is clear and described on wiki. Each group is unordered.
     * @param median
     *         median
     *
     * @return robust price object of lot/bid
     */
    private PriceValueAndObject getRobustPriceObject(final List<List<PriceValueAndObject>> priceGroups,
            final PriceValueAndObject median) {
        BigDecimal medianValue = median.getPriceValue();

        if (medianValue.compareTo(BigDecimal.ZERO) == 0) {
            return median;
        }

        for (List<PriceValueAndObject> priceGroup : priceGroups) {
            // sort the group
            priceGroup.stream().sorted(new GroupPriceComparator(medianValue));

            // search the first price P that meets min(P/ME;ME/P)>0,15 condition
            for (PriceValueAndObject price : priceGroup) {
                BigDecimal priceValue = price.getPriceValue();
                if (priceValue.compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }
                if (priceValue.divide(medianValue, DIVISION_PRECISION, DIVISION_ROUNDING_MODE)
                        .min(medianValue.divide(priceValue, DIVISION_PRECISION, DIVISION_ROUNDING_MODE))
                        .compareTo(new BigDecimal(0.15)) == 1) {
                    return price;
                }
            }
        }

        return null;
    }

    /**
     * Gets VAT of robust price as mode of all VATs.
     *
     * @param allPrices
     *         all prices
     *
     * @return mode of VATs from prices
     */
    private BigDecimal getVat(final List<PriceValueAndObject> allPrices) {
        // We are getting mode
        HashMap<BigDecimal, Integer> vatCountMap = new HashMap<>();
        Integer maxCount = null;
        BigDecimal modeVat = null;
        for (PriceValueAndObject price : allPrices) {
            BigDecimal vat = price.getPriceObject().getVat();
            if (vat == null) {
                continue;
            }
            if (vatCountMap.get(vat) != null) {
                int count = vatCountMap.get(vat);
                count++;
                vatCountMap.put(vat, count);
                if (count > maxCount) {
                    maxCount = count;
                    modeVat = vat;
                }
            } else {
                if (vatCountMap.isEmpty()) {
                    maxCount = 1;
                    modeVat = vat;
                }
                vatCountMap.put(vat, 1);
            }
        }
        return modeVat;
    }

    /**
     * Calculates reliability of robust price.
     *
     * @param allPrices
     *         all prices
     * @param robustPrice
     *         robust price
     *
     * @return reliability
     */
    private float getReliability(final List<PriceValueAndObject> allPrices, final PriceValueAndObject robustPrice) {
        // reliability = ( ( N - 0.5 ) * min ( P/M ; M/P ) ) / N
        // where N is number of prices used in computation and M is their plain average
        BigDecimal n = new BigDecimal(allPrices.size());
        BigDecimal m = new BigDecimal(allPrices.stream()
                .map(price -> price.getPriceValue())
                .mapToInt(BigDecimal::intValue)
                .sum() / n.doubleValue());
        BigDecimal p = robustPrice.getPriceValue();

        if (p.compareTo(BigDecimal.ZERO) == 0 || m.compareTo(BigDecimal.ZERO) == 0) {
            //price.reliability = ( ( N - 0.5 - n ) / N
            Integer nonZeroPrices = 0;
            
            // count non-zero prices
            for (PriceValueAndObject price : allPrices) {
                if (price.getPriceValue().compareTo(BigDecimal.ZERO) != 0) {
                    nonZeroPrices++;
                }
            }
            
            return n.subtract(new BigDecimal(0.5))
                    .subtract(new BigDecimal(nonZeroPrices))
                    .divide(n, DIVISION_PRECISION, DIVISION_ROUNDING_MODE)
                    .floatValue();
        } else {
            return n.subtract(new BigDecimal(0.5))
                    .multiply(p.divide(m, DIVISION_PRECISION, DIVISION_ROUNDING_MODE)
                            .min(m.divide(p, DIVISION_PRECISION, DIVISION_ROUNDING_MODE)))
                    .divide(n, DIVISION_PRECISION, DIVISION_ROUNDING_MODE)
                    .floatValue();
        }
    }
}