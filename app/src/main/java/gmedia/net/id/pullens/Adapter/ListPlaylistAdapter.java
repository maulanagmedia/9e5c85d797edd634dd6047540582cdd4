package gmedia.net.id.pullens.Adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ImageUtils;
import com.maulana.custommodul.ItemValidation;

import java.util.List;

import gmedia.net.id.pullens.PlaylistActivity;
import gmedia.net.id.pullens.R;


/**
 * Created by Shin on 1/8/2017.
 */

public class ListPlaylistAdapter extends ArrayAdapter{

    private Activity context;
    private List<CustomItem> items;
    private ItemValidation iv = new ItemValidation();

    public ListPlaylistAdapter(Activity context, List<CustomItem> items) {
        super(context, R.layout.adapter_playlist, items);
        this.context = context;
        this.items = items;
    }

    private static class ViewHolder {
        private ImageView ivAddList;
        private TextView tvItem1, tvItem2;
    }

    public void addMoreData(List<CustomItem> moreData){

        items.addAll(moreData);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = new ViewHolder();
        //int tipeViewList = getItemViewType(position);

        if(convertView == null){

            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.adapter_playlist, null);

            holder.ivAddList = (ImageView) convertView.findViewById(R.id.iv_add_list);
            holder.tvItem1 = (TextView) convertView.findViewById(R.id.tv_item1);
            holder.tvItem2 = (TextView) convertView.findViewById(R.id.tv_item2);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final CustomItem itemSelected = items.get(position);

        holder.tvItem1.setText(itemSelected.getItem2());
        holder.tvItem2.setText(itemSelected.getItem3());

        holder.ivAddList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog builder = new AlertDialog.Builder(context)
                        .setIcon(R.mipmap.ic_music_red)
                        .setTitle("Konfirmasi")
                        .setMessage("Tambahkan \""+ itemSelected.getItem2() +"\" ke playlist?")
                        .setCancelable(false)
                        .setPositiveButton("Tambahkan", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                PlaylistActivity.validateWifiPlaylist(context, itemSelected.getItem1());
                            }
                        })
                        .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show();
            }
        });

        return convertView;
    }
}
