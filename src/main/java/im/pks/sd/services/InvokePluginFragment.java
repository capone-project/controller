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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import im.pks.sd.controller.R;
import im.pks.sd.controller.invoke.ServiceChooserDialog;
import im.pks.sd.controller.query.ServiceDetails;

import java.util.ArrayList;
import java.util.List;

public class InvokePluginFragment extends PluginFragment {

    private View view;
    private PluginFragment pluginFragment;

    private ServiceDetails invoker;
    private ServiceDetails service;

    public static InvokePluginFragment createFragment(ServiceDetails invoker) {
        InvokePluginFragment fragment = new InvokePluginFragment();
        fragment.invoker = invoker;
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
        service = details;

        TextView serverKey = (TextView) view.findViewById(R.id.server_key);
        serverKey.setText(service.server.publicKey);
        TextView serverAddress = (TextView) view.findViewById(R.id.server_address);
        serverAddress.setText(service.server.address);

        ImageView serviceImage = (ImageView) view.findViewById(R.id.service_image);
        serviceImage.setImageResource(Plugins.getCategoryImageId(service.service.category));
        TextView serviceName = (TextView) view.findViewById(R.id.service_name);
        serviceName.setText(service.service.name);
        TextView serviceType = (TextView) view.findViewById(R.id.service_type);
        serviceType.setText(service.service.category);
        TextView servicePort = (TextView) view.findViewById(R.id.service_port);
        servicePort.setText(String.valueOf(service.service.port));

        Plugin plugin = Plugins.getPlugin(details);
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        pluginFragment = plugin.getFragment(service);
        transaction.add(R.id.plugin_view, pluginFragment);
        transaction.commit();
    }

    @Override
    public List<ServiceDetails.Parameter> getParameters() {
        List<ServiceDetails.Parameter> parameters = new ArrayList<>();
        parameters.add(new ServiceDetails.Parameter("service-identity",
                                                    service.server.publicKey));
        parameters.add(new ServiceDetails.Parameter("service-address",
                                                    service.server.address));
        parameters.add(new ServiceDetails.Parameter("service-port",
                                                    String.valueOf(
                                                            service.service.port)));
        parameters.add(new ServiceDetails.Parameter("service-type",
                                                    service.type));

        for (ServiceDetails.Parameter parameter : pluginFragment.getParameters()) {
            parameters.add(new ServiceDetails.Parameter("service-args", parameter.name));
            parameters.add(new ServiceDetails.Parameter("service-args", parameter.values));
        }

        return parameters;
    }

    public PluginTask createTask() {
        return new InvokePluginTask(invoker, service, getParameters());
    }

}