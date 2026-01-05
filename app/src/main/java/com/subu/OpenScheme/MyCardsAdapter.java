package com.subu.OpenScheme;

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
import com.subu.weddingcraft.CardCatalog;
import com.subu.weddingcraft.R;

import java.util.List;

public class MyCardsAdapter extends RecyclerView.Adapter<MyCardsAdapter.ViewHolder> {
    private final List<CardCatalog> cardList;
    private final OnItemClickListener listener;
    private final Context context;
    private final WedCardHelper helper;

    public MyCardsAdapter(Context context, List<CardCatalog> cardList, OnItemClickListener listener) {
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
        View view = LayoutInflater.from(context).inflate(R.layout.draft_wedcards_layout, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardCatalog card = cardList.get(position);
        holder.cardTitle.setText(card.getTitle());
        holder.cardDate.setText(card.getDate());
        holder.cardImage.setImageResource(card.getImageResId());

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
        ImageView cardImage;

        public ViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            cardTitle = itemView.findViewById(R.id.draft_cardTitle);
            cardDate = itemView.findViewById(R.id.draft_cardDate);
            cardImage = itemView.findViewById(R.id.draft_cardImage);

            itemView.setOnClickListener(view -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getAdapterPosition());
                }
            });
        }
    }
}
