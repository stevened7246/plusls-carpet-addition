package com.plusls.carpet.mixin.rule.useDyeOnShulkerBox;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC <= 11701
//$$ import net.minecraft.nbt.CompoundTag;
//#endif

@Mixin(DyeItem.class)
public abstract class MixinDyeItem extends Item {
    public MixinDyeItem(Properties settings) {
        super(settings);
    }

    @Shadow
    public abstract DyeColor getDyeColor();

    @Override
    @Intrinsic
    public @NotNull InteractionResult useOn(UseOnContext useOnContext) {
        return super.useOn(useOnContext);
    }

    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
    @Inject(
            method = "useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void preUseOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (!PluslsCarpetAdditionSettings.useDyeOnShulkerBox) {
            return;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState blockState = level.getBlockState(pos);

        if (blockState.is(Blocks.SHULKER_BOX)) {
            if (!level.isClientSide()) {
                ShulkerBoxBlockEntity blockEntity = (ShulkerBoxBlockEntity) level.getBlockEntity(pos);
                BlockState newBlockState = ShulkerBoxBlock.getBlockByColor(this.getDyeColor()).defaultBlockState().
                        setValue(ShulkerBoxBlock.FACING, blockState.getValue(ShulkerBoxBlock.FACING));

                if (level.setBlockAndUpdate(pos, newBlockState)) {
                    ShulkerBoxBlockEntity newBlockEntity = (ShulkerBoxBlockEntity) level.getBlockEntity(pos);
                    assert blockEntity != null;
                    assert newBlockEntity != null;
                    //#if MC > 11701
                    newBlockEntity.loadFromTag(blockEntity.saveWithoutMetadata());
                    //#else
                    //$$ newBlockEntity.loadFromTag(new CompoundTag());
                    //#endif
                    newBlockEntity.setCustomName(blockEntity.getCustomName());
                    newBlockEntity.setChanged();
                    context.getItemInHand().shrink(1);
                }
            }
            //#if MC > 11502
            cir.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide));
            //#else
            //$$ cir.setReturnValue(level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.PASS);
            //#endif
        }
    }
}
