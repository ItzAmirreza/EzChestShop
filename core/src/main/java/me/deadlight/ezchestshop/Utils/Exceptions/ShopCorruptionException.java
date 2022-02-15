package me.deadlight.ezchestshop.Utils.Exceptions;

import me.deadlight.ezchestshop.Utils.Objects.EzShop;

public class ShopCorruptionException extends Exception{
    public ShopCorruptionException(String message, EzShop shop) {
        super(message);

    }
}
