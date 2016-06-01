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

package im.pks.sd.services.capabilities;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import im.pks.sd.controller.R;
import im.pks.sd.entities.ServiceDescriptionTo;
import im.pks.sd.services.PluginFragment;
import nano.Capabilities;

import java.util.Collections;
import java.util.List;

public class CapabilityPluginFragment extends PluginFragment implements View.OnClickListener, CapabilityRequestsTask.RequestListener {

    private RecyclerView cardsView;
    private CapabilityRequestsAdapter cardsAdapter;

    private ServiceDescriptionTo service;
    private CapabilityRequestsTask capabilityRequestsTask;

    public static CapabilityPluginFragment createFragment(ServiceDescriptionTo service) {
        CapabilityPluginFragment fragment = new CapabilityPluginFragment();
        fragment.service = service;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plugin_capability, container, false);

        cardsView = (RecyclerView) view.findViewById(R.id.request_cards);
        cardsView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        cardsView.setLayoutManager(layoutManager);

        cardsAdapter = new CapabilityRequestsAdapter();
        cardsView.setAdapter(cardsAdapter);

        Button invoke = (Button) view.findViewById(R.id.button_invoke);
        invoke.setOnClickListener(this);
        return view;
    }

    @Override
    public List<ServiceDescriptionTo.Parameter> getParameters() {
        return Collections.singletonList(new ServiceDescriptionTo.Parameter("mode", "register"));
    }

    @Override
    public void onClick(View v) {
        capabilityRequestsTask = new CapabilityRequestsTask(service, getParameters());
        capabilityRequestsTask.setRequestListener(this);
        capabilityRequestsTask.execute();
    }

    @Override
    public void onRequestReceived(final Capabilities.CapabilityRequest request,
                                  final Runnable accept) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /* FIXME: This is a workaround. We somehow get an initial bogus request without any
                 * information set. Filter by inspecting the invoker's identity.
                 */
                if (request.invokerIdentity.length > 0) {
                    cardsAdapter.addRequest(request, accept);
                }
            }
        });
    }
}

