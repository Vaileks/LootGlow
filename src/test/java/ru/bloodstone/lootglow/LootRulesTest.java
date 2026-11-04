package ru.bloodstone.lootglow;
import com.google.gson.Gson;
import java.io.*;import java.nio.charset.StandardCharsets;import java.util.*;
public final class LootRulesTest {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[]args)throws Exception {
        LootRules.Registry bundled;
        try(var in=LootRulesTest.class.getResourceAsStream("/lootglow-rules.json")) {
            bundled=new Gson().fromJson(new InputStreamReader(Objects.requireNonNull(in),StandardCharsets.UTF_8),LootRules.Registry.class);
        }
        LootRules rules=new LootRules(bundled,new LootRules.Registry());
        for(var model:bundled.models.entrySet())check(rules.classify("minecraft:stick",model.getKey(),null)==model.getValue(),"exact model "+model.getKey());
        for(var prefix:bundled.modelPrefixes.entrySet())check(rules.classify("minecraft:stick",prefix.getKey()+"new_skin/model",null)==prefix.getValue(),"skin family "+prefix.getKey());
        for(var rule:bundled.customModelData)for(var number:rule.values) {
            var expected=rule.item.equals("minecraft:totem_of_undying")?LootRules.Kind.TOTEM:rule.kind;
            check(rules.classify(rule.item,null,number.floatValue())==expected,"original source CMD "+rule.label);
            check(rules.classify("minecraft:cobblestone",null,number.floatValue())==null,"CMD on unrelated base material is not loot");
        }
        check(rules.classify("minecraft:totem_of_undying",null,null)==LootRules.Kind.TOTEM,"ordinary totem");
        check(rules.classify("minecraft:enchanted_golden_apple",null,null)==LootRules.Kind.GAPPLE,"enchanted golden apple");
        for(String item:List.of("minecraft:diamond_sword","minecraft:golden_apple","minecraft:stick","minecraft:diamond"))check(rules.classify(item,null,null)==null,"ordinary or renamed vanilla item excluded "+item);
        check(rules.classify("minecraft:stone_axe","civilization:tools/mjolnir/normal",null)==LootRules.Kind.LEGENDARY,"current Mjolnir model");
        check(rules.classify("minecraft:diamond_sword","civilization:tools/midas_sword",null)==LootRules.Kind.LEGENDARY,"root model without final slash");
        check(rules.classify("minecraft:bow","bloodstone:tools/artemis_bow/skin/test",null)==LootRules.Kind.LEGENDARY,"namespace/skin variant");
        check(rules.classify("minecraft:blaze_rod",null,1f)==LootRules.Kind.LEGENDARY,"current light wand");
        check(rules.classify("minecraft:stick","fullleg_dark:fx/wand",null)==LootRules.Kind.LEGENDARY,"dark wand");
        check(rules.classify("minecraft:paper","fullleg_dark:fx/dome",null)==null,"spell effect is not a wand");
        check(rules.classify("minecraft:stone_shovel",null,1000f)==LootRules.Kind.EPIC,"CSI epic shovel");
        check(rules.classify("minecraft:diamond_sword",null,10001f)==LootRules.Kind.EPIC,"CSI epic Lavorez");
        for(Float cmd:Arrays.asList(null,Float.NaN,Float.POSITIVE_INFINITY,-1f,99.5f))check(rules.classify("minecraft:diamond_sword",null,cmd)==null,"invalid/noninteger CMD cannot match");
        check(rules.classify("minecraft:diamond_sword","bloodstone:tools/unknown_unrelated/model",null)==null,"no catch-all resource-pack namespace match");
        check(rules.classify("minecraft:mace",null,null)==LootRules.Kind.MACE,"ordinary mace without enchantments");
        check(rules.classify("minecraft:mace","bloodstone:mace_skins/roulette",145f)==LootRules.Kind.MACE,"Television mace");
        check(rules.classify("minecraft:mace","minecraft:mace",138f)==LootRules.Kind.MACE,"Glitch mace");
        check(rules.classify("minecraft:mace","example:any_future_skin",Float.NaN)==LootRules.Kind.MACE,"all future mace skins regardless of CMD");
        check(rules.classify("minecraft:mace","bloodstone:tools/lite_mace/default/lite_mace",null)==LootRules.Kind.LEGENDARY,"legendary mace keeps its legendary color");
        check(rules.classify("minecraft:blaze_rod",null,5f)==LootRules.Kind.LEGENDARY,"integrated End Staff");
        check(rules.classify("minecraft:blaze_rod",null,null)==null,"ordinary blaze rod excluded");
        check(rules.classify("minecraft:blaze_rod",null,6f)==null,"unrelated staff CMD excluded");
        check(rules.classify("minecraft:paper","modelengine:blood_ballista_pack_v2/root",null)==LootRules.Kind.EPIC,"whole Ballista root item");
        check(rules.classify("minecraft:paper","modelengine:blood_ballista_pack_v2/bow",null)==null,"Ballista visual part is not a whole dropped weapon");
        check(rules.classify("minecraft:crossbow",null,null)==null,"ordinary crossbow excluded");
        var extra=new LootRules.Registry();extra.models.put("example:my_relic",LootRules.Kind.LEGENDARY);var custom=new LootRules.ModelRule();custom.item="minecraft:book";custom.values=List.of(314);custom.kind=LootRules.Kind.EPIC;extra.customModelData.add(custom);
        var extended=new LootRules(bundled,extra);check(extended.classify("minecraft:paper","example:my_relic",null)==LootRules.Kind.LEGENDARY,"client-only custom model override");check(extended.classify("minecraft:book",null,314f)==LootRules.Kind.EPIC,"client-only CMD override");
        GlowConfig config=new GlowConfig();config.maxDistance=Double.NaN;config.normalize();check(config.maxDistance==64,"NaN distance repaired");config.maxDistance=99999;config.normalize();check(config.maxDistance==128,"distance bounded");
        check(config.color(LootRules.Kind.LEGENDARY)==0xFFAA00&&config.color(LootRules.Kind.TOTEM)==0x55FF55,"default colors");config.legendaryColor="bad";check(config.color(LootRules.Kind.LEGENDARY)==0xFFFFFF,"invalid color fallback");config.legendaries=false;check(!config.enabled(LootRules.Kind.LEGENDARY)&&config.enabled(LootRules.Kind.EPIC),"categories independent");
        config.maces=false;check(!config.enabled(LootRules.Kind.MACE)&&config.enabled(LootRules.Kind.EPIC),"mace category can be toggled independently");
        System.out.println("PASS "+checks+" loot rules/config checks; Minecraft renderer not started by these tests.");
    }
}
