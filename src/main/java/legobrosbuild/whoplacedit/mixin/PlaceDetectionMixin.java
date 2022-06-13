package legobrosbuild.whoplacedit.mixin;

import legobrosbuild.whoplacedit.WhoPlacedIt;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static legobrosbuild.whoplacedit.WhoPlacedIt.*;

@Mixin(Block.class)
public class PlaceDetectionMixin {

    @Inject(at = @At("HEAD"), method = "onPlaced")
    private void whenPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack, CallbackInfo ci) {
        posData.put(pos, placer.getEntityName());
        if (highlight_particles) savePos(posData);

//        WhoPlacedIt.LOGGER.info(pos.getX() + "," + pos.getY() + "," + pos.getZ() + "; Placed by: " + placer.getEntityName());
    }

    @Inject(at = @At("HEAD"), method = "onBreak")
    private void whenBroken(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo ci){
        posData.remove(pos);
        if (highlight_particles) savePos(posData);
    }
}
