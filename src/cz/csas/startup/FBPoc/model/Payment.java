package cz.csas.startup.FBPoc.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by cen29414 on 9.5.2014.
 */
public class Payment extends CreatePayment {
    private String id;
    private Status status;
    private Date paymentDate;

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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

}
