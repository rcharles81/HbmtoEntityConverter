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
@Table(name = "CATEGORIES")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "com.example.domain.category_seq", sequenceName = "CATEGORY_SEQ")
    @Column(name = "CATEGORY_ID")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "parent_id")
    private String parent;

    @OneToMany(mappedBy = "category")
    private Set<Category> children;

}
