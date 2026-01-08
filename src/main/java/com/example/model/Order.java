package com.example.model;

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
    @NamedQuery(name = "findOrdersByDateRange", query = "FROM Order o                 WHERE o.orderDate BETWEEN :startDate AND :endDate                 ORDER BY o.orderDate DESC")
})
@NamedNativeQueries({
    @NamedNativeQuery(name = "findTopSellingProductsByOrder", query = "SELECT p.PRODUCT_ID as productId,                         p.NAME as productName,                        SUM(oi.QUANTITY) as totalQuantity,                        SUM(oi.QUANTITY * oi.UNIT_PRICE) as totalRevenue                 FROM ORDERS o                 JOIN ORDER_ITEMS oi ON o.ORDER_ID = oi.ORDER_ID                 JOIN PRODUCTS p ON oi.PRODUCT_ID = p.PRODUCT_ID                 WHERE o.STATUS = 'COMPLETED'                 AND o.ORDER_DATE BETWEEN :startDate AND :endDate                 GROUP BY p.PRODUCT_ID, p.NAME                 ORDER BY totalRevenue DESC                 LIMIT :limit")
})
@SqlResultSetMappings({
    @SqlResultSetMapping(name = "findTopSellingProductsByOrderMapping",
        columns = {
        @ColumnResult(name = "productId", type = Long.class),
        @ColumnResult(name = "productName", type = String.class),
        @ColumnResult(name = "totalQuantity", type = Integer.class),
        @ColumnResult(name = "totalRevenue", type = BigDecimal.class)
        }
    )
})
@Filter(name = "activeCustomersOnly", condition = "ACCOUNT_STATUS = 'ACTIVE'")
@Filter(name = "regionalFilter", condition = "ADDRESS_STATE = :state")
@Entity
@Table(name = "ORDERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "com.example.domain.order_seq", sequenceName = "ORDER_SEQ")
    @Column(name = "ORDER_ID")
    private Long id;

    @Column(name = "orderNumber")
    private String orderNumber;

    @Column(name = "orderDate")
    private java.sql.Timestamp orderDate;

    @Column(name = "status")
    private String status;

    @Column(name = "totalAmount")
    private BigDecimal totalAmount;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "unitPrice")
    private BigDecimal unitPrice;

    @Column(name = "customer_id")
    private String customer;

    @Column(name = "product_id")
    private String product;

}
