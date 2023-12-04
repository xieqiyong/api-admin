package com.hz.api.admin.web.config.util;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI springShopOpenApi() {
        Info info = new Info()
                .title("后台管理系统")
                .version("v1.0")
                .description("前后端分离")
                .license(new License()
                        .name("Apache 2.0")
                        .url("")
                );

        return new OpenAPI().info(info).externalDocs(new ExternalDocumentation()
                .description("doc").url(""));
    }
}
