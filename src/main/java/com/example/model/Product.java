package com.example.domain;

import java.math.BigDecimal;
import lombok.Data;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.ParamDef;
import java.util.Set;
import org.hibernate.annotations.FilterDef;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Filter(name = "activeCustomersOnly", condition = "ACCOUNT_STATUS = 'ACTIVE'")
@Filter(name = "regionalFilter", condition = "ADDRESS_STATE = :state")
@Entity
@Table(name = "PRODUCTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "com.example.domain.product_seq", sequenceName = "PRODUCT_SEQ")
    @Column(name = "PRODUCT_ID")
    private Long id;

    @Column(name = "sku")
    private String sku;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "stockQuantity")
    private Integer stockQuantity;

    @Column(name = "active")
    private Boolean active;

}
