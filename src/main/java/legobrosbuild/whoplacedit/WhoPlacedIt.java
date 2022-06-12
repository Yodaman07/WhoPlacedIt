package legobrosbuild.whoplacedit;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class WhoPlacedIt implements ModInitializer {
    public static Logger LOGGER = Logger.getLogger("whoplacedit");
    public static HashMap<BlockPos, String> posData = new HashMap<>();
    public static boolean highlight_particles = false;
    @Override
    public void onInitialize() {
        //Todo: support fill and setblock cmds
        //Todo: make the particles less laggy
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> savePos(posData) ); //Unneeded

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            server.getWorlds().forEach(serverWorld -> {
                if (Objects.equals(server.getWorld(serverWorld.getRegistryKey()).toString(), serverWorld.toString())){
                    Pattern pattern = Pattern.compile("\\[(.*)\\]");
                    Matcher worldMatch = pattern.matcher(serverWorld.toString());
                    if (worldMatch.find()) loadPos(worldMatch.group(1));
                }
            });
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {

            dispatcher.register(literal("block-origin")
                    .then(argument("blockPos", BlockPosArgumentType.blockPos()).executes(context -> {
                        BlockPos pos = BlockPosArgumentType.getBlockPos(context, "blockPos");

                        String name = posData.getOrDefault(pos, "World Generation");

                        String result = pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "; Placed by: " + name;
                        context.getSource().getPlayer().sendMessage(Text.of(result), false);
                        return Command.SINGLE_SUCCESS;
                    })).then(literal("highlight-placed").executes(context -> {
                        ServerCommandSource source = context.getSource();
                        //Add a client side version with nice highlights
                        highlight_particles = !highlight_particles;
                        source.sendFeedback(Text.of(highlight_particles? "Highlighting Particles: On": "Highlighting Particles: Off"), true);
                        return Command.SINGLE_SUCCESS;
                    })));
        });
    }

    public static void savePos(HashMap<BlockPos, String> posData) {
        try {
            FileWriter fileWriter = new FileWriter("world/who-placed-it/posData.txt");
            posData.forEach((key, value) ->{
                try {
                    fileWriter.write(key.getX() + "," + key.getY() + "," + key.getZ() + ";" + value + "\n");
                } catch (IOException e) {
                    System.out.println("An error has occurred");
                    throw new RuntimeException(e);
                }
            });
            fileWriter.close();

        }catch (IOException e){
            System.out.println("An error has occurred");
            throw new RuntimeException(e);
        }
    }

    public void loadPos(String worldName){
        try {

            File file = new File(worldName + "/who-placed-it/posData.txt");

            if (!file.exists()){
                Path path = Paths.get(worldName + "/who-placed-it");
                Files.createDirectories(path);

                savePos(posData);

                LOGGER.config("Generating pos-data file");
            }

            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                List<Integer> posList = new ArrayList<>();
                String line = scanner.nextLine();
                Pattern nameP = Pattern.compile(";(.*)");
                Pattern posP = Pattern.compile("(-?\\d+)");
                Matcher nameM = nameP.matcher(line);
                Matcher posM = posP.matcher(line);


                while (posM.find()) {posList.add(Integer.parseInt(posM.group()));}

                if (nameM.find()) {
                    BlockPos pos = BlockPos.ORIGIN.add(posList.get(0), posList.get(1), posList.get(2));
                    posData.put(pos, nameM.group(1));
                }
            }

            LOGGER.config("Pos file has been successfully loaded");


        } catch (FileNotFoundException e){
            System.out.println("The requested file doesn't exist");
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.out.println("An error has occurred");
            throw new RuntimeException(e);
        }
    }

}
