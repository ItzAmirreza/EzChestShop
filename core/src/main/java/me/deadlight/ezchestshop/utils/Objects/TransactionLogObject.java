package me.deadlight.ezchestshop.utils.Objects;
public class TransactionLogObject {

    public String type;
    public String pname;
    public String price;
    public String time;
    public Integer count;

    public TransactionLogObject(String type, String pname, String price, String time, int count) {
        this.type = type;
        this.pname = pname;
        this.price = price;
        this.time = time;
        this.count = count;
    }

}
