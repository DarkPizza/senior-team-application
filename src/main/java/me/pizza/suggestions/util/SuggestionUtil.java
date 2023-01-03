package me.pizza.suggestions.util;

import me.pizza.suggestions.database.entity.SuggestionType;

import java.awt.*;

public class SuggestionUtil {

    public static Color getColorForStatus(String status) {
        switch (status) {
            case "accept":
                return Color.GREEN;
            case "decline":
                return Color.RED;
            case "implement":
                return Color.YELLOW;
            default:
                return null;
        }
    }

    public static SuggestionType getCategoryForStatus(String status) {
        switch (status) {
            case "accept":
                return SuggestionType.ACCEPTED;
            case "decline":
                return SuggestionType.DECLINED;
            case "implement":
                return SuggestionType.IMPLEMENTED;
            default:
                return null;
        }
    }
}
