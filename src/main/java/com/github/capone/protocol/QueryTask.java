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
import com.github.capone.persistence.SigningKeyRecord;
import com.github.capone.protocol.crypto.SigningKey;
import com.github.capone.protocol.entities.Server;
import com.github.capone.protocol.entities.Service;
import com.github.capone.protocol.entities.ServiceDescription;

public abstract class QueryTask
        extends AsyncTask<QueryTask.Parameters, ServiceDescription, Throwable> {

    public static class Parameters {
        public final SigningKey localKey;
        public final Server server;
        public final Service service;

        public Parameters(SigningKey localKey, Server server, Service service) {
            this.localKey = localKey;
            this.server = server;
            this.service = service;
        }
    }

    private Client client;

    @Override
    protected Throwable doInBackground(Parameters... params) {
        for (Parameters param : params) {
            try {
                if (isCancelled())
                    return null;

                client = new Client(SigningKeyRecord.getSigningKey(), param.server);
                publishProgress(client.query(param.service));

                return null;
            } catch (Exception e) {
                /* ignore */
            } finally {
                client = null;
            }
        }

        return null;
    }

    public void cancel() {
        if (client != null) {
            client.disconnect();
        }
    }

    @Override
    public abstract void onProgressUpdate(ServiceDescription... description);

}
