package me.pizza.suggestions;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import me.pizza.suggestions.config.BotConfig;
import me.pizza.suggestions.config.DatabaseConfig;
import me.pizza.suggestions.database.DatabaseMapper;
import me.pizza.suggestions.database.SuggestionMapper;
import me.pizza.suggestions.event.GuildEvents;
import me.pizza.suggestions.event.InteractionEvents;
import me.pizza.suggestions.service.SuggestionService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.skife.config.ConfigurationObjectFactory;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SuggestionBot {

    private static final String DEFAULT_PATH = "src/main/resources/bot/";

    public static void main(String[] args) throws FileNotFoundException {
        final ScheduledExecutorService threadPool = Executors
                .newScheduledThreadPool(Math.max(2, ForkJoinPool.getCommonPoolParallelism()));

        final OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder()
                .connectionPool(new ConnectionPool(2, 20, TimeUnit.SECONDS))
                .build();

        RestAction.setDefaultTimeout(10, TimeUnit.SECONDS);

        final DatabaseConfig databaseConfig = createConfig(DatabaseConfig.class, DEFAULT_PATH + "database-config.properties")
                .orElseThrow(() -> new FileNotFoundException("databasse config file not found"));

        final SqlSession sqlSession = createSqlSession(databaseConfig);
        final SuggestionService suggestionService = createSuggestionService(sqlSession);

        final BotConfig botConfig = createConfig(BotConfig.class,DEFAULT_PATH + "bot-config.properties")
                .orElseThrow(() -> new FileNotFoundException("bot config file not found"));

        final JDA jda = createJDA(okHttpClient, threadPool, botConfig);
        jda.addEventListener(new GuildEvents(botConfig),
                new InteractionEvents(botConfig, suggestionService));
    }

    private static <T> Optional<T> createConfig(Class<T> configClass, String configPath) {
        try (FileInputStream configFile = new FileInputStream(configPath)) {
            final Properties configProps = new Properties();
            configProps.load(configFile);

            final ConfigurationObjectFactory configFactory = new ConfigurationObjectFactory(configProps);
            return Optional.of(configFactory.build(configClass));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    private static SqlSession createSqlSession(DatabaseConfig databaseConfig) {
        final DataSource dataSource = new PooledDataSource("com.mysql.cj.jdbc.Driver",
                databaseConfig.getUrl(), databaseConfig.getUser(), databaseConfig.getPass());
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);

        configuration.addMapper(DatabaseMapper.class);
        configuration.addMapper(SuggestionMapper.class);

        final SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
        final SqlSession sqlSession = sqlSessionFactory.openSession();

        setupDatabase(sqlSession);

        return sqlSessionFactory.openSession();
    }

    private static SuggestionService createSuggestionService(SqlSession sqlSession) {
        final SuggestionMapper unverifiedMapper = sqlSession.getMapper(SuggestionMapper.class);
        return new SuggestionService(unverifiedMapper, sqlSession);
    }

    private static void setupDatabase(SqlSession sqlSession) {
        final DatabaseMapper databaseMapper = sqlSession.getMapper(DatabaseMapper.class);

        databaseMapper.createImplemented();
        databaseMapper.createUnverifiedTable();
        databaseMapper.createAcceptedTable();
        databaseMapper.createDeclinedTable();
    }

    private static JDA createJDA(OkHttpClient okHttpClient, ScheduledExecutorService threadPool, BotConfig botConfig) {
        final JDABuilder jdaBuilder = JDABuilder
                .createDefault(botConfig.getToken())
                .setHttpClient(okHttpClient)
                .setGatewayPool(threadPool)
                .setCallbackPool(threadPool)
                .setRateLimitPool(threadPool)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS));

        return jdaBuilder.build();
    }
}