package ru.bloodstone.lootglow;

import java.util.*;

/** Classification is separate from Minecraft rendering and uses only client-visible components. */
public final class LootRules {
    public enum Kind { LEGENDARY, EPIC, GAPPLE, TOTEM, MACE }
    public static final class ModelRule {
        public String item;
        public List<Integer> values = new ArrayList<>();
        public Kind kind = Kind.LEGENDARY;
        public String label;
    }
    public static final class Registry {
        public Map<String,Kind> models = new LinkedHashMap<>();
        public Map<String,Kind> modelPrefixes = new LinkedHashMap<>();
        public List<ModelRule> customModelData = new ArrayList<>();
    }
    private final Map<String,Kind> models;
    private final List<Map.Entry<String,Kind>> prefixes;
    private final Map<String,Map<Integer,Kind>> values = new HashMap<>();
    public LootRules(Registry bundled, Registry additional) {
        Map<String,Kind> exact=new HashMap<>(), starts=new HashMap<>();
        for(Registry registry:List.of(bundled,additional)) {
            if(registry.models!=null)registry.models.forEach((k,v)->{if(valid(k)&&v!=null)exact.put(k,v);});
            if(registry.modelPrefixes!=null)registry.modelPrefixes.forEach((k,v)->{if(valid(k)&&v!=null&&k.endsWith("/"))starts.put(k,v);});
            if(registry.customModelData!=null)for(ModelRule rule:registry.customModelData) {
                if(rule==null||!valid(rule.item)||rule.kind==null||rule.values==null)continue;
                for(Integer number:rule.values)if(number!=null&&number>=0)values.computeIfAbsent(rule.item,k->new HashMap<>()).put(number,rule.kind);
            }
        }
        models=Map.copyOf(exact);
        prefixes=starts.entrySet().stream().sorted(Comparator.comparingInt((Map.Entry<String,Kind> e)->e.getKey().length()).reversed()).toList();
    }
    private static boolean valid(String id) {return id!=null&&id.matches("[a-z0-9_.-]+:[a-z0-9/._-]+");}
    public Kind classify(String item,String model,Float cmd) {
        if("minecraft:totem_of_undying".equals(item))return Kind.TOTEM;
        if("minecraft:enchanted_golden_apple".equals(item))return Kind.GAPPLE;
        if(model!=null) {
            Kind exact=models.get(model);if(exact!=null)return exact;
            for(var prefix:prefixes)if(model.startsWith(prefix.getKey()))return prefix.getValue();
        }
        if("minecraft:mace".equals(item))return Kind.MACE;
        if(cmd==null||!Float.isFinite(cmd)||cmd<0||cmd>Integer.MAX_VALUE||cmd.doubleValue()!=Math.rint(cmd.doubleValue()))return null;
        return values.getOrDefault(item,Map.of()).get(cmd.intValue());
    }
}
