
package com.hz.api.admin.engine.transport.handler;

import com.perfma.xsea.data.collector.base.packet.MessagePacket;
import com.perfma.xsea.data.collector.exception.DataServerException;
import com.perfma.xsea.data.collector.message.KafkaProducer;
import com.perfma.xsea.data.collector.model.ClientInfo;
import com.perfma.xsea.data.collector.netkit.packet.Packet;
import com.perfma.xsea.data.collector.netkit.server.NetkitConnection;
import com.perfma.xsea.data.collector.transport.ClientConnectionManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author liusu
 */
@Component
public class MessageHandler extends AbstractPacketHandler<MessagePacket> {
    private static final Logger log = LoggerFactory.getLogger(MessageHandler.class);

    @Resource
    private ClientConnectionManager clientConnectionManager;

    @Resource
    private KafkaProducer kafkaProducer;

    @Override
    public String getName() {
        return MessagePacket.NAME;
    }

    @Override
    public MessagePacket handle(NetkitConnection connection, MessagePacket packet) {
        ClientInfo clientInfo = clientConnectionManager.getClientInfoByChannelId(connection.getChannelId());
        if (clientInfo == null) {
            throw new DataServerException("客户端未注册");
        }
        String topic = StringUtils.trimToNull(packet.getTopic());
        byte[] body = packet.getBody();
        Objects.requireNonNull(topic, "Message.topic must not be null");
        Objects.requireNonNull(body, "Message.body must not be null");
        String newBody = new String(body, StandardCharsets.UTF_8);
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, null, newBody);
        kafkaProducer.send(record, new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onSuccess(SendResult<String, String> result) {
            }

            @Override
            public void onFailure(Throwable ex) {
                log.error("转发客户端消息到Kafka失败：packet={}, error={}", packet, ex.getMessage());
            }
        });
        if (packet.isAcks()) {
            return Packet.createResult(packet);
        }
        return null;
    }
}
