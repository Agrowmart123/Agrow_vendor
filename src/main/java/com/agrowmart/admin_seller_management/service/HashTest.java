//// com.agrowmart.admin_seller_management.config.AdminInitializer.java
//package com.agrowmart.admin_seller_management.service;
//
//import com.agrowmart.admin_seller_management.entity.Admin;
//import com.agrowmart.admin_seller_management.enums.AdminRole;
//import com.agrowmart.admin_seller_management.repository.AdminRepository;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//@Component
//public class HashTest implements CommandLineRunner {
//
//    private final AdminRepository adminRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @Value("${superadmin.email:superadmin@agrowmart.com}")
//    private String superAdminEmail;
//
//    @Value("${superadmin.password:admin@2025}")   // ← better to use env variable in production!
//    private String superAdminRawPassword;
//
//    @Value("${superadmin.name:Super Administrator}")
//    private String superAdminName;
//
//    public HashTest(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
//        this.adminRepository = adminRepository;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        if (adminRepository.existsByEmail(superAdminEmail)) {
//            // Optional: you can still update password if needed (careful in prod!)
//            // Admin existing = adminRepository.findByEmail(superAdminEmail).get();
//            // existing.setPasswordHash(passwordEncoder.encode(superAdminRawPassword));
//            // adminRepository.save(existing);
//            System.out.println("SUPER_ADMIN already exists → skipping creation");
//            return;
//        }
//
//        Admin superAdmin = new Admin();
//        superAdmin.setEmail(superAdminEmail);
//        superAdmin.setPasswordHash(passwordEncoder.encode(superAdminRawPassword));
//        superAdmin.setFullName(superAdminName);
//        superAdmin.setPhone("+919999999999");     // or make configurable
//        superAdmin.setRole(AdminRole.SUPER_ADMIN);
//        superAdmin.setActive(true);
//        superAdmin.setDeleted(false);
//
//        // Optional: photo, etc.
//
//        adminRepository.save(superAdmin);
//        System.out.println("=====================================");
//        System.out.println("SUPER_ADMIN created successfully!");
//        System.out.println("Email    : " + superAdminEmail);
//        System.out.println("Password : " + superAdminRawPassword);
//        System.out.println("Role     : SUPER_ADMIN");
//        System.out.println("=====================================");
//    }
//}


package com.agrowmart.admin_seller_management.service;
import com.agrowmart.admin_seller_management.entity.Admin;
import com.agrowmart.admin_seller_management.enums.AdminRole;
import com.agrowmart.admin_seller_management.repository.AdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class HashTest implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(HashTest.class);

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${superadmin.enabled:true}")
    private boolean enabled;

    @Value("${superadmin.email:}")
    private String email;

    @Value("${superadmin.password:}")
    private String rawPassword;

    @Value("${superadmin.full-name:Super Administrator}")
    private String fullName;

    @Value("${superadmin.phone:+919999999999}")
    private String phone;

    @Value("${superadmin.role:SUPER_ADMIN}")
    private String roleName;

    public HashTest(
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!enabled) {
            log.info("Super admin auto-creation is disabled via property");
            return;
        }

        if (email == null || email.trim().isEmpty() || rawPassword == null || rawPassword.trim().isEmpty()) {
            log.warn("Super admin configuration incomplete (email/password missing) → skipping");
            return;
        }

        if (adminRepository.existsByEmail(email.trim())) {
            log.info("Super admin with email {} already exists → skipping creation", email);
            return;
        }

        try {
            Admin superAdmin = new Admin();
            superAdmin.setEmail(email.trim());
            superAdmin.setPasswordHash(passwordEncoder.encode(rawPassword.trim()));
            superAdmin.setFullName(fullName.trim());
            superAdmin.setPhone(phone.trim());
            superAdmin.setRole(AdminRole.valueOf(roleName.trim()));
            superAdmin.setActive(true);
            superAdmin.setDeleted(false);
            // photoUrl, createdBy etc. can stay null for bootstrap account

            adminRepository.save(superAdmin);

            log.info("╔══════════════════════════════════════════════╗");
            log.info("║     SUPER ADMIN ACCOUNT CREATED SUCCESSFULLY     ║");
            log.info("╠══════════════════════════════════════════════╣");
            log.info("║ Email    : {} ║", email);
            log.info("║ Password : {} ║", rawPassword);   // only shown once at startup!
            log.info("║ Role     : {} ║", roleName);
            log.info("║ Name     : {} ║", fullName);
            log.info("╚══════════════════════════════════════════════╝");

        } catch (Exception e) {
            log.error("Failed to create super admin account", e);
        }
    }
}