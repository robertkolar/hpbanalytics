package com.highpowerbear.hpbanalytics.riskmgt;

import com.highpowerbear.hpbanalytics.common.CoreUtil;
import com.highpowerbear.hpbanalytics.common.MessageSender;
import com.highpowerbear.hpbanalytics.common.OptionInfoVO;
import com.highpowerbear.hpbanalytics.enums.OptionType;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.highpowerbear.hpbanalytics.iblogger.IbController;
import com.highpowerbear.hpbanalytics.iblogger.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.highpowerbear.hpbanalytics.common.CoreSettings.JMS_DEST_IBLOGGER_TO_RISKMGT;

/**
 * Created by robertk on 12/28/2017.
 */
@Component
public class RiskMgtMessageReceiver {
    private static final Logger log = LoggerFactory.getLogger(RiskMgtMessageReceiver.class);

    @Autowired private IbController ibController;
    @Autowired MessageSender messageSender;

    private final Map<String, LocalDateTime> messageTimeMap = new ConcurrentHashMap<>();

    @JmsListener(destination = JMS_DEST_IBLOGGER_TO_RISKMGT)
    public void receiveJmsMessage(String message) {

        String accountId = message.split(":")[1].trim();
        performRiskMgt(accountId);
    }

    private void performRiskMgt(String accountId) {
        log.info("performing risk management for account " + accountId);

        LocalDateTime now = LocalDateTime.now();

        List<Position> shortOptionPositions = ibController.getPositions(accountId).stream().filter(p -> p.getSecType() == SecType.OPT).filter(Position::isShort).collect(Collectors.toList());

        for (Position p : shortOptionPositions) {
            OptionInfoVO optionInfo = CoreUtil.parseOptionSymbol(p.getSymbol());

            if (optionInfo != null) {
                OptionType optionType = optionInfo.getOptionType();
                double priceDiference = p.getUnderlyingPrice() - optionInfo.getStrikePrice();

                if (optionType == OptionType.CALL && priceDiference > 0d || optionType == OptionType.PUT && priceDiference < 0d) {
                    LocalDateTime lastSentTime = messageTimeMap.get(p.getSymbol());

                    if (lastSentTime == null || now.minusHours(4).isAfter(lastSentTime)) {
                        String subject = p.getSymbol() + " in the money";
                        String msg = p.toString() + "\n\n" + optionInfo.toString();

                        log.info(subject);
                        messageTimeMap.put(p.getSymbol(), now);
                        messageSender.sendEmailMessage(subject, msg);
                    }
                }
            }
        }
    }
}