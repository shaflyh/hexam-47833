package com.hand.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.service.Tag;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Swagger Api 描述配置
 */
@Configuration
public class SwaggerTags {

    public static final String FILE_MANAGEMENT = "File Management";
    public static final String EXAMPLE = "Example";
    public static final String USER = "User";
    public static final String TASK = "Task";
    public static final String MESSENGER = "Messeger";
    public static final String FILE = "File";


    @Autowired
    public SwaggerTags(Docket docket) {
        docket.tags(
                new Tag(EXAMPLE, "EXAMPLE 案例"),
                new Tag(USER, "User Management"),
                new Tag(TASK, "Task Management"),
                new Tag(MESSENGER, "Messenger Management"),
                new Tag(FILE, "File Management")
        );
    }
}
