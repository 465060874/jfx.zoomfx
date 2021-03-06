package dejv.zoomfx.demo.beans;

import java.nio.file.Paths;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import dejv.commons.config.ConfigException;
import dejv.commons.config.ConfigProvider;
import dejv.commons.config.json.JSONConfigProvider;

/**
 * <p>
 *
 * @author dejv78 (dejv78.github.io)
 * @since 1.0.0
 */
@Configuration
@Lazy
public class Config {


    @Bean
    public ConfigProvider getConfigProvider() throws ConfigException {
        return new JSONConfigProvider(Paths.get("config.json"));
    }

}
