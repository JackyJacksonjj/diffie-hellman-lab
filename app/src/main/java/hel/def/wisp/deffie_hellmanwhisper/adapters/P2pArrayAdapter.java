package hel.def.wisp.deffie_hellmanwhisper.adapters;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import hel.def.wisp.deffie_hellmanwhisper.R;

/**
 * Created by jenea on 2/7/17.
 */
public class P2pArrayAdapter extends ArrayAdapter<WifiP2pDevice> {
    public P2pArrayAdapter(Context context, int resource, List<WifiP2pDevice> objects) {
        super(context, resource, objects);
    }

    public P2pArrayAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        WifiP2pDevice p2pDevice = getItem(position);

        P2pInfoViewHolder viewHolder;

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.p2p_info_adapter_item,null);
            viewHolder = new P2pInfoViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (P2pInfoViewHolder) convertView.getTag();
        }

        viewHolder.name.setText(p2pDevice.deviceName);
        viewHolder.address.setText(p2pDevice.deviceAddress);

        return convertView;
    }

    class P2pInfoViewHolder{

        public TextView address, name;

        public P2pInfoViewHolder(View view){
            name = (TextView)view.findViewById(R.id.p2p_name);
            address = (TextView)view.findViewById(R.id.p2p_address);
        }
    }
}
