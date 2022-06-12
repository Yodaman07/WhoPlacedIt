package legobrosbuild.whoplacedit.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

import static legobrosbuild.whoplacedit.WhoPlacedIt.highlight_particles;
import static legobrosbuild.whoplacedit.WhoPlacedIt.posData;

@Mixin(ServerWorld.class)
public abstract class ParticleTickMixin {
    @Shadow public abstract <T extends ParticleEffect> int spawnParticles(T particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed);
    @Shadow public abstract ServerWorld toServerWorld();

    @Inject(at = @At("TAIL"), method = "tick")
    private void init(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if (highlight_particles){
            posData.forEach((blockPos, s) -> {
                BlockState state = this.toServerWorld().getBlockState(blockPos.add( 0, 1, 0));
                if (state.getBlock() == Blocks.AIR){
                    this.spawnParticles(ParticleTypes.HAPPY_VILLAGER ,blockPos.getX()+0.5, blockPos.getY() + 1.5, blockPos.getZ()+0.5, 1, 0, 0, 0, 0);
                }
            });
        }
    }
}
