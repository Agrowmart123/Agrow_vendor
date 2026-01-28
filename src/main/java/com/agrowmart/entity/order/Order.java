
package com.agrowmart.entity.order;

import com.agrowmart.entity.User;
import com.agrowmart.entity.customer.Customer;
import com.agrowmart.entity.customer.CustomerAddress;
import com.agrowmart.enums.DeliveryMode;
import com.agrowmart.enums.VendorAcceptThenCancelReason;
import com.agrowmart.enums.VendorCancelReason;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @Column(name = "id", length = 50, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "merchant_id", nullable = false)
	private User merchant;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryCharge = BigDecimal.ZERO;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "promo_code", length = 50, nullable = true)
    private String promoCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "payment_mode", length = 10, nullable = false)
    private String paymentMode;

    @Column(name = "payment_status", length = 20, nullable = false)
    private String paymentStatus = "PENDING";

    @Column(name = "settlement_status", length = 20, nullable = false)
    private String settlementStatus = "PENDING";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "cancel_reason", length = 255)
    private String cancelReason;

    @Column(name = "cancelled_by", length = 20)
    private String cancelledBy;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "vendor_cancel_reason")
    private VendorCancelReason vendorCancelReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "vendor_accept_cancel_reason")
    private VendorAcceptThenCancelReason vendorAcceptCancelReason;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

//    @PrePersist
//    protected void onCreate() {
//        if (createdAt == null) createdAt = LocalDateTime.now();
//        updatedAt = createdAt;
//    }

    
    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = "ORD-" + System.currentTimeMillis(); // simple unique string
        }
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public User getMerchant() { return merchant; }
    public void setMerchant(User merchant) { this.merchant = merchant; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getDeliveryCharge() { return deliveryCharge; }
    public void setDeliveryCharge(BigDecimal deliveryCharge) { this.deliveryCharge = deliveryCharge; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getSettlementStatus() { return settlementStatus; }
    public void setSettlementStatus(String settlementStatus) { this.settlementStatus = settlementStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public String getCancelledBy() { return cancelledBy; }
    public void setCancelledBy(String cancelledBy) { this.cancelledBy = cancelledBy; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    public VendorCancelReason getVendorCancelReason() { return vendorCancelReason; }
    public void setVendorCancelReason(VendorCancelReason reason) { this.vendorCancelReason = reason; }
    public VendorAcceptThenCancelReason getVendorAcceptCancelReason() { return vendorAcceptCancelReason; }
    public void setVendorAcceptCancelReason(VendorAcceptThenCancelReason reason) { this.vendorAcceptCancelReason = reason; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public enum OrderStatus {
        PENDING,
        SCHEDULED,
        ACCEPTED,
        READY_FOR_PICKUP,
        PICKED_UP,
        OUT_FOR_DELIVERY,
        DELIVERED,
        CANCELLED,
        REJECTED
    }
    
    
 //-------------------
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_mode", nullable = false)
    private DeliveryMode deliveryMode = DeliveryMode.SELF_DELIVERY;

    @Column(name = "scheduled_delivery_date")
    private LocalDate scheduledDate;

    @Column(name = "scheduled_delivery_slot", length = 50)
    private String scheduledSlot; // e.g. "10:00-12:00", "16:00-18:00"

    @Column(name = "is_scheduled", nullable = false)
    private boolean isScheduled = false;

    // QR & Delivery Partner fields
    @Column(name = "vendor_pickup_token", length = 255)
    private String vendorPickupToken;

    @Column(name = "vendor_pickup_token_expiry")
    private LocalDateTime vendorPickupTokenExpiry;

    @Column(name = "user_delivery_token", length = 255)
    private String userDeliveryToken;

    @Column(name = "user_delivery_token_expiry")
    private LocalDateTime userDeliveryTokenExpiry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_partner_id")
    private User deliveryPartner;

    @Column(name = "pickup_time")
    private LocalDateTime pickupTime;
    
 // Important new helper methods
    public String generateToken() {
        return UUID.randomUUID().toString();
    }

	public DeliveryMode getDeliveryMode() {
		return deliveryMode;
	}

	public void setDeliveryMode(DeliveryMode deliveryMode) {
		this.deliveryMode = deliveryMode;
	}

	public LocalDate getScheduledDate() {
		return scheduledDate;
	}

	public void setScheduledDate(LocalDate scheduledDate) {
		this.scheduledDate = scheduledDate;
	}

	public String getScheduledSlot() {
		return scheduledSlot;
	}

	public void setScheduledSlot(String scheduledSlot) {
		this.scheduledSlot = scheduledSlot;
	}

	public boolean isScheduled() {
		return isScheduled;
	}

	public void setScheduled(boolean isScheduled) {
		this.isScheduled = isScheduled;
	}

	public String getVendorPickupToken() {
		return vendorPickupToken;
	}

	public void setVendorPickupToken(String vendorPickupToken) {
		this.vendorPickupToken = vendorPickupToken;
	}

	public LocalDateTime getVendorPickupTokenExpiry() {
		return vendorPickupTokenExpiry;
	}

	public void setVendorPickupTokenExpiry(LocalDateTime vendorPickupTokenExpiry) {
		this.vendorPickupTokenExpiry = vendorPickupTokenExpiry;
	}

	public String getUserDeliveryToken() {
		return userDeliveryToken;
	}

	public void setUserDeliveryToken(String userDeliveryToken) {
		this.userDeliveryToken = userDeliveryToken;
	}

	public LocalDateTime getUserDeliveryTokenExpiry() {
		return userDeliveryTokenExpiry;
	}

	public void setUserDeliveryTokenExpiry(LocalDateTime userDeliveryTokenExpiry) {
		this.userDeliveryTokenExpiry = userDeliveryTokenExpiry;
	}

	public User getDeliveryPartner() {
		return deliveryPartner;
	}

	public void setDeliveryPartner(User deliveryPartner) {
		this.deliveryPartner = deliveryPartner;
	}

	public LocalDateTime getPickupTime() {
		return pickupTime;
	}

	public void setPickupTime(LocalDateTime pickupTime) {
		this.pickupTime = pickupTime;
	}
	
	
//custmoer add address	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "delivery_address_id", nullable = false)  // Required field
	private CustomerAddress deliveryAddress;

	// Add getters & setters
	public CustomerAddress getDeliveryAddress() {
	    return deliveryAddress;
	}

	public void setDeliveryAddress(CustomerAddress deliveryAddress) {
	    this.deliveryAddress = deliveryAddress;
	}
	
}