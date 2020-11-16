package com.zcjhwl.mybatis.gen.utils;

import com.google.common.base.CaseFormat;
import com.zcjhwl.mybatis.gen.Config;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.List;
import java.util.Map;

@Slf4j
public class ConfigurationParser {


    /**
     * Generate a inputStream of a xml file
     * @param ymlFile yml config file
     * @return a inputStream of a xml file
     * @throws ParserConfigurationException if exception occurred in factory.newDocumentBuilder
     */
    public InputStream createXML(File ymlFile) throws ParserConfigurationException {
        // Create document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        // set DOCTYPE
        setDocType(document);
        Element element = document.createElement("generatorConfiguration");
        // context node
        Element context = document.createElement("context");
        context.setAttribute("id","simple");
        // Reads the yml file
        Map<String, Object> objectMapFromSource = YmlUtil.getObjectMapFromSource(ymlFile);
        // Starting parse the yml file
        Map<String, Object> objectMap = (Map<String, Object>) objectMapFromSource.get("mybatisGenerator");
        String targetRuntime = (String) objectMap.get("targetRuntime");
        context.setAttribute("targetRuntime",targetRuntime);

        // parse plugin configuration
        parsePlugins(objectMap,document,context);

        // disable default comment configuration
        disableDefaultComment(document,context);

        // parse datasource configuration
        parseConnectionConfig(objectMap,document,context);

        // set model date fields type
        setJavaTypeResolver(objectMap,document,context);

        // parse target package configuration
        parseTargetPackage(objectMap,document,context);

        // parse tables configuration
        parseTables(objectMap,document,context);

        // add elements to the document
        element.appendChild(context);
        document.appendChild(element);
        return getXMLInputStream(document);

    }

    private void setDocType(Document document){
        //Create doc type
        DOMImplementation domImpl = document.getImplementation();
        DocumentType doctype = domImpl.createDocumentType("generatorConfiguration", "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN", "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd");
        document.appendChild(doctype);
    }

    private InputStream getXMLInputStream(Document document){
        TransformerFactory tff = TransformerFactory.newInstance();
        try {
            Transformer tf = tff.newTransformer();
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, document.getDoctype().getPublicId());
            tf.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, document.getDoctype().getSystemId());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Result outputTarget = new StreamResult(outputStream);
            tf.transform(new DOMSource(document),outputTarget);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (TransformerException e) {
            log.error(e.getMessage(),e);
        }
        return null;
    }

    private void appendMapperAnnotationPlugin(Document document, Element context) {
        Element pluginSwagger = document.createElement("plugin");
        pluginSwagger.setAttribute("type","org.mybatis.generator.plugins.MapperAnnotationPlugin");
        context.appendChild(pluginSwagger);
    }

    private void appendSerializablePlugin(Document document, Element context) {
        Element pluginSwagger = document.createElement("plugin");
        pluginSwagger.setAttribute("type","org.mybatis.generator.plugins.SerializablePlugin");
        context.appendChild(pluginSwagger);
    }


    /**
     * Generates Swagger2 annotations on models
     * <plugin type="Swagger2Plugin">
     * 		<property name="apiModelAnnotationPackage" value="io.swagger.annotations.ApiModel" />
     * 		<property name="apiModelPropertyAnnotationPackage" value="io.swagger.annotations.ApiModelProperty" />
     * </plugin>
     */
    private void appendSwaggerPlugin(Document document, Element context){
        Element pluginSwagger = document.createElement("plugin");
        pluginSwagger.setAttribute("type","Swagger2Plugin");
        Element pluginSwaggerProperty1 = document.createElement("property");
        pluginSwaggerProperty1.setAttribute("name","apiModelAnnotationPackage");
        pluginSwaggerProperty1.setAttribute("value","io.swagger.annotations.ApiModel");
        Element pluginSwaggerProperty2 = document.createElement("property");
        pluginSwaggerProperty2.setAttribute("name","apiModelPropertyAnnotationPackage");
        pluginSwaggerProperty2.setAttribute("value","io.swagger.annotations.ApiModelProperty");
        pluginSwagger.appendChild(pluginSwaggerProperty1);
        pluginSwagger.appendChild(pluginSwaggerProperty2);
        context.appendChild(pluginSwagger);
    }


    /**
     * Generates lombok annotations on models
     * <plugin type="LombokPlugin" >
     * 		<property name="hasLombok" value="true"/>
     * 	</plugin>
     */
    private void appendLombokPlugin(Document document, Element context){
        Element pluginSwagger = document.createElement("plugin");
        pluginSwagger.setAttribute("type","LombokPlugin");
        Element pluginSwaggerProperty1 = document.createElement("property");
        pluginSwaggerProperty1.setAttribute("name","hasLombok");
        pluginSwaggerProperty1.setAttribute("value","true");
        pluginSwagger.appendChild(pluginSwaggerProperty1);
        context.appendChild(pluginSwagger);
    }

    /**
     * Generates lombok annotations on models
     */
    private void appendCommentPlugin(Document document, Element context){
        Element pluginSwagger = document.createElement("plugin");
        pluginSwagger.setAttribute("type","CommentPlugin");
        Element pluginSwaggerProperty1 = document.createElement("property");
        pluginSwaggerProperty1.setAttribute("name","hasComment");
        pluginSwaggerProperty1.setAttribute("value","true");
        pluginSwagger.appendChild(pluginSwaggerProperty1);
        context.appendChild(pluginSwagger);
    }


    private void parseConnectionConfig(Map<String, Object> objectMap, Document document, Element context){
        Map<String, Object> datasource = (Map<String, Object>)objectMap.get("datasource");
        String type = (String) datasource.get("type");
        String address = (String) datasource.get("address");
        String db = (String) datasource.get("db");
        String username = (String) datasource.get("username");
        String password = String.valueOf(datasource.get("password"));
        Element connection = document.createElement("jdbcConnection");
        String driverName = null;
        String url = null;
        if("mysql".equals(type)){
            driverName= Config.DRIVER_MYSQL;
            url=Config.URL_MYSQL_PREFIX+address+Config.URL_MYSQL_SUFFIX+db;
        }else if("sqlserver".equals(type)){
            driverName=Config.DRIVER_SQL_SERVER;
            url=Config.URL_SQL_SERVER_PREFIX+address+Config.URL_SQL_SERVER_SUFFIX+db;
        }else if("oracle".equals(type)){
            driverName=Config.DRIVER_SQL_ORACLE;
            url=Config.URL_ORACLE_PREFIX+address+Config.URL_ORACLE_SUFFIX+db;
        }else{
            //TODO add more db support
        }

        connection.setAttribute("driverClass",driverName);
        connection.setAttribute("connectionURL",url);
        connection.setAttribute("userId",username);
        connection.setAttribute("password",password);

        Element temp = document.createElement("property");
        temp.setAttribute("name","nullCatalogMeansCurrent");
        temp.setAttribute("value","true");
        connection.appendChild(temp);
        context.appendChild(connection);
    }

    private void parsePlugins(Map<String, Object> objectMap, Document document, Element context){
        Map<String, Object> plugins = (Map<String, Object>) objectMap.get("plugins");
        Boolean comment = (Boolean) plugins.get("comment");
        Boolean lombok = (Boolean) plugins.get("lombok");
        Boolean swagger = (Boolean) plugins.get("swagger");
        Boolean serializable = (Boolean) plugins.get("serializable");
        Boolean mapperAnnotation = (Boolean) plugins.get("mapperAnnotation");
        // override xml mapper
        appendOverrideXmlPlugin(document, context);
        //  swagger
        if(swagger){
            appendSwaggerPlugin(document,context);
        }
        if(lombok){
            appendLombokPlugin(document,context);
        }
        if(comment){
            appendCommentPlugin(document,context);
        }
        if(serializable){
            appendSerializablePlugin(document,context);
        }
        if(mapperAnnotation){
            appendMapperAnnotationPlugin(document,context);
        }
    }

    private void appendOverrideXmlPlugin(Document document, Element context) {
        Element overridePlugin = document.createElement("plugin");
        overridePlugin.setAttribute("type","org.mybatis.generator.plugins.UnmergeableXmlMappersPlugin");
        context.appendChild(overridePlugin);
    }

    private void disableDefaultComment(Document document, Element context){
        Element temp = document.createElement("commentGenerator");
        Element temp1 = document.createElement("property");
        temp1.setAttribute("name","suppressAllComments");
        temp1.setAttribute("value","true");
        Element temp2 = document.createElement("property");
        temp2.setAttribute("name","suppressDate");
        temp2.setAttribute("value","true");
        temp.appendChild(temp1);
        temp.appendChild(temp2);
        context.appendChild(temp);
    }


    private void setJavaTypeResolver(Map<String, Object> objectMap, Document document, Element context){
        Boolean java8 = (Boolean) objectMap.get("java8");
        if(java8==null){
            java8=false;
        }
        Element temp3 = document.createElement("javaTypeResolver");
        Element temp4 = document.createElement("property");
        temp4.setAttribute("name","forceBigDecimals");
        temp4.setAttribute("value","false");
        Element temp5 = document.createElement("property");
        temp5.setAttribute("name","useJSR310Types");
        temp5.setAttribute("value",String.valueOf(java8));
        temp3.appendChild(temp4);
        temp3.appendChild(temp5);
        context.appendChild(temp3);
    }


    private void parseTargetPackage(Map<String, Object> objectMap, Document document, Element context){
        Map<String, Object> targetPackage = (Map<String, Object>) objectMap.get("targetPackage");
        String modelPackage = (String) targetPackage.get("model");
        String mapperPackage = (String) targetPackage.get("mapper");
        String xmlmapperPackage = (String) targetPackage.get("xmlmapper");
        Boolean javaXmlFilesSamePackage = (Boolean) targetPackage.get("javaXmlFilesSamePackage");
        Element javaModel = document.createElement("javaModelGenerator");
        javaModel.setAttribute("targetPackage",modelPackage);
        javaModel.setAttribute("targetProject","src/main/java");
        context.appendChild(javaModel);

        Element javaMap = document.createElement("javaClientGenerator");
        javaMap.setAttribute("type","XMLMAPPER");
        javaMap.setAttribute("targetPackage",xmlmapperPackage);
        javaMap.setAttribute("targetProject","src/main/resources");

        Element sqlMap = document.createElement("sqlMapGenerator");
        sqlMap.setAttribute("targetPackage",mapperPackage);
        if(javaXmlFilesSamePackage){
            sqlMap.setAttribute("targetProject","src/main/java");
        }else{
            sqlMap.setAttribute("targetProject","src/main/resources");
        }
        context.appendChild(sqlMap);
        context.appendChild(javaMap);
    }


    private void parseTables(Map<String, Object> objectMap, Document document, Element context){
        String mapperSuffixName = (String) objectMap.get("mapperSuffixName");
        List<String> tableNames = ( List<String>) objectMap.get("tables");
        for(String tableName : tableNames){
            Element pluginSwagger = document.createElement("table");
            pluginSwagger.setAttribute("tableName",tableName);
            String mapperName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tableName)+CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, mapperSuffixName);
            pluginSwagger.setAttribute("mapperName",mapperName);
            Boolean disableExample = (Boolean) objectMap.get("disableExample");
            if(disableExample){
                pluginSwagger.setAttribute("enableCountByExample","false");
                pluginSwagger.setAttribute("enableUpdateByExample","false");
                pluginSwagger.setAttribute("enableDeleteByExample","false");
                pluginSwagger.setAttribute("enableSelectByExample","false");
                pluginSwagger.setAttribute("selectByExampleQueryId","false");
            }
            context.appendChild(pluginSwagger);
        }

    }

}