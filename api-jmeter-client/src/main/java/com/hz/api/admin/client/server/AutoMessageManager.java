package com.hz.api.admin.client.server;


import com.hz.api.admin.client.AbstractDataModule;
import com.hz.api.admin.client.ConfigureHelper;
import com.hz.api.admin.client.DataServerClient;
import com.hz.api.admin.netkit.filter.AndFilter;
import com.hz.api.admin.netkit.filter.PacketClassFilter;
import com.hz.api.admin.netkit.filter.PacketFilter;
import com.hz.api.admin.netkit.filter.PacketTypeFilter;
import com.hz.api.admin.netkit.listener.PacketListener;
import com.hz.api.admin.packet.AutoMessagePacket;

import java.util.HashSet;
import java.util.Set;

/**
 * @author xieqiyong66@gmail.com
 * @description: AutoMessageManager
 * @date 2022/12/15 2:23 下午
 */
public class AutoMessageManager <T> extends AbstractDataModule {

    private final Set<ServerDataListener> listeners = new HashSet();

    @Override
    protected void internalInitialize(DataServerClient client) {
        ConfigureHelper.loadToObject(client.getConfiguration(), "autoMessageManager.", this);
    }

    @Override
    protected void internalStart() {
        ServerDataListenerImpl listener = new ServerDataListenerImpl();
        PacketFilter packetFilter = new AndFilter(new PacketClassFilter(AutoMessagePacket.class), PacketTypeFilter.REQUEST);
        client.getNetkitClient().addPacketListener(listener, packetFilter);
    }

    public synchronized void registerServerDataListener(ServerDataListener serverDataListener){
        listeners.add(serverDataListener);
    }

    class ServerDataListenerImpl implements PacketListener<AutoMessagePacket> {
        @Override
        public void processPacket(AutoMessagePacket packet) {
            for (ServerDataListener listener : listeners) {
                listener.onData(packet.getBizData());
            }
        }
    }


}
