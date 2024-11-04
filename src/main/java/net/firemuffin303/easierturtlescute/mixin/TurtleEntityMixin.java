package net.firemuffin303.easierturtlescute.mixin;

import net.firemuffin303.easierturtlescute.TutelBrushingMod;
import net.firemuffin303.easierturtlescute.TurtleAccessor;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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

import java.util.Iterator;
import java.util.List;

@Mixin(TurtleEntity.class)
public abstract class TurtleEntityMixin extends AnimalEntity implements TurtleAccessor {
    @Unique private static final TrackedData<Boolean> IS_COVERED = DataTracker.registerData(TurtleEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    @Unique private static final TrackedData<Boolean> IS_WAXED = DataTracker.registerData(TurtleEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
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
                this.brushing(SoundCategory.PLAYERS);
                itemStack.damage(1,player,e -> e.sendToolBreakStatus(hand));

            }
            return ActionResult.success(this.getWorld().isClient);
        }else if(itemStack.isOf(Items.HONEYCOMB) && !this.isWaxed()){
            if(!this.getWorld().isClient){
                this.playSound(SoundEvents.ITEM_HONEYCOMB_WAX_ON,1.0f,1.0f);
                this.setWax(true);
                if(!player.isCreative()) {itemStack.decrement(1);}
                this.getWorld().syncWorldEvent(player, 3003, this.getBlockPos(), 0);
            }
            return ActionResult.success(this.getWorld().isClient);

        }else if (itemStack.getItem() instanceof AxeItem && this.isWaxed()){
            if(!this.getWorld().isClient){
                this.playSound(SoundEvents.ITEM_AXE_WAX_OFF,1.0f,1.0f);
                this.setWax(false);
                itemStack.damage(1,player,e -> e.sendToolBreakStatus(hand));
                this.getWorld().syncWorldEvent(player,3004,this.getBlockPos(),0);
            }
            return ActionResult.success(this.getWorld().isClient);
        }

        return super.interactMob(player, hand);
    }

    
    @Override
    public void tick() {
        super.tick();
        if(!this.getWorld().isClient){
            if(!this.isCovered() && this.isTouchingWater() && !this.isBaby() && !this.isWaxed()){
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
        this.dataTracker.startTracking(IS_WAXED,false);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void mod$writeCustomDataToNBT(NbtCompound nbt, CallbackInfo ci){
        nbt.putBoolean("IsCovered",this.isCovered());
        nbt.putInt("CoverTimer",this.coverTimer);
        nbt.putBoolean("IsWaxed",this.isWaxed());
    }

    @Inject(method = "readCustomDataFromNbt",at = @At("TAIL"))
    public void mod$readCustomDataToNBT(NbtCompound nbt, CallbackInfo ci){
        this.setCovered(nbt.getBoolean("IsCovered"));
        this.coverTimer = nbt.getInt("CoverTimer");
        this.setWax(nbt.getBoolean("IsWaxed"));
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

    @Unique
    public boolean isWaxed(){
        return this.dataTracker.get(IS_WAXED);
    }

    @Unique
    public void setWax(boolean value){
        this.dataTracker.set(IS_WAXED,value);
    }

    @Override
    public boolean easierTurtleScute$getCover() {
        return this.isCovered();
    }

    @Override
    public void brushing(SoundCategory category) {
        this.getWorld().playSoundFromEntity(null,this,SoundEvents.ITEM_BRUSH_BRUSHING_GENERIC,category,1.0f,1.0f);
        LootTable lootTable = this.getWorld().getServer().getLootManager().getLootTable(TutelBrushingMod.TURTLE_BRUSHING_GAMEPLAY);
        LootContextParameterSet lootContextParameterSet = (new LootContextParameterSet.Builder((ServerWorld)this.getWorld())).add(LootContextParameters.ORIGIN, this.getPos()).add(LootContextParameters.THIS_ENTITY, this).build(LootContextTypes.GIFT);
        List<ItemStack> list = lootTable.generateLoot(lootContextParameterSet);
        BlockPos blockPos = this.getBlockPos();
        for(ItemStack itemStack:list){
            this.getWorld().spawnEntity(
                    new ItemEntity(this.getWorld(),
                            blockPos.getX(),
                            blockPos.getY(),
                            blockPos.getZ(), itemStack));
        }
        this.setCovered(false);
        this.coverTimer = this.random.nextBetween(MIN_TIMER,MAX_TIMER);

    }
}
