package rbasamoyai.createbigcannons.munitions;

import com.simibubi.create.foundation.block.ITE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import rbasamoyai.createbigcannons.CBCBlockEntities;
import rbasamoyai.createbigcannons.munitions.fuzes.FuzeItem;

public abstract class FuzedProjectileBlock extends ProjectileBlock implements ITE<FuzedBlockEntity> {

	protected FuzedProjectileBlock(Properties properties) {
		super(properties);
	}
	
	@Override public Class<FuzedBlockEntity> getTileEntityClass() { return FuzedBlockEntity.class; }
	@Override public BlockEntityType<? extends FuzedBlockEntity> getTileEntityType() { return CBCBlockEntities.FUZED_BLOCK.get(); }

	protected static ItemStack getFuze(BlockEntity blockEntity) {
		if (blockEntity == null) return ItemStack.EMPTY;
		Direction facing = blockEntity.getBlockState().getValue(FACING);
		LazyOptional<IItemHandler> items = blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
		return items.map(h -> h.getStackInSlot(0)).orElse(ItemStack.EMPTY);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
		return this.onTileEntityUse(level, pos, be -> {
			if (!level.isClientSide) {
				be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, result.getDirection()).ifPresent(h -> {
					if (player.getItemInHand(hand).isEmpty() && !h.getStackInSlot(0).isEmpty()) {
						player.addItem(h.extractItem(0, 1, false));
						level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.NEUTRAL, 1.0f, 1.0f);
					} else if (h.getStackInSlot(0).isEmpty() && player.getItemInHand(hand).getItem() instanceof FuzeItem) {
						player.setItemInHand(hand, h.insertItem(0, player.getItemInHand(hand), false));
						level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.NEUTRAL, 1.0f, 1.0f);
					}
				});
				be.setChanged();
				be.sendData();
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		});
	}
	
}
