package com.appdevelopersblog.estore.ProductService;

import com.appdevelopersblog.estore.ProductService.command.interceptor.CreateProductCommandInterceptor;
import com.appdevelopersblog.estore.ProductService.core.errorhandling.ProductsServiceEventsErrorHandler;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.config.EventProcessingConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationContext;

@EnableDiscoveryClient
@SpringBootApplication
public class ProductServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductServiceApplication.class, args);
	}

	@Autowired
	public void registerCreateProductCommandInterceptor(ApplicationContext context, CommandBus commandBus){
		commandBus.registerDispatchInterceptor(context.getBean(CreateProductCommandInterceptor.class));
	}
	@Autowired
	public void configure(EventProcessingConfigurer config){
		config.registerListenerInvocationErrorHandler("product-group", conf -> new ProductsServiceEventsErrorHandler());
	}
}
