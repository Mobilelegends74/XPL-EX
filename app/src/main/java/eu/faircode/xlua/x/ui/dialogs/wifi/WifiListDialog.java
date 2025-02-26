package eu.faircode.xlua.x.ui.dialogs.wifi;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;


public class WifiListDialog extends AppCompatDialogFragment {
    private static final String TAG = "XLua.WifiListDialog";

    private Context context;
    private final List<XWifiNetwork> networks = new ArrayList<>();
    private WifiNetworkAdapter adapter;
    private WifiNetworkCallback callback;

    public interface WifiNetworkCallback {
        void onNetworksUpdated(List<XWifiNetwork> networks);
    }

    public static WifiListDialog create() {
        return new WifiListDialog();
    }

    public WifiListDialog setCallback(WifiNetworkCallback callback) {
        this.callback = callback;
        return this;
    }

    public WifiListDialog setList(List<XWifiNetwork> networks) {
        this.networks.clear();
        if (networks != null) {
            this.networks.addAll(networks);
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.wifi_list_dialog, null);

        ListView lvWifiNetworks = view.findViewById(R.id.lvWifiNetworks);
        Button btnCreate = view.findViewById(R.id.btnCreateNetwork);

        // Setup adapter
        adapter = new WifiNetworkAdapter(context, networks);
        lvWifiNetworks.setAdapter(adapter);

        // Handle network selection
        lvWifiNetworks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                XWifiNetwork network = networks.get(position);
                openNetworkEditDialog(network, position);
            }
        });

        // Handle create button
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XWifiNetwork newNetwork = new XWifiNetwork();
                openNetworkEditDialog(newNetwork, -1); // -1 indicates a new network
            }
        });

        builder.setView(view)
                .setTitle(R.string.title_wifi_networks)
                .setNegativeButton(R.string.option_cancel, (dialog, which) -> dismiss());

        return builder.create();
    }

    private void openNetworkEditDialog(XWifiNetwork network, int position) {
        WifiNetworkEditDialog.create()
                .setNetwork(network)
                .setContext(context)
                .setCallback(new WifiNetworkEditDialog.WifiNetworkEditCallback() {
                    @Override
                    public void onNetworkSaved(XWifiNetwork network) {
                        if (position >= 0) {
                            // Update existing network
                            networks.set(position, network);
                        } else {
                            // Add new network
                            networks.add(network);
                        }

                        adapter.notifyDataSetChanged();

                        if (callback != null) {
                            callback.onNetworksUpdated(networks);
                        }
                    }

                    @Override
                    public void onNetworkDeleted(XWifiNetwork network) {
                        if (position >= 0) {
                            networks.remove(position);
                            adapter.notifyDataSetChanged();

                            if (callback != null) {
                                callback.onNetworksUpdated(networks);
                            }
                        }
                    }
                })
                .show(getParentFragmentManager(), "wifi_network_edit");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
}