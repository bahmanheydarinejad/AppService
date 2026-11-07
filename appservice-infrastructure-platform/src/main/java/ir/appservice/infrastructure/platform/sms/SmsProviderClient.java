package ir.appservice.infrastructure.platform.sms;

import ir.appservice.core.domain.entity.SmsMessage;
import ir.appservice.core.domain.enums.SmsProviderType;
import ir.appservice.core.domain.port.outbound.SmsGatewayPort.SmsReceipt;

/**
 * Strategy contract for outbound SMS vendor integration clients.
 */
public interface SmsProviderClient {

    SmsProviderType getProviderType();

    SmsReceipt send(SmsMessage message);
}
