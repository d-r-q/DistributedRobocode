/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.*;
import java.nio.channels.SocketChannel;

public class Client {

    private boolean isAlive = true;
    private SocketChannel socketChannel;

    public Client(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void sendRSBattleResults(RSBattleResults rsBattleResults) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            final ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(rsBattleResults);
            oos.flush();
            oos.close();
            socketChannel.write(ByteBuffer.wrap(baos.toByteArray()));
        } catch (IOException e) {
            isAlive = false;
        }
    }

}
