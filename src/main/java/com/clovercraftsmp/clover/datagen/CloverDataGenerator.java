package com.clovercraftsmp.clover.datagen;

import com.clovercraftsmp.clover.datagen.providers.StructureProvider;
import com.clovercraftsmp.clover.datagen.providers.StructureSetProvider;
import com.clovercraftsmp.clover.datagen.providers.TemplatePoolProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class CloverDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider(TemplatePoolProvider::new);
        pack.addProvider(StructureProvider::new);
        pack.addProvider(StructureSetProvider::new);
    }
}
