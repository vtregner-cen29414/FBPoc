package cz.csas.startup.FBPoc.model;

import java.math.BigDecimal;

/**
 * Created by cen29414 on 29.4.2014.
 */
public class Account {
    private Long prefix;
    private Long number;
    private String type;
    private BigDecimal balance;
    private String currency;

    public Long getPrefix() {
        return prefix;
    }

    public void setPrefix(Long prefix) {
        this.prefix = prefix;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
