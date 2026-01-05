package com.subu.weddingcraft;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.subu.Helper.WedCardHelper;

import java.util.List;

public class VenuesViewAdapter extends RecyclerView.Adapter<VenuesViewAdapter.ViewHolder> {
    private final Context context;
    private final List<CardCatalog> cardList;
    private final OnItemClickListener listener;
    private final WedCardHelper helper;

    public VenuesViewAdapter(Context context, List<CardCatalog> cardList, OnItemClickListener listener) {
        this.helper = new WedCardHelper(context, cardList, position -> {
            cardList.remove(position);
            notifyItemRemoved(position);
            Toast.makeText(context, "Card deleted", Toast.LENGTH_SHORT).show();
        });
        this.context = context;
        this.cardList = cardList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_layout, parent, false);
        return new ViewHolder(view, listener);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardCatalog card = cardList.get(position);
        holder.cardTitle.setText(card.getTitle());
        holder.cardDate.setText(card.getDate());
        holder.imageCard.setImageResource(card.getImageResId());

        holder.itemView.setOnLongClickListener(view -> {
            helper.showPopup(view, position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView cardTitle, cardDate;
        ImageView imageCard;

        public ViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            imageCard = itemView.findViewById(R.id.imageCard);
            cardTitle = itemView.findViewById(R.id.titleCard);
            cardDate = itemView.findViewById(R.id.cardDate);

            itemView.setOnClickListener(view -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getAdapterPosition());
                }
            });
        }
    }
}
