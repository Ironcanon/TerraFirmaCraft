package net.dries007.tfc.util.json;

import java.lang.reflect.Type;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;

import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.util.IResourceNameable;

public class GenericJsonReloadListener<T extends IResourceNameable> extends JsonReloadListener
{
    private static final Logger LOGGER = LogManager.getLogger();

    protected final BiMap<ResourceLocation, T> types;
    protected final BiMap<Integer, T> typeIds;

    protected final List<T> orderedTypes;
    private final List<Runnable> callbacks;
    private final Gson gson;
    private final Type resourceType;
    private final String typeName;

    public GenericJsonReloadListener(Gson gson, String domain, Type resourceType, String typeName)
    {
        super(gson, TerraFirmaCraft.MOD_ID + "/" + domain);

        this.types = HashBiMap.create();
        this.typeIds = HashBiMap.create();
        this.orderedTypes = new ArrayList<>();
        this.callbacks = new ArrayList<>();
        this.gson = gson;
        this.resourceType = resourceType;
        this.typeName = typeName;
    }

    @Nullable
    public T get(ResourceLocation id)
    {
        return types.get(id);
    }

    @Nullable
    public ResourceLocation getName(T type)
    {
        return types.inverse().get(type);
    }

    public int getId(T type)
    {
        return typeIds.inverse().get(type);
    }

    public T get(int id)
    {
        return typeIds.get(id);
    }

    @Nonnull
    public Set<T> getValues()
    {
        return types.values();
    }

    @Nonnull
    public Set<ResourceLocation> getKeys()
    {
        return types.keySet();
    }

    @Nonnull
    public List<T> getOrderedValues()
    {
        return orderedTypes;
    }

    public void addCallback(Runnable callback)
    {
        callbacks.add(callback);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonObject> resources, IResourceManager resourceManager, IProfiler profiler)
    {
        types.clear();
        orderedTypes.clear();
        SortedMap<ResourceLocation, T> sortedEntries = new TreeMap<>();
        for (Map.Entry<ResourceLocation, JsonObject> entry : resources.entrySet())
        {
            ResourceLocation name = entry.getKey();
            JsonObject json = entry.getValue();
            try
            {
                if (CraftingHelper.processConditions(json, "conditions"))
                {
                    T object = gson.fromJson(json, resourceType);
                    object.setId(name);
                    types.put(name, object);
                    sortedEntries.put(name, object);
                }
                else
                {
                    LOGGER.info("Skipping loading {} '{}' as it's conditions were not met", typeName, name);
                }
            }
            catch (IllegalArgumentException | JsonParseException e)
            {
                LOGGER.warn("{} '{}' failed to parse. This is most likely caused by incorrectly specified JSON.", typeName, name);
                LOGGER.warn("Error: ", e);
            }
        }

        LOGGER.info("Registered {} {}s Successfully.", types.size(), typeName);

        // Setup entry -> id map from sorted names
        int id = 0;
        for (ResourceLocation name : sortedEntries.keySet())
        {
            typeIds.put(id++, types.get(name));
        }

        postProcess();
    }

    /**
     * Here for subclasses to override
     */
    protected void postProcess()
    {
        for (Runnable callback : callbacks)
        {
            callback.run();
        }
    }
}