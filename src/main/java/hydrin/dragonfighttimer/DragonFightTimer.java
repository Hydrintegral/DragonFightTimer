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
    static boolean crystalMessageFound = false;
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

    public static Text timerMessage(long start, int minutes, double seconds) {
        if (start == 0) {
            return Text.literal("[").formatted(Formatting.DARK_GRAY)
                    .append(Text.literal("D").formatted(Formatting.DARK_AQUA))
                    .append(Text.literal("F").formatted(Formatting.GOLD))
                    .append(Text.literal("Timer").formatted(Formatting.AQUA))
                    .append(Text.literal("] ").formatted(Formatting.DARK_GRAY))
                    .append(Text.literal("Failed to time the fight.").formatted(Formatting.RED));

        }

        if (minutes == 0) {
            return Text.literal("[").formatted(Formatting.DARK_GRAY)
                    .append(Text.literal("D").formatted(Formatting.DARK_AQUA))
                    .append(Text.literal("F").formatted(Formatting.GOLD))
                    .append(Text.literal("Timer").formatted(Formatting.AQUA))
                    .append(Text.literal("] ").formatted(Formatting.DARK_GRAY))
                    .append(Text.literal("Time elapsed: ").formatted(Formatting.GOLD))
                    .append(Text.literal(String.format("%05.2f", seconds) + "s").formatted(Formatting.YELLOW))
                    .append(Text.literal(".").formatted(Formatting.GOLD));
        } else {
            return Text.literal("[").formatted(Formatting.DARK_GRAY)
                    .append(Text.literal("D").formatted(Formatting.DARK_AQUA))
                    .append(Text.literal("F").formatted(Formatting.GOLD))
                    .append(Text.literal("Timer").formatted(Formatting.AQUA))
                    .append(Text.literal("] ").formatted(Formatting.DARK_GRAY))
                    .append(Text.literal("Time elapsed: ").formatted(Formatting.GOLD))
                    .append(Text.literal(minutes + "m " + String.format("%05.2f", seconds) + "s").formatted(Formatting.YELLOW))
                    .append(Text.literal(".").formatted(Formatting.GOLD));
        }
    }

    public static void onReceiveGameMessage(Text message, boolean overlay) {
        String msgString = message.getString();

        if (msgString.equals("[DragonFight] Dragon fight begins now: Place!")) {
            crystalMessageFound = true;
        }

        if (msgString.startsWith("[DragonFight] The dragon has been slain by ")) {
            // It's highly unlikely a DragonFight will go into the hour mark

            double timeElapsed = System.nanoTime() - startTime;
            int minutesElapsed = (int) (timeElapsed / 60_000_000_000L);
            double secondsElapsed = ((timeElapsed / 1_000_000_000) % 60);

            if (crystalMessageFound) {
                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(timerMessage(startTime, minutesElapsed, secondsElapsed));
                crystalMessageFound = false;
            } else {
                MinecraftClient.getInstance().inGameHud.getChatHud()
                        .addMessage(Text.literal("[").formatted(Formatting.DARK_GRAY)
                            .append(Text.literal("D").formatted(Formatting.DARK_AQUA))
                            .append(Text.literal("F").formatted(Formatting.GOLD))
                            .append(Text.literal("Timer").formatted(Formatting.AQUA))
                            .append(Text.literal("] ").formatted(Formatting.DARK_GRAY))
                            .append(Text.literal("Caution: The crystal place message was not recorded, so the following time may be inaccurate.").formatted(Formatting.RED))
                );
                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(timerMessage(startTime, minutesElapsed, secondsElapsed));
            }
        }
    }
}