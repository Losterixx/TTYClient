package dev.losterixx.ttyclient.mixin.accessors;

import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OptionInstance.class)
public interface OptionInstanceAccessor {

    @Accessor("value")
    Object getOptionValue();

    @Accessor("value")
    void setOptionValue(Object value);
}


