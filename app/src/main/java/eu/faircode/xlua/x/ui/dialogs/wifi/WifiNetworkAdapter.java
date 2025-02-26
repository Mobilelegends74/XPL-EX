package eu.faircode.xlua.x.ui.dialogs.wifi;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import eu.faircode.xlua.R;
import eu.faircode.xlua.x.ui.dialogs.wifi.XWifiNetwork;

public class WifiNetworkAdapter extends BaseAdapter {
    private final Context context;
    private final List<XWifiNetwork> networks;
    private final LayoutInflater inflater;

    public WifiNetworkAdapter(Context context, List<XWifiNetwork> networks) {
        this.context = context;
        this.networks = networks;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return networks.size();
    }

    @Override
    public Object getItem(int position) {
        return networks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.wifi_network_list_item, parent, false);
            holder = new ViewHolder();
            holder.tvSSID = convertView.findViewById(R.id.tvSSID);
            holder.tvBSSID = convertView.findViewById(R.id.tvBSSID);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        XWifiNetwork network = networks.get(position);
        holder.tvSSID.setText(network.ssid);
        holder.tvBSSID.setText(network.bssid);

        return convertView;
    }

    private static class ViewHolder {
        TextView tvSSID;
        TextView tvBSSID;
    }
}