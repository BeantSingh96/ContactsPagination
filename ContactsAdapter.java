package com.example.chattingdemo.contacts;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chattingdemo.BaseViewHolder;
import com.example.chattingdemo.R;
import com.example.chattingdemo.Utils;
import com.example.chattingdemo.contact.BitmapUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ContactsAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_NORMAL = 1;
    private boolean isLoaderVisible = false;

    private List<ContactHandler> contacts;
    private Activity activity;
    private ContactsListener listener;

    public ContactsAdapter(Activity activity, List<ContactHandler> contacts,
                           ContactsListener listener) {
        this.contacts = contacts;
        this.activity = activity;
        this.listener = listener;
    }

    interface ContactsListener {

        void onClick(int position);

        void onClickContact(int position);
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        switch (viewType) {
            case VIEW_TYPE_NORMAL:
                return new ViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_layout_list, parent, false));
            case VIEW_TYPE_LOADING:
                return new ProgressHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoaderVisible) {
            return position == contacts.size() - 1 ? VIEW_TYPE_LOADING : VIEW_TYPE_NORMAL;
        } else {
            return VIEW_TYPE_NORMAL;
        }
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void addItems(List<ContactHandler> items) {
        contacts.addAll(items);
        notifyDataSetChanged();
    }

    public void addLoading() {
        isLoaderVisible = true;
        contacts.add(new ContactHandler("", "", "", 0,
                0, "", "", ""));
        notifyItemInserted(contacts.size() - 1);
    }

    public void removeLoading() {
        isLoaderVisible = false;
        int position = contacts.size() - 1;
        ContactHandler item = getItem(position);
        if (item != null) {
            contacts.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        contacts.clear();
        notifyDataSetChanged();
    }

    private ContactHandler getItem(int position) {
        return contacts.get(position);
    }

    public class ViewHolder extends BaseViewHolder {

        TextView cMessage;
        TextView cDot;
        TextView cName;
        ImageView cImage;
        ImageButton cOptions;

        ViewHolder(final View convertView) {
            super(convertView);

            cMessage = convertView.findViewById(R.id.contactMessage);
            cDot = convertView.findViewById(R.id.contactDot);
            cName = convertView.findViewById(R.id.contactName);
            cImage = convertView.findViewById(R.id.contactImage);
            cOptions = convertView.findViewById(R.id.contactButton);

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    listener.onClick(getAdapterPosition());
                }
            });
            cOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    listener.onClickContact(getAdapterPosition());
                }
            });
        }

        @Override
        public void onBind(int position) {
            super.onBind(position);

            String photo = contacts.get(position).getPhoto();
            String name = contacts.get(position).getName();
            cName.setText(name);
            cMessage.setText(contacts.get(position).getMessage());
            if (photo != null && !photo.equals("null")) {
                Glide.with(activity)
                        .load(Utils.PROFILE + photo)
                        //TODO remove comment
                        // .placeholder(R.drawable.ic_no_photo)
                        .apply(RequestOptions.circleCropTransform())
                        .centerCrop()
                        .circleCrop()
                        .into(cImage);
            } else {
                String first = name.substring(0, 1).toUpperCase();
                Glide.with(activity)
                        .load(BitmapUtils.bitmapToByte(BitmapUtils.imageWithText(first, 2, activity)))
                        //TODO remove comment
                        //.placeholder(R.drawable.ic_no_photo)
                        .apply(RequestOptions.circleCropTransform())
                        .circleCrop()
                        .into(cImage);
            }

            //TODO change icon here
            if (contacts.get(position).getOnlineStatus() > 0) {
                cDot.setBackgroundResource(R.drawable.ic_launcher_background);
            } else {
                cDot.setBackgroundResource(R.drawable.ic_launcher_background);
            }
        }

        @Override
        protected void clear() {

        }
    }

    public class ProgressHolder extends BaseViewHolder {
        ProgressHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void clear() {
        }
    }
}