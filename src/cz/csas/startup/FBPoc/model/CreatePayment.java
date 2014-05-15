package cz.csas.startup.FBPoc.model;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

/**
 * Created by cen29414 on 15.5.2014.
 */
public class CreatePayment {
    private String recipientId;
    private String recipientName;
    private BigDecimal amount;
    private String currency;
    private String note;
    private Long senderAccount;

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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getSenderAccount() {
        return senderAccount;
    }

    public void setSenderAccount(Long senderAccount) {
        this.senderAccount = senderAccount;
    }

    public String toString() {
        JSONObject object = new JSONObject();
        try {
            object.put("senderAccount", getSenderAccount());
            object.put("recipientId", getRecipientId());
            object.put("recipientName", getRecipientName());
            object.put("amount", getAmount());
            object.put("currency", "CZK");
            object.put("note", getNote());
            return object.toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
