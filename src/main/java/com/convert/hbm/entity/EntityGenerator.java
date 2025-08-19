
package com.convert.hbm.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class EntityGenerator {
    private static final Logger logger = LoggerFactory.getLogger(EntityGenerator.class);
    private final String outputDirectory;

    public EntityGenerator(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void generateEntity(EntityMetadata metadata) {
        String packageName = extractPackageName(metadata.getClassName());
        String className = extractSimpleClassName(metadata.getClassName());
        String packageDir = outputDirectory + File.separator + packageName.replace(".", File.separator);

        // Create package directories
        new File(packageDir).mkdirs();

        // Generate entity class file
        String filePath = packageDir + File.separator + className + ".java";
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write package declaration
            if (packageName != null && !packageName.isEmpty()) {
                writer.println("package " + packageName + ";");
                writer.println();
            }

            // Write imports
            Set<String> imports = new HashSet<>();
            imports.add("javax.persistence.*");
            imports.add("lombok.Data");
            imports.add("lombok.NoArgsConstructor");
            imports.add("lombok.AllArgsConstructor");
            imports.add("java.math.BigDecimal");
            imports.add("java.util.Set");
            imports.add("org.hibernate.annotations.Filter");
            imports.add("org.hibernate.annotations.FilterDef");
            imports.add("org.hibernate.annotations.ParamDef");

            // Add imports for collection types
            for (PropertyMetadata prop : metadata.getProperties()) {
                if (prop.getType().startsWith("java.util.")) {
                    imports.add(prop.getType().substring(0, prop.getType().indexOf('<')));
                }
            }

            // Write imports
            for (String imp : imports) {
                writer.println("import " + imp + ";");
            }
            writer.println();

            // Write named queries
            if (!metadata.getQueries().isEmpty()) {
                writer.println("@NamedQueries({");
                String queries = metadata.getQueries().stream()
                    .map(q -> String.format("    @NamedQuery(name = \"%s\", query = \"%s\")",
                        q.getName(), q.getQuery().replace("\n", " ").replace("\"", "\\\"")))
                    .collect(Collectors.joining(",\n"));
                writer.println(queries);
                writer.println("})");
            }

            // Write native queries
            if (!metadata.getSqlQueries().isEmpty()) {
                writer.println("@NamedNativeQueries({");
                String nativeQueries = metadata.getSqlQueries().stream()
                    .map(q -> {
                        StringBuilder sb = new StringBuilder();
                        sb.append(String.format("    @NamedNativeQuery(name = \"%s\", query = \"%s\"",
                            q.getName(), q.getQuery().replace("\n", " ").replace("\"", "\\\"")));
                        
                        // Add result class/column mappings if present
                        if (!q.getResultMappings().isEmpty()) {
                            boolean hasEntityResults = q.getResultMappings().stream()
                                .anyMatch(rm -> !rm.isScalar());
                            
                            if (hasEntityResults) {
                                sb.append(",\n        resultSetMapping = \"").append(q.getName()).append("Mapping\"");
                            }
                        }
                        
                        sb.append(")");
                        return sb.toString();
                    })
                    .collect(Collectors.joining(",\n"));
                writer.println(nativeQueries);
                writer.println("})");
            }

            // Write SQL result set mappings if needed
            if (!metadata.getSqlQueries().isEmpty()) {
                writer.println("@SqlResultSetMappings({");
                String resultSetMappings = metadata.getSqlQueries().stream()
                    .filter(q -> !q.getResultMappings().isEmpty())
                    .map(q -> {
                        StringBuilder sb = new StringBuilder();
                        sb.append(String.format("    @SqlResultSetMapping(name = \"%sMapping\",", q.getName()));
                        
                        // Entity results
                        String entityResults = q.getResultMappings().stream()
                            .filter(rm -> !rm.isScalar())
                            .map(rm -> String.format("\n        @EntityResult(entityClass = %s.class, fields = @FieldResult(name = \"%s\", column = \"%s\"))",
                                rm.getClassName(), rm.getAlias(), rm.getColumnName()))
                            .collect(Collectors.joining(","));
                        if (!entityResults.isEmpty()) {
                            sb.append("\n        entities = {").append(entityResults).append("\n        }");
                        }
                        
                        // Column results
                        String columnResults = q.getResultMappings().stream()
                            .filter(rm -> rm.isScalar())
                            .map(rm -> String.format("\n        @ColumnResult(name = \"%s\", type = %s.class)",
                                rm.getColumnName(), getJavaType(rm.getColumnType())))
                            .collect(Collectors.joining(","));
                        if (!columnResults.isEmpty()) {
                            if (!entityResults.isEmpty()) {
                                sb.append(",");
                            }
                            sb.append("\n        columns = {").append(columnResults).append("\n        }");
                        }
                        
                        sb.append("\n    )");
                        return sb.toString();
                    })
                    .collect(Collectors.joining(",\n"));
                writer.println(resultSetMappings);
                writer.println("})");
            }

            // Write filters
            if (!metadata.getFilters().isEmpty()) {
                for (FilterMetadata filter : metadata.getFilters()) {
                    writer.println("@Filter(name = \"" + filter.getName() + "\", condition = \"" + filter.getCondition() + "\")");
                }
            }

            // Write class annotations
            writer.println("@Entity");
            writer.println("@Table(name = \"" + metadata.getTableName() + "\")");
            writer.println("@Data");
            writer.println("@NoArgsConstructor");
            writer.println("@AllArgsConstructor");
            writer.println("public class " + className + " {");

            // Handle ID generation
            writeIdField(writer, metadata);

            // Write fields
            for (PropertyMetadata prop : metadata.getProperties()) {
                String fieldType = getJavaType(prop.getType().trim());
                
                // Add JPA annotations
                if (prop.getColumnName() != null) {
                    writer.println("    @Column(name = \"" + prop.getColumnName() + "\")");
                }
                
                // Handle relationships
                if (prop.getType().startsWith("java.util.Set<")) {
                    String elementType = prop.getType().substring(prop.getType().indexOf('<') + 1, prop.getType().length() - 1);
                    writer.println("    @OneToMany(mappedBy = \"" + className.toLowerCase() + "\")");
                    writer.println("    private Set<" + extractSimpleClassName(elementType) + "> " + prop.getName() + ";");
                } else if (prop.getType().contains(".")) {
                    writer.println("    @ManyToOne");
                    writer.println("    @JoinColumn(name = \"" + prop.getColumnName() + "\")");
                    writer.println("    private " + fieldType + " " + prop.getName() + ";");
                } else {
                    writer.println("    private " + fieldType + " " + prop.getName() + ";");
                }
                writer.println();
            }

            writer.println("}");

            logger.info("Generated entity class: {}", filePath);
        } catch (IOException e) {
            logger.error("Error generating entity class: {}", filePath, e);
            throw new RuntimeException("Failed to generate entity class", e);
        }
    }

    private void writeIdField(PrintWriter writer, EntityMetadata metadata) {
        if (metadata.getId() != null) {
            writer.println("    @Id");
            if (metadata.getId().getGeneratorClass() != null) {
                switch (metadata.getId().getGeneratorClass().toLowerCase()) {
                    case "sequence":
                        writer.println("    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = \"" + 
                            metadata.getClassName().toLowerCase() + "_seq_gen\")");
                        String sequenceName = metadata.getId().getGeneratorParams().getOrDefault("sequence",
                            metadata.getTableName().toLowerCase() + "_" + metadata.getId().getColumnName().toLowerCase() + "_seq");
                        writer.println("    @SequenceGenerator(name = \"" + metadata.getClassName().toLowerCase() + "_seq_gen\", " +
                                    "sequenceName = \"" + sequenceName + "\", " +
                                    "allocationSize = 1)");
                        break;
                    case "identity":
                        writer.println("    @GeneratedValue(strategy = GenerationType.IDENTITY)");
                        break;
                    case "uuid":
                        writer.println("    @GeneratedValue(generator = \"uuid\")");
                        writer.println("    @GenericGenerator(name = \"uuid\", strategy = \"uuid2\")");
                        break;
                    default:
                        writer.println("    @GeneratedValue(strategy = GenerationType.AUTO)");
                        break;
                }
            }
            writer.println("    @Column(name = \"" + metadata.getId().getColumnName() + "\")");
            writer.println("    private " + getJavaType(metadata.getId().getType()) + " " + metadata.getId().getName() + ";");
            writer.println();
        }
    }

    private String extractPackageName(String fullClassName) {
        int lastDot = fullClassName.lastIndexOf('.');
        return lastDot > 0 ? fullClassName.substring(0, lastDot) : "";
    }

    private String extractSimpleClassName(String fullClassName) {
        int lastDot = fullClassName.lastIndexOf('.');
        return lastDot > 0 ? fullClassName.substring(lastDot + 1) : fullClassName;
    }

    private String getJavaType(String hbmType) {
        switch (hbmType.toLowerCase()) {
            case "string":
                return "String";
            case "long":
                return "Long";
            case "integer":
                return "Integer";
            case "int":
                return "Integer";
            case "double":
                return "Double";
            case "float":
                return "Float";
            case "boolean":
                return "Boolean";
            case "date":
                return "java.util.Date";
            case "timestamp":
                return "java.sql.Timestamp";
            case "big_decimal":
            case "bigdecimal":
                return "BigDecimal";
            case "binary":
                return "byte[]";
            case "text":
                return "String";
            case "status":
                return "String";
            default:
                if (hbmType.startsWith("java.")) {
                    return hbmType;
                }
                return "String"; // Default to String for unknown types
        }
    }
}
