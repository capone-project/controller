/*
 * Copyright (C) 2016 Patrick Steinhardt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.capone.protocol;

import android.os.AsyncTask;
import com.github.capone.entities.ServiceDescriptionTo;
import com.github.capone.entities.SessionTo;
import com.github.capone.persistence.Identity;
import nano.Capone;
import org.abstractj.kalium.keys.VerifyKey;

import java.io.IOException;

public class ConnectTask extends AsyncTask<Void, Void, Throwable> {

    public interface Handler {
        void handleConnection(Channel channel) throws IOException, VerifyKey.SignatureException;
    }

    private final SessionTo session;
    private final ServiceDescriptionTo service;

    private Channel channel;
    private Handler handler;

    public ConnectTask(SessionTo session, ServiceDescriptionTo service) {
        this.session = session;
        this.service = service;
    }

    @Override
    protected Throwable doInBackground(Void... params) {
        try {
            connect();
            return null;
        } catch (IOException | VerifyKey.SignatureException e) {
            return e;
        }
    }

    public void connect() throws IOException, VerifyKey.SignatureException {
        Capone.ConnectionInitiationMessage connectionInitiation = new Capone
                                                                         .ConnectionInitiationMessage();
        connectionInitiation.type = Capone.ConnectionInitiationMessage.CONNECT;
        Capone.SessionConnectMessage sessionInitiation = new Capone.SessionConnectMessage();

        sessionInitiation.capability = session.capability.toMessage();
        sessionInitiation.identifier = session.identifier;

        try {
            channel = new TcpChannel(service.server.address, service.service.port);
            channel.connect();
            channel.enableEncryption(Identity.getSigningKey(), service.server.signatureKey.key);
            channel.writeProtobuf(connectionInitiation);
            channel.writeProtobuf(sessionInitiation);

            Capone.SessionConnectResult result = new Capone.SessionConnectResult();
            channel.readProtobuf(result);

            if (result.result == 0 && handler != null) {
                handler.handleConnection(channel);
            }

            channel.close();
        } catch (VerifyKey.SignatureException | IOException e) {
            throw e;
        } finally {
            try {
                if (channel != null)
                    channel.close();
            } catch (IOException e) {
                // ignore
            } finally {
                channel = null;
            }
        }
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void cancel() {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                // ignore
            } finally {
                channel = null;
            }
        }
    }

}
