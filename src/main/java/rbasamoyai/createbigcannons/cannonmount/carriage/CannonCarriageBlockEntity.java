package rbasamoyai.createbigcannons.cannonmount.carriage;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.components.structureMovement.AssemblyException;
import com.simibubi.create.content.contraptions.components.structureMovement.IDisplayAssemblyExceptions;
import com.simibubi.create.foundation.tileEntity.SyncedTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.CBCBlocks;
import rbasamoyai.createbigcannons.CBCEntityTypes;
import rbasamoyai.createbigcannons.cannonmount.CannonMountBlockEntity;
import rbasamoyai.createbigcannons.cannonmount.MountedCannonContraption;
import rbasamoyai.createbigcannons.cannonmount.PitchOrientedContraptionEntity;

public class CannonCarriageBlockEntity extends SyncedTileEntity implements IDisplayAssemblyExceptions {

    private AssemblyException lastException = null;

    public CannonCarriageBlockEntity(BlockEntityType<? extends CannonCarriageBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void tryAssemble() {
        try {
            this.assemble();
            this.lastException = null;
        } catch (AssemblyException e) {
            this.lastException = e;
            this.sendData();
        }
    }

    protected void assemble() throws AssemblyException {
        if (!CBCBlocks.CANNON_CARRIAGE.has(this.getBlockState())) return;
        BlockPos assemblyPos = this.worldPosition.above();
        if (this.level.isOutsideBuildHeight(assemblyPos)) {
            throw CannonMountBlockEntity.cannonBlockOutsideOfWorld(assemblyPos);
        }

        Direction facing = this.getBlockState().getValue(CannonCarriageBlock.FACING);
        CannonCarriageEntity carriage = CBCEntityTypes.CANNON_CARRIAGE.create(this.level);
        carriage.setPos(Vec3.atCenterOf(this.getBlockPos()));
        carriage.setYRot(facing.toYRot());
        this.level.addFreshEntity(carriage);

        MountedCannonContraption mountedCannon = new MountedCannonContraption();
        if (mountedCannon.assemble(this.level, assemblyPos)) {
            if (mountedCannon.initialOrientation() != facing) {
                // TODO: throw error for incorrect orientation
                return;
            }
            mountedCannon.removeBlocksFromWorld(this.level, BlockPos.ZERO);
            PitchOrientedContraptionEntity contraptionEntity = PitchOrientedContraptionEntity.create(this.level, mountedCannon, facing);
            carriage.setCannonContraption(contraptionEntity);
            carriage.applyRotation();
            this.level.addFreshEntity(contraptionEntity);
        }

        this.setRemoved();
        this.level.removeBlock(this.worldPosition, false);

        AllSoundEvents.CONTRAPTION_ASSEMBLE.playOnServer(this.level, this.worldPosition);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        AssemblyException.write(tag, this.lastException);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.lastException = AssemblyException.read(tag);
    }

    @Override public AssemblyException getLastAssemblyException() { return this.lastException; }
}
