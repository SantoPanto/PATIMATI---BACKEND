package com.works.patimati.specification;

import com.works.patimati.entity.Ad;
import com.works.patimati.entity.AdComplaint;
import com.works.patimati.entity.AdoptionComplaint;
import com.works.patimati.entity.User;
import com.works.patimati.entity.UserComplaint;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class AdminSpecificationTest {

    private Root<User> userRoot;
    private Root<Ad> adRoot;
    private Root<AdComplaint> adComplaintRoot;
    private Root<UserComplaint> userComplaintRoot;
    private Root<AdoptionComplaint> adoptionComplaintRoot;
    private CriteriaQuery<?> query;
    private CriteriaBuilder cb;
    private Path<Object> pathMock;
    private Expression<String> lowerExprMock;
    private Subquery<Long> subqueryMock;
    private Root<User> userSubqueryRootMock;
    private Root<Ad> adSubqueryRootMock;
    private Join<Ad, User> adUserJoinMock;

    @BeforeEach
    void setUp() {
        userRoot = mock(Root.class);
        adRoot = mock(Root.class);
        adComplaintRoot = mock(Root.class);
        userComplaintRoot = mock(Root.class);
        adoptionComplaintRoot = mock(Root.class);
        query = mock(CriteriaQuery.class);
        cb = mock(CriteriaBuilder.class);
        pathMock = mock(Path.class);
        lowerExprMock = mock(Expression.class);
        subqueryMock = mock(Subquery.class);
        userSubqueryRootMock = mock(Root.class);
        adSubqueryRootMock = mock(Root.class);
        adUserJoinMock = mock(Join.class);

        when(userRoot.get(anyString())).thenReturn(pathMock);
        when(adRoot.get(anyString())).thenReturn(pathMock);
        when(adComplaintRoot.get(anyString())).thenReturn(pathMock);
        when(userComplaintRoot.get(anyString())).thenReturn(pathMock);
        when(adoptionComplaintRoot.get(anyString())).thenReturn(pathMock);
        when(pathMock.as(String.class)).thenReturn(lowerExprMock);

        when(cb.lower(any())).thenReturn(lowerExprMock);
        when(cb.concat(any(Expression.class), any(Expression.class))).thenReturn(lowerExprMock);
        when(cb.concat(any(Expression.class), anyString())).thenReturn(lowerExprMock);
        when(cb.concat(anyString(), any(Expression.class))).thenReturn(lowerExprMock);
        when(cb.conjunction()).thenReturn(mock(Predicate.class));
        when(cb.like(any(), anyString())).thenReturn(mock(Predicate.class));
        when(cb.or(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        when(query.subquery(Long.class)).thenReturn(subqueryMock);
        when(subqueryMock.from(User.class)).thenReturn(userSubqueryRootMock);
        when(subqueryMock.from(Ad.class)).thenReturn(adSubqueryRootMock);
        when(subqueryMock.select(any())).thenReturn(subqueryMock);
        when(subqueryMock.where(any(Predicate.class))).thenReturn(subqueryMock);

        when(userSubqueryRootMock.get(anyString())).thenReturn(pathMock);
        when(adSubqueryRootMock.get(anyString())).thenReturn(pathMock);
        doReturn(adUserJoinMock).when(adSubqueryRootMock).join("user");
        when(adUserJoinMock.get(anyString())).thenReturn(pathMock);
    }

    @Test
    void userSpecification_whenSearchBlank_returnsConjunction() {
        Specification<User> spec = UserSpecification.withSearch("  ");
        Predicate predicate = spec.toPredicate(userRoot, query, cb);
        assertThat(predicate).isNotNull();
        verify(cb).conjunction();
    }

    @Test
    void userSpecification_whenSearchPresent_createsLikePredicates() {
        Specification<User> spec = UserSpecification.withSearch("ahmet");
        Predicate predicate = spec.toPredicate(userRoot, query, cb);
        assertThat(predicate).isNotNull();
        verify(cb, times(3)).like(any(), eq("%ahmet%"));
    }

    @Test
    void adSpecification_whenSearchBlank_returnsConjunction() {
        Specification<Ad> spec = AdSpecification.withSearch(null);
        Predicate predicate = spec.toPredicate(adRoot, query, cb);
        assertThat(predicate).isNotNull();
        verify(cb).conjunction();
    }

    @Test
    void adSpecification_whenSearchPresent_createsLikePredicates() {
        Specification<Ad> spec = AdSpecification.withSearch("tekir");
        Predicate predicate = spec.toPredicate(adRoot, query, cb);
        assertThat(predicate).isNotNull();
        verify(cb, times(4)).like(any(), eq("%tekir%"));
    }

    @Test
    void complaintSpecification_forAd_whenSearchPresent_createsSubqueriesAndPredicates() {
        Specification<AdComplaint> spec = ComplaintSpecification.forAd("dolandirici");
        Predicate predicate = spec.toPredicate(adComplaintRoot, query, cb);
        assertThat(predicate).isNotNull();
        verify(query, times(3)).subquery(Long.class);
    }

    @Test
    void complaintSpecification_forUser_whenSearchPresent_createsSubqueriesAndPredicates() {
        Specification<UserComplaint> spec = ComplaintSpecification.forUser("kufur");
        Predicate predicate = spec.toPredicate(userComplaintRoot, query, cb);
        assertThat(predicate).isNotNull();
        verify(query, times(2)).subquery(Long.class);
    }

    @Test
    void complaintSpecification_forAdoption_whenSearchPresent_createsSubqueriesAndPredicates() {
        Specification<AdoptionComplaint> spec = ComplaintSpecification.forAdoption("sahiplendirme");
        Predicate predicate = spec.toPredicate(adoptionComplaintRoot, query, cb);
        assertThat(predicate).isNotNull();
        verify(query, times(3)).subquery(Long.class);
    }
}
