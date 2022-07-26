package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.gui.GuiAddWaypoint;
import com.mamiyaotaru.voxelmap.gui.GuiSelectPlayer;
import com.mamiyaotaru.voxelmap.interfaces.AbstractVoxelMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandUtils {
    private static final String NEW_WAYPOINT_COMMAND = "/newWaypoint ";
    private static final int NEW_WAYPOINT_COMMAND_LENGTH = "/newWaypoint ".length();
    private static final String TELEPORT_COMMAND = "/ztp ";
    private static final int TELEPORT_COMMAND_LENGTH = "/ztp ".length();
    private static Random generator = new Random();
    public static Pattern pattern = Pattern.compile("\\[(\\w+\\s*:\\s*[-#]?[^\\[\\]]+)(,\\s*\\w+\\s*:\\s*[-#]?[^\\[\\]]+)+\\]", 2);

    public static boolean checkForWaypoints(Text chat, MessageIndicator indicator) {
        if (indicator != null && indicator.loggedName() != null && indicator.loggedName().equals("ModifiedbyVoxelMap")) {
            return true;
        }


        String message = chat.getString();
        ArrayList<String> waypointStrings = getWaypointStrings(message);
        if (waypointStrings.size() <= 0) {
            return true;
        } else {
            ArrayList<Text> textComponents = new ArrayList();
            int count = 0;

            for (String waypointString : waypointStrings) {
                int waypointStringLocation = message.indexOf(waypointString);
                if (waypointStringLocation > count) {
                    textComponents.add(Text.literal(message.substring(count, waypointStringLocation)));
                }

                MutableText clickableWaypoint = Text.literal(waypointString);
                Style chatStyle = clickableWaypoint.getStyle();
                chatStyle = chatStyle.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/newWaypoint " + waypointString.substring(1, waypointString.length() - 1)));
                chatStyle = chatStyle.withColor(Formatting.AQUA);
                Text hover = Text.literal(I18nUtils.getString("minimap.waypointshare.tooltip1") + "\n" + I18nUtils.getString("minimap.waypointshare.tooltip2"));
                chatStyle = chatStyle.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
                clickableWaypoint.setStyle(chatStyle);
                textComponents.add(clickableWaypoint);
                count = waypointStringLocation + waypointString.length();
            }

            if (count < message.length() - 1) {
                textComponents.add(Text.literal(message.substring(count, message.length())));
            }

            MutableText finalTextComponent = Text.literal("");

            for (Text textComponent : textComponents) {
                finalTextComponent.append(textComponent);
            }

            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(finalTextComponent, null, new MessageIndicator(Color.MAGENTA.getRGB(), MessageIndicator.Icon.CHAT_MODIFIED, null, "ModifiedbyVoxelMap"));
            return false;
        }
    }

    public static ArrayList<String> getWaypointStrings(String message) {
        ArrayList<String> list = new ArrayList<>();
        if (message.contains("[") && message.contains("]")) {
            Matcher matcher = pattern.matcher(message);

            while (matcher.find()) {
                String match = matcher.group();
                if (createWaypointFromChat(match.substring(1, match.length() - 1)) != null) {
                    list.add(match);
                }
            }
        }

        return list;
    }

    private static Waypoint createWaypointFromChat(String details) {
        Waypoint waypoint = null;
        String[] pairs = details.split(",");

        try {
            String name = "";
            Integer x = null;
            Integer z = null;
            int y = 64;
            boolean enabled = true;
            float red = generator.nextFloat();
            float green = generator.nextFloat();
            float blue = generator.nextFloat();
            String suffix = "";
            String world = "";
            TreeSet<DimensionContainer> dimensions = new TreeSet<>();

            for (String pair : pairs) {
                int splitIndex = pair.indexOf(":");
                if (splitIndex != -1) {
                    String key = pair.substring(0, splitIndex).toLowerCase().trim();
                    String value = pair.substring(splitIndex + 1).trim();
                    if (key.equals("name")) {
                        name = TextUtils.descrubName(value);
                    } else if (key.equals("x")) {
                        x = Integer.parseInt(value);
                    } else if (key.equals("z")) {
                        z = Integer.parseInt(value);
                    } else if (key.equals("y")) {
                        y = Integer.parseInt(value);
                    } else if (key.equals("enabled")) {
                        enabled = Boolean.parseBoolean(value);
                    } else if (key.equals("red")) {
                        red = Float.parseFloat(value);
                    } else if (key.equals("green")) {
                        green = Float.parseFloat(value);
                    } else if (key.equals("blue")) {
                        blue = Float.parseFloat(value);
                    } else if (key.equals("color")) {
                        int color = Integer.decode(value);
                        red = (float) (color >> 16 & 0xFF) / 255.0F;
                        green = (float) (color >> 8 & 0xFF) / 255.0F;
                        blue = (float) (color & 0xFF) / 255.0F;
                    } else if (!key.equals("suffix") && !key.equals("icon")) {
                        switch (key) {
                            case "world" -> world = TextUtils.descrubName(value);
                            case "dimensions" -> {
                                String[] dimensionStrings = value.split("#");
                                for (String dimensionString : dimensionStrings) {
                                    dimensions.add(AbstractVoxelMap.getInstance().getDimensionManager().getDimensionContainerByIdentifier(dimensionString));
                                }
                            }
                            case "dimension", "dim" ->
                                    dimensions.add(AbstractVoxelMap.getInstance().getDimensionManager().getDimensionContainerByIdentifier(value));
                        }
                    } else {
                        suffix = value;
                    }
                }
            }

            if (world.equals("")) {
                world = AbstractVoxelMap.getInstance().getWaypointManager().getCurrentSubworldDescriptor(false);
            }

            if (dimensions.size() == 0) {
                dimensions.add(AbstractVoxelMap.getInstance().getDimensionManager().getDimensionContainerByWorld(MinecraftClient.getInstance().world));
            }

            if (x != null && z != null) {
                if (dimensions.size() == 1 && (dimensions.first()).type.coordinateScale() != 1.0) {
                    double dimensionScale = (dimensions.first()).type.coordinateScale();
                    x = (int) ((double) x * dimensionScale);
                    z = (int) ((double) z * dimensionScale);
                }

                waypoint = new Waypoint(name, x, z, y, enabled, red, green, blue, suffix, world, dimensions);
            }
        } catch (NumberFormatException var20) {
            waypoint = null;
        }

        return waypoint;
    }

    public static void waypointClicked(String command) {
        boolean control = InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), InputUtil.fromTranslationKey("key.keyboard.left.control").getCode()) || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), InputUtil.fromTranslationKey("key.keyboard.right.control").getCode());
        String details = command.substring(NEW_WAYPOINT_COMMAND_LENGTH);
        Waypoint newWaypoint = createWaypointFromChat(details);
        if (newWaypoint != null) {
            for (Waypoint existingWaypoint : AbstractVoxelMap.getInstance().getWaypointManager().getWaypoints()) {
                if (newWaypoint.getX() == existingWaypoint.getX() && newWaypoint.getZ() == existingWaypoint.getZ()) {
                    if (control) {
                        MinecraftClient.getInstance().setScreen(new GuiAddWaypoint(null, AbstractVoxelMap.getInstance(), existingWaypoint, true));
                    } else {
                        AbstractVoxelMap.getInstance().getWaypointManager().setHighlightedWaypoint(existingWaypoint, false);
                    }

                    return;
                }
            }

            if (control) {
                MinecraftClient.getInstance().setScreen(new GuiAddWaypoint(null, AbstractVoxelMap.getInstance(), newWaypoint, false));
            } else {
                AbstractVoxelMap.getInstance().getWaypointManager().setHighlightedWaypoint(newWaypoint, false);
            }
        }

    }

    public static void sendWaypoint(Waypoint waypoint) {
        Identifier resourceLocation = AbstractVoxelMap.getInstance().getDimensionManager().getDimensionContainerByWorld(MinecraftClient.getInstance().world).resourceLocation;
        int color = ((int) (waypoint.red * 255.0F) & 0xFF) << 16 | ((int) (waypoint.green * 255.0F) & 0xFF) << 8 | (int) (waypoint.blue * 255.0F) & 0xFF;
        StringBuilder hexColor = new StringBuilder(Integer.toHexString(color));

        while (hexColor.length() < 6) {
            hexColor.insert(0, "0");
        }

        hexColor.insert(0, "#");
        String world = AbstractVoxelMap.getInstance().getWaypointManager().getCurrentSubworldDescriptor(false);
        if (waypoint.world != null && !waypoint.world.equals("")) {
            world = waypoint.world;
        }

        String suffix = waypoint.imageSuffix;
        Object[] args = new Object[]{TextUtils.scrubNameRegex(waypoint.name), waypoint.getX(), waypoint.getY(), waypoint.getZ(), resourceLocation.toString()};
        String message = String.format("[name:%s, x:%s, y:%s, z:%s, dim:%s", args);
        if (world != null && !world.equals("")) {
            message = message + ", world:" + world;
        }

        if (suffix != null && !suffix.equals("")) {
            message = message + ", icon:" + suffix;
        }

        message = message + "]";
        MinecraftClient.getInstance().setScreen(new GuiSelectPlayer((Screen) null, AbstractVoxelMap.getInstance(), message, true));
    }

    public static void sendCoordinate(int x, int y, int z) {
        String message = String.format("[x:%s, y:%s, z:%s]", x, y, z);
        MinecraftClient.getInstance().setScreen(new GuiSelectPlayer((Screen) null, AbstractVoxelMap.getInstance(), message, false));
    }

    public static void teleport(String command) {
        String details = command.substring(TELEPORT_COMMAND_LENGTH);

        for (Waypoint wp : AbstractVoxelMap.getInstance().getWaypointManager().getWaypoints()) {
            if (wp.name.equalsIgnoreCase(details) && wp.inDimension && wp.inWorld) {
                boolean mp = !MinecraftClient.getInstance().isIntegratedServerRunning();
                int y = wp.getY() > MinecraftClient.getInstance().world.getBottomY() ? wp.getY() : (!MinecraftClient.getInstance().player.world.getDimension().hasCeiling() ? MinecraftClient.getInstance().world.getTopY() : 64);
                MinecraftClient.getInstance().player.sendCommand("tp " + MinecraftClient.getInstance().player.getName().getString() + " " + wp.getX() + " " + y + " " + wp.getZ());
                if (mp) {
                    MinecraftClient.getInstance().player.sendCommand("tppos " + wp.getX() + " " + y + " " + wp.getZ());
                }

                return;
            }
        }

    }

    public static int getSafeHeight(int x, int y, int z, World worldObj) {
        boolean inNetherDimension = worldObj.getDimension().hasCeiling();
        BlockPos blockPos = new BlockPos(x, y, z);
        worldObj.getChunk(blockPos);
        worldObj.getChunkManager().getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4, ChunkStatus.FULL, true);
        if (inNetherDimension) {
            int safeY = -1;

            for (int t = 0; t < 127; ++t) {
                if (y + t < 127 && isBlockStandable(worldObj, x, y + t, z) && isBlockOpen(worldObj, x, y + t + 1, z) && isBlockOpen(worldObj, x, y + t + 2, z)) {
                    safeY = y + t + 1;
                    t = 128;
                }

                if (y - t > 0 && isBlockStandable(worldObj, x, y - t, z) && isBlockOpen(worldObj, x, y - t + 1, z) && isBlockOpen(worldObj, x, y - t + 2, z)) {
                    safeY = y - t + 1;
                    t = 128;
                }
            }

            y = safeY;
        } else if (y <= 0) {
            y = worldObj.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z) + 1;
        }

        return y;
    }

    private static boolean isBlockStandable(World worldObj, int par1, int par2, int par3) {
        BlockPos blockPos = new BlockPos(par1, par2, par3);
        BlockState blockState = worldObj.getBlockState(blockPos);
        Block block = blockState.getBlock();
        return block != null && blockState.getMaterial().blocksLight();
    }

    private static boolean isBlockOpen(World worldObj, int par1, int par2, int par3) {
        BlockPos blockPos = new BlockPos(par1, par2, par3);
        BlockState blockState = worldObj.getBlockState(blockPos);
        Block block = blockState.getBlock();
        return block == null || !blockState.shouldSuffocate(worldObj, blockPos);
    }
}
