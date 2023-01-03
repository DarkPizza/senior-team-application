package me.pizza.suggestions.service;

import lombok.Getter;
import me.pizza.suggestions.database.SuggestionMapper;
import me.pizza.suggestions.database.entity.Suggestion;
import me.pizza.suggestions.database.entity.SuggestionType;
import me.pizza.suggestions.database.entity.impl.UnverifiedSuggestion;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SuggestionService {

    @Getter
    private final SuggestionMapper suggestionMapper;
    private final SqlSession sqlSession;
    private final Logger logger = LoggerFactory.getLogger(SuggestionService.class);

    public SuggestionService(SuggestionMapper suggestionMapper, SqlSession sqlSession) {
        this.suggestionMapper = suggestionMapper;
        this.sqlSession = sqlSession;
    }

    public List<? extends Suggestion> getSuggestionByType(SuggestionType suggestionType) {
        switch (suggestionType) {
            case ACCEPTED:
                return this.suggestionMapper.getAcceptedSuggestions();
            case DECLINED:
                return this.suggestionMapper.getDeclinedSuggestions();
            case UNVERIFIED:
                return this.suggestionMapper.getUnverifiedSuggestions();
            case IMPLEMENTED:
                return this.suggestionMapper.getImplementedSuggestions();
            default:
                return Collections.emptyList();
        }
    }

    public Optional<Suggestion> getSuggestionByType(int id, SuggestionType suggestionType) {
        final List<Suggestion> suggestionList = (List<Suggestion>) this.getSuggestionByType(suggestionType);

        return suggestionList.stream().filter(it -> it.suggestionId() == id).findFirst();
    }

    public void createSuggestion(UnverifiedSuggestion suggestion) {
        try {
            this.suggestionMapper.insertUnverifiedSuggestion(suggestion);
            this.sqlSession.commit();

            this.logger.info("Created suggestion: {}", suggestion.getSuggestionId());
        } catch (Exception e) {
            this.logger.error("Error creating suggestion:", e);
            this.sqlSession.rollback();
        }
    }

    private void acceptSuggestion(int suggestionId, long accepter) {
        try {
            this.suggestionMapper.moveToAccepted(suggestionId, accepter);
            this.suggestionMapper.deleteUnverifiedSuggestion(suggestionId);
            this.sqlSession.commit();

            this.logger.info("Suggestion accepted: {}", suggestionId);
        } catch (Exception e) {
            logger.error("Error accepting suggestion:", e);
            this.sqlSession.rollback();
        }
    }

    private void declineSuggestions(int suggestionId, long decliner) {
        try {
            this.suggestionMapper.moveToDeclined(suggestionId, decliner);
            this.suggestionMapper.deleteUnverifiedSuggestion(suggestionId);
            this.sqlSession.commit();

            this.logger.info("Suggestion declined: {}", suggestionId);
        } catch (Exception e) {
            logger.error("Error denying suggestion:", e);
            this.sqlSession.rollback();
        }
    }

    private void implementSuggestion(int suggestionId, long implementer) {
        try {
            this.suggestionMapper.moveToImplemented(suggestionId, implementer);
            this.suggestionMapper.deleteUnverifiedSuggestion(suggestionId);
            this.sqlSession.commit();

            this.logger.info("Suggestion implemented: {}", suggestionId);
        } catch (Exception e) {
            logger.error("Error implementing suggestion:", e);
            this.sqlSession.rollback();
        }
    }

    public void moveSuggestionByCategory(int suggestionId, SuggestionType type, long author) {
        switch (type) {
            case ACCEPTED:
                this.acceptSuggestion(suggestionId, author);
                break;
            case DECLINED:
                this.declineSuggestions(suggestionId, author);
                break;
            case IMPLEMENTED:
                this.implementSuggestion(suggestionId, author);
        }
    }
}
