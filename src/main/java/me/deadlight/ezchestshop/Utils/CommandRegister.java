package me.deadlight.ezchestshop.Utils;

import me.deadlight.ezchestshop.Utils.Exceptions.CommandFetchException;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;

import javax.inject.Singleton;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;

@Singleton
public class CommandRegister {

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
            Field field = this.getServerField("commandMap", Bukkit.getServer().getClass());
            this.makeAccessible(field);
            this.removeFinalModifier(field);
            return (SimpleCommandMap) field.get(Bukkit.getServer());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new CommandFetchException("commandMap");
        }
    }

    private Map<String, Command> getKnownCommands() throws CommandFetchException {
        try {
            SimpleCommandMap commandMap = this.getSimpleCommandMap();
            Field field = this.getServerField("knownCommands", SimpleCommandMap.class);
            this.makeAccessible(field);
            this.removeFinalModifier(field);
            return (Map<String, Command>) field.get(commandMap);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new CommandFetchException("knownCommands");
        }
    }

    private void makeAccessible(Field field) {
        field.setAccessible(true);
    }

    private void removeFinalModifier(Field field) throws IllegalAccessException, NoSuchFieldException {
        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }

    private Field getServerField(String name, Class<?> clazz) throws NoSuchFieldException {
        return clazz.getDeclaredField(name);
    }

}
