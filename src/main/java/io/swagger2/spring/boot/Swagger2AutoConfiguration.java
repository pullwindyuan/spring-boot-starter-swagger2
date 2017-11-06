package io.swagger2.spring.boot;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.ObjectUtils;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;

import io.swagger2.spring.boot.config.DocketInfo;
import io.swagger2.spring.boot.config.GlobalOperationParameter;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
@Import({ Swagger2Configuration.class })
@ConditionalOnClass({ Docket.class })
@ConditionalOnProperty(name = { "swagger.enable" }, havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties({ Swagger2Properties.class })
public class Swagger2AutoConfiguration implements BeanFactoryAware {

	private BeanFactory beanFactory;

	@Bean
	@ConditionalOnMissingBean
	public Swagger2Properties swaggerProperties() {
		return new Swagger2Properties();
	}
	
	protected ApiInfo apiInfo(Swagger2Properties properties) {
       
		ApiInfo apiInfo = new ApiInfoBuilder()
				.title(properties.getTitle())
				.description(properties.getDescription())
				.version(properties.getVersion())
				.license(properties.getLicense())
				.licenseUrl(properties.getLicenseUrl())
				.contact(new Contact(properties.getContact().getName(), properties.getContact().getUrl(), properties.getContact().getEmail()))
				.termsOfServiceUrl(properties.getTermsOfServiceUrl()).build();
		
		return apiInfo;
		
    }
	
	protected ApiInfo apiInfo(Swagger2Properties properties, DocketInfo docketInfo) {

		ApiInfo apiInfo = new ApiInfoBuilder()
				.title(docketInfo.getTitle().isEmpty() ? properties.getTitle() : docketInfo.getTitle())
				.description(docketInfo.getDescription().isEmpty() ? properties.getDescription() : docketInfo.getDescription())
				.version(docketInfo.getVersion().isEmpty() ? properties.getVersion() : docketInfo.getVersion())
				.license(docketInfo.getLicense().isEmpty() ? properties.getLicense() : docketInfo.getLicense())
				.licenseUrl(docketInfo.getLicenseUrl().isEmpty() ? properties.getLicenseUrl() : docketInfo.getLicenseUrl())
				.contact(new Contact(
						docketInfo.getContact().getName().isEmpty() ? properties.getContact().getName() : docketInfo.getContact().getName(),
						docketInfo.getContact().getUrl().isEmpty() ? properties.getContact().getUrl() : docketInfo.getContact().getUrl(),
						docketInfo.getContact().getEmail().isEmpty() ? properties.getContact().getEmail() : docketInfo.getContact().getEmail()))
				.termsOfServiceUrl(docketInfo.getTermsOfServiceUrl().isEmpty() ? properties.getTermsOfServiceUrl() : docketInfo.getTermsOfServiceUrl())
				.build();

		 return apiInfo;
		
    }
	
	@Bean
	@ConditionalOnMissingBean
	public List<Docket> createRestApi(Swagger2Properties properties) {
		
		ConfigurableBeanFactory configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;
		
		List<Docket> docketList = new LinkedList<Docket>();
		
		// base-path处理
		
		// 当没有配置任何path的时候，解析/**
		if (properties.getBasePath().isEmpty()) {
			properties.getBasePath().add("/**");
		}
		List<Predicate<String>> basePath = new ArrayList<Predicate<String>>();
		for (String path : properties.getBasePath()) {
			basePath.add(PathSelectors.ant(path));
		}

		// exclude-path处理
		List<Predicate<String>> excludePath = new ArrayList<Predicate<String>>();
		for (String path : properties.getExcludePath()) {
			excludePath.add(PathSelectors.ant(path));
		}
		
		// 没有分组
		if ( ObjectUtils.isEmpty(properties.getDockets()) ) {

			Docket docket = new Docket(DocumentationType.SWAGGER_2)
					.host(properties.getHost())
					.apiInfo(this.apiInfo(properties)).select()
					.apis(RequestHandlerSelectors.basePackage(properties.getBasePackage()))
					.paths(Predicates.and(Predicates.not(Predicates.or(excludePath)), Predicates.or(basePath)))
					.build()
					.directModelSubstitute(org.joda.time.LocalDate.class, java.sql.Date.class)
	                .directModelSubstitute(org.joda.time.DateTime.class, java.util.Date.class);
			
			//注册到上下文
			configurableBeanFactory.registerSingleton("defaultDocket", docket);
			
			docketList.add(docket);
			
		}
		// 分组创建
		else {
		
			for (DocketInfo docketInfo : properties.getDockets()) {
				
				Docket docket = new Docket(DocumentationType.SWAGGER_2)
						.host(properties.getHost())
						.apiInfo(this.apiInfo(properties, docketInfo))
						.globalOperationParameters(assemblyGlobalOperationParameters(properties.getGlobalOperationParameters(),
	                            docketInfo.getGlobalOperationParameters()))
						.groupName(docketInfo.getGroupName())
						.select()
						.apis(RequestHandlerSelectors.basePackage(docketInfo.getBasePackage()))
						.paths(Predicates.and(Predicates.not(Predicates.or(excludePath)), Predicates.or(basePath))).build();

				//注册到上下文
				configurableBeanFactory.registerSingleton(docketInfo.getGroupName(), docket);
				
				docketList.add(docket);
			}
			
		}
		return docketList;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
	
	 private List<Parameter> buildGlobalOperationParametersFromSwaggerProperties(
	            List<GlobalOperationParameter> globalOperationParameters) {
	        List<Parameter> parameters = Lists.newArrayList();

	        if (Objects.isNull(globalOperationParameters)) {
	            return parameters;
	        }
	        for (GlobalOperationParameter globalOperationParameter : globalOperationParameters) {
	            parameters.add(new ParameterBuilder()
	                    .name(globalOperationParameter.getName())
	                    .description(globalOperationParameter.getDescription())
	                    .modelRef(new ModelRef(globalOperationParameter.getModelRef()))
	                    .parameterType(globalOperationParameter.getParameterType())
	                    .required(Boolean.parseBoolean(globalOperationParameter.getRequired()))
	                    .build());
	        }
	        return parameters;
	    }

	    /**
	     * 局部参数按照name覆盖局部参数
	     *
	     * @param globalOperationParameters
	     * @param docketOperationParameters
	     * @return
	     */
	    private List<Parameter> assemblyGlobalOperationParameters(
	            List<GlobalOperationParameter> globalOperationParameters,
	            List<GlobalOperationParameter> docketOperationParameters) {

	        if (Objects.isNull(docketOperationParameters) || docketOperationParameters.isEmpty()) {
	            return buildGlobalOperationParametersFromSwaggerProperties(globalOperationParameters);
	        }

	        Set<String> docketNames = docketOperationParameters.stream()
	                .map(GlobalOperationParameter::getName)
	                .collect(Collectors.toSet());

	        List<GlobalOperationParameter> resultOperationParameters = Lists.newArrayList();

	        if (Objects.nonNull(globalOperationParameters)) {
	            for (GlobalOperationParameter parameter : globalOperationParameters) {
	                if (!docketNames.contains(parameter.getName())) {
	                    resultOperationParameters.add(parameter);
	                }
	            }
	        }

	        resultOperationParameters.addAll(docketOperationParameters);
	        return buildGlobalOperationParametersFromSwaggerProperties(resultOperationParameters);
	    }
	    
}
