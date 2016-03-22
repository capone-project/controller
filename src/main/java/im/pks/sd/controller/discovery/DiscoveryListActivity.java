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

package im.pks.sd.controller.discovery;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import im.pks.sd.controller.R;
import org.abstractj.kalium.keys.SigningKey;

import java.util.ArrayList;
import java.util.List;

public class DiscoveryListActivity extends ListActivity {

    private SigningKey key = new SigningKey();
    private List<Server> servers;
    private DiscoveryTask serviceLoader;
    private ArrayAdapter<Server> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery_list);

        servers = new ArrayList<>();

        adapter = new ArrayAdapter<Server>(this, R.layout.list_item_server, servers) {
            @Override
            public View getView(final int position, View view, ViewGroup group) {
                final Server server = servers.get(position);

                if (view == null) {
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = inflater.inflate(R.layout.list_item_server, null);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(DiscoveryListActivity.this, ServerDetailActivity.class);
                            intent.putExtra(ServerDetailActivity.EXTRA_SERVER, server);
                            startActivity(intent);
                        }
                    });
                }

                TextView serverKey = (TextView) view.findViewById(R.id.server_key);
                serverKey.setText(server.publicKey);

                return view;
            }
        };
        setListAdapter(adapter);

    }

    @Override
    protected void onResume() {
        super.onResume();

        serviceLoader = new DiscoveryTask(key.getVerifyKey()) {
            @Override
            public void onProgressUpdate(Server... server) {
                adapter.add(server[0]);
            }
        };
        serviceLoader.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        serviceLoader.cancel(true);
    }

}