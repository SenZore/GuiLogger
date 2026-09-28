package dev.senzore.logrecord;

import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.commands.Commands;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import org.lwjgl.glfw.GLFW;

public final class LogRecordGameTest implements FabricClientGameTest, ClientModInitializer {
    private static final AtomicInteger SERVER_COMMANDS = new AtomicInteger();

    @Override public void onInitializeClient() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) ->
                dispatcher.register(Commands.literal("logrecord")
                        .then(Commands.literal("start").executes(command -> SERVER_COMMANDS.incrementAndGet()))
                        .then(Commands.literal("stop").executes(command -> SERVER_COMMANDS.incrementAndGet()))));
    }

    @Override public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            context.waitFor(client -> client.player != null && client.level != null);
            context.setScreen(() -> new ChatScreen("/logrecord start", false));
            context.getInput().pressKey(GLFW.GLFW_KEY_ENTER);
            context.waitFor(client -> LogRecordClient.isRecording());

            var contents = new SimpleContainer(27);
            var diamond = new ItemStack(Items.DIAMOND, 12);
            diamond.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("Buy price: $1,250"), Component.literal("Sell price: 900 coins"))));
            contents.setItem(0, diamond);
            context.setScreen(() -> {
                var client = net.minecraft.client.Minecraft.getInstance();
                return new ContainerScreen(ChestMenu.threeRows(12, client.player.getInventory(), contents),
                        client.player.getInventory(), Component.literal("Shop: Minerals"));
            });
            context.waitForScreen(ContainerScreen.class);
            context.waitFor(client -> LogRecordClient.isRecording() && Files.exists(LogRecordClient.lastPath().resolve("events/00000003.json")));
            context.waitTicks(4);
            context.clickScreenButton("Stop");
            context.waitFor(client -> !LogRecordClient.isRecording());
            try {
                var output = JsonParser.parseString(Files.readString(LogRecordClient.lastPath())).getAsJsonObject();
                var events = output.getAsJsonArray("events");
                boolean found = false;
                boolean renderedTitle = false;
                for (var event : events) {
                    var record = event.getAsJsonObject();
                    if (record.get("type").getAsString().equals("gui_text")
                            && record.getAsJsonObject("data").get("title_text").getAsString().equals("Shop: Minerals")) {
                        renderedTitle = record.getAsJsonObject("data").getAsJsonArray("lines").toString().contains("Shop: Minerals");
                    }
                    if (!record.get("type").getAsString().equals("menu_snapshot")) continue;
                    var data = record.getAsJsonObject("data");
                    if (!data.get("title_text").getAsString().equals("Shop: Minerals")) continue;
                    var item = data.getAsJsonArray("slots").get(0).getAsJsonObject().getAsJsonObject("item");
                    found = item.get("item_id").getAsString().equals("minecraft:diamond")
                            && item.get("count").getAsInt() == 12
                            && item.getAsJsonArray("price_text").toString().contains("$1,250")
                            && item.getAsJsonObject("components").has("minecraft:lore");
                }
                if (!found) throw new AssertionError("Shop item, price or component missing from JSON");
                if (!renderedTitle) throw new AssertionError("Rendered shop text missing from JSON");
            } catch (Exception error) { throw new AssertionError(error); }

            context.setScreen(() -> new ChatScreen("/logrecord start", false));
            context.getInput().pressKey(GLFW.GLFW_KEY_ENTER);
            context.waitFor(client -> LogRecordClient.isRecording());
            context.setScreen(() -> new ChatScreen("/logrecord stop", false));
            context.getInput().pressKey(GLFW.GLFW_KEY_ENTER);
            context.waitFor(client -> !LogRecordClient.isRecording());
            context.runOnClient(client -> client.getConnection().getConnection()
                    .send(new ServerboundChatCommandPacket("logrecord start")));
            context.waitTicks(3);
            if (SERVER_COMMANDS.get() != 0) throw new AssertionError("Recorder command reached the server");
        }
    }
}
