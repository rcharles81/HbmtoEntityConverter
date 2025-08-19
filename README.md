# HBM to Entity Converter

A Java tool to convert Hibernate mapping (HBM) XML files to JPA Entity classes.

## Features

- Converts HBM XML files to modern JPA Entity classes
- Generates Lombok-based entities for reduced boilerplate
- Preserves table and column names from the HBM files
- Supports common Hibernate data types
- Generates clean, well-formatted code

## Requirements

- Java 14 or higher
- Maven 3.6 or higher

## Building

```bash
mvn clean package
```

## Usage

```bash
java -jar target/hbm2entityconverter-1.0-SNAPSHOT.jar <hbm-files-directory> <output-directory>
```

Where:
- `<hbm-files-directory>`: Directory containing your .hbm.xml files
- `<output-directory>`: Directory where the generated entity classes will be saved

The tool will:
1. Scan the input directory for .hbm.xml files
2. Parse each HBM file
3. Generate corresponding JPA entity classes in the output directory
4. Preserve the package structure from the HBM files

## Example

If you have an HBM file `User.hbm.xml`:

```xml
<?xml version="1.0"?>
<!DOCTYPE hibernate-mapping PUBLIC
        "-//Hibernate/Hibernate Mapping DTD 3.0//EN"
        "http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd">
<hibernate-mapping>
    <class name="com.example.model.User" table="users">
        <id name="id" type="long" column="user_id">
            <generator class="native"/>
        </id>
        <property name="username" type="string" column="username"/>
        <property name="email" type="string" column="email"/>
    </class>
</hibernate-mapping>
```

It will generate a JPA entity class:

```java
package com.example.model;

import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Column(name = "user_id")
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "email")
    private String email;
}
```

## Dependencies

- Hibernate Core 5.6.15.Final
- Lombok 1.18.22
- SLF4J 1.7.32
- Logback 1.2.9

