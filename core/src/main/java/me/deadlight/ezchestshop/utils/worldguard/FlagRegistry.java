package me.deadlight.ezchestshop.utils.worldguard;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;

public class FlagRegistry {

    // All the flags:

    public static StateFlag CREATE_SHOP;
    public static StateFlag CREATE_TRADE_SHOP;
    public static StateFlag CREATE_ADMIN_SHOP;
    public static StateFlag REMOVE_SHOP;
    public static StateFlag REMOVE_TRADE_SHOP;
    public static StateFlag REMOVE_ADMIN_SHOP;
    public static StateFlag USE_SHOP;
    public static StateFlag USE_TRADE_SHOP;
    public static StateFlag USE_ADMIN_SHOP;

    public static void onLoad() {
        CREATE_SHOP = registerStateFlag("ecs-create-shop", true);
        CREATE_TRADE_SHOP = registerStateFlag("ecs-create-trade-shop", true);
        CREATE_ADMIN_SHOP = registerStateFlag("ecs-create-admin-shop", true);

        REMOVE_SHOP = registerStateFlag("ecs-remove-shop", true);
        REMOVE_TRADE_SHOP = registerStateFlag("ecs-remove-trade-shop", true);
        REMOVE_ADMIN_SHOP = registerStateFlag("ecs-remove-admin-shop", true);

        USE_SHOP = registerStateFlag("ecs-use-shop", true);
        USE_TRADE_SHOP = registerStateFlag("ecs-use-trade-shop", true);
        USE_ADMIN_SHOP = registerStateFlag("ecs-use-admin-shop", true);
    }


    // register a Boolean based flag:
    private static StateFlag registerStateFlag(String name, boolean def) {
        com.sk89q.worldguard.protection.flags.registry.FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        Flag<?> existing = registry.get(name);
        if (existing != null) {
            return (StateFlag) existing;
        }

        try {
            StateFlag flag = new StateFlag(name, def);
            registry.register(flag);
            return flag;
        } catch (FlagConflictException e) {
            Flag<?> existing2 = registry.get(name);
            if (existing2 instanceof BooleanFlag) {
                return (StateFlag) existing2;
            }
        }
        //This will never run as there's a try catch above.
        return null;
    }
}
