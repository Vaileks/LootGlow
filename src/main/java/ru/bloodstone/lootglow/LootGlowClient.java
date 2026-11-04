package ru.bloodstone.lootglow;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public final class LootGlowClient implements ClientModInitializer {
    private static final Logger LOG=LoggerFactory.getLogger("LootGlow");
    private static LootGlowClient instance;
    private final Map<ItemEntity,Cached> cache=new WeakHashMap<>();
    private GlowConfig config;
    private LootRules rules;
    private record Cached(int age,ItemStack stack,LootRules.Kind kind) {}
    @Override public void onInitializeClient() {
        Gson gson=new GsonBuilder().setPrettyPrinting().create();
        Path file=FabricLoader.getInstance().getConfigDir().resolve("lootglow.json");
        config=new GlowConfig();
        try {
            if(Files.exists(file))try(Reader reader=Files.newBufferedReader(file,StandardCharsets.UTF_8)) {
                GlowConfig loaded=gson.fromJson(reader,GlowConfig.class);if(loaded!=null)config=loaded;
            }
            else {Files.createDirectories(file.getParent());Files.writeString(file,gson.toJson(config)+"\n",StandardCharsets.UTF_8,StandardOpenOption.CREATE_NEW);}
        }catch(IOException|RuntimeException e){LOG.warn("Cannot load lootglow.json; using defaults for this session",e);config=new GlowConfig();}
        config.normalize();
        try(InputStream input=LootGlowClient.class.getResourceAsStream("/lootglow-rules.json")) {
            if(input==null)throw new IOException("Bundled loot rules missing");
            LootRules.Registry bundled=gson.fromJson(new InputStreamReader(input,StandardCharsets.UTF_8),LootRules.Registry.class);
            rules=new LootRules(Objects.requireNonNull(bundled),config.additionalRules);
            instance=this;
            LOG.info("LootGlow loaded: enabled={}, distance={}, legendaries={}, epics={}, gapples={}, totems={}, maces={}",
                    config.enabled,config.maxDistance,config.legendaries,config.epics,config.enchantedGoldenApples,config.totems,config.maces);
            if(!config.enabled)LOG.warn("LootGlow outlines are disabled by config/lootglow.json (enabled=false)");
        }catch(IOException|RuntimeException e){LOG.error("LootGlow disabled: cannot load bundled rules",e);}
    }
    /** Called only for rendered entities; never scans worlds or modifies entity/network state. */
    public static Integer outlineColor(Entity entity) {
        LootGlowClient mod=instance;
        if(mod==null||!mod.config.enabled||!(entity instanceof ItemEntity item)||item.isRemoved())return null;
        MinecraftClient client=MinecraftClient.getInstance();
        if(client.world==null||client.player==null||item.squaredDistanceTo(client.player)>mod.config.maxDistance*mod.config.maxDistance)return null;
        ItemStack stack=item.getStack();if(stack.isEmpty())return null;
        Cached cached=mod.cache.get(item);
        LootRules.Kind kind;
        if(cached!=null&&cached.age==item.age&&cached.stack==stack)kind=cached.kind;
        else {
            Identifier model=stack.get(DataComponentTypes.ITEM_MODEL);
            CustomModelDataComponent cmd=stack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
            kind=mod.rules.classify(Registries.ITEM.getId(stack.getItem()).toString(),model==null?null:model.toString(),cmd==null?null:cmd.getFloat(0));
            mod.cache.put(item,new Cached(item.age,stack,kind));
        }
        return kind!=null&&mod.config.enabled(kind)?mod.config.color(kind):null;
    }
}
