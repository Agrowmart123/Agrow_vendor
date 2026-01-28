//package com.agrowmart.service;
//
//
//import com.agrowmart.dto.auth.subscription.SubscriptionResponse;
//import com.agrowmart.dto.auth.subscription.SubscriptionUpgradeRequest;
//import com.agrowmart.entity.Subscription;
//import com.agrowmart.entity.User;
//import com.agrowmart.enums.SubscriptionPlan;
//import com.agrowmart.exception.SubscriptionLimitExceededException;
//import com.agrowmart.repository.AgriProductRepository;
//import com.agrowmart.repository.SubscriptionRepository;
//import com.agrowmart.repository.UserRepository;
//import com.razorpay.Order;
//import com.razorpay.RazorpayClient;
//import com.razorpay.RazorpayException;
//import org.json.JSONObject;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//
//
//@Service
//public class SubscriptionService {
//
//	private final SubscriptionRepository subscriptionRepo;
//    private final AgriProductRepository agriProductRepo;
//    private final UserRepository userRepo;
//    private final RazorpayClient razorpay;
//
//    // CHANGED: Use the property names that already exist in application.properties
// // NEW (matches your application.properties):
//    // Match your properties file (add _id and _secret suffixes)
//    @Value("${razorpay.key}")
//    private String razorpayKeyId;
//
//    @Value("${razorpay.secret}")
//    private String razorpayKeySecret;
//    
//    public SubscriptionService(
//            SubscriptionRepository subscriptionRepo,
//            AgriProductRepository agriProductRepo,
//            UserRepository userRepo) throws RazorpayException {
//        this.subscriptionRepo = subscriptionRepo;
//        this.agriProductRepo = agriProductRepo;
//        this.userRepo = userRepo;
//        this.razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
//    }
//
//    private void ensureAgriVendor(User user) {
//        if (user == null || !"AGRI".equals(user.getRole().getName())) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
//                    "Only AGRI vendors can use subscription system");
//        }
//    }
//
//    /**
//     * This is the most important check — called before every product creation/update
//     */
//    public void checkProductLimit(User user) {
//        ensureAgriVendor(user);
//
//        Subscription active = user.getActiveSubscription();
//        if (active == null) {
//            throw new SubscriptionLimitExceededException("No active subscription. Please subscribe to add products.");
//        }
//
//        long currentCount = agriProductRepo.countByVendor(user);
//
//        if (currentCount >= active.getPlan().getMaxProducts()) {
//            throw new SubscriptionLimitExceededException(
//                    "You have reached the maximum limit (" +
//                            active.getPlan().getMaxProducts() +
//                            ") for your current plan: " +
//                            active.getPlan().getDescription());
//        }
//    }
//
//    public SubscriptionResponse getCurrentStatus(User user) {
//        ensureAgriVendor(user);
//
//        Subscription active = user.getActiveSubscription();
//        long count = agriProductRepo.countByVendor(user);
//
//        return new SubscriptionResponse(
//                active != null ? active.getPlan() : SubscriptionPlan.NONE,
//                active != null ? active.getStartDate() : null,
//                active != null ? active.getExpiryDate() : null,
//                active != null && active.isActive(),
//                active != null ? active.getPlan().getMaxProducts() : 0,
//                (int) count,
//                active != null ? active.getPlan().getDescription() : "No active subscription"
//        );
//    }
//
//    public Map<String, String> createRazorpayOrder(User user, SubscriptionPlan plan) {
//        ensureAgriVendor(user);
//
//        try {
//            int amountPaise = plan.getPrice() * 100;
//
//            JSONObject req = new JSONObject()
//                    .put("amount", amountPaise)
//                    .put("currency", "INR")
//                    .put("receipt", "agri_" + user.getId())
//                    .put("notes", Map.of("userId", user.getId(), "plan", plan.name()));
//
//            Order order = razorpay.orders.create(req);
//
//            return Map.of(
//                    "orderId", order.get("id"),
//                    "amount", String.valueOf(amountPaise),
//                    "currency", "INR",
//                    "key", razorpayKeyId
//            );
//        } catch (RazorpayException e) {
//            throw new RuntimeException("Failed to create Razorpay order", e);
//        }
//    }
//
//    @Transactional
//    public void processSuccessfulPayment(String orderId, String paymentId) throws RazorpayException {
//        Order order = razorpay.orders.fetch(orderId);
//
//        if (!"paid".equals(order.get("status"))) {
//            return;
//        }
//
//        JSONObject notes = order.get("notes");
//        Long userId = Long.parseLong(notes.getString("userId"));
//        SubscriptionPlan plan = SubscriptionPlan.valueOf(notes.getString("plan"));
//
//        User user = userRepo.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        ensureAgriVendor(user);
//
//        // Create new subscription record
//        Subscription sub = new Subscription();
//        sub.setUser(user);
//        sub.setPlan(plan);
//        sub.setStartDate(LocalDateTime.now());
//        sub.setExpiryDate(LocalDateTime.now().plusMonths(1));
//        sub.setActive(true);
//        sub.setRazorpayPaymentId(paymentId);
//        subscriptionRepo.save(sub);
//
//        // Make existing products visible again
//        agriProductRepo.findByVendor(user)
//                .forEach(p -> p.setVisibleToCustomers(true));
//        agriProductRepo.saveAll(agriProductRepo.findByVendor(user));
//    }
//
//    @Scheduled(cron = "0 5 0 * * ?") // every day at 00:05
//    @Transactional
//    public void handleExpiredSubscriptions() {
//        List<Subscription> expired = subscriptionRepo.findAllExpiredActiveSubscriptions(LocalDateTime.now());
//
//        for (Subscription s : expired) {
//            s.setActive(false);
//            subscriptionRepo.save(s);
//
//            // Hide all products of this vendor
//            User vendor = s.getUser();
//            agriProductRepo.findByVendor(vendor)
//                    .forEach(p -> p.setVisibleToCustomers(false));
//            agriProductRepo.saveAll(agriProductRepo.findByVendor(vendor));
//        }
//    }
//    
//}


package com.agrowmart.service;

import com.agrowmart.dto.auth.subscription.SubscriptionResponse;
import com.agrowmart.entity.Subscription;
import com.agrowmart.entity.User;
import com.agrowmart.enums.SubscriptionPlan;
import com.agrowmart.exception.SubscriptionLimitExceededException;
import com.agrowmart.repository.AgriProductRepository;
import com.agrowmart.repository.SubscriptionRepository;
import com.agrowmart.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.hibernate.Hibernate;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepo;
    private final AgriProductRepository agriProductRepo;
    private final UserRepository userRepo;
    private final RazorpayClient razorpay;

    @Value("${razorpay.key}")
    private String razorpayKeyId;

    @Value("${razorpay.secret}")
    private String razorpayKeySecret;

    public SubscriptionService(
            SubscriptionRepository subscriptionRepo,
            AgriProductRepository agriProductRepo,
            UserRepository userRepo) throws RazorpayException {
        this.subscriptionRepo = subscriptionRepo;
        this.agriProductRepo = agriProductRepo;
        this.userRepo = userRepo;
        this.razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
    }

    private void ensureAgriVendor(User user) {
        if (user == null || !"AGRI".equals(user.getRole().getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only AGRI vendors can use subscription system");
        }
    }

    /**
     * Checks if the vendor can add more products based on active subscription or free limit (3 products)
     */
    @Transactional(readOnly = true)
    public void checkProductLimit(User user) {
        ensureAgriVendor(user);

        // No Hibernate.initialize needed anymore
        Optional<Subscription> activeOpt = subscriptionRepo.findFirstByUserAndActiveTrueAndExpiryDateAfterOrderByStartDateDesc(
            user,
            LocalDateTime.now()
        );

        long currentCount = agriProductRepo.countByVendor(user);

        int maxAllowed = activeOpt
            .map(s -> s.getPlan().getMaxProducts())
            .orElse(SubscriptionPlan.NONE.getMaxProducts()); // 3

        if (currentCount >= maxAllowed) {
            String msg = activeOpt.isPresent()
                ? "Upgrade your " + activeOpt.get().getPlan().getDescription() + " plan."
                : "Free plan allows only 10 products. Please subscribe.";

            throw new SubscriptionLimitExceededException(
                "Product limit reached (" + maxAllowed + "). " + msg
            );
        }
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getCurrentStatus(User user) {
        ensureAgriVendor(user);

     // Important: force load subscriptions WHILE transaction is active
        Hibernate.initialize(user.getSubscriptions());

        Subscription active = user.getActiveSubscription();

        long count = agriProductRepo.countByVendor(user);

        SubscriptionPlan plan = active != null ? active.getPlan() : SubscriptionPlan.NONE;
        int maxProducts = active != null
                ? active.getPlan().getMaxProducts()
                : SubscriptionPlan.NONE.getMaxProducts(); // 3 for free

        String description = active != null
                ? active.getPlan().getDescription()
                : "Free plan – maximum 10 products";

        return new SubscriptionResponse(
                plan,
                active != null ? active.getStartDate() : null,
                active != null ? active.getExpiryDate() : null,
                active != null && active.isActive(),
                maxProducts,
                (int) count,
                description
        );
    }

    public Map<String, String> createRazorpayOrder(User user, SubscriptionPlan plan) {
        ensureAgriVendor(user);

        try {
            int amountPaise = plan.getPrice() * 100;

            JSONObject req = new JSONObject()
                    .put("amount", amountPaise)
                    .put("currency", "INR")
                    .put("receipt", "agri_" + user.getId())
                    .put("notes", Map.of("userId", user.getId(), "plan", plan.name()));

            Order order = razorpay.orders.create(req);

            return Map.of(
                    "orderId", order.get("id"),
                    "amount", String.valueOf(amountPaise),
                    "currency", "INR",
                    "key", razorpayKeyId
            );
        } catch (RazorpayException e) {
            throw new RuntimeException("Failed to create Razorpay order", e);
        }
    }

    @Transactional
    public void processSuccessfulPayment(String orderId, String paymentId) throws RazorpayException {
        Order order = razorpay.orders.fetch(orderId);

        if (!"paid".equals(order.get("status"))) {
            return;
        }

        JSONObject notes = order.get("notes");
        Long userId = Long.parseLong(notes.getString("userId"));
        SubscriptionPlan plan = SubscriptionPlan.valueOf(notes.getString("plan"));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ensureAgriVendor(user);

        // Create new subscription record
        Subscription sub = new Subscription();
        sub.setUser(user);
        sub.setPlan(plan);
        sub.setStartDate(LocalDateTime.now());
        sub.setExpiryDate(LocalDateTime.now().plusMonths(1));
        sub.setActive(true);
        sub.setRazorpayPaymentId(paymentId);
        subscriptionRepo.save(sub);

        // Make existing products visible again (if they were hidden)
        agriProductRepo.findByVendor(user)
                .forEach(p -> p.setVisibleToCustomers(true));
        agriProductRepo.saveAll(agriProductRepo.findByVendor(user));
    }

    @Scheduled(cron = "0 5 0 * * ?") // every day at 00:05
    @Transactional
    public void handleExpiredSubscriptions() {
        List<Subscription> expired = subscriptionRepo.findAllExpiredActiveSubscriptions(LocalDateTime.now());

        for (Subscription s : expired) {
            s.setActive(false);
            subscriptionRepo.save(s);

            // Hide all products of this vendor
            User vendor = s.getUser();
            agriProductRepo.findByVendor(vendor)
                    .forEach(p -> p.setVisibleToCustomers(false));
            agriProductRepo.saveAll(agriProductRepo.findByVendor(vendor));
        }
    }
}