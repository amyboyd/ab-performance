package com.abperf;

import controllers.PaypalIPNListener;
import java.util.Date;
import java.math.BigDecimal;
import java.util.Map;
import models.PaypalIPNLog;
import models.Project;
import play.Play;
import static controllers.PaypalIPNListener.error;

public class ProjectPayment extends PaypalIPNListener.IPNHandler {
    private Project project;

    @Override
    public void decodeCustomField() {
        Map<String, String> custom = ipn.getCustomValues();
        this.project = Project.findById(Long.valueOf(custom.get("project")));
    }

    @Override
    public boolean isCorrectRecipient() {
        String recipient = ipn.recipientPrimaryEmail,
                correctRecipient = Play.configuration.getProperty("application.billingEmailAddress");

        if (!recipient.equals(correctRecipient)) {
            error(ipn, "IPN receiver email is: " + ipn.recipientPrimaryEmail + ", expected " + correctRecipient);
        }
        return true;
    }

    @Override
    public boolean isCorrectAmount() {
        BigDecimal expectedPrice = project.price;
        if (!ipn.amountReceivedAfterFees.equals(expectedPrice)) {
            error(ipn, "Expected price to be " + expectedPrice + ", got " + ipn.amountReceivedAfterFees);
        }
        return true;
    }

    @Override
    public void paymentStatus_completed() {
        project.paymentReceivedAt = new Date();
        project.save();

        ipn.processedSuccessfully = true;
        ipn.paymentActionType = PaypalIPNLog.PaymentActionType.PROJECT;
        ipn.save();
    }
}
