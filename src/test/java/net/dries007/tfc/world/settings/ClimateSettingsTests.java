package net.dries007.tfc.world.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import com.mojang.serialization.JsonOps;
import net.dries007.tfc.TestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClimateSettingsTests
{
    @BeforeAll
    public static void setup()
    {
        TestHelper.boostrap();
    }

    @Test
    public void testEncodeDecodePresets()
    {
        final ClimateSettings preset = ClimateSettings.DEFAULT_TEMPERATURE;
        final JsonElement encoded = ClimateSettings.CODEC.encodeStart(JsonOps.INSTANCE, preset).getOrThrow(false, Assertions::fail);
        final ClimateSettings decoded = ClimateSettings.CODEC.decode(JsonOps.INSTANCE, encoded).getOrThrow(false, Assertions::fail).getFirst();

        assertEquals(preset, decoded);
    }

    @Test
    public void testEncodeDecodeCustom()
    {
        final ClimateSettings custom = new ClimateSettings(1, 2, 3, 4, 5, false);
        final JsonElement encoded = ClimateSettings.CODEC.encodeStart(JsonOps.INSTANCE, custom).getOrThrow(false, Assertions::fail);
        final ClimateSettings decoded = ClimateSettings.CODEC.decode(JsonOps.INSTANCE, encoded).getOrThrow(false, Assertions::fail).getFirst();

        assertEquals(custom, decoded);
    }

    @Test
    public void testDecodePreset()
    {
        final JsonElement json = new JsonPrimitive("tfc:default_rainfall");
        final ClimateSettings decoded = ClimateSettings.CODEC.decode(JsonOps.INSTANCE, json).getOrThrow(false, Assertions::fail).getFirst();

        assertEquals(decoded, ClimateSettings.DEFAULT_RAINFALL);
    }

    @Test
    public void testDecodeCustom()
    {
        final JsonObject json = new JsonObject();

        json.addProperty("first_max", 1);
        json.addProperty("second_max", 2);
        json.addProperty("third_max", 3);
        json.addProperty("fourth_max", 4);
        json.addProperty("scale", 5);
        json.addProperty("endless_poles", false);

        final ClimateSettings decoded = ClimateSettings.CODEC.decode(JsonOps.INSTANCE, json).getOrThrow(false, Assertions::fail).getFirst();
        final ClimateSettings expected = new ClimateSettings(1, 2, 3, 4, 5, false);

        assertEquals(expected, decoded);
    }
}