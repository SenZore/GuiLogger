package dev.senzore.logrecord;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class ItemCapture {
    private static final Pattern PRICE = Pattern.compile("(?i)(price|buy|sell|cost|balance|coins?|money|[$€£¥₹])");
    private ItemCapture() {}

    public static JsonObject item(ItemStack stack) {
        JsonObject json = new JsonObject();
        json.addProperty("empty", stack.isEmpty());
        if (stack.isEmpty()) return json;
        Minecraft client = Minecraft.getInstance();
        var ops = client.level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        json.addProperty("item_id", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        json.addProperty("count", stack.getCount());
        json.add("name", component(stack.getHoverName()));
        putResult(json, "stack", ItemStack.CODEC.encodeStart(ops, stack));
        JsonObject components = new JsonObject();
        for (var entry : stack.getComponents()) {
            String key = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(entry.type()).toString();
            try {
                putResult(components, key, entry.encodeValue(ops));
            } catch (RuntimeException error) {
                components.addProperty(key + "_error", error.toString());
                components.addProperty(key + "_fallback", String.valueOf(entry.value()));
            }
        }
        json.add("components", components);
        try {
            var context = Item.TooltipContext.of(client.level);
            var tooltip = stack.getTooltipLines(context, client.player, TooltipFlag.NORMAL);
            json.add("tooltip", lines(tooltip));
            json.add("advanced_tooltip", lines(stack.getTooltipLines(context, client.player, TooltipFlag.ADVANCED)));
            JsonArray priceText = new JsonArray();
            for (Component line : tooltip) {
                if (PRICE.matcher(line.getString()).find()) priceText.add(line.getString());
            }
            json.add("price_text", priceText);
        } catch (RuntimeException error) {
            json.addProperty("tooltip_error", error.toString());
        }
        return json;
    }

    public static JsonObject component(Component component) {
        JsonObject result = new JsonObject();
        result.addProperty("text", component.getString());
        Minecraft client = Minecraft.getInstance();
        var ops = client.level == null ? JsonOps.INSTANCE
                : client.level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        putResult(result, "formatted", ComponentSerialization.CODEC.encodeStart(ops, component));
        return result;
    }

    private static JsonArray lines(List<Component> lines) {
        JsonArray result = new JsonArray();
        lines.forEach(line -> result.add(component(line)));
        return result;
    }

    private static void putResult(JsonObject object, String key, DataResult<JsonElement> result) {
        result.result().ifPresent(value -> object.add(key, value));
        result.error().ifPresent(error -> object.addProperty(key + "_error", error.message()));
    }
}
