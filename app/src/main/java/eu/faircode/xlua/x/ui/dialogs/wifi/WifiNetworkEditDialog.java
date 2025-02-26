package eu.faircode.xlua.x.ui.dialogs.wifi;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.Random;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;

public class WifiNetworkEditDialog extends AppCompatDialogFragment {
    private static final String TAG = "XLua.WifiNetworkEditDialog";

    private Context context;
    private XWifiNetwork network;
    private WifiNetworkEditCallback callback;
    private Random random = new Random();

    public interface WifiNetworkEditCallback {
        void onNetworkSaved(XWifiNetwork network);
        void onNetworkDeleted(XWifiNetwork network);
    }

    public static WifiNetworkEditDialog create() {
        return new WifiNetworkEditDialog();
    }

    public WifiNetworkEditDialog setContext(Context context) {
        this.context = context;
        return this;
    }

    public WifiNetworkEditDialog setNetwork(XWifiNetwork network) {
        this.network = network;
        return this;
    }

    public WifiNetworkEditDialog setCallback(WifiNetworkEditCallback callback) {
        this.callback = callback;
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.wifi_network_edit_dialog, null);

        // Initialize views
        EditText etSSID = view.findViewById(R.id.etSSID);
        EditText etBSSID = view.findViewById(R.id.etBSSID);
        EditText etSignal = view.findViewById(R.id.etSignal);
        EditText etFrequency = view.findViewById(R.id.etFrequency);
        CheckBox cbRandomSignal = view.findViewById(R.id.cbRandomSignal);
        CheckBox cbRandomFrequency = view.findViewById(R.id.cbRandomFrequency);
        ImageView ivRandomizeSSID = view.findViewById(R.id.ivRandomizeSSID);
        ImageView ivRandomizeBSSID = view.findViewById(R.id.ivRandomizeBSSID);
        ImageView ivRandomizeSignal = view.findViewById(R.id.ivRandomizeSignal);
        ImageView ivRandomizeFrequency = view.findViewById(R.id.ivRandomizeFrequency);

        // Set initial values
        if (network != null) {
            etSSID.setText(network.ssid);
            etBSSID.setText(network.bssid);

            cbRandomSignal.setChecked(network.isRandomSignal());
            if (!network.isRandomSignal()) {
                etSignal.setText(String.valueOf(network.signal));
            }

            cbRandomFrequency.setChecked(network.isRandomFrequency());
            if (!network.isRandomFrequency()) {
                etFrequency.setText(String.valueOf(network.frequency));
            }
        }

        // Setup randomize SSID button
        ivRandomizeSSID.setOnClickListener(v -> {
            etSSID.setText(generateRandomSSID());
        });

        // Setup randomize BSSID button
        ivRandomizeBSSID.setOnClickListener(v -> {
            etBSSID.setText(generateRandomBSSID());
        });

        // Setup randomize Signal button
        ivRandomizeSignal.setOnClickListener(v -> {
            etSignal.setText(String.valueOf(generateRandomSignal()));
        });

        // Setup randomize Frequency button
        ivRandomizeFrequency.setOnClickListener(v -> {
            etFrequency.setText(String.valueOf(generateRandomFrequency()));
        });

        // Setup random signal checkbox
        cbRandomSignal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                etSignal.setEnabled(!isChecked);
                ivRandomizeSignal.setEnabled(!isChecked);
                ivRandomizeSignal.setAlpha(isChecked ? 0.5f : 1.0f);

                if (isChecked) {
                    etSignal.setText("");
                }
            }
        });

        // Setup random frequency checkbox
        cbRandomFrequency.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                etFrequency.setEnabled(!isChecked);
                ivRandomizeFrequency.setEnabled(!isChecked);
                ivRandomizeFrequency.setAlpha(isChecked ? 0.5f : 1.0f);

                if (isChecked) {
                    etFrequency.setText("");
                }
            }
        });

        // Initial UI update based on checkbox states
        etSignal.setEnabled(!cbRandomSignal.isChecked());
        ivRandomizeSignal.setEnabled(!cbRandomSignal.isChecked());
        ivRandomizeSignal.setAlpha(cbRandomSignal.isChecked() ? 0.5f : 1.0f);

        etFrequency.setEnabled(!cbRandomFrequency.isChecked());
        ivRandomizeFrequency.setEnabled(!cbRandomFrequency.isChecked());
        ivRandomizeFrequency.setAlpha(cbRandomFrequency.isChecked() ? 0.5f : 1.0f);

        builder.setView(view)
                .setTitle(R.string.title_edit_wifi_network)
                .setPositiveButton(R.string.option_save, null)
                .setNegativeButton(R.string.option_cancel, (dialog, which) -> dismiss())
                .setNeutralButton(R.string.option_delete, null);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);

            // Handle save button
            positiveButton.setOnClickListener(v -> {
                String ssid = etSSID.getText().toString().trim();
                String bssid = etBSSID.getText().toString().trim();

                if (TextUtils.isEmpty(ssid) || TextUtils.isEmpty(bssid)) {
                    // Show error message
                    if (TextUtils.isEmpty(ssid)) {
                        etSSID.setError("SSID is required");
                    }
                    if (TextUtils.isEmpty(bssid)) {
                        etBSSID.setError("BSSID is required");
                    }
                    return;
                }

                // Create updated network
                XWifiNetwork updatedNetwork = new XWifiNetwork();
                updatedNetwork.ssid = ssid;
                updatedNetwork.bssid = bssid;

                // Handle signal
                if (cbRandomSignal.isChecked()) {
                    updatedNetwork.signal = -3; // Random
                } else {
                    try {
                        updatedNetwork.signal = Integer.parseInt(etSignal.getText().toString());
                    } catch (NumberFormatException e) {
                        etSignal.setError("Invalid signal value");
                        return;
                    }
                }

                // Handle frequency
                if (cbRandomFrequency.isChecked()) {
                    updatedNetwork.frequency = -3; // Random
                } else {
                    try {
                        updatedNetwork.frequency = Integer.parseInt(etFrequency.getText().toString());
                    } catch (NumberFormatException e) {
                        etFrequency.setError("Invalid frequency value");
                        return;
                    }
                }

                if (callback != null) {
                    callback.onNetworkSaved(updatedNetwork);
                }

                dismiss();
            });

            // Handle delete button
            neutralButton.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onNetworkDeleted(network);
                }
                dismiss();
            });
        });

        return dialog;
    }

    /**
     * Generate a random SSID name for a WiFi network
     */
    private String generateRandomSSID() {
        String[] prefixes = {"Home", "WiFi", "Net", "Private", "Guest", "Corp", "IoT", "Smart", "TP-LINK", "D-Link", "Netgear", "ASUS", "Linksys"};
        String[] suffixes = {"Network", "5G", "2.4G", "Connect", "Hub", "Spot", "_" + randomNumeric(4), "-" + randomNumeric(3)};

        String prefix = prefixes[random.nextInt(prefixes.length)];
        String suffix = suffixes[random.nextInt(suffixes.length)];

        return prefix + suffix;
    }

    /**
     * Generate a random BSSID (MAC address) formatted as XX:XX:XX:XX:XX:XX
     */
    private String generateRandomBSSID() {
        byte[] macAddress = new byte[6];
        random.nextBytes(macAddress);

        // Ensure it's a valid MAC address (unicast, locally administered)
        macAddress[0] = (byte) ((macAddress[0] & 0xFC) | 0x02);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < macAddress.length; i++) {
            sb.append(String.format("%02X", macAddress[i]));
            if (i < macAddress.length - 1) {
                sb.append(":");
            }
        }

        return sb.toString();
    }

    /**
     * Generate a random signal strength value in dBm (typically between -30 and -90)
     */
    private int generateRandomSignal() {
        // WiFi signal strengths typically range from -30 dBm (excellent) to -90 dBm (poor)
        return -30 - random.nextInt(61); // Range from -30 to -90
    }

    /**
     * Generate a random frequency value in MHz (either 2.4GHz or 5GHz band)
     */
    private int generateRandomFrequency() {
        // Randomly choose between 2.4GHz and 5GHz bands
        if (random.nextBoolean()) {
            // 2.4GHz band: Channels 1-14 (2412-2484 MHz)
            return 2412 + (random.nextInt(14) * 5);
        } else {
            // 5GHz band: Various channels between 5180-5825 MHz
            int[] channels5GHz = {5180, 5200, 5220, 5240, 5260, 5280, 5300, 5320,
                    5500, 5520, 5540, 5560, 5580, 5600, 5620, 5640,
                    5660, 5680, 5700, 5720, 5745, 5765, 5785, 5805, 5825};
            return channels5GHz[random.nextInt(channels5GHz.length)];
        }
    }

    /**
     * Generate a random numeric string of a specific length
     */
    private String randomNumeric(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
}