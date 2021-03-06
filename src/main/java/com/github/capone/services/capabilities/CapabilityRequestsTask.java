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

package com.github.capone.services.capabilities;

import android.os.AsyncTask;
import com.github.capone.protocol.crypto.SigningKey;
import com.github.capone.protocol.crypto.SymmetricKey;
import com.github.capone.protocol.crypto.VerifyKey;
import com.github.capone.protocol.entities.*;
import com.github.capone.persistence.SigningKeyRecord;
import com.github.capone.protocol.Channel;
import com.github.capone.protocol.Client;
import com.github.capone.protocol.ProtocolException;
import com.google.protobuf.nano.MessageNano;
import nano.Capabilities;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CapabilityRequestsTask extends AsyncTask<Void, Void, CapabilityRequestsTask.Result>
        implements Client.SessionHandler {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private Client client;

    private final Server server;
    private final ServiceDescription service;
    private final MessageNano parameters;

    public static class Result {
        public final Throwable t;

        public Result(Throwable t) {
            this.t = t;
        }

    }

    public interface RequestListener {
        void onRequestReceived(CapabilityRequest request, Runnable accept);
    }

    private RequestListener listener;

    public CapabilityRequestsTask(Server server, ServiceDescription service,
                                  MessageNano parameters) {
        this.server = server;
        this.service = service;
        this.parameters = parameters;
    }

    public void setRequestListener(RequestListener listener) {
        this.listener = listener;
    }

    @Override
    protected Result doInBackground(Void... params) {
        try {
            client = new Client(SigningKeyRecord.getSigningKey(), server);
            Session session = client.request(service, parameters);
            client.connect(service, session, this);
            return null;
        } catch (IOException | ProtocolException e) {
            return new Result(e);
        } finally {
            client = null;
        }
    }

    @Override
    public void onSessionStarted(ServiceDescription service, Session session,
                                 final Channel channel) {
        final Capabilities.CapabilitiesCommand command = new Capabilities.CapabilitiesCommand();

        try {
            while (command.clear() != null && channel.readProtobuf(command) != null) {
                switch (command.cmd) {
                    case Capabilities.CapabilitiesCommand.REQUEST:
                        final CapabilityRequest requestTo =
                                new CapabilityRequest(command.request, new Date());

                        listener.onRequestReceived(requestTo, new Runnable() {
                            @Override
                            public void run() {
                                executor.submit(new Runnable() {
                                    @Override
                                    public void run() {
                                        accept(channel, requestTo);
                                    }
                                });
                            }
                        });
                        break;
                    case Capabilities.CapabilitiesCommand.TERMINATE:
                        cancel();
                        return;
                }
            }
        } catch (IOException | VerifyKey.InvalidKeyException | SymmetricKey.DecryptionException e) {
            /* ignore */
        }
    }

    private void accept(Channel channel, CapabilityRequest request) {
        SigningKey key = SigningKeyRecord.getSigningKey();
        Client client = new Client(key, request.serviceAddress, request.serviceIdentity.key);
        Session session = null;
        try {
            session = client.request(request.servicePort, request.parameters);
        } catch (Exception e) {
            /* ignore */
        }

        Capabilities.Capability capability = new Capabilities.Capability();
        capability.requestid = request.requestId;
        capability.sessionid = session.identifier;
        capability.capability = session.capability.createReference(Capability.RIGHT_EXEC,
                                                                   request.requesterIdentity).toMessage();
        capability.serviceIdentity = request.serviceIdentity.toMessage();

        try {
            channel.writeProtobuf(capability);
        } catch (IOException | SymmetricKey.EncryptionException e) {
            return;
        }
    }

    public void cancel() {
        if (client != null) {
            client.disconnect();
        }
        executor.shutdown();
    }

}
