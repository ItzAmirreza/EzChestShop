package me.deadlight.ezchestshop.utils.Exceptions;

import me.deadlight.ezchestshop.utils.Objects.EzShop;

public class ShopCorruptionException extends Exception{
    public ShopCorruptionException(String message, EzShop shop) {
        super(message);

    }
}
