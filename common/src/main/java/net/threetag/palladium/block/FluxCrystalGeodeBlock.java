package net.threetag.palladium.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class FluxCrystalGeodeBlock extends Block {

    private static final Direction[] DIRECTIONS = Direction.values();

    public FluxCrystalGeodeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel level, BlockPos pos, RandomSource randomSource) {
        if (randomSource.nextInt(5) == 0) {
            Direction direction = DIRECTIONS[randomSource.nextInt(DIRECTIONS.length)];
            BlockPos blockPos = pos.relative(direction);
            Block block = null;
            if (canClusterGrowAtState(blockState)) {
                block = PalladiumBlocks.SMALL_REDSTONE_FLUX_CRYSTAL_BUD.value();
            } else if (blockState.is(PalladiumBlocks.SMALL_REDSTONE_FLUX_CRYSTAL_BUD.value()) && blockState.getValue(AmethystClusterBlock.FACING) == direction) {
                block = PalladiumBlocks.MEDIUM_REDSTONE_FLUX_CRYSTAL_BUD.value();
            } else if (blockState.is(PalladiumBlocks.MEDIUM_REDSTONE_FLUX_CRYSTAL_BUD.value()) && blockState.getValue(AmethystClusterBlock.FACING) == direction) {
                block = PalladiumBlocks.LARGE_REDSTONE_FLUX_CRYSTAL_BUD.value();
            } else if (blockState.is(PalladiumBlocks.LARGE_REDSTONE_FLUX_CRYSTAL_BUD.value()) && blockState.getValue(AmethystClusterBlock.FACING) == direction) {
                block = PalladiumBlocks.REDSTONE_FLUX_CRYSTAL_CLUSTER.value();
            }

            if (block != null) {
                BlockState blockState2 = block.defaultBlockState().setValue(AmethystClusterBlock.FACING, direction).setValue(AmethystClusterBlock.WATERLOGGED, blockState.getFluidState().getType() == Fluids.WATER);
                level.setBlockAndUpdate(blockPos, blockState2);
            }

        }
    }

    public static boolean canClusterGrowAtState(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) && state.getFluidState().getAmount() == 8;
    }

}
