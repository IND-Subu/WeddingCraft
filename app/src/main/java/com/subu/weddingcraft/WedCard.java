package com.subu.weddingcraft;

import java.util.List;

public class WedCard {
    // Basic wedding info
    public String bride;
    public String groom;
    public String date;
    public String time;
    public String venue;
    public String host;
    public String rsvp;
    public String quote;
    public String contact_type;
    public String theme;

    // Extended metadata
    public String bride_parents;
    public String groom_parents;
    public String bride_position;
    public String groom_position;
    public String mantra;

    // Optional for organizing cards
    public String cardTitle;
//    public String cardDate;
//    public String foldDirection;

    // Core content
    public List<Fold> folds;

    public static class Fold {
        public String fold; // "front", "left", "right", "back"
//        public String name; // Optional UI label
        public String type; // e.g., "cover", "content", "back"
        public String backgroundColor;
        public String image;
        public String order; // Redundant
        public List<Block> blocks; // Multiple styled blocks of content
    }

    public static class Block {
        public String type; // e.g., "text", "heading", "quote", "event_list"
        public String content; // actual text
        public TextStyle textStyle;
        public List<Event> events; // only used if type == "event_list"
    }

    public static class TextStyle {
        public String font;
        public String color; // hex like "#FFFFFF"
        public int size; // in sp
    }

    public static class Event {
        public String title;
        public String time;
        public String location;
    }
}
