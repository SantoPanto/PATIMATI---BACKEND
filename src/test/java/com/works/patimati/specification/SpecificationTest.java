package com.works.patimati.specification;

import com.works.patimati.entity.Ad;
import com.works.patimati.entity.AdComplaint;
import com.works.patimati.entity.AdoptionComplaint;
import com.works.patimati.entity.User;
import com.works.patimati.entity.UserComplaint;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;

class SpecificationTest {

    @Test
    void userSpecification_WithNullOrBlank_ShouldReturnNullSpecification() {
        Specification<User> specNull = UserSpecification.withSearch(null);
        Specification<User> specBlank = UserSpecification.withSearch("   ");

        assertThat(specNull.toPredicate(null, null, null)).isNull();
        assertThat(specBlank.toPredicate(null, null, null)).isNull();
    }

    @Test
    void adSpecification_WithNullOrBlank_ShouldReturnNullSpecification() {
        Specification<Ad> specNull = AdSpecification.withSearch(null);
        Specification<Ad> specBlank = AdSpecification.withSearch("");

        assertThat(specNull.toPredicate(null, null, null)).isNull();
        assertThat(specBlank.toPredicate(null, null, null)).isNull();
    }

    @Test
    void complaintSpecification_WithNullOrBlank_ShouldReturnNullSpecifications() {
        Specification<AdComplaint> adSpec = ComplaintSpecification.withAdComplaintSearch(null);
        Specification<UserComplaint> userSpec = ComplaintSpecification.withUserComplaintSearch("");
        Specification<AdoptionComplaint> adoptionSpec = ComplaintSpecification.withAdoptionComplaintSearch("  ");

        assertThat(adSpec.toPredicate(null, null, null)).isNull();
        assertThat(userSpec.toPredicate(null, null, null)).isNull();
        assertThat(adoptionSpec.toPredicate(null, null, null)).isNull();
    }
}
