package cz.csas.startup.FBPoc.model;

import java.math.BigDecimal;

/**
 * Created by cen29414 on 9.5.2014.
 */
public class Payment {
    private String id;
    private String recipientId;
    private String recipientName;
    private BigDecimal amount;
    private String currency;
    private Status status;

    public enum Status {
        PENDING(0), ACCEPTED(1), REFUSED(2), TIMEOUTED(3);

        int value;

        Status(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Status valueOf(int value) {
            Status[] valueEnums = Status.values();
            for (Status valueEnum : valueEnums) {
                if (valueEnum.getValue() == value)
                {
                    return valueEnum;
                }
            }
            return null;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
