package muramasa.antimatter.datagen.providers;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import muramasa.antimatter.AntimatterAPI;
import muramasa.antimatter.datagen.ExistingFileHelperOverride;
import muramasa.antimatter.datagen.IAntimatterProvider;
import muramasa.antimatter.datagen.resources.DynamicResourcePack;
import muramasa.antimatter.fluid.AntimatterFluid;
import muramasa.antimatter.util.TagUtils;
import net.minecraft.data.DataGenerator;
import net.minecraft.fluid.Fluid;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.data.ForgeFluidTagsProvider;

import java.util.HashMap;
import java.util.Map;

import static muramasa.antimatter.util.TagUtils.getForgeFluidTag;

public class AntimatterFluidTagProvider extends ForgeFluidTagsProvider implements IAntimatterProvider {

    private final String providerDomain, providerName;
    private final boolean replace;

    public Object2ObjectMap<ResourceLocation, JsonObject> TAGS = new Object2ObjectOpenHashMap<>();

    public AntimatterFluidTagProvider(String providerDomain, String providerName, boolean replace, DataGenerator gen,  ExistingFileHelperOverride fh) {
        super(gen, fh);
        this.providerDomain = providerDomain;
        this.providerName = providerName;
        this.replace = replace;
    }

    @Override
    public void run() {
        Map<ResourceLocation, ITag.Builder> b = new HashMap<>(this.tagToBuilder);
        this.tagToBuilder.clear();
        registerTags();
        TagUtils.getTags(Fluid.class).forEach((k,v)-> addTag("fluids",k, getOrCreateBuilder(v).getInternalBuilder()));
        tagToBuilder.forEach((k, v) -> addTag("fluids", k, v));
        b.forEach(tagToBuilder::put);
    }

    @Override
    public Types staticDynamic() {
        return Types.DYNAMIC;
    }


    @Override
    public Dist getSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void registerTags() {
        processTags(providerDomain);
    }

    protected void processTags(String domain) {
        AntimatterAPI.all(AntimatterFluid.class, domain).forEach(f ->
                getOrCreateBuilder(getForgeFluidTag(f.getId()))
                        .add(f.getFluid(), f.getFlowingFluid())
                        .replace(replace));
    }

    @Override
    public String getName() {
        return providerName;
    }

    // Must append 's' in the identifier
    public void addTag(String identifier, ResourceLocation loc, JsonObject obj) {
        this.TAGS.put(getTagLoc(identifier, loc), obj);
    }

    // Must append 's' in the identifier
    // Appends data to the tag.
    public void addTag(String identifier, ResourceLocation loc, ITag.Builder obj) {
        JsonObject json = TAGS.get(getTagLoc(identifier, loc));
        //if no tag just put this one in.
        if (json == null) {
            addTag(identifier, loc, obj.serialize());
        } else {
            obj = obj.deserialize(json, "Antimatter - Dynamic Data");
            TAGS.put(getTagLoc(identifier, loc), obj.serialize());
        }
    }
    public static ResourceLocation getTagLoc(String identifier, ResourceLocation tagId) {
        return new ResourceLocation(tagId.getNamespace(), String.join("", "tags/", identifier, "/", tagId.getPath(), ".json"));
    }

    @Override
    public void onCompletion() {
        TAGS.forEach(DynamicResourcePack::addTag);
    }
}
