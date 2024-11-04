package net.firemuffin303.easierturtlescute;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.DispenserBlock;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

public class TutelBrushingMod implements ModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MODID = "tutelbrushing";
    public static Identifier TURTLE_BRUSHING_GAMEPLAY = new Identifier(MODID,"gameplay/turtle_brushing");

    @Override
    public void onInitialize() {
        DispenserBlock.registerBehavior(Items.BRUSH,new TutelBrushDispenserBehavior());
    }
}
