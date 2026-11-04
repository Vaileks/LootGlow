package ru.bloodstone.lootglow.mixin;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodstone.lootglow.LootGlowClient;
@Mixin(Entity.class)
abstract class EntityMixin {
    @Inject(method="getTeamColorValue",at=@At("RETURN"),cancellable=true)
    private void lootglow$color(CallbackInfoReturnable<Integer> result) {
        Integer color=LootGlowClient.outlineColor((Entity)(Object)this);
        if(color!=null)result.setReturnValue(color);
    }
}
