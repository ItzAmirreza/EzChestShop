package me.deadlight.ezchestshop.Utils;

import java.time.LocalDateTime;

public class TransactionLogObject {

    public String type;
    public String pname;
    public String price;
    public String time;

    public TransactionLogObject(String type, String pname, String price, String time) {
        this.type = type;
        this.pname = pname;
        this.price = price;
        this.time = time;
    }

}
