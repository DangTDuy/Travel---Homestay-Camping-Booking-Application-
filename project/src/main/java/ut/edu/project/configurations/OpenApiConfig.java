package ut.edu.project.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // Mô tả thông tin API (Tên, mô tả, phiên bản)
                .info(new Info()
                        .title("Chỗ Tốt Travel API")
                        .version("1.0")
                        .description("Hệ thống API cho dịch vụ Homestay và Camping của Chỗ Tốt Travel.")
                )
                // Cấu hình bảo mật sử dụng JWT Bearer Token
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                )
                // Yêu cầu bảo mật đối với tất cả các endpoint
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME));
    }
}
