package humblePhlipper.resources;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class UserAgent {
    public String name;
    public UserAgent() {
        List<String> userAgents = userAgents();
        this.name = userAgents.get(new Random().nextInt(userAgents.size()));
    }
    public static List<String> userAgents() {
        List<String> userAgents = new ArrayList<>();
        userAgents.add("price_analysis_bot");
        userAgents.add("P2P Flipper");
        userAgents.add("Treasure Hunter");
        userAgents.add("marketInspector");
        userAgents.add("item_scraper");
        userAgents.add("loot_Tracker");
        userAgents.add("Value Analyzer");
        userAgents.add("Gold_Digger");
        userAgents.add("merchant_toolkit");
        userAgents.add("trade_watchdog");
        userAgents.add("auctionObserver");
        userAgents.add("PriceSpotter");
        userAgents.add("InvestmentGuru");
        userAgents.add("ProfitMaximizer");
        userAgents.add("currency_collector");
        userAgents.add("Trading Scout");
        userAgents.add("Wealth_Tracker");
        userAgents.add("priceSpotter");
        userAgents.add("market_navigator");
        userAgents.add("fortune_seeker");
        userAgents.add("flipper_bot");
        userAgents.add("capitalist_adventurer");
        userAgents.add("treasure-hunter");
        userAgents.add("Wealth Manager");
        userAgents.add("itemEvaluator");
        userAgents.add("value_hunter");
        userAgents.add("Gold_Seeker");
        userAgents.add("trade-analyzer");
        userAgents.add("investment Guru");
        userAgents.add("merchant toolkit");
        userAgents.add("TradeGuardian");
        userAgents.add("Auction_Observer");
        userAgents.add("priceWatcher");
        userAgents.add("profit tracker");
        userAgents.add("Currency Trader");
        userAgents.add("Trading_Guru");
        userAgents.add("wealthOptimizer");
        userAgents.add("Price_Checker");
        userAgents.add("Market Explorer");
        userAgents.add("fortune_finder");
        userAgents.add("flipperAssistant");
        userAgents.add("Capitalist Explorer");
        userAgents.add("treasure_seeker");
        userAgents.add("wealth_planner");
        userAgents.add("Item_profiler");
        userAgents.add("value_investigator");
        userAgents.add("Gold_digger");
        userAgents.add("trade observer");
        userAgents.add("Investment_Advisor");
        userAgents.add("merchant_helper");
        userAgents.add("Trade_Monitor");
        userAgents.add("Auction_Inspector");
        userAgents.add("Price_Observer");
        userAgents.add("Profit_Checker");
        userAgents.add("Currency_Observer");
        userAgents.add("TradingSavant");
        userAgents.add("Wealth_Analyst");
        userAgents.add("price_Inspector");
        userAgents.add("market_Observer");
        userAgents.add("fortune seeker");
        userAgents.add("Flipper Manager");
        userAgents.add("capitalist_Journeyman");
        userAgents.add("treasure_seeker");
        userAgents.add("Wealth_Architect");
        userAgents.add("item_Ambassador");
        userAgents.add("Value_observer");
        userAgents.add("gold_seeker");
        userAgents.add("trade_manager");
        userAgents.add("Investment Strategist");
        userAgents.add("Merchant Inspector");
        userAgents.add("trade_watchman");
        userAgents.add("auction_scout");
        userAgents.add("Price_checker");
        userAgents.add("Profit Inspector");
        userAgents.add("currency_evaluator");
        userAgents.add("trading advisor");
        userAgents.add("WealthPlanner");
        return userAgents;
    }
}
