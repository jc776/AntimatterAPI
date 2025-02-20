package muramasa.antimatter.proxy;

import muramasa.antimatter.AntimatterAPI;
import muramasa.antimatter.capability.AntimatterCaps;
import muramasa.antimatter.datagen.providers.AntimatterItemTagProvider;
import muramasa.antimatter.datagen.resources.DynamicResourcePack;
import muramasa.antimatter.recipe.loader.IRecipeRegistrate;
import muramasa.antimatter.recipe.map.RecipeMap;
import muramasa.antimatter.util.TagUtils;
import muramasa.antimatter.worldgen.AntimatterWorldGenerator;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CommonHandler implements IProxyHandler {

    public CommonHandler() { }

    @SuppressWarnings("unused")
    public static void setup(FMLCommonSetupEvent e) {
        AntimatterCaps.register();
        AntimatterWorldGenerator.setup();
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, CommonHandler::reload);
    }

    public static void reload(AddReloadListenerEvent ev) {
        ev.addListener(new ReloadListener<Void>() {
            @Override
            protected Void prepare(IResourceManager resourceManagerIn, IProfiler profilerIn) {
                return null;
            }
            @Override
            protected void apply(Void objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn) {
                AntimatterAPI.onRecipeCompile(ev.getDataPackRegistries().getRecipeManager(), TagCollectionManager.getManager().getItemTags()::getOwningTags);
            }
        });
    }

    @Override
    public World getClientWorld() {
        return null;
    }

    @Override
    public PlayerEntity getClientPlayer() {
        return null;
    }
}
