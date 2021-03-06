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

package com.github.capone.controller.invoke;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.github.capone.controller.R;
import com.github.capone.persistence.SigningKeyRecord;
import com.github.capone.protocol.QueryTask;
import com.github.capone.protocol.crypto.SigningKey;
import com.github.capone.protocol.entities.Server;
import com.github.capone.protocol.entities.Service;
import com.github.capone.protocol.entities.ServiceDescription;
import com.github.capone.services.Plugin;
import com.github.capone.services.Plugins;

public class InvokeActivity extends AppCompatActivity {

    public static final String EXTRA_SERVER = "server";
    public static final String EXTRA_SERVICE = "service";

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

        Intent intent = getIntent();
        final Server server = intent.getParcelableExtra(EXTRA_SERVER);
        Service service = intent.getParcelableExtra(EXTRA_SERVICE);

        final QueryTask queryTask = new QueryTask() {
            @Override
            public void onProgressUpdate(ServiceDescription... description) {
                setServiceDescription(server, description[0]);
            }

            @Override
            protected void onPostExecute(Throwable throwable) {
                if (throwable != null) {
                    Toast.makeText(InvokeActivity.this,
                                   throwable.getLocalizedMessage(),
                                   Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        };

        SigningKey key = SigningKeyRecord.getSigningKey();
        QueryTask.Parameters parameters = new QueryTask.Parameters(key, server, service);
        queryTask.execute(parameters);

        progressDialog = ProgressDialog.show(this, getString(R.string.loading),
                                             getString(R.string.query_loading),
                                             true, true);
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (queryTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
                    queryTask.cancel();
                    finish();
                }
            }
        });
    }

    private void setServiceDescription(Server server, ServiceDescription results) {
        Plugin plugin = Plugins.getPlugin(results.type);

        if (plugin == null) {
            Toast.makeText(InvokeActivity.this,
                           String.format(getString(R.string.no_plugin_for_type), results.type),
                           Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            return;
        }

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.plugin_view, plugin.getFragment(server, results));
        transaction.commit();

        progressDialog.dismiss();
    }

}
