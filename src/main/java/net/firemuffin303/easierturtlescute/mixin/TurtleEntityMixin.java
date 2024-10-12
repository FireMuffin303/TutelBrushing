package net.firemuffin303.easierturtlescute.mixin;

import net.firemuffin303.easierturtlescute.Easierturtlescute;
import net.firemuffin303.easierturtlescute.TurtleAccessor;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.GameRules;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TurtleEntity.class)
public abstract class TurtleEntityMixin extends AnimalEntity implements TurtleAccessor {
    @Unique private static final TrackedData<Boolean> IS_COVERED = DataTracker.registerData(TurtleEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    @Unique private int coverTimer = 0;
    @Unique private final int  MIN_TIMER = 4800;
    @Unique private final int MAX_TIMER = 12000;


    protected TurtleEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);

        if(this.isCovered() && itemStack.isOf(Items.BRUSH) && !this.isBaby() && this.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_LOOT)){
            if(!this.getWorld().isClient){
                this.playSound(SoundEvents.ITEM_BRUSH_BRUSHING_GENERIC,1.0f,1.0f);
                this.playSound(SoundEvents.ENTITY_ITEM_PICKUP,1.0f,1.0f);
                this.dropItem(Items.SCUTE,1);
                this.setCovered(false);
                this.coverTimer = this.random.nextBetween(MIN_TIMER,MAX_TIMER);
            }
            return ActionResult.success(this.getWorld().isClient);
        }

        return super.interactMob(player, hand);
    }

    @Override
    public void tick() {
        super.tick();
        if(!this.getWorld().isClient){
            if(!this.isCovered() && this.isTouchingWater() && !this.isBaby()){
                if(this.coverTimer-- <= 0){
                    this.setCovered(true);
                    this.playSound(SoundEvents.ENTITY_TURTLE_EGG_CRACK,1.0f,1.0f);
                }
            }
        }

    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    public void mod$initDataTracker(CallbackInfo ci){
        this.dataTracker.startTracking(IS_COVERED,false);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void mod$writeCustomDataToNBT(NbtCompound nbt, CallbackInfo ci){
        nbt.putBoolean("IsCovered",this.isCovered());
        nbt.putInt("CoverTimer",this.coverTimer);
    }

    @Inject(method = "readCustomDataFromNbt",at = @At("TAIL"))
    public void mod$readCustomDataToNBT(NbtCompound nbt, CallbackInfo ci){
        this.setCovered(nbt.getBoolean("IsCovered"));
        this.coverTimer = nbt.getInt("CoverTimer");
    }

    @Inject(method = "onGrowUp", at = @At("TAIL"))
    public void mod$onGrowUp(CallbackInfo ci){
        this.coverTimer = this.random.nextBetween(MIN_TIMER,MAX_TIMER);
    }

    @Inject(method = "initialize",at = @At("TAIL"))
    public void easierTurtleScute$initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData, NbtCompound entityNbt, CallbackInfoReturnable<EntityData> cir){
        if(!this.isBaby()){
            this.coverTimer = this.random.nextBetween(MIN_TIMER,MAX_TIMER);
        }
    }

    @Unique
    public boolean isCovered(){
        return this.dataTracker.get(IS_COVERED);
    }

    @Unique
    public void setCovered(boolean value){
        this.dataTracker.set(IS_COVERED,value);
    }

    @Override
    public boolean easierTurtleScute$getCover() {
        return this.isCovered();
    }
}
