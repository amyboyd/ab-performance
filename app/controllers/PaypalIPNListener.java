package controllers;

import com.abperf.ProjectPayment;
import com.abperf.Constants;
import com.alienmegacorp.utils.CollectionUtils;
import java.util.*;
import models.*;
import play.Logger;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Util;

/**
 * IPN = instant payment notification.
 * Read and understand this guide before changing any of the IPN code:
 * {@linkplain https://cms.paypal.com/cms_content/US/en_US/files/developer/IPNGuide.pdf }
 *
 * The IPN protocol consists of three steps:
 * 1. PayPal sends your IPN listener a message that notifies you of the event.
 * 2. Your listener sends the complete unaltered message back to PayPal; the message
 *    must contain the same fields in the same order and be encoded in the same way
 *    as the original message.
 * 3. PayPal sends a single word back, which is either VERIFIED if the message originated
 *    with PayPal or INVALID if there is any discrepancy with what was originally sent.
 *
 * Your listener must respond to each message, whether or not you intend to do anything with it.
 * If you do not respond, PayPal assumes that the message was not received and resends the
 * message. PayPal continues to resend the message periodically until your listener sends the correct
 * message back, although the interval between resent messages increases each time. The message
 * can be resent for up to four days.
 *
 * PayPal expects to receive a response to an IPN message within 30 seconds.
 * Your listener should not perform time-consuming operations, such as creating
 * a process, before responding to the IPN message.
 *
 * After PayPal verifies the message, there are additional checks that your listener or back-end or
 * administrative software must take:
 * 1. Verify that you are the intended recipient of the IPN message by checking the email address
 *    in the message; this handles a situation where another merchant could accidentally or
 *    intentionally attempt to use your listener.
 * 2. Avoid duplicate IPN messages. Check that you have not already processed the transaction
 *    identified by the transaction ID returned in the IPN message. You may need to store
 *    transaction IDs returned by IPN messages in a file or database so that you can check for
 *    duplicates. If the transaction ID sent by PayPal is a duplicate, you should not process it
 *    again.
 * 3. Because IPN messages can be sent at various stages in a transaction's progress, make sure
 *    that the transaction's payment status is "completed" before enabling shipment of
 *    merchandise or allowing the download of digital media.
 * 4. Verify that the payment amount actually matches what you intend to charge. Although not
 *    technically an IPN issue, if you do not encrypt buttons, it is possible for someone to capture
 *    the original transmission and change the price. Without this check, you could accept a
 *    lesser payment than what you expected.
 *
 * Related: {@link models.PaypalIPNLog}
 */
public class PaypalIPNListener extends Controller {
    final private static String IPN_URL = play.Play.configuration.getProperty("paypal.ipnUrl", "https://www.paypal.com/cgi-bin/webscr");

    public void project() {
        verifyThenContinueToHandler(new ProjectPayment());
    }

    @Util
    private static void verifyThenContinueToHandler(IPNHandler handler) {
        Logger.info("IPN received: handler is %s, post data is: %s", handler.getClass().getSimpleName(), CollectionUtils.
                mapToQueryString(params.allSimple()));

        // Log all IPN messages.
        PaypalIPNLog ipn = new PaypalIPNLog(request);
        ipn.create();

        // Only allow test messages in dev and staging.
        if (Constants.IS_PROD && ipn.test) {
            error(ipn, "IPN is test, not allowed");
        }

        // Avoid duplicate IPN messages. Check that we have not already processed the
        // transaction identified by the transaction ID returned in the IPN message. If
        // the transaction ID sent by PayPal is a duplicate, we should not process it again.
        if (PaypalIPNLog.count("paypalTransactionId", ipn.paypalTransactionId) > 0) {
            error(ipn, "Already processed transaction: " + ipn.paypalTransactionId);
        }

        // Before we can trust the contents of the message, we must first verify that the
        // message came from PayPal. To verify the message, send back the parameters in the
        // exact order they were received and precede it with the command _notify-validate.
        Map<String, Object> verifyParams = new LinkedHashMap<String, Object>(10);
        verifyParams.put("cmd", "_notify-validate");
        verifyParams.putAll(params.allSimple());
        WSRequest verifyRequest = play.libs.WS.url(IPN_URL);
        verifyRequest.parameters = verifyParams;
        HttpResponse verifyResponse = verifyRequest.post();

        if (verifyResponse.getString().equals("VERIFIED")) {
            // IPN definetly came from PayPal's servers.
            handler.setIPNLog(ipn);
            handler.decodeCustomField();

            if (handler.isCorrectRecipient() && handler.isCorrectAmount()) {
                switch (ipn.paymentStatus) {
                    case Completed:
                        handler.paymentStatus_completed();
                        break;
                    case Canceled_Reversal:
                        handler.paymentStatus_canceledReversal();
                        break;
                    case Created:
                        handler.paymentStatus_created();
                        break;
                    case Denied:
                        handler.paymentStatus_denied();
                        break;
                    case Expried:
                        handler.paymentStatus_expired();
                        break;
                    case Failed:
                        handler.paymentStatus_failed();
                        break;
                    case Pending:
                        handler.paymentStatus_pending();
                        break;
                    case Processed:
                        handler.paymentStatus_processed();
                        break;
                    case Refunded:
                        handler.paymentStatus_refunded();
                        break;
                    case Reversed:
                        handler.paymentStatus_reversed();
                        break;
                    case Voided:
                        handler.paymentStatus_voided();
                        break;
                    default:
                        error(ipn, "Unknown payment status: " + ipn.paymentStatus);
                        break;
                }
            }
        } else if (verifyResponse.getString().equals("INVALID")) {
            // Log for investigation.
            error(ipn, "IPN did not come from PayPal, or other IPN error");
        } else {
            // Log for investigation.
            error(ipn, "Unexpected verification response: " + verifyResponse.getString());
        }

        // Should never reach here.
        error(ipn, "Execution should not reach here");
    }

    @Util
    public static void error(PaypalIPNLog ipn, String errorMessage) {
        play.Logger.error("IPN %s had error: %s", ipn.id, errorMessage);

        response.status = Http.StatusCode.INTERNAL_ERROR;
        renderText(errorMessage);
    }

    public abstract static class IPNHandler {
        public IPNHandler() {
        }

        protected PaypalIPNLog ipn;

        public void setIPNLog(PaypalIPNLog ipn) {
            this.ipn = ipn;
        }

        /**
         * The "custom" params are one we pass to PayPal before payment, and they are
         * returned to the IPN listener.
         */
        public abstract void decodeCustomField();

        /**
         * Verify that we are the intended recipient of the IPN message by checking the
         * email address in the message; this handles a situation where another merchant
         * could accidentally or intentionally attempts to use our listener.
         */
        public abstract boolean isCorrectRecipient();

        /**
         * Verify that the payment amount actually matches what we intend to charge. Without
         * this check, we could accept a lesser payment than what we expected.
         */
        public abstract boolean isCorrectAmount();

        public abstract void paymentStatus_completed();

        public void paymentStatus_canceledReversal() {
            error(ipn, "Unsupported payment status: " + ipn.paymentStatus);
        }

        public void paymentStatus_created() {
            error(ipn, "Unsupported payment status: " + ipn.paymentStatus);
        }

        public void paymentStatus_denied() {
            error(ipn, "Unsupported payment status: " + ipn.paymentStatus);
        }

        public void paymentStatus_expired() {
            error(ipn, "Unsupported payment status: " + ipn.paymentStatus);
        }

        public void paymentStatus_failed() {
            error(ipn, "Unsupported payment status: " + ipn.paymentStatus);
        }

        public void paymentStatus_pending() {
            error(ipn, "Unsupported payment status: " + ipn.paymentStatus);
        }

        public void paymentStatus_processed() {
            error(ipn, "Unsupported payment status: " + ipn.paymentStatus);
        }

        public void paymentStatus_refunded() {
            error(ipn, "Unsupported payment status: " + ipn.paymentStatus);
        }

        public void paymentStatus_reversed() {
            error(ipn, "Unsupported payment status: " + ipn.paymentStatus);
        }

        public void paymentStatus_voided() {
            error(ipn, "Unsupported payment status: " + ipn.paymentStatus);
        }
    }
}
