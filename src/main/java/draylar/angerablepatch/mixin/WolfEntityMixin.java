package draylar.angerablepatch.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WolfEntity.class)
public abstract class WolfEntityMixin extends MobEntity implements Angerable {

    private WolfEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    // what am I doing here? good question.
    // mixin tries to be smart and casts the world in this method to serverworld for pretty much any inject or redirect related to the offending line.
    // the previous line is an if statement, so you can't inject after (well, you probably can, but I don't know how)
    // collar color isn't saved in a spawner, so we check if that is the case, then run the angerable logic and cancel (this will break custom reading, but only in spawners, i think)
    // then if the color color could be set, we do custom check and cancel (and hope everyone put custom saving before this if statement)

    @Inject(
            method = "readCustomDataFromTag",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;contains(Ljava/lang/String;I)Z", shift = At.Shift.BEFORE),
            cancellable = true
    )
    private void preWorldCheckAngerFromTag(CompoundTag tag, CallbackInfo ci) {
        if (!tag.contains("CollarColor", 99)) {
            if(!this.world.isClient) {
                this.angerFromTag((ServerWorld) world, tag);
            }

            ci.cancel();
        }
    }

    @Inject(
            method = "readCustomDataFromTag",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/WolfEntity;setCollarColor(Lnet/minecraft/util/DyeColor;)V", shift = At.Shift.AFTER),
            cancellable = true
    )
    private void worldCheckAngerFromTag(CompoundTag tag, CallbackInfo ci) {
        if(!this.world.isClient) {
            this.angerFromTag((ServerWorld) world, tag);
        }

        ci.cancel();
    }
}
