package me.deadlight.ezchestshop.utils.exceptions;

public class CommandFetchException extends Exception {

    public CommandFetchException(String field) {
        super(String.format("Could not find Bukkit %s map (unsuported server version?)", field));
    }

}
