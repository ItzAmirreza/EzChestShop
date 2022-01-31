package me.deadlight.ezchestshop.Utils.Exceptions;

public class CommandFetchException extends Exception {

    public CommandFetchException(String field) {
        super(String.format("Could not find Bukkit %s map (unsuported server version?)", field));
    }

}
