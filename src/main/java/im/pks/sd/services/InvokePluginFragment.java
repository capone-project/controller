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

package im.pks.sd.services;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import im.pks.sd.controller.R;
import im.pks.sd.controller.invoke.ServiceChooserDialog;
import im.pks.sd.controller.query.ServiceDetails;
import im.pks.sd.persistence.Identity;
import im.pks.sd.protocol.Channel;
import im.pks.sd.protocol.ConnectTask;
import im.pks.sd.protocol.RequestTask;
import org.abstractj.kalium.crypto.SecretBox;
import org.abstractj.kalium.encoders.Encoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InvokePluginFragment extends PluginFragment {

    private View view;
    private ServiceDetails service;
    private ServiceDetails invocationService;

    public static InvokePluginFragment createFragment(ServiceDetails service) {
        InvokePluginFragment fragment = new InvokePluginFragment();
        fragment.service = service;
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_plugin_invoke, container, false);

        Button button = (Button) view.findViewById(R.id.button_select_server);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ServiceChooserDialog dialog = new ServiceChooserDialog() {
                    @Override
                    public void onServiceChosen(ServiceDetails details) {
                        setServiceDetails(details);
                    }
                };
                dialog.show(getFragmentManager(), "ServiceChooserDialog");
            }
        });


        return view;
    }

    private void setServiceDetails(ServiceDetails details) {
        invocationService = details;

        TextView serverKey = (TextView) view.findViewById(R.id.server_key);
        serverKey.setText(invocationService.server.publicKey);
        TextView serverAddress = (TextView) view.findViewById(R.id.server_address);
        serverAddress.setText(invocationService.server.address);

        ImageView serviceImage = (ImageView) view.findViewById(R.id.service_image);
        serviceImage.setImageResource(Services.getImageId(invocationService.service.type));
        TextView serviceName = (TextView) view.findViewById(R.id.service_name);
        serviceName.setText(invocationService.service.name);
        TextView serviceType = (TextView) view.findViewById(R.id.service_type);
        serviceType.setText(invocationService.service.type);
        TextView servicePort = (TextView) view.findViewById(R.id.service_port);
        servicePort.setText(String.valueOf(invocationService.service.port));
    }

    private void onSessionInitiated(RequestTask.Session session) {
        List<ServiceDetails.Parameter> parameters = new ArrayList<>();
        parameters.add(new ServiceDetails.Parameter("service-identity", invocationService.server.publicKey));
        parameters.add(new ServiceDetails.Parameter("service-address", invocationService.server.address));
        parameters.add(new ServiceDetails.Parameter("service-port", String.valueOf(invocationService.service.port)));
        parameters.add(new ServiceDetails.Parameter("service-type", invocationService.subtype));
        parameters.add(new ServiceDetails.Parameter("sessionid", Integer.toString(session.sessionId)));
        parameters.add(new ServiceDetails.Parameter("sessionkey", Encoder.HEX.encode(session.key)));

        /* TODO: fill parameters */
        parameters.add(new ServiceDetails.Parameter("service-args", "--port"));
        parameters.add(new ServiceDetails.Parameter("service-args", "9999"));

        RequestTask invocationServiceRequest = new RequestTask() {
            @Override
            public void onPostExecute(Session session) {
                onInvocationSessionInitiated(session);
            }
        };

        RequestTask.RequestParameters request = new RequestTask.RequestParameters(
                Identity.getSigningKey(), service, parameters);
        invocationServiceRequest.execute(request);
    }

    private void onInvocationSessionInitiated(RequestTask.Session session) {
        ConnectTask connectTask = new ConnectTask() {
            @Override
            public void handleConnection(Channel channel) {
                try {
                    channel.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        };

        ConnectTask.Parameters connectParameter = new ConnectTask.Parameters(
                session.sessionId,
                new SecretBox(session.key),
                service.server,
                service.service);
        connectTask.execute(connectParameter);
    }

    @Override
    public void onConnectClicked() {
        RequestTask invocationServiceRequest = new RequestTask() {
            @Override
            public void onPostExecute(Session session) {
                onSessionInitiated(session);
            }
        };

        List<ServiceDetails.Parameter> parameters = new ArrayList<>();
        /* TODO: fill parameters */

        RequestTask.RequestParameters request = new RequestTask.RequestParameters(
                Identity.getSigningKey(), invocationService, parameters);
        invocationServiceRequest.execute(request);
    }

}
