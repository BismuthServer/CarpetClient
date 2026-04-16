package carpetclient.mixins;

import carpetclient.Config;
import net.minecraft.advancement.criterion.CriterionTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.LiquidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BucketItem.class)
public class BucketItemMixin extends Item {

    @Shadow
    private @Final
    Block liquid;

    @Shadow
    private ItemStack fill(ItemStack emptyBuckets, PlayerEntity player, Item fullBucket) {
        return null;
    }

    @Shadow
    public boolean place(@Nullable PlayerEntity player, World worldIn, BlockPos posIn) {
        return false;
    }

    /*
     * Inject at head to remove clientside placement or removal of world liquids to force the server to only do it fixing liquid ghost blocks.
     */
    @Inject(method = "startUsing", at = @At("HEAD"), cancellable = true)
    public void onItemRightClick(World worldIn, PlayerEntity playerIn, InteractionHand handIn, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        boolean flag = this.liquid == Blocks.AIR;
        ItemStack itemstack = playerIn.getItemInHand(handIn);
        HitResult raytraceresult = this.getUseTarget(worldIn, playerIn, flag);

        if (raytraceresult == null) {
            cir.setReturnValue(new InteractionResultHolder<ItemStack>(InteractionResult.PASS, itemstack));
        } else if (raytraceresult.type != HitResult.Type.BLOCK) {
            cir.setReturnValue(new InteractionResultHolder<ItemStack>(InteractionResult.PASS, itemstack));
        } else {
            BlockPos blockpos = raytraceresult.getPos();

            if (!worldIn.canModify(playerIn, blockpos)) {
                cir.setReturnValue(new InteractionResultHolder<ItemStack>(InteractionResult.FAIL, itemstack));
            } else if (flag) {
                if (!playerIn.canUseItemOn(blockpos.offset(raytraceresult.face), raytraceresult.face, itemstack)) {
                    cir.setReturnValue(new InteractionResultHolder<ItemStack>(InteractionResult.FAIL, itemstack));
                } else {
                    BlockState iblockstate = worldIn.getBlockState(blockpos);
                    Material material = iblockstate.getMaterial();

                    if (material == Material.WATER && ((Integer) iblockstate.get(LiquidBlock.LEVEL)).intValue() == 0) {
                        playerIn.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
                        if (!worldIn.isClient || !Config.bucketGhostBlockFix.getValue()) {
                            worldIn.setBlockState(blockpos, Blocks.AIR.defaultState(), 11);
                            playerIn.incrementStat(Stats.itemUsed(this));
                            cir.setReturnValue(new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, this.fill(itemstack, playerIn, Items.WATER_BUCKET)));
                            return;
                        }
                        cir.setReturnValue(new InteractionResultHolder<ItemStack>(InteractionResult.PASS, itemstack));
                    } else if (material == Material.LAVA && ((Integer) iblockstate.get(LiquidBlock.LEVEL)).intValue() == 0) {
                        playerIn.playSound(SoundEvents.ITEM_BUCKET_FILL_LAVA, 1.0F, 1.0F);
                        if (!worldIn.isClient || !Config.bucketGhostBlockFix.getValue()) {
                            worldIn.setBlockState(blockpos, Blocks.AIR.defaultState(), 11);
                            playerIn.incrementStat(Stats.itemUsed(this));
                            cir.setReturnValue(new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, this.fill(itemstack, playerIn, Items.LAVA_BUCKET)));
                            return;
                        }
                        cir.setReturnValue(new InteractionResultHolder<ItemStack>(InteractionResult.PASS, itemstack));
                    } else {
                        cir.setReturnValue(new InteractionResultHolder<ItemStack>(InteractionResult.FAIL, itemstack));
                    }
                }
            } else {
                boolean flag1 = worldIn.getBlockState(blockpos).getBlock().canBeReplaced(worldIn, blockpos);
                BlockPos blockpos1 = flag1 && raytraceresult.face == Direction.UP ? blockpos : blockpos.offset(raytraceresult.face);

                if (!playerIn.canUseItemOn(blockpos1, raytraceresult.face, itemstack)) {
                    cir.setReturnValue(new InteractionResultHolder<ItemStack>(InteractionResult.FAIL, itemstack));
                } else if (this.place(playerIn, worldIn, blockpos1)) {
                    if (!worldIn.isClient) {
                        if (playerIn instanceof ServerPlayerEntity) {
                            CriterionTriggers.PLACED_BLOCK.run((ServerPlayerEntity) playerIn, blockpos1, itemstack);
                        }

                        playerIn.incrementStat(Stats.itemUsed(this));
                        cir.setReturnValue(!playerIn.abilities.creativeMode ? new InteractionResultHolder(InteractionResult.SUCCESS, new ItemStack(Items.BUCKET)) : new InteractionResultHolder(InteractionResult.SUCCESS, itemstack));
                        return;
                    }
                    cir.setReturnValue(new InteractionResultHolder<ItemStack>(InteractionResult.PASS, itemstack));
                } else {
                    cir.setReturnValue(new InteractionResultHolder<ItemStack>(InteractionResult.FAIL, itemstack));
                }
            }
        }
    }

    /*
     * Edit the play sound and return to remove clientside liquid updates.
     */
    @Inject(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/living/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"), cancellable = true)
    public void tryPlaceContainedLiquidInject(PlayerEntity player, World worldIn, BlockPos posIn, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.bucketGhostBlockFix.getValue()) return;

        if (this.liquid != null && player != null) {
            SoundEvent soundevent = this.liquid == Blocks.FLOWING_LAVA ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY;
            player.playSound(soundevent, 1.0F, 1.0F);
            if (worldIn.isClient) cir.setReturnValue(true);
        }
    }

//    @Overwrite
//    public boolean tryPlaceContainedLiquid(@Nullable EntityPlayer player, World worldIn, BlockPos posIn) {
//        if (this.containedBlock == Blocks.AIR) {
//            return false;
//        } else {
//            IBlockState iblockstate = worldIn.getBlockState(posIn);
//            Material material = iblockstate.getMaterial();
//            boolean flag = !material.isSolid();
//            boolean flag1 = iblockstate.getBlock().isReplaceable(worldIn, posIn);
//
//            if (!worldIn.isAirBlock(posIn) && !flag && !flag1) {
//                return false;
//            } else {
//                if (worldIn.provider.doesWaterVaporize() && this.containedBlock == Blocks.FLOWING_WATER) {
//                    int l = posIn.getX();
//                    int i = posIn.getY();
//                    int j = posIn.getZ();
//                    worldIn.playSound(player, posIn, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.8F);
//
//                    for (int k = 0; k < 8; ++k) {
//                        worldIn.spawnParticle(EnumParticleTypes.SMOKE_LARGE, (double) l + Math.random(), (double) i + Math.random(), (double) j + Math.random(), 0.0D, 0.0D, 0.0D);
//                    }
//                } else {
//                    if (!worldIn.isRemote && (flag || flag1) && !material.isLiquid()) {
//                        worldIn.destroyBlock(posIn, true);
//                    }
//
//                    SoundEvent soundevent = this.containedBlock == Blocks.FLOWING_LAVA ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY;
////                    worldIn.playSound(player, posIn, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
//                    if (!player.equals(Minecraft.getMinecraft().player)) {
//                        worldIn.playSound(player, posIn, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
//                    } else {
//                        player.playSound(soundevent, 1.0F, 1.0F);
//                    }
//                    if (!worldIn.isRemote) {
//                        System.out.println("containedBlock " + containedBlock);
//                        worldIn.setBlockState(posIn, this.containedBlock.getDefaultState(), 11);
//                    }
//                }
//
//                return true;
//            }
//        }
//    }
}
