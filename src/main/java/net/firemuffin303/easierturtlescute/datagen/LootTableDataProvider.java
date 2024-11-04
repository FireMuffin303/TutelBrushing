package net.firemuffin303.easierturtlescute.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.firemuffin303.easierturtlescute.TutelBrushingMod;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.util.Identifier;

import java.util.function.BiConsumer;

public class LootTableDataProvider extends SimpleFabricLootTableProvider {
    public LootTableDataProvider(FabricDataOutput output) {
        super(output, LootContextTypes.GIFT);
    }

    @Override
    public void accept(BiConsumer<Identifier, LootTable.Builder> exporter) {
        exporter.accept(TutelBrushingMod.TURTLE_BRUSHING_GAMEPLAY,
                LootTable.builder().pool(
                        LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f))
                                .with(ItemEntry.builder(Items.SCUTE))));
    }
}
