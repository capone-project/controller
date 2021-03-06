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

package com.github.capone.controller.discovery;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.github.capone.controller.R;
import com.github.capone.controller.services.ServiceListActivity;
import com.github.capone.protocol.entities.Server;
import com.github.capone.persistence.ServerRecord;
import com.github.capone.protocol.DiscoveryTask;

import java.util.ArrayList;

public class DiscoveryListFragment extends Fragment
        implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static String EXTRA_SERVERS = "servers";

    private DiscoveryTask serviceLoader;
    private ServerListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new ServerListAdapter(getActivity());
        adapter.setOnStarClickedListener(new ServerListAdapter.OnStarClickedListener() {
            @Override
            public boolean onStarClicked(Server to) {
                ServerRecord server = ServerRecord.findByTo(to);
                if (server == null) {
                    server = new ServerRecord(to);
                    server.save();
                    return true;
                } else {
                    server.delete();
                    return false;
                }
            }
        });

        if (savedInstanceState != null) {
            ArrayList<Server> servers = savedInstanceState.getParcelableArrayList(EXTRA_SERVERS);
            adapter.addAll(servers);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        ArrayList<Server> servers = new ArrayList<>(adapter.getCount());

        for (int i = 0; i < adapter.getCount(); i++) {
            servers.add(adapter.getItem(i));
        }

        outState.putParcelableArrayList(EXTRA_SERVERS, servers);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ListView list = new ListView(getActivity());

        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
        list.setOnItemLongClickListener(this);

        return list;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), ServiceListActivity.class);
        intent.putExtra(ServiceListActivity.EXTRA_SERVER, adapter.getItem(position));
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final Server to = adapter.getItem(position);

        getActivity().startActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                getActivity().getMenuInflater().inflate(R.menu.discovery, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                ServerRecord server = ServerRecord.findByTo(to);
                if (server == null) {
                    menu.findItem(R.id.remove).setVisible(false);
                } else {
                    menu.findItem(R.id.add).setVisible(false);
                }
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.add:
                        onAddClicked(to);
                        mode.finish();
                        return true;
                    case R.id.remove:
                        onRemoveClicked(to);
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        });

        return true;
    }

    private void onAddClicked(Server to) {
        new ServerRecord(to).save();
    }

    private void onRemoveClicked(Server server) {
        ServerRecord.findByTo(server).delete();
    }

    public void notifyDataSetChanged() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void startDiscovery() {
        serviceLoader = new DiscoveryTask() {
            @Override
            public void onProgressUpdate(Server... server) {
                if (adapter != null) {
                    if (adapter.getPosition(server[0]) == -1) {
                        adapter.add(server[0]);
                    }
                }
            }

            @Override
            protected void onPostExecute(Throwable throwable) {
                if (throwable != null) {
                    Toast.makeText(DiscoveryListFragment.this.getContext(),
                                   throwable.getLocalizedMessage(),
                                   Toast.LENGTH_SHORT).show();
                }
            }
        };
        serviceLoader.execute();
    }

    public void stopDiscovery() {
        if (serviceLoader != null) {
            serviceLoader.cancel();
            serviceLoader = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopDiscovery();
    }

}
