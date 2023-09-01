package me.deadlight.ezchestshop.utils;

import me.deadlight.ezchestshop.data.LanguageManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

public class ItemUtils {


    private static LanguageManager lm = new LanguageManager();


    /**
     * Encode a ItemStack into a Base64 encoded String
     *
     * @param item The item to encode
     * @return The encoded item as a String
     */
    public static String encodeItem(ItemStack item) {
        try {
            ByteArrayOutputStream io = new ByteArrayOutputStream();
            BukkitObjectOutputStream os = new BukkitObjectOutputStream(io);

            os.writeObject(item);

            os.flush();
            byte[] rawData = io.toByteArray();

            String encodedData = Base64.getEncoder().encodeToString(rawData);

            os.close();
            return encodedData;

        } catch (IOException ex) {
            System.out.println(ex);
            return null;
        }
    }


    /**
     * Decode a ItemStack from Base64 into a ItemStack
     *
     * @param encodedItem The String encoded item
     * @return The decoded item
     */
    public static ItemStack decodeItem(String encodedItem) {

        byte[] rawData = Base64.getDecoder().decode(encodedItem);

        try {

            ByteArrayInputStream io = new ByteArrayInputStream(rawData);
            BukkitObjectInputStream in = new BukkitObjectInputStream(io);

            ItemStack thatItem = (ItemStack) in.readObject();

            in.close();

            return thatItem;

        } catch (IOException | ClassNotFoundException ex) {
            System.out.println(ex);
            return null;
        }

    }

    /**
     * Convert an Item to a Text Component. Used in Text Component Builders to show
     * items in chat.
     *
     * @param itemStack The item to convert
     * @return The item as a text component
     */
    public static String ItemToTextCompoundString(ItemStack itemStack) {
        return Utils.versionUtils.ItemToTextCompoundString(itemStack);
    }

    /**
     * Get the name of an item. If the item has a display name, it will return that.
     * If the item is an enchanted book, it will return the enchantment name as long as it has only one enchantment.
     * If not, it will return the name of the item type.
     *
     * @param item The item to get the name of
     * @return The name of the item
     */
    public static String getFinalItemName(ItemStack item) {
        String itemname;
        if (!item.hasItemMeta()) {
            if (item.getItemMeta().hasDisplayName()) {
                itemname = StringUtils.colorify(item.getItemMeta().getDisplayName());
            } else if (item.getType() == Material.ENCHANTED_BOOK
                    && ((EnchantmentStorageMeta) item.getItemMeta()).getStoredEnchants().size() == 1) {
                EnchantmentStorageMeta emeta = (EnchantmentStorageMeta) item.getItemMeta();

                Map.Entry<Enchantment,Integer> entry = emeta.getStoredEnchants().entrySet().iterator().next();
                itemname = lm.itemEnchantHologram(entry.getKey(), entry.getValue());
            } else if (item.getItemMeta().hasLocalizedName()) {
                itemname = item.getItemMeta().getLocalizedName();
            } else {
                itemname = StringUtils.capitalizeFirstSplit(item.getType().toString());
            }
        } else {
            itemname = StringUtils.capitalizeFirstSplit(item.getType().toString());
        }
        return StringUtils.colorify(itemname).trim();
    }

    /**
     * Check if two items are similar. This is an improved version of ItemStack.isSimilar(ItemStack) as it checks for
     * some special cases such as firework rockets, which have been known to cause issues.
     *
     * @param stack1 The first item
     * @param stack2 The second item
     * @return Whether the items are similar
     */
    public static boolean isSimilar(@Nullable ItemStack stack1, @Nullable ItemStack stack2) {
        if (stack1 == null || stack2 == null) {
            return false;
        } else if (stack1 == stack2) {
            return true;
        } else {
            // Check the problematic materials!
            if (stack1.getType() == Material.FIREWORK_ROCKET && stack2.getType() == Material.FIREWORK_ROCKET) {
                FireworkMeta meta1 = (FireworkMeta) stack1.getItemMeta();
                FireworkMeta meta2 = (FireworkMeta) stack2.getItemMeta();
                if (meta1 != null && meta2 != null) {
                    if (meta1.getEffects().size() != meta2.getEffects().size()) {
                        return false;
                    }
                    if (meta1.getPower() != meta2.getPower()) {
                        return false;
                    }
                    for (int i = 0; i < meta1.getEffects().size(); i++) {
                        if (!meta1.getEffects().get(i).equals(meta2.getEffects().get(i))) {
                            return false;
                        }
                    }
                    if (meta1.hasDisplayName() != meta2.hasDisplayName()) {
                        return false;
                    } else if (meta1.hasDisplayName()) {
                        if (!meta1.getDisplayName().equals(meta2.getDisplayName())) {
                            return false;
                        }
                    }

                    if (meta1.hasLore() != meta2.hasLore()) {
                        return false;
                    } else if (meta1.hasLore()) {
                        if (!meta1.getLore().equals(meta2.getLore())) {
                            return false;
                        }
                    }

                    if (meta1.hasCustomModelData() != meta2.hasCustomModelData()) {
                        return false;
                    } else if (meta1.hasCustomModelData()) {
                        if (meta1.getCustomModelData() != meta2.getCustomModelData()) {
                            return false;
                        }
                    }

                    if (meta1.hasEnchants() != meta2.hasEnchants()) {
                        return false;
                    } else if (meta1.hasEnchants()) {
                        if (!meta1.getEnchants().equals(meta2.getEnchants())) {
                            return false;
                        }
                    }

                    if (!meta1.getItemFlags().equals(meta2.getItemFlags())) {
                        return false;
                    }
                    if (meta1.getAttributeModifiers() != null) {
                        if (!meta1.getAttributeModifiers().equals(meta2.getAttributeModifiers())) {
                            return false;
                        }
                    } else if (meta2.getAttributeModifiers() != null) {
                        return false;
                    }

                    if (meta1.isUnbreakable() != meta2.isUnbreakable()) {
                        return false;
                    }
                }
                // if the item didn't return false, it's safe to assume it's similar.
                // some very, very special cases might not be covered, but it's unlikely and
                // guaranteed not a possibility in survival.
                return true;
            }
        }
        // If it's not a problematic (or checked) material, just use the default method.
        return stack1.isSimilar(stack2);
    }

}
