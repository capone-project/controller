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

package im.pks.sd.services.invoke;

import android.os.AsyncTask;
import com.google.protobuf.nano.MessageNano;
import im.pks.sd.entities.ServiceDescriptionTo;
import im.pks.sd.entities.SessionTo;
import im.pks.sd.protocol.RequestTask;
import im.pks.sd.protocol.SessionTask;
import nano.Invoke;
import org.abstractj.kalium.encoders.Encoder;
import org.abstractj.kalium.keys.VerifyKey;

import java.io.IOException;

public class InvokePluginTask extends AsyncTask<Void, Void, Throwable> {

    private final Invoke.InvokeParams parameters;
    private final MessageNano sessionParameters;
    private final ServiceDescriptionTo invoker;
    private final ServiceDescriptionTo service;

    public InvokePluginTask(ServiceDescriptionTo invoker,
                            ServiceDescriptionTo service,
                            Invoke.InvokeParams parameters,
                            MessageNano sessionParameters) {
        this.invoker = invoker;
        this.service = service;
        this.parameters = parameters;
        this.sessionParameters = sessionParameters;
    }

    @Override
    protected Throwable doInBackground(Void... params) {
        try {
            SessionTo session = sendSessionRequest(sessionParameters);
            parameters.cap = session.capability.toMessage();
            parameters.sessionid = session.identifier;

            SessionTask sessionTask = new SessionTask(invoker, parameters, null);
            sessionTask.startSession();
            return null;
        } catch (IOException | VerifyKey.SignatureException e) {
            return e;
        }
    }

    private SessionTo sendSessionRequest(MessageNano sessionParameters)
            throws IOException, VerifyKey.SignatureException {
        /* TODO: fill parameters with parameters for the specific invoker */
        VerifyKey identity = new VerifyKey(invoker.server.publicKey, Encoder.HEX);

        RequestTask request = new RequestTask(service, null);

        return request.requestSession();
    }

}
