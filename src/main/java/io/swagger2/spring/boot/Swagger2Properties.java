package io.swagger2.spring.boot;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import io.swagger2.spring.boot.config.Contact;
import io.swagger2.spring.boot.config.DocketInfo;
import io.swagger2.spring.boot.config.GlobalOperationParameter;

/**
 * 参考 ： https://github.com/dyc87112/spring-boot-starter-swagger
 */
@ConfigurationProperties(Swagger2Properties.PREFIX)
public class Swagger2Properties {

	public static final String PREFIX = "swagger";

	/** 是否开启swagger **/
	private Boolean enabled = false;
	/** 标题 **/
	private String title = "";
	/** 描述 **/
	private String description = "";
	/** 版本 **/
	private String version = "";
	/** 许可证 **/
	private String license = "";
	/** 许可证URL **/
	private String licenseUrl = "";
	/** 服务条款URL **/
	private String termsOfServiceUrl = "";
	/** Swagger在线文档联系人信息 **/
	private Contact contact = new Contact();
	/** Swagger会解析的包路径 **/
	private String basePackage = "";
	/** Swagger会解析的url规则 **/
	private List<String> basePath = new ArrayList<String>();
	/** 在basePath基础上需要排除的url规则 **/
	private List<String> excludePath = new ArrayList<String>();
	/** Swagger分组文档 **/
	@NestedConfigurationProperty
	private List<DocketInfo> dockets = new ArrayList<DocketInfo>();
	/** host信息 **/
	private String host = "";
	/** 全局参数配置 **/
	private List<GlobalOperationParameter> globalOperationParameters;

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public String getLicenseUrl() {
		return licenseUrl;
	}

	public void setLicenseUrl(String licenseUrl) {
		this.licenseUrl = licenseUrl;
	}

	public String getTermsOfServiceUrl() {
		return termsOfServiceUrl;
	}

	public void setTermsOfServiceUrl(String termsOfServiceUrl) {
		this.termsOfServiceUrl = termsOfServiceUrl;
	}

	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}

	public String getBasePackage() {
		return basePackage;
	}

	public void setBasePackage(String basePackage) {
		this.basePackage = basePackage;
	}

	public List<String> getBasePath() {
		return basePath;
	}

	public void setBasePath(List<String> basePath) {
		this.basePath = basePath;
	}

	public List<String> getExcludePath() {
		return excludePath;
	}

	public void setExcludePath(List<String> excludePath) {
		this.excludePath = excludePath;
	}

	public List<DocketInfo> getDockets() {
		return dockets;
	}

	public void setDockets(List<DocketInfo> dockets) {
		this.dockets = dockets;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public List<GlobalOperationParameter> getGlobalOperationParameters() {
		return globalOperationParameters;
	}

	public void setGlobalOperationParameters(List<GlobalOperationParameter> globalOperationParameters) {
		this.globalOperationParameters = globalOperationParameters;
	}

}
