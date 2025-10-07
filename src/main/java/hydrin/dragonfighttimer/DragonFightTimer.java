package hydrin.dragonfighttimer;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DragonFightTimer implements ModInitializer {
	public static final String MOD_ID = "dragonfighttimer";
    static boolean wasDragonAlive = false;
    static long startTime = 0;

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
        ClientTickEvents.END_CLIENT_TICK.register(DragonFightTimer::onEndClientTick);
        ClientReceiveMessageEvents.GAME.register(DragonFightTimer::onReceiveGameMessage);
	}

    public static void onEndClientTick(MinecraftClient client) {
        if (client.world != null) {
            if (!client.world.getEnderDragonParts().isEmpty()) {
                if (!wasDragonAlive) {
                    startTime = System.nanoTime();
                }
                wasDragonAlive = true;
            } else {
                wasDragonAlive = false;
            }
        }
    }

    public static void onReceiveGameMessage(Text message, boolean overlay) {
        if (message.getString().startsWith("[DragonFight] The dragon has been slain by ") && message.getString().endsWith("!")) {
            if (startTime > 0) {
                // It's highly unlikely a DragonFight will go into the hour mark

                double timeElapsed = System.nanoTime() - startTime;
                int minutesElapsed = (int) (timeElapsed / 60_000_000_000L);
                double secondsElapsed = ((timeElapsed / 1_000_000_000) % 60);

                if (minutesElapsed != 0) {
                    MinecraftClient.getInstance().inGameHud.getChatHud()
                            .addMessage(Text.literal("[").formatted(Formatting.DARK_GRAY)
                                    .append(Text.literal("D").formatted(Formatting.DARK_AQUA))
                                    .append(Text.literal("F").formatted(Formatting.GOLD))
                                    .append(Text.literal("Timer").formatted(Formatting.AQUA))
                                    .append(Text.literal("] ").formatted(Formatting.DARK_GRAY))
                                    .append(Text.literal("Time elapsed: ").formatted(Formatting.GOLD))
                                    .append(Text.literal(minutesElapsed + "m " + String.format("%.2f", secondsElapsed) + "s").formatted(Formatting.YELLOW))
                                    .append(Text.literal(".").formatted(Formatting.GOLD))
                            );
                } else {
                    MinecraftClient.getInstance().inGameHud.getChatHud()
                            .addMessage(Text.literal("[").formatted(Formatting.DARK_GRAY)
                                    .append(Text.literal("D").formatted(Formatting.DARK_AQUA))
                                    .append(Text.literal("F").formatted(Formatting.GOLD))
                                    .append(Text.literal("Timer").formatted(Formatting.AQUA))
                                    .append(Text.literal("] ").formatted(Formatting.DARK_GRAY))
                                    .append(Text.literal("Time elapsed: ").formatted(Formatting.GOLD))
                                    .append(Text.literal(String.format("%.2f", secondsElapsed) + "s").formatted(Formatting.YELLOW))
                                    .append(Text.literal(".").formatted(Formatting.GOLD))
                            );
                }

            } else {
                MinecraftClient.getInstance().inGameHud.getChatHud()
                        .addMessage(Text.literal("[").formatted(Formatting.DARK_GRAY)
                                .append(Text.literal("D").formatted(Formatting.DARK_AQUA))
                                .append(Text.literal("F").formatted(Formatting.GOLD))
                                .append(Text.literal("Timer").formatted(Formatting.AQUA))
                                .append(Text.literal("] ").formatted(Formatting.DARK_GRAY))
                                .append(Text.literal("Failed to time the fight.").formatted(Formatting.RED))
                        );
            }
        }
    }
}