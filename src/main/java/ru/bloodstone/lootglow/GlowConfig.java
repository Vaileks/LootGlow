package ru.bloodstone.lootglow;

public final class GlowConfig {
    public boolean enabled=true;
    public double maxDistance=64;
    public boolean legendaries=true;
    public boolean epics=true;
    public boolean enchantedGoldenApples=true;
    public boolean totems=true;
    public boolean maces=true;
    public String legendaryColor="#FFAA00";
    public String epicColor="#AA00AA";
    public String enchantedGoldenAppleColor="#FFD700";
    public String totemColor="#55FF55";
    public String maceColor="#AA00AA";
    public LootRules.Registry additionalRules=new LootRules.Registry();
    public boolean enabled(LootRules.Kind kind) {
        return switch(kind) {case LEGENDARY->legendaries;case EPIC->epics;case GAPPLE->enchantedGoldenApples;case TOTEM->totems;case MACE->maces;};
    }
    public int color(LootRules.Kind kind) {
        String value=switch(kind) {case LEGENDARY->legendaryColor;case EPIC->epicColor;case GAPPLE->enchantedGoldenAppleColor;case TOTEM->totemColor;case MACE->maceColor;};
        try {if(value!=null&&value.matches("#[0-9a-fA-F]{6}"))return Integer.parseInt(value.substring(1),16);}catch(NumberFormatException ignored){}
        return 0xFFFFFF;
    }
    public void normalize() {
        if(!Double.isFinite(maxDistance))maxDistance=64;
        maxDistance=Math.max(1,Math.min(128,maxDistance));
        if(additionalRules==null)additionalRules=new LootRules.Registry();
    }
}
