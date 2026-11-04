package ru.bloodstone.lootglow.mixin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodstone.lootglow.LootGlowClient;
@Mixin(MinecraftClient.class)
abstract class MinecraftClientMixin {
    @Inject(method="hasOutline",at=@At("RETURN"),cancellable=true)
    private void lootglow$outline(Entity entity,CallbackInfoReturnable<Boolean> result) {
        if(LootGlowClient.outlineColor(entity)!=null)result.setReturnValue(true);
    }
}
