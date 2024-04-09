package humblePhlipper;

import org.dreambot.api.Client;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Trading {
    private final ResourceManager rm;
    private static final Integer SLEEP = humblePhlipper.Main.SLEEP;
    private static final Set<Integer> restrictedIdSet = new HashSet<>(Arrays.asList(1521, 1519, 1515, 317, 315, 321, 319, 377, 379, 434, 1761, 436, 438, 440, 442, 444, 453, 447, 449, 451, 1739, 229, 227, 1937, 313, 314, 221, 245, 556, 555, 557, 554, 558, 562));
    public Trading(ResourceManager rm) {
        this.rm = rm;
    }
    public static Set<Integer> getRestrictedIdSet() {
        return restrictedIdSet;
    }
    public void Select() {
        for (Integer ID : rm.items.keySet()) {
            humblePhlipper.resources.Items.Item item = rm.items.get(ID);
            if (item.getAsk() == null || item.getBid() == null) {
                rm.config.removeFromSelections(item.getId());
                continue;
            }
            if (item.getTargetVol() == Integer.MAX_VALUE) {
                rm.config.removeFromSelections(item.getId());
                continue;
            }
            if (item.getId() == 13190) { // bonds
                rm.config.removeFromSelections(item.getId());
                continue;
            }
            if (item.getOneHour().getLowPriceVolume() + item.getOneHour().getHighPriceVolume() < rm.config.getMinVol()) {
                rm.config.removeFromSelections(item.getId());
                continue;
            }
            if ((float) item.getOneHour().getLowPriceVolume() / item.getOneHour().getHighPriceVolume() > rm.config.getMaxBidAskVolRatio()) {
                rm.config.removeFromSelections(item.getId());
                continue;
            }
            if (getProfitMargin(item.getId()) < rm.config.getMinMargin()) {
                rm.config.removeFromSelections(item.getId());
                continue;
            }
            if (item.getBid() > rm.config.getMaxBidPrice()) {
                rm.config.removeFromSelections(item.getId());
                continue;
            }
            if (rm.config.getTradeRestricted() && restrictedIdSet.contains(item.getId())) {
                rm.config.removeFromSelections(item.getId());
                continue;
            }
            if (!rm.config.getMembers() && item.getMapping().getMembers()) {
                rm.config.removeFromSelections(item.getId());
                continue;
            }
            rm.config.incrementSelections(item.getId());
        }
        Order();
        //rm.config.setSelections(rm.config.getSelections().stream().limit(rm.config.getNumToSelect()).collect(Collectors.toCollection(LinkedHashSet::new))); // Keep first N selections
    }
    public Double getProfitMargin(int ID) {
        return (rm.items.get(ID).getBid() == null || rm.items.get(ID).getAsk() == null) ? null : Math.max(Math.ceil(0.99 * rm.items.get(ID).getAsk()), rm.items.get(ID).getAsk() - 5000000) - rm.items.get(ID).getBid();
    }
    private double getVol(int ID) {
        return rm.items.get(ID).getOneHour().getLowPriceVolume() + rm.items.get(ID).getOneHour().getHighPriceVolume();
    }
    private Long getCapitalBinding(int ID) {
        return (rm.items.get(ID).getBid() == null) ? null : -1L * rm.items.get(ID).getBid() * rm.items.get(ID).getTargetVol();
    }
    public void Order() {
        List<Integer> profitOrderedSelection = new ArrayList<>(rm.config.getSelections());
        profitOrderedSelection.sort(Comparator.comparingDouble(id -> {
            return (getProfitMargin(id) != null) ? getProfitMargin(id) : Double.NEGATIVE_INFINITY;
        }));

        List<Integer> volOrderedSelection = new ArrayList<>(rm.config.getSelections());
        volOrderedSelection.sort(Comparator.comparingDouble(this::getVol));

        List<Integer> capitalBindingOrderedSelection = new ArrayList<>(rm.config.getSelections());
        capitalBindingOrderedSelection.sort(Comparator.comparingLong(id -> {
            return (getCapitalBinding(id) != null) ? getCapitalBinding(id) : Long.MAX_VALUE;
        }));

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
    public boolean Cancel(int slotIndex) {
        if (!Client.isLoggedIn() || !GrandExchange.isOpen()) {
            return false;
        }
        if (GrandExchange.isBuyOpen() || GrandExchange.isSellOpen()) {
            return false;
        }
        humblePhlipper.dbGE.Slot geSlot = humblePhlipper.dbGE.Slot.get(slotIndex);
        if (geSlot.getType().equals("Empty") || geSlot.getItemId() == -1) {
            return false;
        }
        if (geSlot.getTradeBarWidth() == humblePhlipper.dbGE.Slot.maxTradeBarWidth) {
            return false;
        }
        humblePhlipper.resources.Items.Item item = rm.items.get(geSlot.getItemId());
        if (geSlot.isBuyOffer() && (
                                    geSlot.getPrice() == item.getBid() &&
                                    getProfitMargin(item.getId()) >= rm.config.getMinMargin() &&
                                    (rm.config.getCancelPartialBids() ? geSlot.getTradeBarWidth() == 0 : true) &&
                                    rm.session.getBidding() )
                                    ) {
            return false;
        }
        if (geSlot.isSellOffer() && (
                                    geSlot.getPrice() == (rm.config.getNeverSellAtLoss() ? Math.max(item.getAsk(), getBreakEvenAsk(item.getLastBuyPrice())) : item.getAsk()))
                                    ) {
            return false;
        }
        return (Sleep.sleepUntil(() -> GrandExchange.cancelOffer(slotIndex), 1000));
    }

    public boolean Collect(int slotIndex) {
        if (!Client.isLoggedIn() || !GrandExchange.isOpen()) {
            return false;
        }
        if (GrandExchange.isBuyOpen() || GrandExchange.isSellOpen()) {
            return false;
        }
        humblePhlipper.dbGE.Slot geSlot = humblePhlipper.dbGE.Slot.get(slotIndex);
        if (geSlot.getType().equals("Empty") || geSlot.getItemId() == -1) {
            return false;
        }
        if (!geSlot.isReadyToCollect()) {
            return false;
        }
        humblePhlipper.resources.Items.Item item = rm.items.get(geSlot.getItemId());

        boolean isBuyOffer = geSlot.isBuyOffer();
        if (Sleep.sleepUntil(() -> GrandExchange.openSlotInterface(slotIndex), SLEEP)) {
            Sleep.sleep(SLEEP);
        } else if (Sleep.sleepUntil(() -> humblePhlipper.dbGE.Slot.openSlotInterface(slotIndex), SLEEP)) {
            Sleep.sleep(SLEEP);
        } else {
            return false;
        }

        boolean collectionSuccess = false;
        int vol = humblePhlipper.dbGE.OpenOffer.getTransferredAmount();
        double price = (double) humblePhlipper.dbGE.OpenOffer.getTransferredValue()/vol;
        price = (isBuyOffer) ? -1 * price : price;

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

        if (!collectionSuccess || vol == 0) {
            return false;
        }

        if (isBuyOffer) {
            item.setBought(item.getBought() + vol);
            item.setLastBuyPrice(-1 * price);

            if (rm.fourHourLimits.get(item.getId()).getCountdownMinutes() < 0 || rm.fourHourLimits.get(item.getId()).getUsedLimit() == 0) {
                rm.fourHourLimits.get(item.getId()).setRefreshTime(Instant.now().toEpochMilli());
            }
            rm.fourHourLimits.get(item.getId()).incrementUsedLimit(vol);
            rm.saveFourHourLimits();

            item.updateFourHourLimit();
            item.updateTargetVol();
        } else {
            item.setSold(item.getSold() + vol);
            item.setProfit(item.getProfit() + (price - item.getLastBuyPrice()) * vol);

            rm.session.setProfit(0);
            for (humblePhlipper.resources.Items.Item loopItem : rm.items.values()) {
                rm.session.incrementProfit(loopItem.getProfit());
            }
            rm.session.incrementTimeCumProfitMap(rm.session.getTimer().elapsed(), rm.session.getProfit());
        }

        humblePhlipper.resources.data.Trades.Trade trade = new humblePhlipper.resources.data.Trades.Trade(LocalDateTime.now(), item.getMapping().getName(), vol, price);
        item.getTrades().increment(trade);
        Logger.log("<trade>\n" + trade.getCSV() + "</trade>");

        return true;
    }

    public Boolean MakeAsk(Integer ID) {
        if (!Client.isLoggedIn() || !GrandExchange.isOpen()) {
            return false;
        }

        humblePhlipper.resources.Items.Item item = rm.items.get(ID);

        if (GrandExchange.getFirstOpenSlot() == -1) {
            return false;
        }
        if (Arrays.stream(GrandExchange.getItems()).anyMatch(geItem -> geItem.getID() == item.getMapping().getId() || geItem.getName().equals(item.getMapping().getName()))) {
            return false;
        }
        if (Inventory.count(item.getMapping().getName()) == 0 && Inventory.count(item.getMapping().getId()) == 0) {
            return false;
        }
        if (item.getSold() >= item.getBought()) {
            return false;
        }
        final int finalAsk = rm.config.getNeverSellAtLoss() ? Math.max(item.getAsk(), getBreakEvenAsk(item.getLastBuyPrice())) : item.getAsk();
        if (Sleep.sleepUntil(() -> GrandExchange.sellItem(item.getMapping().getId(), (item.getBought() - item.getSold()), finalAsk), SLEEP)) {
            return true; // We need this to buy items like Falador tablet (teleport)
        }
        if (Sleep.sleepUntil(() -> GrandExchange.sellItem(item.getMapping().getName(), (item.getBought() - item.getSold()), finalAsk), SLEEP)) {
            return true; // We need this to sell items like Black d'hide body and Bat bones (???)
        }
        return false;
    }

    public Boolean MakeBid(Integer ID) {
        if (!Client.isLoggedIn() || !GrandExchange.isOpen()) {
            return false;
        }

        humblePhlipper.resources.Items.Item item = rm.items.get(ID);

        if (GrandExchange.getFirstOpenSlot() == -1) {
            return false;
        }
        if (Arrays.stream(GrandExchange.getItems()).anyMatch(geItem -> geItem.getID() == item.getMapping().getId() || geItem.getName().equals(item.getMapping().getName()))) {
            return false;
        }
        if (item.getBid() == null) {
            return false;
        }
        if (item.getTargetVol() == 0 || !rm.session.getBidding() || Inventory.count("Coins") < item.getBid() || getProfitMargin(item.getId()) <= 0) {
            return false;
        }
        if (Inventory.count(item.getMapping().getName()) != 0 || Inventory.count(item.getMapping().getId()) != 0) {
            return false;
        }
        if (rm.session.getNoCompetitionIds() != null) {
            if (rm.config.getNoSelfCompeting() && rm.session.getNoCompetitionIds().contains(item.getId())) {
                return false;
            }
        }
        if (item.getBought() > item.getSold()) {
            return false;
        }
        int vol = Math.min(item.getTargetVol(), (int) Math.floor((double) Inventory.count("Coins") / item.getBid()));
        vol = Math.min(vol, (int) Math.floor((double) rm.config.getMaxBidValue() / item.getBid()));
        vol = Math.min(vol, rm.config.getMaxBidVol());
        final int finalVol = vol;
        if (vol == 0) {
            return false;
        }
        if (Sleep.sleepUntil(() -> GrandExchange.buyItem(item.getMapping().getId(), finalVol, item.getBid()), SLEEP)) {
            return true; // This seems to work for all items
        }
        if (Sleep.sleepUntil(() -> GrandExchange.buyItem(item.getMapping().getName(), finalVol, item.getBid()), SLEEP)) {
            return true; // But we will play it safe in case it doesn't
        }
        return false;
    }
    private static int getBreakEvenAsk(double lastBuyPrice) {
        return (int) Math.ceil(lastBuyPrice/0.99 - 1);
    }
}
