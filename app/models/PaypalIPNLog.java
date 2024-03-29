package models;

import com.abperf.Currency;
import com.alienmegacorp.utils.CollectionUtils;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import javax.persistence.*;
import play.data.validation.*;
import play.db.jpa.Model;
import play.mvc.Http.Request;

/**
 * IPN = instant payment notification. PayPal calls our IPN URL after payments.
 *
 * @see controllers.api.PaypalIPNListener
 */
@Entity
@Table(name = "paypal_ipn_log")
public class PaypalIPNLog extends Model {
    @Email
    public String payerEmail;

    /**
     * Because anonymous item orders are allowed, a name is needed from anonymous users.
     */
    public String payerFirstName;

    /**
     * Because anonymous item orders are allowed, a name is needed from anonymous users.
     */
    public String payerLastName;

    /**
     * The primary email address of the recipient's Paypal account. If the payment
     * is sent to a non-primary email address of the Paypal account, this is still the
     * primary address.
     */
    @Email
    public String recipientPrimaryEmail;

    /**
     * The email address used by the recipient. This may be the {@link #recipientPrimaryEmail},
     * but it can also be another if the recipient has multiple email address in her PayPal account.
     */
    @Email
    public String recipientUsedEmail;

    @Required
    public String transactionType;

    @Enumerated(EnumType.STRING)
    public PaymentActionType paymentActionType;

    /**
     * An ID generated by Paypal.
     * This must be unique to prevent a fraudster re-using an old transaction ID.
     */
    @Column(unique = true)
    public String paypalTransactionId;

    /**
     * In the case of a refund, reversal, or canceled reversal, this is the
     * {@link #paypalTransactionId} of the original transaction, while this
     * record's {@link #paypalTransactionId} is a new ID for the new transaction.
     */
    public String paypalOriginalTransactionId;

    /**
     * IP address the IPN came from.
     */
    public String paypalServerIp;

    /**
     * The data Paypal sends us, as JSON.
     */
    public String paypalDataReceived;

    /**
     * Paypal's internal ID for the recipient.
     */
    public String paypalInternalRecipientId;

    /**
     * Paypal's internal ID for the payer.
     */
    public String paypalInternalPayerId;

    /**
     * The custom value we passed to PayPal and they then passed to our IPN listener.
     */
    public String customValue;

    /**
     * What is this?
     */
    public String invoice;

    @Enumerated(EnumType.STRING)
    public PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    public PaymentType paymentType;

    /**
     * When POSTing an IPN's encrypted data back to Paypal, their response body is
     * the text "VERIFIED" or "INVALID". If we receive an "INVALID" notification,
     * it should be treated as suspicious and be investigated.
     */
    public String paypalResponse;

    /**
     * Amount of money the recipient gets after Paypal takes fees.
     */
    public BigDecimal amountReceivedAfterFees;

    /**
     * Amount of money Paypal took in fees.
     */
    public BigDecimal amountOfFeesTaken;

    /**
     * 3-letter code for the currency.
     */
    @Match(value = "^[A-Z]+$", message = "Not a valid currency code")
    @MaxSize(3)
    @MinSize(3)
    @Enumerated(EnumType.STRING)
    public Currency currency;

    public boolean test;

    public boolean processedSuccessfully;

    @Temporal(TemporalType.TIMESTAMP)
    public Date createdAt;

    public PaypalIPNLog(Request request) {
        this.payerEmail = request.params.get("payer_email");
        this.payerFirstName = request.params.get("payer_first_name");
        this.payerLastName = request.params.get("payer_last_name");
        this.recipientPrimaryEmail = request.params.get("receiver_email");
        this.recipientUsedEmail = request.params.get("receiver_email");
        this.transactionType = request.params.get("txn_type");
        this.paypalTransactionId = request.params.get("txn_id");
        this.paypalOriginalTransactionId = request.params.get("parent_txn_id");
        this.paypalServerIp = request.remoteAddress;
        this.paypalDataReceived = request.querystring;
        this.paypalInternalRecipientId = request.params.get("receiver_id");
        this.paypalInternalPayerId = request.params.get("payer_id");
        this.customValue = request.params.get("custom");
        this.paymentStatus = PaymentStatus.valueOf(request.params.get("payment_status"));
        this.paymentType = PaymentType.valueOf(request.params.get("payment_type"));
        this.currency = Currency.valueOf(request.params.get("mc_currency"));
        this.amountOfFeesTaken = new BigDecimal(request.params.get("mc_fee"));
        this.amountReceivedAfterFees = new BigDecimal(request.params.get("mc_gross"));
        this.test = request.params.get("test_ipn", Boolean.class).booleanValue();
        this.createdAt = new Date();
    }

    @PrePersist
    protected void prePersist() {
        if (createdAt == null) {
            createdAt = new Date();
        }
    }

    public Map<String, String> getCustomValues() {
        return CollectionUtils.queryStringToMap(customValue);
    }

    public enum PaymentActionType {
        PROJECT,}

    public enum PaymentStatus {
        Completed,
        Denied,
        Failed,
        Refunded,
        Reversed,
        Processed,
        Pending,
        Canceled_Reversal,
        Created,
        Expried,
        Voided,}

    public enum PaymentType {
        instant,
        echeck,}
}
