package me.pizza.suggestions.database;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DatabaseMapper {

    @Update("CREATE DATABASE IF NOT EXISTS suggestions")
    void createSuggestionDatabase();

    @Update("CREATE TABLE  IF NOT EXISTS unverified (suggestion_id INTEGER PRIMARY KEY AUTO_INCREMENT, message_id BIGINT NOT NULL, author_id BIGINT NOT NULL, content VARCHAR(255) NOT NULL)")
    void createUnverifiedTable();

    @Update("CREATE TABLE IF NOT EXISTS declined (suggestion_id INTEGER PRIMARY KEY AUTO_INCREMENT, message_id BIGINT NOT NULL, author_id BIGINT NOT NULL, content VARCHAR(255) NOT NULL, decliner BIGINT NOT NULL)")
    void createDeclinedTable();

    @Update("CREATE TABLE IF NOT EXISTS accepted (suggestion_id INTEGER PRIMARY KEY AUTO_INCREMENT, message_id BIGINT NOT NULL, author_id BIGINT NOT NULL, content VARCHAR(255) NOT NULL, accepter BIGINT NOT NULL)")
    void createAcceptedTable();

    @Update("CREATE TABLE IF NOT EXISTS implemented (suggestion_id INTEGER PRIMARY KEY AUTO_INCREMENT, message_id BIGINT NOT NULL, author_id BIGINT NOT NULL, content VARCHAR(255) NOT NULL, implementer BIGINT NOT NULL)")
    void createImplemented();
}
