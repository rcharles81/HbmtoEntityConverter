package com.convert.hbm.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HbmParser {
    private static final Logger logger = LoggerFactory.getLogger(HbmParser.class);
    private final List<File> hbmFiles = new ArrayList<>();

    public void addHbmFile(File hbmFile) {
        try {
            hbmFiles.add(hbmFile);
            logger.info("Added HBM file: {}", hbmFile.getName());
        } catch (Exception e) {
            logger.error("Error adding HBM file: {}", hbmFile.getName(), e);
            throw new RuntimeException("Failed to parse HBM file", e);
        }
    }

    public List<EntityMetadata> parseMetadata() {
        List<EntityMetadata> entities = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Disable DTD validation and loading
            factory.setValidating(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver((publicId, systemId) -> {
                // Return empty input source to prevent DTD loading
                return new org.xml.sax.InputSource(new java.io.StringReader(""));
            });

            for (File hbmFile : hbmFiles) {
                Document doc = builder.parse(hbmFile);
                doc.getDocumentElement().normalize();

                // Get package name from hibernate-mapping
                Element hibernateMapping = doc.getDocumentElement();
                String packageName = hibernateMapping.getAttribute("package");
                if (packageName == null || packageName.isEmpty()) {
                    packageName = "com.example.model"; // Default package
                }

                // Parse class elements
                NodeList classList = doc.getElementsByTagName("class");
                for (int i = 0; i < classList.getLength(); i++) {
                    Node classNode = classList.item(i);
                    if (classNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element classElement = (Element) classNode;
                        EntityMetadata entityMetadata = parseClass(classElement, packageName);
                        
                        // Parse queries
                        parseQueries(classElement, entityMetadata);
                        
                        // Parse SQL queries
                        parseSqlQueries(classElement, entityMetadata);
                        
                        // Parse filters
                        parseFilters(hibernateMapping, entityMetadata);
                        
                        entities.add(entityMetadata);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to parse metadata", e);
            throw new RuntimeException("Failed to parse metadata", e);
        }
        
        return entities;
    }

    private EntityMetadata parseClass(Element classElement, String packageName) {
        EntityMetadata metadata = new EntityMetadata();
        String className = classElement.getAttribute("name");
        if (!className.contains(".")) {
            className = packageName + "." + className;
        }
        metadata.setClassName(className);
        metadata.setTableName(classElement.getAttribute("table"));

        // Parse ID
        Element idElement = (Element) classElement.getElementsByTagName("id").item(0);
        if (idElement != null) {
            IdMetadata id = new IdMetadata();
            id.setName(idElement.getAttribute("name"));
            id.setType(idElement.getAttribute("type"));
            id.setColumnName(idElement.getAttribute("column"));

            Element generatorElement = (Element) idElement.getElementsByTagName("generator").item(0);
            if (generatorElement != null) {
                id.setGeneratorClass(generatorElement.getAttribute("class"));
                NodeList paramElements = generatorElement.getElementsByTagName("param");
                for (int i = 0; i < paramElements.getLength(); i++) {
                    Element paramElement = (Element) paramElements.item(i);
                    id.getGeneratorParams().put(
                        paramElement.getAttribute("name"),
                        paramElement.getTextContent().trim()
                    );
                }
            }
            metadata.setId(id);
        }

        // Parse properties
        parseProperties(classElement, metadata);
        
        return metadata;
    }

    private void parseProperties(Element classElement, EntityMetadata entityMetadata) {
        // Parse regular properties
        NodeList propertyList = classElement.getElementsByTagName("property");
        for (int j = 0; j < propertyList.getLength(); j++) {
            Element propertyElement = (Element) propertyList.item(j);
            PropertyMetadata prop = new PropertyMetadata();
            
            prop.setName(propertyElement.getAttribute("name"));
            prop.setType(propertyElement.getAttribute("type"));
            
            // Get column name
            NodeList columnList = propertyElement.getElementsByTagName("column");
            if (columnList.getLength() > 0) {
                Element columnElement = (Element) columnList.item(0);
                prop.setColumnName(columnElement.getAttribute("name"));
            } else {
                // If no column specified, use property name
                prop.setColumnName(prop.getName());
            }
            
            entityMetadata.addProperty(prop);
        }
        
        // Parse many-to-one relationships
        NodeList manyToOneList = classElement.getElementsByTagName("many-to-one");
        for (int j = 0; j < manyToOneList.getLength(); j++) {
            Element manyToOneElement = (Element) manyToOneList.item(j);
            PropertyMetadata prop = new PropertyMetadata();
            
            prop.setName(manyToOneElement.getAttribute("name"));
            prop.setType(manyToOneElement.getAttribute("class"));
            
            // Get column name
            NodeList columnList = manyToOneElement.getElementsByTagName("column");
            if (columnList.getLength() > 0) {
                Element columnElement = (Element) columnList.item(0);
                prop.setColumnName(columnElement.getAttribute("name"));
            } else {
                // If no column specified, use property name + "_id"
                prop.setColumnName(prop.getName() + "_id");
            }
            
            entityMetadata.addProperty(prop);
        }
        
        // Parse one-to-many relationships
        NodeList oneToManyList = classElement.getElementsByTagName("one-to-many");
        for (int j = 0; j < oneToManyList.getLength(); j++) {
            Element oneToManyElement = (Element) oneToManyList.item(j);
            PropertyMetadata prop = new PropertyMetadata();
            
            // Get the set element (parent of one-to-many)
            Element setElement = (Element) oneToManyElement.getParentNode();
            prop.setName(setElement.getAttribute("name"));
            prop.setType("java.util.Set<" + oneToManyElement.getAttribute("class") + ">");
            
            // For one-to-many, we don't set a column name as it's managed by the other side
            prop.setColumnName(null);
            
            entityMetadata.addProperty(prop);
        }
    }

    private void parseQueries(Element classElement, EntityMetadata entityMetadata) {
        NodeList queryList = classElement.getElementsByTagName("query");
        for (int i = 0; i < queryList.getLength(); i++) {
            Element queryElement = (Element) queryList.item(i);
            QueryMetadata query = new QueryMetadata();
            
            query.setName(queryElement.getAttribute("name"));
            query.setQuery(queryElement.getTextContent().trim());
            
            entityMetadata.addQuery(query);
        }
    }

    private void parseSqlQueries(Element classElement, EntityMetadata entityMetadata) {
        NodeList sqlQueryList = classElement.getElementsByTagName("sql-query");
        for (int i = 0; i < sqlQueryList.getLength(); i++) {
            Element sqlQueryElement = (Element) sqlQueryList.item(i);
            SqlQueryMetadata query = new SqlQueryMetadata();
            
            query.setName(sqlQueryElement.getAttribute("name"));
            query.setQuery(sqlQueryElement.getTextContent().trim());
            
            // Parse return elements
            NodeList returnList = sqlQueryElement.getElementsByTagName("return");
            for (int j = 0; j < returnList.getLength(); j++) {
                Element returnElement = (Element) returnList.item(j);
                SqlQueryMetadata.ResultMapping mapping = new SqlQueryMetadata.ResultMapping();
                
                mapping.setAlias(returnElement.getAttribute("alias"));
                mapping.setClassName(returnElement.getAttribute("class"));
                mapping.setScalar(false);
                
                query.addResultMapping(mapping);
            }
            
            // Parse return-scalar elements
            NodeList scalarList = sqlQueryElement.getElementsByTagName("return-scalar");
            for (int j = 0; j < scalarList.getLength(); j++) {
                Element scalarElement = (Element) scalarList.item(j);
                SqlQueryMetadata.ResultMapping mapping = new SqlQueryMetadata.ResultMapping();
                
                mapping.setColumnName(scalarElement.getAttribute("column"));
                mapping.setColumnType(scalarElement.getAttribute("type"));
                mapping.setScalar(true);
                
                query.addResultMapping(mapping);
            }
            
            entityMetadata.addSqlQuery(query);
        }
    }

    private void parseFilters(Element hibernateMapping, EntityMetadata entityMetadata) {
        NodeList filterDefList = hibernateMapping.getElementsByTagName("filter-def");
        for (int i = 0; i < filterDefList.getLength(); i++) {
            Element filterDefElement = (Element) filterDefList.item(i);
            FilterMetadata filter = new FilterMetadata();
            
            filter.setName(filterDefElement.getAttribute("name"));
            
            // Parse filter parameters
            NodeList paramList = filterDefElement.getElementsByTagName("filter-param");
            for (int j = 0; j < paramList.getLength(); j++) {
                Element paramElement = (Element) paramList.item(j);
                FilterMetadata.FilterParameter param = new FilterMetadata.FilterParameter();
                
                param.setName(paramElement.getAttribute("name"));
                param.setType(paramElement.getAttribute("type"));
                
                filter.addParameter(param);
            }
            
            // Find corresponding filter element in class
            NodeList filterList = hibernateMapping.getElementsByTagName("filter");
            for (int j = 0; j < filterList.getLength(); j++) {
                Element filterElement = (Element) filterList.item(j);
                if (filterElement.getAttribute("name").equals(filter.getName())) {
                    filter.setCondition(filterElement.getAttribute("condition"));
                    break;
                }
            }
            
            entityMetadata.addFilter(filter);
        }
    }
}
