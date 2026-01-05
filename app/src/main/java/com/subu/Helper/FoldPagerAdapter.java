package com.subu.Helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.subu.weddingcraft.R;
import com.subu.weddingcraft.WedCard;

import java.util.HashMap;
import java.util.List;

public class FoldPagerAdapter extends RecyclerView.Adapter<FoldPagerAdapter.FoldViewHolder> {
    private final Context context;
    private final List<WedCard.Fold> folds;
    private final HashMap<String, Bitmap> imageMap;

    // Enhanced color palette for wedding cards
    private final int GOLD_COLOR = Color.parseColor("#D4AF37");
    private final int DARK_RED = Color.parseColor("#8B0000");
    private final int CREAM_COLOR = Color.parseColor("#F5F5DC");
    private final int BURGUNDY = Color.parseColor("#800020");
    private final int ELEGANT_GOLD = Color.parseColor("#FFD700");

    public FoldPagerAdapter(Context context, List<WedCard.Fold> folds, HashMap<String, Bitmap> imageMap) {
        this.context = context;
        this.folds = folds;
        this.imageMap = imageMap;
    }

    @NonNull
    @Override
    public FoldViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fold_page, parent, false);
        return new FoldViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoldViewHolder holder, int position) {
        WedCard.Fold fold = folds.get(position);

        // Set background image
        Bitmap foldImage = imageMap.get(fold.image);
        if (foldImage != null) {
            holder.imageView.setImageBitmap(foldImage);
            holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        // Clear previous blocks and set proper container styling
        holder.blockContainer.removeAllViews();
        setupBlockContainer(holder.blockContainer);

        if (fold.blocks != null) {
            addSpacerView(holder.blockContainer, 40); // Top spacing

            for (int i = 0; i < fold.blocks.size(); i++) {
                WedCard.Block block = fold.blocks.get(i);

                switch (block.type.toLowerCase()) {
                    case "heading":
                        addHeadingBlock(holder.blockContainer, block);
                        break;
                    case "text":
                        addTextBlock(holder.blockContainer, block);
                        break;
                    case "quote":
                        addQuoteBlock(holder.blockContainer, block);
                        break;
                    case "event_list":
                        addEventListBlock(holder.blockContainer, block);
                        break;
                }

                // Add spacing between blocks
                if (i < fold.blocks.size() - 1) {
                    addSpacerView(holder.blockContainer, 16);
                }
            }

            addSpacerView(holder.blockContainer, 40); // Bottom spacing
        }
    }

    private void setupBlockContainer(LinearLayout container) {
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER_HORIZONTAL);
        container.setPadding(32, 60, 32, 60); // Increased padding for better spacing
    }

    private void addHeadingBlock(LinearLayout container, WedCard.Block block) {
        TextView heading = createStyledTextView(
                block.content,
                28, // Reduced from 60 for better proportion
                Typeface.BOLD,
                Gravity.CENTER,
                ELEGANT_GOLD
        );

        // Apply custom styles if available
        if (block.textStyle != null) {
            if (block.textStyle.size > 0) {
                heading.setTextSize(TypedValue.COMPLEX_UNIT_SP, block.textStyle.size);
            }
            if (block.textStyle.color != null) {
                heading.setTextColor(Color.parseColor(block.textStyle.color));
            }
        }

        // Add elegant shadow effect
        heading.setShadowLayer(2, 1, 1, Color.parseColor("#80000000"));
        heading.setLetterSpacing(0.1f); // Add letter spacing for elegance

        container.addView(heading);
    }

    private void addTextBlock(LinearLayout container, WedCard.Block block) {
        TextView textView = createStyledTextView(
                block.content,
                16,
                Typeface.NORMAL,
                Gravity.CENTER,
                DARK_RED
        );

        textView.setLineSpacing(4, 1.2f); // Better line spacing
        container.addView(textView);
    }

    private void addQuoteBlock(LinearLayout container, WedCard.Block block) {
        // Create quote container for better styling
        LinearLayout quoteContainer = new LinearLayout(context);
        quoteContainer.setOrientation(LinearLayout.VERTICAL);
        quoteContainer.setGravity(Gravity.CENTER);
        quoteContainer.setPadding(24, 16, 24, 16);

        // Quote text with decorative formatting
        TextView quote = createStyledTextView("\"" + block.content + "\"",
                18,
                Typeface.ITALIC,
                Gravity.CENTER,
                BURGUNDY
        );

        quote.setLineSpacing(6, 1.3f);
        quote.setShadowLayer(1, 1, 1, Color.parseColor("#40000000"));

        quoteContainer.addView(quote);
        container.addView(quoteContainer);
    }

    private void addEventListBlock(LinearLayout container, WedCard.Block block) {
        // Create events container
        LinearLayout eventsContainer = new LinearLayout(context);
        eventsContainer.setOrientation(LinearLayout.VERTICAL);
        eventsContainer.setGravity(Gravity.CENTER);
        eventsContainer.setPadding(16, 8, 16, 8);

        // Add "Events" header
        TextView eventsHeader = createStyledTextView(
                "✧ Events ✧",
                20,
                Typeface.BOLD,
                Gravity.CENTER,
                GOLD_COLOR
        );
        eventsHeader.setLetterSpacing(0.15f);
        eventsContainer.addView(eventsHeader);

        addSpacerView(eventsContainer, 12);

        // Add each event with elegant formatting
        for (WedCard.Event event : block.events) {
            LinearLayout eventRow = new LinearLayout(context);
            eventRow.setOrientation(LinearLayout.VERTICAL);
            eventRow.setGravity(Gravity.CENTER);
            eventRow.setPadding(0, 8, 0, 8);

            // Event title
            TextView eventTitle = createStyledTextView(
                    event.title,
                    16,
                    Typeface.BOLD,
                    Gravity.CENTER,
                    DARK_RED
            );

            // Event time
            TextView eventTime = createStyledTextView(
                    event.time,
                    14,
                    Typeface.NORMAL,
                    Gravity.CENTER,
                    BURGUNDY
            );

            eventRow.addView(eventTitle);
            eventRow.addView(eventTime);
            eventsContainer.addView(eventRow);

            // Add decorative line between events
            if (block.events.indexOf(event) < block.events.size() - 1) {
                TextView divider = createStyledTextView(
                        "♦",
                        12,
                        Typeface.NORMAL,
                        Gravity.CENTER,
                        GOLD_COLOR
                );
                divider.setPadding(0, 8, 0, 8);
                eventsContainer.addView(divider);
            }
        }

        container.addView(eventsContainer);
    }

    private TextView createStyledTextView(String text, int spSize, int style, int gravity, int color) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, spSize);
        tv.setTypeface(null, style);
        tv.setGravity(gravity);
        tv.setTextColor(color);

        // Enhanced layout parameters for better positioning
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 4, 8, 4);
        tv.setLayoutParams(params);

        return tv;
    }

    private void addSpacerView(LinearLayout container, int heightDp) {
        View spacer = new View(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(heightDp)
        );
        spacer.setLayoutParams(params);
        container.addView(spacer);
    }

    private int dpToPx(int dp) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        ));
    }

    @Override
    public int getItemCount() {
        return folds.size();
    }

    public static class FoldViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        LinearLayout blockContainer;

        public FoldViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.fold_image);
            blockContainer = itemView.findViewById(R.id.block_container);
        }
    }
}