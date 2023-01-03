package me.pizza.suggestions.database;

import me.pizza.suggestions.database.entity.impl.AcceptedSuggestion;
import me.pizza.suggestions.database.entity.impl.DeclinedSuggestion;
import me.pizza.suggestions.database.entity.impl.UnverifiedSuggestion;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface SuggestionMapper {

    @Select("SELECT * FROM unverified")
    List<UnverifiedSuggestion> getUnverifiedSuggestions();

    @Select("SELECT * FROM accepted")
    List<AcceptedSuggestion> getAcceptedSuggestions();

    @Select("SELECT * FROM declined")
    List<DeclinedSuggestion> getDeclinedSuggestions();

    @Select("SELECT * FROM implemented")
    List<DeclinedSuggestion> getImplementedSuggestions();

    @Insert("INSERT INTO unverified (suggestion_id, message_id, author_id, content) VALUES (#{suggestionId}, #{messageId}, #{authorId}, #{content})")
    void insertUnverifiedSuggestion(UnverifiedSuggestion suggestion);

    @Delete("DELETE FROM unverified WHERE suggestion_id = #{suggestion_id}")
    void deleteUnverifiedSuggestion(@Param("suggestion_id") long suggestionId);

    @Select("SELECT * FROM unverified WHERE message_id = #{messageId}")
    UnverifiedSuggestion getUnverifiedSuggestionByMessageId(@Param("messageId") long messageId);

    @Insert("INSERT INTO accepted (suggestion_id, message_id, author_id, content, accepter) " +
            "SELECT suggestion_id, message_id, author_id, content, #{accepter} FROM unverified WHERE suggestion_id = #{suggestion_id}")
    void moveToAccepted(@Param("suggestion_id") long suggestionId, @Param("accepter") long accepter);

    @Insert("INSERT INTO declined (suggestion_id, message_id, author_id, content, decliner) " +
            "SELECT suggestion_id, message_id, author_id, content, #{decliner} FROM unverified WHERE suggestion_id = #{suggestion_id}")
    void moveToDeclined(@Param("suggestion_id") long suggestionId, @Param("decliner") long decliner);

    @Insert("INSERT INTO implemented (suggestion_id, message_id, author_id, content, implementer) " +
            "SELECT suggestion_id, message_id, author_id, content, #{implementer} FROM unverified WHERE suggestion_id = #{suggestion_id}")
    void moveToImplemented(@Param("suggestion_id") long suggestionId, @Param("implementer") long implementer);
}