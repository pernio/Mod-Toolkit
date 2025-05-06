// package emcMod.mixin;
//
// import emcMod.client.EmcModClient;
// import net.minecraft.client.network.AbstractClientPlayerEntity;
// import net.minecraft.client.render.entity.PlayerEntityRenderer;
// import net.minecraft.text.Text;
// import net.minecraft.util.Formatting;
// import org.spongepowered.asm.mixin.Mixin;
// import org.spongepowered.asm.mixin.injection.At;
// import org.spongepowered.asm.mixin.injection.Inject;
// import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//
// import java.util.Locale;
//
// @Mixin(PlayerEntityRenderer.class)
// public class PlayerEntityRendererMixin {
//
//     @Inject(method = "getLabel", at = @At("HEAD"), cancellable = true)
//     private void modifyNametagLabel(AbstractClientPlayerEntity player, CallbackInfoReturnable<Text> cir) {
//         if (!EmcModClient.toolsEnabled) return;
//
//         String playerName = player.getName().getString().toLowerCase(Locale.ROOT);
//         if (EmcModClient.followedPlayers.contains(playerName)) {
//             cir.setReturnValue(Text.literal(player.getName().getString()).formatted(EmcModClient.followColor));
//         }
//     }
// }