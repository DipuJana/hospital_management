package com.jana.hospital_management.specification;

import com.jana.hospital_management.entity.Patient;
import com.jana.hospital_management.entity.Gender;
import org.springframework.data.jpa.domain.Specification;

public class PatientSpecification {

    private PatientSpecification() { }

    public static Specification<Patient> withFilters(
            String name,
            String email,
            Gender gender
    ) {
        Specification<Patient> spec = (root, query, cb) -> cb.conjunction();

        if(name != null && !name.trim().isEmpty()) {
            spec = spec.and(hasName(name.trim()));
        }

        if(email != null && !email.trim().isEmpty()) {
            spec = spec.and(hasEmail(email.trim()));
        }

        if(gender != null) {
            spec = spec.and(hasGender(gender));
        }
        return spec;
    }

    private static Specification<Patient> hasName(String name) {
        return (root, query, cb) ->
                cb.like(
                        cb.lower(root.get("fullName")),
                        "%" + name.toLowerCase() + "%"
                );
    }

    private static Specification<Patient> hasEmail(String email) {
        return (root, query, cb) ->
                cb.like(
                        cb.lower(root.get("email")),
                        "%" + email.toLowerCase() + "%"
                );
    }

    private static Specification<Patient> hasGender(Gender gender) {
        return (root, query, cb) ->
                cb.equal(root.get("gender"), gender);
    }

}
