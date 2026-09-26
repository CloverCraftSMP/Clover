package com.clovercraftsmp.clover.datagen;
//? if <=1.21.1 {
/*import com.clovercraftsmp.clover.datagen.providers.RecipeProvider;
import com.clovercraftsmp.clover.datagen.providers.StructureProvider;
import com.clovercraftsmp.clover.structure.ModStructures;
import com.clovercraftsmp.clover.util.ItemStackUtil;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;

public class CloverDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider((output, registriesFuture) -> {
            ItemStackUtil.onEnchantmentRegistryReady(registriesFuture.join());
            return new RecipeProvider(output, registriesFuture);
        });
        pack.addProvider(StructureProvider::new);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        registryBuilder.add(Registries.TEMPLATE_POOL, ModStructures::bootstrapTemplatePools);
        registryBuilder.add(Registries.STRUCTURE, ModStructures::bootstrapStructures);
        registryBuilder.add(Registries.STRUCTURE_SET, ModStructures::bootstrapStructureSets);
    }
}
*///?}