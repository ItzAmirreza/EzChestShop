package me.deadlight.ezchestshop.utils.holograms;

import me.deadlight.ezchestshop.data.Config;
import me.deadlight.ezchestshop.utils.ASHologram;
import me.deadlight.ezchestshop.utils.FloatingItem;
import me.deadlight.ezchestshop.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is used to manage the hologram for a player for a specific shop. It is used to
 * show and hide the hologram, as well as update the contents of the hologram. 
 * <br>
 * While the primary task is to manage this single Hologram, some settings like the hologram position 
 * are shared with all other players that are viewing the hologram, so the BlockBoundHologram class 
 * is called to update all viewer holograms.
 */
public class PlayerBlockBoundHologram {

    private Player player;
    private BlockBoundHologram blockHolo;
    private boolean isHologramReverse = false;

    // Replacements
    private HashMap<String, String> textReplacements;
    private HashMap<String, ItemStack> itemReplacements;
    private HashMap<String,  Boolean> conditionalTags;

    // Line indexes for replacements
    private HashMap<String,  List<Integer>> textReplacementLines = new HashMap<>();
    private HashMap<String,  List<Integer>> itemReplacementLines = new HashMap<>();
    private HashMap<String,  List<Integer>> conditionalTagLines = new HashMap<>();

    // Entities mapped to lines
    private HashMap<Integer, ASHologram> lineTextHoloMap = new HashMap<>();
    private HashMap<Integer, FloatingItem> lineItemHoloMap = new HashMap<>();
    // Other special lines
    private List<Integer> emptyLines = new ArrayList<>();
    private List<Integer> alwaysVisibleLines = new ArrayList<>();





    /*
 ███████████             █████     ████   ███              ██████████              █████
░░███░░░░░███           ░░███     ░░███  ░░░              ░░███░░░░███            ░░███
 ░███    ░███ █████ ████ ░███████  ░███  ████   ██████     ░███   ░░███  ██████   ███████    ██████
 ░██████████ ░░███ ░███  ░███░░███ ░███ ░░███  ███░░███    ░███    ░███ ░░░░░███ ░░░███░    ░░░░░███
 ░███░░░░░░   ░███ ░███  ░███ ░███ ░███  ░███ ░███ ░░░     ░███    ░███  ███████   ░███      ███████
 ░███         ░███ ░███  ░███ ░███ ░███  ░███ ░███  ███    ░███    ███  ███░░███   ░███ ███ ███░░███
 █████        ░░████████ ████████  █████ █████░░██████     ██████████  ░░████████  ░░█████ ░░████████
░░░░░          ░░░░░░░░ ░░░░░░░░  ░░░░░ ░░░░░  ░░░░░░     ░░░░░░░░░░    ░░░░░░░░    ░░░░░   ░░░░░░░░
      */

    public PlayerBlockBoundHologram(Player player, BlockBoundHologram blockHolo,
                                    HashMap<String, String> textReplacements, HashMap<String, ItemStack> itemReplacements,
                                    HashMap<String,  Boolean> conditionalTags) {
        this.player = player;
        this.blockHolo = blockHolo;
        this.textReplacements = textReplacements;
        this.itemReplacements = itemReplacements;
        this.conditionalTags = conditionalTags;

        isHologramReverse = blockHolo.getRotation() == BlockBoundHologram.HologramRotation.DOWN;

        queryReplacementLines();
    }


    public HashMap<String, String> getTextReplacements() {
        return textReplacements;
    }

    public HashMap<String, ItemStack> getItemReplacements() {
        return itemReplacements;
    }

    /*
 ██████   ██████              █████  ███     ██████                 █████████   █████               █████
░░██████ ██████              ░░███  ░░░     ███░░███               ███░░░░░███ ░░███               ░░███
 ░███░█████░███   ██████   ███████  ████   ░███ ░░░  █████ ████   ░███    ░░░  ███████    ██████   ███████    ██████
 ░███░░███ ░███  ███░░███ ███░░███ ░░███  ███████   ░░███ ░███    ░░█████████ ░░░███░    ░░░░░███ ░░░███░    ███░░███
 ░███ ░░░  ░███ ░███ ░███░███ ░███  ░███ ░░░███░     ░███ ░███     ░░░░░░░░███  ░███      ███████   ░███    ░███████
 ░███      ░███ ░███ ░███░███ ░███  ░███   ░███      ░███ ░███     ███    ░███  ░███ ███ ███░░███   ░███ ███░███░░░
 █████     █████░░██████ ░░████████ █████  █████     ░░███████    ░░█████████   ░░█████ ░░████████  ░░█████ ░░██████
░░░░░     ░░░░░  ░░░░░░   ░░░░░░░░ ░░░░░  ░░░░░       ░░░░░███     ░░░░░░░░░     ░░░░░   ░░░░░░░░    ░░░░░   ░░░░░░
                                                      ███ ░███
                                                     ░░██████
                                                      ░░░░░░
     */


    /**
     * Show the hologram to a player.
     * <br>
     * This is the initial method to call to show the hologram to a player.
     * Only once spawned, updates can be made to the hologram.
     */
    public void show() {

        if (blockHolo.getLocation().getBlock().getType() == Material.AIR) {
            blockHolo.hideForAll();
            return;
        }

        // If the hologram is already spawned, do nothing
        if (!lineTextHoloMap.isEmpty() && !lineItemHoloMap.isEmpty()) {
            return;
        // If the texts are empty, but the items are not, remove the items.
        } else if (lineTextHoloMap.isEmpty() && !lineItemHoloMap.isEmpty()) {
            for (FloatingItem itemHologram : lineItemHoloMap.values()) {
                Utils.onlinePackets.remove(itemHologram);
            }
            lineItemHoloMap.clear();
        // If the items are empty, but the texts are not, remove the texts.
        } else if (!lineTextHoloMap.isEmpty() && lineItemHoloMap.isEmpty()) {
            for (ASHologram textHologram : lineTextHoloMap.values()) {
                Utils.onlinePackets.remove(textHologram);
            }
            lineTextHoloMap.clear();
        }

        /*
         Process the contents of the hologram
         */
        spawnTextHolograms(false);
        spawnItemHolograms();
        rearrangeHolograms();

        // Add the player to the hologram's inspector list
        blockHolo.addInspector(player, this);
    }

    /**
     * Hide the hologram from a player.
     * <br>
     * This will remove the hologram from the player's view.
     */
    public void hide() {
        for (ASHologram textHologram : lineTextHoloMap.values()) {
            textHologram.destroy();
            Utils.onlinePackets.remove(textHologram);
        }
        for (FloatingItem itemHologram : lineItemHoloMap.values()) {
            itemHologram.destroy();
            Utils.onlinePackets.remove(itemHologram);
        }
        lineTextHoloMap.clear();
        lineItemHoloMap.clear();
        blockHolo.removeViewer(player);
    }

    /**
     * Show only the item of the hologram.
     * <br>
     * This will remove the text (that is not always visible) of the hologram and only show the item.
     * <br>
     * If the hologram is already showing only the item, nothing will happen.
     */
    public void showOnlyItem() {
        if (!lineTextHoloMap.isEmpty()) {
            // only remove texts if they are not always visible
            List<Integer> toRemove = new ArrayList<>();
            lineTextHoloMap.forEach((key, value) -> {
                if (alwaysVisibleLines.contains(key))
                    return;
                value.destroy();
                Utils.onlinePackets.remove(value);
                toRemove.add(key);
            });
            toRemove.forEach(lineTextHoloMap::remove);
            emptyLines.clear();
        }
        if (lineItemHoloMap.isEmpty()) {
            spawnItemHolograms();
        }
        rearrangeHolograms();
        blockHolo.removeInspector(player);
    }

    /**
     * Show the hologram texts. Items are spawned in if they don't already exist.
     */
    public void showTextAfterItem() {
        if (lineItemHoloMap.isEmpty()) {
            spawnItemHolograms();
        }
        spawnTextHolograms(false);
        rearrangeHolograms();
        blockHolo.addInspector(player, this);
    }

    /**
     * Show the hologram texts that should always be visible (e.g. Empty Shop Info or Hologram Messages).
     * <br>
     * Items are spawned in if they don't already exist.
     */
    public void showAlwaysVisibleText() {
        if (lineItemHoloMap.isEmpty()) {
            spawnItemHolograms();
        }
        spawnTextHolograms(true);
        rearrangeHolograms();
    }

    /**
     * Update the text replacement for a placeholder.
     * <br>
     * This will update the text replacement for a placeholder and update the
     * hologram accordingly.
     * <br>
     * The update will only change affected lines to reduce flickering.
     *
     * @param key         The placeholder to update
     * @param replacement The new replacement text
     */
    public void updateTextReplacement(String key, String replacement, boolean updateForAllPlayers, boolean spawnIfNotExists) {
        List<PlayerBlockBoundHologram> playerHoloList = new ArrayList<>();
        if (updateForAllPlayers) {
            playerHoloList = blockHolo.getViewerHolograms();
        } else {
            playerHoloList.add(this);
        }

        for (PlayerBlockBoundHologram playerHolo : playerHoloList) {
            // Update the text replacement and query the lines that contain the placeholder
            playerHolo.textReplacements.put(key, replacement);
            playerHolo.queryReplacementLines();

            // Update the text replacements for all lines that contain the placeholder
            for (int line : playerHolo.textReplacementLines.get(key)) {
                String content = playerHolo.calculateLineContent(line);
                boolean emptyContent = content == null || content.trim().isEmpty() || content.equals("<empty/>");
                if (!playerHolo.lineTextHoloMap.containsKey(line)) {
                    if (emptyContent || !spawnIfNotExists) {
                        continue;
                    }
                    // spawn a new hologram
                    playerHolo.spawnTextLine(line);
                } else if (emptyContent) {
                    // remove the existing hologram
                    ASHologram textHologram = playerHolo.lineTextHoloMap.get(line);
                    textHologram.destroy();
                    Utils.onlinePackets.remove(textHologram);
                    playerHolo.lineTextHoloMap.remove(line);
                } else {
                    // update the existing hologram
                    ASHologram textHologram = playerHolo.lineTextHoloMap.get(line);
                    textHologram.rename(content);
                }
            }
            playerHolo.rearrangeHolograms();
        }

    }

    /**
     * Update the conditional tag for a placeholder.
     * <br>
     * This will update the conditional tag for a placeholder and update the
     * hologram accordingly.
     * <br>
     * The update will only change affected lines to reduce flickering.
     *
     * @param key   The conditional tag to update
     * @param value true if the tag should be shown, false if it should be hidden
     */
    public void updateConditionalTag(String key, boolean value, boolean updateForAllPlayers) {

        List<PlayerBlockBoundHologram> playerHoloList = new ArrayList<>();
        if (updateForAllPlayers) {
            playerHoloList = blockHolo.getViewerHolograms();
        } else {
            playerHoloList.add(this);
        }

        for (PlayerBlockBoundHologram playerHolo : playerHoloList) {
            // Update the text replacement and query the lines that contain the placeholder
            conditionalTags.put(key, value);
            queryReplacementLines();

            // Update the text replacements for all lines that contain the placeholder
            for (int line : playerHolo.conditionalTagLines.get(key)) {
                String content = playerHolo.calculateLineContent(line);
                boolean emptyContent = content == null || content.trim().isEmpty() || content.equals("<empty/>");
                if (!playerHolo.lineTextHoloMap.containsKey(line)) {
                    if (emptyContent) {
                        continue;
                    }
                    // spawn a new hologram
                    playerHolo.spawnTextLine(line);
                } else if (emptyContent) {
                    // remove the existing hologram
                    ASHologram textHologram = playerHolo.lineTextHoloMap.get(line);
                    textHologram.destroy();
                    Utils.onlinePackets.remove(textHologram);
                    playerHolo.lineTextHoloMap.remove(line);
                } else {
                    // update the existing hologram
                    ASHologram textHologram = playerHolo.lineTextHoloMap.get(line);
                    textHologram.rename(content);
                }
            }
            playerHolo.rearrangeHolograms();
        }
    }

    /**
     * Update the position of the hologram. This simply calls the rearrangeHolograms method for all viewers.
     */
    public void updatePosition() {;
        for (PlayerBlockBoundHologram playerHolo : blockHolo.getViewerHolograms()) {
            playerHolo.rearrangeHolograms();
        }
    }

    /**
     * This method will return a reference to the block bound hologram.
     * @return The block bound hologram
     */
    public BlockBoundHologram getBlockHologram() {
        return blockHolo;
    }

    public Player getPlayer() {
        return player;
    }

    /*
 █████   █████          ████
░░███   ░░███          ░░███
 ░███    ░███   ██████  ░███  ████████   ██████  ████████   █████
 ░███████████  ███░░███ ░███ ░░███░░███ ███░░███░░███░░███ ███░░
 ░███░░░░░███ ░███████  ░███  ░███ ░███░███████  ░███ ░░░ ░░█████
 ░███    ░███ ░███░░░   ░███  ░███ ░███░███░░░   ░███      ░░░░███
 █████   █████░░██████  █████ ░███████ ░░██████  █████     ██████
░░░░░   ░░░░░  ░░░░░░  ░░░░░  ░███░░░   ░░░░░░  ░░░░░     ░░░░░░
                              ░███
                              █████
                             ░░░░░
    */

    /**
     * Queries the lines that need to be replaced for each replacement.
     * <br>
     * <br>
     * This method is called when the replacements are updated or when the constructor is called.
     * <br>
     * <br>
     * <p>
     *     This is done by iterating through the contents and checking if the line contains the key
     *     If it does, the line number is added to the list of lines that need to be replaced
     *     This is done for both text and item replacements
     *     The results are stored in the textReplacementLines and itemReplacementLines HashMaps
     *     The key is the replacement key and the value is a list of line numbers that need to be replaced
     * </p>
     */
    private void queryReplacementLines() {

        queryReplacementLinesIndividual(textReplacements.keySet(), textReplacementLines, "");

        queryReplacementLinesIndividual(itemReplacements.keySet(), itemReplacementLines, "");

        // The conditional tags come without the < and >, as they also have a </> equivalent tag.
        // This is why we need to add them in, to check if the line contains the full tag.
        queryReplacementLinesIndividual(conditionalTags.keySet()
                .stream().map(s -> "<" + s + ">").collect(Collectors.toSet()), conditionalTagLines, "<|>");
    }

    /**
     * This method is only called by queryReplacementLines and is only used to reduce code duplication
     *
     * @param keys The set of keys that need to be queried
     * @param replacementLines The HashMap that will contain the results
     * @param replacement The replacement that needs to be removed from the key
     * <br>
     * <br>
     * @see #queryReplacementLines()
     */
    private void queryReplacementLinesIndividual(Set<String> keys, HashMap<String, List<Integer>> replacementLines, String replacement) {
        for (String key : keys) {
            List<Integer> lines = new ArrayList<>();
            for (int i = 0; i < blockHolo.getContents().size(); i++) {
                if (blockHolo.getContents().get(i).contains(key)) {
                    lines.add(i);
                }
            }
            replacementLines.put(key.replaceAll(replacement, ""), lines);
        }
    }

    /**
     * Spawn a line of the hologram. This is useful for update operations, but
     * does not cover destruction of holograms.
     * <br>
     * The content is calculated based on the line number.
     * @param line The line number
     */
    private void spawnTextLine(int line) {
        String content = calculateLineContent(line);

        ASHologram textHologram = new ASHologram(player, content, blockHolo.getHoloLoc(blockHolo.getLocation().getBlock()));
        Utils.onlinePackets.add(textHologram);
        lineTextHoloMap.put(line, textHologram);
    }

    /**
     * Calculate the content of a hologram line.
     * @param line The line number
     * @return The calculated content of the line
     */
    private String calculateLineContent(int line) {
        // Get the new line
        String lineContent = blockHolo.getContents().get(line);
        // Replace the placeholder with all the replacements
        for (String replacementKey : textReplacements.keySet()) {
            lineContent = lineContent.replace(replacementKey, textReplacements.get(replacementKey));
        }

        // Make sure the conditional tags are applied
        for (String conditionKey : conditionalTags.keySet()) {
            if (!conditionalTagLines.containsKey(conditionKey)) {
                continue;
            }
            boolean conditionalValue = conditionalTags.get(conditionKey);
            if (conditionalValue) {
                // If the value is true, the text should be shown
                if (blockHolo.getConditionalText(conditionKey) != null) {
                    // If the text has a replacement, apply it
                    lineContent = lineContent.replaceAll("<" + conditionKey + ">.*?<\\/" + conditionKey + ">", blockHolo.getConditionalText(conditionKey));
                } else {
                    // Otherwise, just remove the start & ending tags
                    lineContent = lineContent.replaceAll("<\\/?" + conditionKey + ">", "");
                }
            } else {
                // If the value is false, remove the text
                lineContent = lineContent.replaceAll("<" + conditionKey + ">.*?<\\/" + conditionKey + ">", "");
            }
        }

        // Apply color codes
        return StringUtils.colorify(lineContent);
    }

    /**
     * Rearrange the holograms and items.
     * <br>
     * This will rearrange the holograms and items to make sure they are in the
     * correct order.
     * <br>
     * This is done by calculating the location of each line and teleporting the
     * holograms and items to the correct location.
     * <br>
     * Call it after adding or removing holograms or items.
     */
    private void rearrangeHolograms() {
        List<Integer> lines = new ArrayList<>(lineTextHoloMap.keySet());
        lines.addAll(lineItemHoloMap.keySet());
        lines.addAll(emptyLines);
        lines.sort(Comparator.naturalOrder());

        // keep this variable updated...
        isHologramReverse = blockHolo.getRotation() == BlockBoundHologram.HologramRotation.DOWN;

        Location spawnLocation = blockHolo.getHoloLoc(blockHolo.getLocation().getBlock());
        Location lineLocation = spawnLocation.clone().subtract(0, 0.1, 0);

        List<Integer> arrangedLines = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            int line = lines.get(i);
            boolean appliedSpacing = false;
            boolean alreadyAppliedSpace = arrangedLines.contains(line);
            if (lineTextHoloMap.containsKey(line)) {
                ASHologram textHologram = lineTextHoloMap.get(line);
                // when the hologram is on the same line as an item, do not apply spacing
                if (!alreadyAppliedSpace) {
                    textHologram.teleport(lineLocation);
                    if (!lineItemHoloMap.containsKey(line)) {
                        addOrSubtractLocationIfReverse(lineLocation, 0.3);
                        appliedSpacing = true;
                    }
                }
            }
            // always apply spacing for items as they are the largest
            if (lineItemHoloMap.containsKey(line)) {
                if (!alreadyAppliedSpace) {
                    addOrSubtractLocationIfReverse(lineLocation, 0.15);
                }
                FloatingItem itemHologram = lineItemHoloMap.get(line);
                if (!alreadyAppliedSpace) {
                    itemHologram.teleport(lineLocation);
                    addOrSubtractLocationIfReverse(lineLocation, 0.35);
                }
                appliedSpacing = true;
            }
            // only apply empty lines if no spacing was applied yet
            if (emptyLines.contains(line) && !appliedSpacing && !alreadyAppliedSpace) {
                addOrSubtractLocationIfReverse(lineLocation, 0.3);
            }
            // When 2 elements are on the same line, only apply spacing once
            arrangedLines.add(line);
        }
    }

    /**
     * Spawn the items of the hologram.
     * <br>
     * This will spawn the items of the hologram.
     * <br>
     * This is done by iterating through the contents and checking if the line
     * contains an item placeholder.
     * <br>
     * If it does, the item is spawned at the correct location.
     */
    private void spawnItemHolograms() {
        List<String> processedContents = calculateProcessedContent(false);

        // Calculate the location of the hologram lines

        Location spawnLocation = blockHolo.getHoloLoc(blockHolo.getLocation().getBlock());

        for (int i = 0; i < processedContents.size(); i++) {
            String lineContent = processedContents.get(i);
            if (lineContent == null || lineContent.isEmpty()) {
                continue;
            }
            boolean containsItem = false;
            // Check for item replacements
            for (String key : itemReplacements.keySet()) {
                if (lineContent.contains(key)) {
                    containsItem = true;
                    break;
                }
            }
            // Calculate the location of the line
            if (containsItem) {
                // spawn an item - currently only supports one item per line,
                // everything else in the line will be ignored
                for (String key : itemReplacements.keySet()) {
                    ItemStack item = itemReplacements.get(key);
                    if (lineContent.contains(key)) {
                        FloatingItem itemHologram = new FloatingItem(player, item, spawnLocation);
                        Utils.onlinePackets.add(itemHologram);
                        // if multiple items are on the same line,
                        // this will break, but that is not supported anyway rn
                        lineItemHoloMap.put(i, itemHologram);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Spawn the text holograms of the hologram.
     * <br>
     * This will spawn the holograms of the hologram.
     * <br>
     * This is done by iterating through the contents and checking if the line
     * does not contain an item placeholder.
     * <br>
     * If it does not, the hologram is spawned at the correct location.
     */
    private void spawnTextHolograms(boolean onlyAlwaysVisibleTexts) {
        List<String> processedContents = calculateProcessedContent(onlyAlwaysVisibleTexts);

        // Calculate the location of the hologram lines

        Location spawnLocation = blockHolo.getHoloLoc(blockHolo.getLocation().getBlock());

        for (int i = 0; i < processedContents.size(); i++) {
            String lineContent = processedContents.get(i);
            if (lineContent == null || lineContent.isEmpty()) {
                continue;
            }
            // Check for item replacements
            for (String key : itemReplacements.keySet()) {
                if (lineContent.contains(key)) {
                    lineContent = lineContent.replace(key, "");
                    break;
                }
            }
            if (lineContent == null || lineContent.trim().isEmpty()) {
                continue;
            }
            // Calculate the location of the line
            if (!lineContent.equals("<empty/>")) {
                // if the line already exists (e.g. from a always visible line), do not add it again.
                // or if the hologram is visible through other means.
                if ((!onlyAlwaysVisibleTexts && alwaysVisibleLines.contains(i)) || lineTextHoloMap.containsKey(i))
                    continue;
                // add any line that is not defined as empty
                ASHologram textHologram = new ASHologram(player, lineContent, spawnLocation);
                Utils.onlinePackets.add(textHologram);
                lineTextHoloMap.put(i, textHologram);
            } else {
                // add an empty line
                emptyLines.add(i);
            }
        }
    }

    /**
     * Calculate the processed contents of the hologram.
     * <br>
     * The processed contents are the contents of the hologram with all
     * replacements, colorifications, reversals and conditional tags applied.
     *
     * @return The processed contents
     */
    private List<String> calculateProcessedContent(boolean onlyAlwaysVisibleTexts) {
        List<String> processedContents = new ArrayList<>(blockHolo.getContents());

        //filter out all lines that are not always visible if onlyAlwaysVisibleTexts is true
        // getting the lines needs to happen before the replacements, or they will not be found
        if (onlyAlwaysVisibleTexts) {
            for (int i = 0; i < processedContents.size(); i++) {
                int finalI = i;
                // if there is any match in this line, it is always visible
                if (blockHolo.alwaysVisibleTextReplacements.stream()
                        .anyMatch(visibleReplacement -> processedContents.get(finalI).contains(visibleReplacement))) {
                    alwaysVisibleLines.add(i);
                } else {
                    processedContents.set(i, "");
                }
            }
        }

        // Process the text replacements
        for (String key : textReplacements.keySet()) {
            String replacement = textReplacements.get(key);
            for (int line : textReplacementLines.get(key)) {
                // replace the placeholder with the replacement text
                processedContents.set(line, processedContents.get(line).replace(key, replacement));
            }
        }
        // Process the conditional tags
        for (String key : conditionalTags.keySet()) {
            if (!conditionalTagLines.containsKey(key)) {
                continue;
            }
            boolean value = conditionalTags.get(key);
            for (int line : conditionalTagLines.get(key)) {
                // if the value is true, remove the tag, but keep the inner contents
                // otherwise, remove the entire tag and its contents
                if (value) {
                    // If the value is true, the text should be shown
                    if (blockHolo.getConditionalText(key) != null) {
                        // If the text has a replacement, apply it
                        processedContents.set(line, processedContents.get(line)
                                .replaceAll("<" + key + ">.*?<\\/" + key + ">", blockHolo.getConditionalText(key)));
                    } else {
                        // Otherwise, just remove the start & ending tags
                        processedContents.set(line, processedContents.get(line)
                                .replaceAll("<\\/?" + key + ">", ""));
                    }
                } else {
                    // If the value is false, remove the text
                    processedContents.set(line, processedContents.get(line)
                            .replaceAll("<" + key + ">.*?<\\/" + key + ">", ""));
                }
            }
        }

        // Apply color codes
        return processedContents.stream()
                .map((line) -> StringUtils.colorify(line)).collect(Collectors.toList());
    }

    /**
     * Add amount to the y coordinate of the location if the hologram.
     * <br>
     * If the hologram is reversed, the amount will be subtracted instead.
     * @param location The location to modify - the original location will be modified
     * @param amount The amount to add or subtract - Config.holo_linespacing * amount applies.
     */
    private void addOrSubtractLocationIfReverse(Location location, double amount) {
        if (isHologramReverse) {
            location.subtract(0, amount * Config.holo_linespacing, 0);
        } else {
            location.add(0, amount * Config.holo_linespacing, 0);
        }
    }

}
