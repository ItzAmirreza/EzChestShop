package me.deadlight.ezchestshop.Utils;

import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.Exceptions.CommandFetchException;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.SimplePluginManager;

import javax.inject.Singleton;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

@Singleton
public class CommandRegister {

    private static final Field COMMAND_MAP_FIELD;

    static {
        try {
            COMMAND_MAP_FIELD = SimplePluginManager.class.getDeclaredField("commandMap");
            COMMAND_MAP_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public boolean registerCommandAlias(Command command, String alias) throws CommandFetchException{
        // Check if the provided command exists.
        if (command == null) return false;

        // Hack into Bukkit and get the SimpleCommandMap and it's knownCommands...
        Map<String, Command> knownCommands = this.getKnownCommands();

        // ... check if it's even registered ...
        if (!knownCommands.containsKey(command.getName())) return false;

        // First replace it the normal way.
        knownCommands.put(alias, command);

        int aliases = 0;

        command.setAliases(Arrays.asList(alias));
        return true;
    }

    // -------------------------------------------- //
    // PRIVATE
    // -------------------------------------------- //

    private SimpleCommandMap getSimpleCommandMap() throws CommandFetchException {
        try {
            return (SimpleCommandMap) getCommandMap(EzChestShop.getPlugin().getServer());
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommandFetchException("commandMap");
        }
    }

    private Map<String, Command> getKnownCommands() throws CommandFetchException {
        try {
            SimpleCommandMap commandMap = this.getSimpleCommandMap();
            Field field = this.getServerField("knownCommands", SimpleCommandMap.class);
            this.makeAccessible(field);
            return (Map<String, Command>) field.get(commandMap);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new CommandFetchException("knownCommands");
        }
    }

    private void makeAccessible(Field field) {
        field.setAccessible(true);
    }

    public static CommandMap getCommandMap(Server server) {
        try {
            return (CommandMap) COMMAND_MAP_FIELD.get(server.getPluginManager());
        } catch (Exception e) {
            throw new RuntimeException("Could not get CommandMap", e);
        }
    }

    private Field getServerField(String name, Class<?> clazz) throws NoSuchFieldException {
        return clazz.getDeclaredField(name);
    }

}
