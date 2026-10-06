package com.hidetrainmap;

import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.locating.IModFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Refuses to start when another mod could show Create's rail network or trains on a map,
 * which would bypass the patches of this mod.
 */
final class ForbiddenModsCheck {

    /** Mods known to draw Create trains or tracks on a map, with the reason shown to the player. */
    private static final Map<String, String> KNOWN_MODS = Map.of(
            "journeymap", "JourneyMap (Create draws its train map on it)",
            "ftbchunks", "FTB Chunks (Create draws its train map on it)",
            "xaerotrainmap", "Xaero Train Map",
            "createtrackmap", "Create Track Map",
            "atlasrailways", "Antique Atlas: Create Train Networks",
            "sablexaeromaps", "Sable x Xaero's Maps (draws Create contraptions, including trains, on the maps)");

    /** Map mods: any other mod depending on one of them is scanned for Create train references. */
    private static final Set<String> MAP_MODS = Set.of(
            "xaeroworldmap", "xaerominimap", "xaerominimapfair", "xaeroworldmapfair",
            "journeymap", "ftbchunks", "antiqueatlas", "map_atlases", "voxelmap", "surveyor");

    /** Class constant-pool strings that only appear in code reading Create's rail network or trains. */
    private static final List<byte[]> TRAIN_REFERENCES = Stream.of(
                    "com/simibubi/create/content/trains/",
                    "com/simibubi/create/compat/trainmap/")
            .map(s -> s.getBytes(StandardCharsets.UTF_8))
            .toList();

    private ForbiddenModsCheck() {
    }

    static void run() {
        List<String> found = new ArrayList<>();
        Set<IModFile> scanned = new HashSet<>();

        for (IModInfo mod : ModList.get().getMods()) {
            String id = mod.getModId();
            String known = KNOWN_MODS.get(id);
            if (known != null) {
                found.add(known + " [" + id + "]");
                continue;
            }
            if (id.equals(HideTrainMap.MOD_ID) || id.equals("create") || MAP_MODS.contains(id)) {
                continue;
            }
            boolean dependsOnMap = mod.getDependencies().stream().anyMatch(dep -> MAP_MODS.contains(dep.getModId()));
            IModFile file = mod.getOwningFile().getFile();
            if (dependsOnMap && scanned.add(file) && referencesTrains(file)) {
                found.add(mod.getDisplayName() + " [" + id + "] (map addon reading Create's rail network)");
            }
        }

        if (!found.isEmpty()) {
            throw new IllegalStateException("[Hide Train Map] Forbidden mod(s) that can show Create trains or tracks on a map: "
                    + String.join(", ", found) + ". Remove them to play on this server.");
        }
    }

    private static boolean referencesTrains(IModFile file) {
        Path root = file.getSecureJar().getRootPath();
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.filter(p -> p.toString().endsWith(".class")).anyMatch(ForbiddenModsCheck::containsTrainReference);
        } catch (IOException e) {
            throw new UncheckedIOException("[Hide Train Map] Could not inspect " + file.getFileName(), e);
        }
    }

    private static boolean containsTrainReference(Path classFile) {
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(classFile);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        for (byte[] pattern : TRAIN_REFERENCES) {
            if (indexOf(bytes, pattern) >= 0) {
                return true;
            }
        }
        return false;
    }

    private static int indexOf(byte[] data, byte[] pattern) {
        outer:
        for (int i = 0; i <= data.length - pattern.length; i++) {
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
