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

@NamedQueries({
    @NamedQuery(name = "findCustomerByEmail", query = "FROM Customer c                 WHERE c.emailAddress = :email"),
    @NamedQuery(name = "findPremiumCustomersWithMinLoyaltyPoints", query = "FROM PremiumCustomer pc                 WHERE pc.loyaltyPoints >= :minPoints                 ORDER BY pc.loyaltyPoints DESC"),
    @NamedQuery(name = "findCustomersWithPendingOrders", query = "SELECT DISTINCT c                 FROM Customer c                 JOIN c.orders o                 WHERE o.status = 'PENDING'                 AND o.totalAmount > :minAmount                 ORDER BY c.lastName, c.firstName")
})
@NamedNativeQueries({
    @NamedNativeQuery(name = "findCustomerWithOrderCounts", query = "SELECT c.*, COUNT(o.ORDER_ID) as orderCount                 FROM CUSTOMERS c                 LEFT JOIN ORDERS o ON c.CUSTOMER_ID = o.CUSTOMER_ID                 WHERE c.ACCOUNT_STATUS = :status                 GROUP BY c.CUSTOMER_ID, c.EMAIL, c.FIRST_NAME, c.LAST_NAME,                          c.DATE_OF_BIRTH, c.CREATED_AT, c.LAST_UPDATED,                          c.ACCOUNT_STATUS, c.STREET, c.CITY, c.STATE,                          c.ZIP_CODE, c.COUNTRY, c.CUSTOMER_TYPE                 HAVING COUNT(o.ORDER_ID) > :minOrders                 ORDER BY orderCount DESC",
        resultSetMapping = "findCustomerWithOrderCountsMapping"),
    @NamedNativeQuery(name = "findCustomersWithCategoryProducts", query = "SELECT {c.*}, {p.*}                 FROM CUSTOMERS c                 JOIN ORDERS o ON c.CUSTOMER_ID = o.CUSTOMER_ID                 JOIN ORDER_ITEMS oi ON o.ORDER_ID = oi.ORDER_ID                 JOIN PRODUCTS p ON oi.PRODUCT_ID = p.PRODUCT_ID                 JOIN PRODUCT_CATEGORY pc ON p.PRODUCT_ID = pc.PRODUCT_ID                 WHERE pc.CATEGORY_ID = :categoryId                 AND o.ORDER_DATE BETWEEN :startDate AND :endDate",
        resultSetMapping = "findCustomersWithCategoryProductsMapping")
})
@SqlResultSetMappings({
    @SqlResultSetMapping(name = "findCustomerWithOrderCountsMapping",
        entities = {
        @EntityResult(entityClass = Customer.class, fields = @FieldResult(name = "c", column = "null"))
        },
        columns = {
        @ColumnResult(name = "orderCount", type = Integer.class)
        }
    ),
    @SqlResultSetMapping(name = "findCustomersWithCategoryProductsMapping",
        entities = {
        @EntityResult(entityClass = Customer.class, fields = @FieldResult(name = "c", column = "null")),
        @EntityResult(entityClass = Product.class, fields = @FieldResult(name = "p", column = "null"))
        }
    )
})
@Filter(name = "activeCustomersOnly", condition = "ACCOUNT_STATUS = 'ACTIVE'")
@Filter(name = "regionalFilter", condition = "ADDRESS_STATE = :state")
@Entity
@Table(name = "CUSTOMERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "com.example.domain.customer_seq", sequenceName = "CUSTOMER_SEQ")
    @Column(name = "CUSTOMER_ID")
    private Long id;

    @Column(name = "emailAddress")
    private String emailAddress;

    @Column(name = "firstName")
    private String firstName;

    @Column(name = "lastName")
    private String lastName;

    @Column(name = "dateOfBirth")
    private java.util.Date dateOfBirth;

    @Column(name = "createdAt")
    private java.sql.Timestamp createdAt;

    @Column(name = "lastUpdated")
    private java.sql.Timestamp lastUpdated;

    @Column(name = "accountStatus")
    private String accountStatus;

    @Column(name = "fullName")
    private String fullName;

    @Column(name = "street")
    private String street;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "zipCode")
    private String zipCode;

    @Column(name = "country")
    private String country;

    @Column(name = "membershipLevel")
    private String membershipLevel;

    @Column(name = "loyaltyPoints")
    private Integer loyaltyPoints;

    @Column(name = "companyName")
    private String companyName;

    @Column(name = "taxId")
    private String taxId;

    @OneToMany(mappedBy = "customer")
    private Set<Order> orders;

}
