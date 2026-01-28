package com.agrowmart.enums;



public enum SubscriptionPlan {
    NONE(10, 0, "Free - No products allowed"),
    STARTER(5, 499, "Starter - ₹499/month (max 5 products)"),
    GROWTH(10, 999, "Growth - ₹999/month (max 10 products)"),
    VISIBILITY(15, 1499, "Visibility - ₹1,499/month (max 15 products)"),
    LEAD_BOOSTER(20, 1999, "Lead Booster - ₹1,999/month (max 20 products)"),
    MARKETING_PLUS(30, 2999, "Marketing Plus - ₹2,999/month (max 30 products)"),
    REGIONAL_PARTNER(Integer.MAX_VALUE, 4999, "Regional Partner - Unlimited products"),
    PREMIUM_BRAND(Integer.MAX_VALUE, 7999, "Premium Brand - Unlimited products"),
    ENTERPRISE(Integer.MAX_VALUE, 9999, "Enterprise - Unlimited products"),
    SMART_AI_PROMOTION(Integer.MAX_VALUE, 14999, "Smart AI - Unlimited products"),
    STRATEGIC_PARTNER(Integer.MAX_VALUE, 24999, "Strategic Partner - Unlimited products");

    private final int maxProducts;
    private final int price;
    private final String description;

    SubscriptionPlan(int maxProducts, int price, String description) {
        this.maxProducts = maxProducts;
        this.price = price;
        this.description = description;
    }

    public int getMaxProducts() { return maxProducts; }
    public int getPrice() { return price; }
    public String getDescription() { return description; }
}