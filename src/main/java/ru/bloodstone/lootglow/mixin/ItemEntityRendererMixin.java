package ru.bloodstone.lootglow.mixin;

import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodstone.lootglow.LootGlowClient;

/** Set the actual render state before WorldRenderer decides whether to run the outline pass. */
@Mixin(ItemEntityRenderer.class)
abstract class ItemEntityRendererMixin {
    @Inject(method="updateRenderState(Lnet/minecraft/entity/ItemEntity;Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;F)V", at=@At("TAIL"))
    private void lootglow$itemOutline(ItemEntity entity, ItemEntityRenderState state, float tickProgress, CallbackInfo ci) {
        Integer color = LootGlowClient.outlineColor(entity);
        if (color != null) state.outlineColor = 0xFF000000 | color;
    }
}
