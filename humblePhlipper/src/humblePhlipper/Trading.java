// Trading.java

package humblePhlipper;

import humblePhlipper.Resources.SavedData.Trade;
import org.dreambot.api.Client;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.grandexchange.GrandExchangeItem;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Trading {
    private final ResourceManager rm;
    private static final Integer SLEEP = 1000;
    private static final Set<Integer> restrictedIdSet = new HashSet<>(Arrays.asList(1521, 1519, 1515, 317, 315, 321, 319, 377, 379, 434, 1761, 436, 438, 440, 442, 444, 453, 447, 449, 451, 1739, 229, 227, 1937, 313, 314, 221, 245, 556, 555, 557, 554, 558, 562));
    public Trading(ResourceManager rm) {
        this.rm = rm;
    }
    public static Set<Integer> getRestrictedIdSet() {
        return restrictedIdSet;
    }
    public void Select() {
        for (Integer ID : rm.items.keySet()) {
            humblePhlipper.Resources.Items.Item item = rm.items.get(ID);
            if (item.getAsk() == null || item.getBid() == null) {
                continue;
            }
            if (item.getTargetVol() == Integer.MAX_VALUE) {
                continue;
            }
            if (item.getId() == 13190) {
                continue;
            }
            if (item.getOneHour().getLowPriceVolume() + item.getOneHour().getHighPriceVolume() < rm.config.getMinVol()) {
                continue;
            }
            if ((float) item.getOneHour().getLowPriceVolume() / item.getOneHour().getHighPriceVolume() > rm.config.getMaxBidAskVolRatio()) {
                continue;
            }
            if (0.99 * item.getAsk() - item.getBid() < rm.config.getMinMargin()) {
                continue;
            }
            if (item.getBid() > rm.config.getMaxBidPrice()) {
                continue;
            }
            if (rm.config.getTradeRestricted() && restrictedIdSet.contains(item.getId())) {
                continue;
            }
            if (!rm.config.getMembers() && item.getMapping().getMembers()) {
                continue;
            }
            rm.config.incrementSelections(item.getId());
        }
        Order();
        rm.config.setSelections(rm.config.getSelections().stream().limit(rm.config.getNumToSelect()).collect(Collectors.toCollection(LinkedHashSet::new))); // Keep first N selections
    }
    private double getProfitMargin(int ID) {
        return 0.99 * rm.items.get(ID).getAsk() - rm.items.get(ID).getBid();
    }
    private double getVol(int ID) {
        return rm.items.get(ID).getOneHour().getLowPriceVolume() + rm.items.get(ID).getOneHour().getHighPriceVolume();
    }
    private long getCapitalBinding(int ID) {
        return -1L * rm.items.get(ID).getBid() * rm.items.get(ID).getTargetVol();
    }
    public void Order() {
        List<Integer> profitOrderedSelection = new ArrayList<>(rm.config.getSelections());
        profitOrderedSelection.sort(Comparator.comparingDouble(this::getProfitMargin));

        List<Integer> volOrderedSelection = new ArrayList<>(rm.config.getSelections());
        volOrderedSelection.sort(Comparator.comparingDouble(this::getVol));

        List<Integer> capitalBindingOrderedSelection = new ArrayList<>(rm.config.getSelections());
        capitalBindingOrderedSelection.sort(Comparator.comparingLong(this::getCapitalBinding));

        List<Integer> orderedSelections = new ArrayList<>(rm.config.getSelections());
        orderedSelections.sort(Comparator.comparingInt(id -> {
            int profitIndex = profitOrderedSelection.indexOf(id);
            int volIndex = volOrderedSelection.indexOf(id);
            int capitalBindingIndex = capitalBindingOrderedSelection.indexOf(id);

            // Calculate the average of the indices
            return -1 * (rm.config.getPriorityProfit() * profitIndex + rm.config.getPriorityVol() * volIndex + rm.config.getPriorityCapitalBinding() * capitalBindingIndex);
        }));
        rm.config.setSelections(new LinkedHashSet<>(orderedSelections));
    }
    public void Cancel(GrandExchangeItem geItem) {
        if (!Client.isLoggedIn() || !GrandExchange.isOpen()) {
            return;
        }
        humblePhlipper.Resources.Items.Item item = rm.items.get(geItem.getID());
        if (GrandExchange.isBuyOpen() || GrandExchange.isSellOpen()) {
            return;
        }
        if ((geItem.isBuyOffer() && (geItem.getPrice() != item.getBid() ||
                0.99 * item.getAsk() - item.getBid() <= 0 ||
                Widgets.get(465, 7 + geItem.getSlot(), 22).getWidth() > 0 ||
                !rm.session.getBidding())) ||
                (geItem.isSellOffer() && geItem.getPrice() != item.getAsk())) {
            if (Sleep.sleepUntil(() -> GrandExchange.cancelOffer(geItem.getSlot()), 1000)) {
                Sleep.sleep(1000);
            }
        }
    }

    public void Collect(GrandExchangeItem geItem) {
        if (!Client.isLoggedIn() || !GrandExchange.isOpen()) {
            return;
        }

        humblePhlipper.Resources.Items.Item item = rm.items.get(geItem.getID());

        if (!geItem.isReadyToCollect()) {
            return;
        }
        if (!Sleep.sleepUntil(() -> GrandExchange.openSlotInterface(geItem.getSlot()), SLEEP)) {
            return;
        } else {
            Sleep.sleep(SLEEP);
        }

        boolean collectionSuccess = false;
        int vol = geItem.getTransferredAmount();
        double price;

        if (geItem.isBuyOffer()) {
            price = (double) -1 * geItem.getTransferredValue() / vol;
        } else {
            // getTransferredValue() is pre-tax for Asks, so we use regex with a widget
            Pattern pattern = Pattern.compile("for\\s<col=ffb83f>([,\\d]+)</col>\\scoins");
            Matcher matcher = pattern.matcher(Widgets.get(465, 23, 1).getText());
            matcher.find();
            price = (double) Integer.parseInt(matcher.group(1).replaceAll(",", "")) / vol;
        }

        if (!GrandExchange.getOfferSecondItemWidget().isHidden()) {
            if (Sleep.sleepUntil(() -> GrandExchange.getOfferSecondItemWidget().interact(), SLEEP)) {
                Sleep.sleep(SLEEP);
                if (Sleep.sleepUntil(() -> GrandExchange.getOfferFirstItemWidget().interact(), SLEEP)) {
                    Sleep.sleep(SLEEP);
                    collectionSuccess = true;
                }
            }
        } else {
            if (Sleep.sleepUntil(() -> GrandExchange.getOfferFirstItemWidget().interact(), SLEEP)) {
                Sleep.sleep(SLEEP);
                collectionSuccess = true;
            }
        }

        if (Inventory.count(geItem.getName()) > 0) {
            collectionSuccess = true;
        }

        if (!collectionSuccess || vol == 0) {
            return;
        }

        if (geItem.isBuyOffer()) {
            item.setBought(item.getBought() + vol);
            item.setLastBuyPrice(-1 * price);

            if (Duration.between(rm.fourHourLimits.get(item.getId()).getRefreshTime(), LocalDateTime.now()).toMinutes() > 240 || rm.fourHourLimits.get(item.getId()).getUsedLimit() == 0) {
                rm.fourHourLimits.get(item.getId()).setRefreshTime(LocalDateTime.now());
            }
            rm.fourHourLimits.get(item.getId()).incrementUsedLimit(vol);
            rm.saveFourHourLimits();

            item.updateFourHourLimit();
            item.updateTargetVol();
        } else {
            item.setSold(item.getSold() + vol);
            item.setProfit(item.getProfit() + (price - item.getLastBuyPrice()) * vol);

            rm.session.setProfit(0);
            for (humblePhlipper.Resources.Items.Item loopItem : rm.items.values()) {
                rm.session.incrementProfit(loopItem.getProfit());
            }
            rm.session.incrementTimeCumProfitMap(rm.session.getTimer().elapsed(), rm.session.getProfit());
        }

        Trade trade = new Trade(LocalDateTime.now(), geItem.getName(), vol, price);
        item.incrementTradeList(trade);
        Logger.log("<trade>\n" + trade.getCSV() + "</trade>");
    }

    public Boolean MakeAsk(Integer ID) {
        if (!Client.isLoggedIn() || !GrandExchange.isOpen()) {
            return false;
        }

        humblePhlipper.Resources.Items.Item item = rm.items.get(ID);

        if (GrandExchange.getFirstOpenSlot() == -1) {
            return false;
        }
        if (Arrays.stream(GrandExchange.getItems()).anyMatch(geItem -> geItem.getName().equals(item.getMapping().getName()))) {
            return false;
        }
        if (item.getSold() >= item.getBought() || Inventory.count(item.getMapping().getName()) == 0) {
            return false;
        }
        return Sleep.sleepUntil(() -> GrandExchange.sellItem(item.getMapping().getName(), (item.getBought() - item.getSold()), item.getAsk()), SLEEP);
    }

    public Boolean MakeBid(Integer ID) {
        if (!Client.isLoggedIn() || !GrandExchange.isOpen()) {
            return false;
        }

        humblePhlipper.Resources.Items.Item item = rm.items.get(ID);

        if (GrandExchange.getFirstOpenSlot() == -1) {
            return false;
        }
        if (Arrays.stream(GrandExchange.getItems()).anyMatch(geItem -> geItem.getName().equals(item.getMapping().getName()))) {
            return false;
        }
        if (item.getTargetVol() == 0 || !rm.session.getBidding() || Inventory.count("Coins") < item.getBid() || 0.99 * item.getAsk() - item.getBid() <= 0) {
            return false;
        }
        int vol = Math.min(item.getTargetVol(), (int) Math.floor((double) Inventory.count("Coins") / item.getBid()));
        vol = Math.min(vol, (int) Math.floor((double) rm.config.getMaxBidValue() / item.getBid()));
        vol = Math.min(vol, rm.config.getMaxBidVol());
        if (vol == 0) {
            return false;
        }
        final int finalVol = vol;
        return Sleep.sleepUntil(() -> GrandExchange.buyItem(item.getMapping().getName(), finalVol, item.getBid()), SLEEP);
    }
}
