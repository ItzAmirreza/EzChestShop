package me.deadlight.ezchestshop.utils.exceptions;

import me.deadlight.ezchestshop.utils.objects.EzShop;

public class ShopCorruptionException extends Exception{
    public ShopCorruptionException(String message, EzShop shop) {
        super(message);

    }
}
