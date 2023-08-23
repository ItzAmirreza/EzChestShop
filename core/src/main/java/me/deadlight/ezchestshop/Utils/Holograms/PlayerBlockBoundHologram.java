package me.deadlight.ezchestshop.Utils.Holograms;

import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.ASHologram;
import me.deadlight.ezchestshop.Utils.FloatingItem;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class PlayerBlockBoundHologram {

    private Player player;
    private BlockBoundHologram blockBoundHologram;
    private Location hologramLocation;
    private boolean isHologramReverse = false;

    // Replacements
    private HashMap<String, String> textReplacements;
    private HashMap<String, ItemStack> itemReplacements;
    private HashMap<String,  Boolean> conditionalTags;

    // Line indexes for replacements
    private HashMap<String,  List<Integer>> textReplacementLines = new HashMap<>();
    private HashMap<String,  List<Integer>> itemReplacementLines = new HashMap<>();
    private HashMap<String,  List<Integer>> conditionalTagLines = new HashMap<>();

    // Entities
    private HashMap<Integer, ASHologram> holograms = new HashMap<>();
    private HashMap<Integer, FloatingItem> items = new HashMap<>();
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

    public PlayerBlockBoundHologram(Player player, BlockBoundHologram blockBoundHologram,
                                    HashMap<String, String> textReplacements, HashMap<String, ItemStack> itemReplacements,
                                    HashMap<String,  Boolean> conditionalTags) {
        this.player = player;
        this.blockBoundHologram = blockBoundHologram;
        this.textReplacements = textReplacements;
        this.itemReplacements = itemReplacements;
        this.conditionalTags = conditionalTags;

        isHologramReverse = blockBoundHologram.getRotation() == BlockBoundHologram.HologramRotation.DOWN;

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

        if (blockBoundHologram.getLocation().getBlock().getType() == Material.AIR) {
            EzChestShop.logDebug("Shop block is in air, hiding it: " + blockBoundHologram.getLocation());
            blockBoundHologram.hideForAll();
            return;
        }

//        EzChestShop.logDebug("Showing hologram for " + player.getName() + " at " + blockBoundHologram.getLocation());
        // If the hologram is already spawned, do nothing
        if (!holograms.isEmpty() && !items.isEmpty()) {
            return;
        // If the texts are empty, but the items are not, remove the items.
        } else if (holograms.isEmpty() && !items.isEmpty()) {
            for (FloatingItem item : items.values()) {
                EzChestShop.logDebug("Already spawned: Removing item: " + item);
                Utils.onlinePackets.remove(item);
            }
            items.clear();
        // If the items are empty, but the texts are not, remove the texts.
        } else if (!holograms.isEmpty() && items.isEmpty()) {
            for (ASHologram hologram : holograms.values()) {
                EzChestShop.logDebug("Already spawned: Removing hologram: " + hologram);
                Utils.onlinePackets.remove(hologram);
            }
            holograms.clear();
        }
//        EzChestShop.logDebug("Showing hologram for " + player.getName() + " at " + blockBoundHologram.getLocation() + " (2)");

        /*
         Process the contents of the hologram
         */
        spawnHolograms(false);
        spawnItems();
        rearrangeHolograms();

        // Add the player to the hologram's inspector list
        blockBoundHologram.addInspector(player, this);
    }

    /**
     * Hide the hologram from a player.
     * <br>
     * This will remove the hologram from the player's view.
     */
    public void hide() {
//        EzChestShop.logDebug("Hiding hologram for " + player.getName() + " at " + blockBoundHologram.getLocation() + " Items: " + items.size() + " Holograms: " + holograms.size());
        for (ASHologram hologram : holograms.values()) {
            hologram.destroy();
            Utils.onlinePackets.remove(hologram);
        }
        for (FloatingItem item : items.values()) {
            item.destroy();
            Utils.onlinePackets.remove(item);
        }
        holograms.clear();
        items.clear();
        blockBoundHologram.removeViewer(player);
    }

    /**
     * Show only the item of the hologram.
     * <br>
     * This will remove the text of the hologram and only show the item.
     * <br>
     * If the hologram is already showing only the item, nothing will happen.
     */
    public void showOnlyItem() {
        if (!holograms.isEmpty()) {
            // only remove texts if they are not always visible
            List<Integer> toRemove = new ArrayList<>();
            holograms.forEach((key, value) -> {
                if (alwaysVisibleLines.contains(key))
                    return;
                value.destroy();
                Utils.onlinePackets.remove(value);
                toRemove.add(key);
            });
            toRemove.forEach(holograms::remove);
            emptyLines.clear();
        }
        if (items.isEmpty()) {
            spawnItems();
        }
        rearrangeHolograms();
        blockBoundHologram.removeInspector(player);
    }

    /**
     * Show the hologram texts. Items are spawned in if they don't already exist.
     */
    public void showTextAfterItem() {
        if (items.isEmpty()) {
            spawnItems();
        }
        spawnHolograms(false);
        rearrangeHolograms();
        blockBoundHologram.addInspector(player, this);
    }

    /**
     * Show the hologram texts that should always be visible (e.g. Empty Shop Info or Hologram Messages).
     * <br>
     * Items are spawned in if they don't already exist.
     */
    public void showAlwaysVisibleText() {
        if (items.isEmpty()) {
            spawnItems();
        }
        spawnHolograms(true);
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
        List<PlayerBlockBoundHologram> hologramList = new ArrayList<>();
        if (updateForAllPlayers) {
            hologramList = blockBoundHologram.getViewerHolograms();
        } else {
            hologramList.add(this);
        }

        for (PlayerBlockBoundHologram hologram : hologramList) {
            // Update the text replacement and query the lines that contain the placeholder
            hologram.textReplacements.put(key, replacement);
            hologram.queryReplacementLines();

            // Update the text replacements for all lines that contain the placeholder
            for (int line : hologram.textReplacementLines.get(key)) {
                String content = hologram.calculateLineContent(line);
                boolean emptyContent = content == null || content.trim().isEmpty() || content.equals("<empty/>");
                if (!hologram.holograms.containsKey(line)) {
                    if (emptyContent || !spawnIfNotExists) {
                        continue;
                    }
                    // spawn a new hologram
                    hologram.spawnTextLine(line);
                } else if (emptyContent) {
                    // remove the existing hologram
                    ASHologram hologram_text = hologram.holograms.get(line);
                    hologram_text.destroy();
                    Utils.onlinePackets.remove(hologram_text);
                    hologram.holograms.remove(line);
                } else {
                    // update the existing hologram
                    ASHologram hologram_text = hologram.holograms.get(line);
                    hologram_text.rename(content);
                }
            }
            hologram.rearrangeHolograms();
        }

    }

    /**
     * Update the text replacements for all placeholders.
     * <br>
     * This will update all text replacements and update the hologram accordingly.
     *
     * @param replacements The new replacements
     */
    public void updateTextReplacements(HashMap<String, String> replacements) {
        textReplacements = replacements;
        queryReplacementLines();

        hide();
        show();
    }

    /**
     * Update the item replacement for a placeholder. NOT LIKElY TO BE USED AND POTENTIALLY BROKEN
     * (check if hide removes marks this hologram as to be removed)
     * <br>
     * This will update the item replacement for a placeholder and update the
     * hologram accordingly.
     * <br>
     * The update will only change affected lines to reduce flickering.
     *
     * @param key         The placeholder to update
     * @param replacement The new replacement item
     */
    public void updateItemReplacement(String key, ItemStack replacement) {
        itemReplacements.put(key, replacement);
        queryReplacementLines();

        // Update the item replacements for all lines that contain the placeholder
        /*for (int line : itemReplacementLines.get(key)) {
            FloatingItem item = items.get(line);
            // Get the location of the hologram line
            Location itemLocation = item.getLocation();
            // Remove the old hologram
            Utils.onlinePackets.remove(item);
            item.destroy();

            // Spawn the new hologram - currently only supports one item per line
            FloatingItem newItem = new FloatingItem(player, replacement, itemLocation);
            Utils.onlinePackets.add(newItem);
            items.put(line, newItem);
        }*/
        hide();
        show();
    }

    /**
     * Update the item replacements for all placeholders.
     * <br>
     * This will update all item replacements and update the hologram accordingly.
     *
     * @param replacements The new replacements
     */
    public void updateItemReplacements(HashMap<String, ItemStack> replacements) {
        itemReplacements = replacements;
        queryReplacementLines();

        hide();
        show();
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

        List<PlayerBlockBoundHologram> hologramList = new ArrayList<>();
        if (updateForAllPlayers) {
            hologramList = blockBoundHologram.getViewerHolograms();
        } else {
            hologramList.add(this);
        }

        for (PlayerBlockBoundHologram hologram : hologramList) {
            // Update the text replacement and query the lines that contain the placeholder
            conditionalTags.put(key, value);
            queryReplacementLines();

            // Update the text replacements for all lines that contain the placeholder
            for (int line : hologram.conditionalTagLines.get(key)) {
                String content = hologram.calculateLineContent(line);
                boolean emptyContent = content == null || content.trim().isEmpty() || content.equals("<empty/>");
                if (!hologram.holograms.containsKey(line)) {
                    if (emptyContent) {
                        continue;
                    }
                    // spawn a new hologram
                    hologram.spawnTextLine(line);
                } else if (emptyContent) {
                    // remove the existing hologram
                    ASHologram hologram_text = hologram.holograms.get(line);
                    hologram_text.destroy();
                    Utils.onlinePackets.remove(hologram_text);
                    hologram.holograms.remove(line);
                } else {
                    EzChestShop.logDebug("Rename: " + content + " Line: " + line);
                    // update the existing hologram
                    ASHologram hologram_text = hologram.holograms.get(line);
                    hologram_text.rename(content);
                }
            }
            hologram.rearrangeHolograms();
        }
    }

    /**
     * Update the conditional tags for all placeholders.
     * <br>
     * This will update all conditional tags and update the hologram accordingly.
     *
     * @param tags The new tags
     */
    public void updateConditionalTags(HashMap<String, Boolean> tags) {
        conditionalTags = tags;
        queryReplacementLines();

        hide();
        show();
    }

    public void updatePosition() {;
        for (PlayerBlockBoundHologram hologram : blockBoundHologram.getViewerHolograms()) {
            hologram.rearrangeHolograms();
        }
    }

    /**
     * This method will return a reference to the block bound hologram.
     * @return The block bound hologram
     */
    public BlockBoundHologram getBlockHologram() {
        return blockBoundHologram;
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
     * <br>
     * <br>
     * @see #queryReplacementLines()
     */
    private void queryReplacementLinesIndividual(Set<String> keys, HashMap<String, List<Integer>> replacementLines, String replacement) {
        for (String key : keys) {
            List<Integer> lines = new ArrayList<>();
            for (int i = 0; i < blockBoundHologram.getContents().size(); i++) {
                if (blockBoundHologram.getContents().get(i).contains(key)) {
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

        ASHologram hologram = new ASHologram(player, content, blockBoundHologram.getHoloLoc(blockBoundHologram.getLocation().getBlock()));
        Utils.onlinePackets.add(hologram);
        holograms.put(line, hologram);
    }

    /**
     * Calculate the content of a hologram line.
     * @param line
     * @return
     */
    private String calculateLineContent(int line) {
        // Get the new line
        String newLine = blockBoundHologram.getContents().get(line);
        // Replace the placeholder with all the replacements
        for (String k : textReplacements.keySet()) {
            newLine = newLine.replace(k, textReplacements.get(k));
        }

        // Make sure the conditional tags are applied
        for (String condition : conditionalTags.keySet()) {
            if (!conditionalTagLines.containsKey(condition)) {
                continue;
            }
            boolean value = conditionalTags.get(condition);
            if (value) {
                // If the value is true, the text should be shown
                if (blockBoundHologram.getConditionalText(condition) != null) {
                    // If the text has a replacement, apply it
                    newLine = newLine.replaceAll("<" + condition + ">.*?<\\/" + condition + ">", blockBoundHologram.getConditionalText(condition));
                } else {
                    // Otherwise, just remove the start & ending tags
                    newLine = newLine.replaceAll("<\\/?" + condition + ">", "");
                }
            } else {
                // If the value is false, remove the text
                newLine = newLine.replaceAll("<" + condition + ">.*?<\\/" + condition + ">", "");
            }
        }

        // Apply color codes
        return Utils.colorify(newLine);
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
        List<Integer> lines = new ArrayList<>(holograms.keySet());
        lines.addAll(items.keySet());
        lines.addAll(emptyLines);
        lines.sort(Comparator.naturalOrder());

        // keep this variable updated...
        isHologramReverse = blockBoundHologram.getRotation() == BlockBoundHologram.HologramRotation.DOWN;

        Location spawnLocation = blockBoundHologram.getHoloLoc(blockBoundHologram.getLocation().getBlock());
        Location lineLocation = spawnLocation.clone().subtract(0, 0.1, 0);
        hologramLocation = lineLocation.clone();

        List<Integer> arrangedLines = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            int line = lines.get(i);
            boolean appliedSpacing = false;
            boolean alreadyAppliedSpace = arrangedLines.contains(line);
            if (holograms.containsKey(line)) {
                ASHologram hologram = holograms.get(line);
                // when the hologram is on the same line as an item, do not apply spacing
                if (!alreadyAppliedSpace) {
                    hologram.teleport(lineLocation);
                    if (!items.containsKey(line)) {
                        addOrSubtractLocationIfReverse(lineLocation, 0.3);
                        appliedSpacing = true;
                    }
                }
            }
            // always apply spacing for items as they are the largest
            if (items.containsKey(line)) {
                if (!alreadyAppliedSpace) {
                    addOrSubtractLocationIfReverse(lineLocation, 0.15);
                }
                FloatingItem item = items.get(line);
                if (!alreadyAppliedSpace) {
                    item.teleport(lineLocation);
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
    private void spawnItems() {
        List<String> processedContents = calculateProcessedContent(false);

        // Calculate the location of the hologram lines

        Location spawnLocation = blockBoundHologram.getHoloLoc(blockBoundHologram.getLocation().getBlock());

        for (int i = 0; i < processedContents.size(); i++) {
            String line = processedContents.get(i);
            if (line == null || line.isEmpty()) {
                continue;
            }
            boolean containsItem = false;
            // Check for item replacements
            for (String key : itemReplacements.keySet()) {
                if (line.contains(key)) {
                    containsItem = true;
                    break;
                }
            }
            // Calculate the location of the line
            if (containsItem) {
                // spawn an item - currently only supports one item per line,
                // everything else in the line will be ignored
                for (String key : itemReplacements.keySet()) {
                    ItemStack thatItem = itemReplacements.get(key);
                    if (line.contains(key)) {
                        FloatingItem floatingItem = new FloatingItem(player, thatItem, spawnLocation);
                        Utils.onlinePackets.add(floatingItem);
//                        EzChestShop.logDebug("Spawned item " + thatItem.getType().name() + " at " + spawnLocation);
                        // if multiple items are on the same line,
                        // this will break, but that is not supported anyway rn
                        items.put(i, floatingItem);
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
    private void spawnHolograms(boolean onlyAlwaysVisibleTexts) {
        List<String> processedContents = calculateProcessedContent(onlyAlwaysVisibleTexts);

        // Calculate the location of the hologram lines

        Location spawnLocation = blockBoundHologram.getHoloLoc(blockBoundHologram.getLocation().getBlock());

        for (int i = 0; i < processedContents.size(); i++) {
            String line = processedContents.get(i);
            if (line == null || line.isEmpty()) {
                continue;
            }
            // Check for item replacements
            for (String key : itemReplacements.keySet()) {
                if (line.contains(key)) {
                    line = line.replace(key, "");
                    break;
                }
            }
            if (line == null || line.trim().isEmpty()) {
                continue;
            }
            // Calculate the location of the line
            if (!line.equals("<empty/>")) {
                // if the line already exists (e.g. from a always visible line), do not add it again.
                // or if the hologram is visible through other means.
                if ((!onlyAlwaysVisibleTexts && alwaysVisibleLines.contains(i)) || holograms.containsKey(i))
                    continue;
                // add any line that is not defined as empty
                ASHologram hologram = new ASHologram(player, line, spawnLocation);
                Utils.onlinePackets.add(hologram);
                holograms.put(i, hologram);
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
        List<String> processedContents = new ArrayList<>(blockBoundHologram.getContents());

        //filter out all lines that are not always visible if onlyAlwaysVisibleTexts is true
        // getting the lines needs to happen before the replacements, or they will not be found
        if (onlyAlwaysVisibleTexts) {
            for (int i = 0; i < processedContents.size(); i++) {
                int finalI = i;
                // if there is any match in this line, it is always visible
                if (blockBoundHologram.alwaysVisibleTextReplacements.stream()
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
                    if (blockBoundHologram.getConditionalText(key) != null) {
                        // If the text has a replacement, apply it
                        processedContents.set(line, processedContents.get(line)
                                .replaceAll("<" + key + ">.*?<\\/" + key + ">", blockBoundHologram.getConditionalText(key)));
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
                .map((line) -> Utils.colorify(line)).collect(Collectors.toList());
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
