package org.bibalex.eol.harvester;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
public class MainHarvester extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(MainHarvester.class);
    }

    public static void main (String [] args){
        SpringApplication.run(MainHarvester.class, args);
    }
}
