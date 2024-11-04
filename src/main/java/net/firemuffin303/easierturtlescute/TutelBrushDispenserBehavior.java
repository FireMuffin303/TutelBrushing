package net.firemuffin303.easierturtlescute;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.List;

public class TutelBrushDispenserBehavior extends FallibleItemDispenserBehavior {

    @Override
    protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        ServerWorld serverWorld = pointer.getWorld();
        if(!serverWorld.isClient){
            BlockPos blockPos = pointer.getPos().offset((Direction)pointer.getBlockState().get(DispenserBlock.FACING));
            this.setSuccess(tryBrushingTurtle(serverWorld,blockPos));
            if(this.isSuccess() && stack.damage(1,serverWorld.getRandom(),null)){
                stack.setCount(0);
            }

        }
        return stack;
    }

    private static boolean tryBrushingTurtle(ServerWorld world, BlockPos pos){
        List<LivingEntity> list = world.getEntitiesByClass(LivingEntity.class, new Box(pos), EntityPredicates.EXCEPT_SPECTATOR);
        for(LivingEntity livingEntity : list){
            if(livingEntity instanceof TurtleEntity turtleEntity ){
                if(((TurtleAccessor)turtleEntity).easierTurtleScute$getCover()){
                    ((TurtleAccessor)turtleEntity).brushing(SoundCategory.BLOCKS);
                    return true;
                }
            }
        }
        return false;
    }
}