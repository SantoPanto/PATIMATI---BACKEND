package com.works.patimati.controller;

import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.dto.admin.AdComplaintAdminResponse;
import com.works.patimati.dto.admin.AdoptionComplaintAdminResponse;
import com.works.patimati.dto.admin.UserComplaintAdminResponse;
import com.works.patimati.dto.admin.UserDetailForAdminDTO;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.ComplaintReason;
import com.works.patimati.entity.enums.ComplaintStatus;
import com.works.patimati.service.AdminService;
import com.works.patimati.service.InstagramPublishService;
import com.works.patimati.service.VetClinicService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminControllerSearchTest {

    private AdminService adminService;
    private InstagramPublishService instagramPublishService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        adminService = mock(AdminService.class);
        instagramPublishService = mock(InstagramPublishService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminController(adminService, instagramPublishService, mock(VetClinicService.class)))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void getAllUsers_withSearchAndSort_passesParametersToService() throws Exception {
        UserDetailForAdminDTO dto = new UserDetailForAdminDTO(
                1L, "Ahmet", "Yılmaz", "ahmet@test.com", "5551234567",
                true, true, User.Role.USER, Instant.now()
        );

        when(adminService.getAllUsers(eq("ahmet"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dto), org.springframework.data.domain.PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/admin/users")
                        .param("page", "0")
                        .param("size", "10")
                        .param("search", "ahmet")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("Ahmet"));

        verify(adminService).getAllUsers(eq("ahmet"), any(Pageable.class));
    }

    @Test
    void getAllAds_withSearchAndSort_passesParametersToService() throws Exception {
        when(adminService.getAllAds(eq("tekir"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), org.springframework.data.domain.PageRequest.of(0, 10), 0));

        mockMvc.perform(get("/api/admin/ads")
                        .param("page", "0")
                        .param("size", "10")
                        .param("search", "tekir")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk());

        verify(adminService).getAllAds(eq("tekir"), any(Pageable.class));
    }

    @Test
    void getAdComplaints_withSearchAndSort_passesParametersToService() throws Exception {
        AdComplaintAdminResponse response = new AdComplaintAdminResponse(
                1L, 10L, "Reporter Name", "reporter@test.com",
                100L, "Ad Title", 20L, "Owner Name",
                ComplaintReason.SAHTE_ILAN, "Fake ad desc",
                ComplaintStatus.BEKLEMEDE, Instant.now()
        );

        when(adminService.getAdComplaints(eq("fake"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response), org.springframework.data.domain.PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/admin/complaints/ads")
                        .param("search", "fake")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].adTitle").value("Ad Title"));

        verify(adminService).getAdComplaints(eq("fake"), any(Pageable.class));
    }

    @Test
    void getUserComplaints_withSearchAndSort_passesParametersToService() throws Exception {
        UserComplaintAdminResponse response = new UserComplaintAdminResponse(
                1L, 10L, "Reporter Name", "reporter@test.com",
                20L, "Reported Name", "reported@test.com",
                ComplaintReason.KOTU_DIL_KULLANIMI, "Bad language",
                ComplaintStatus.BEKLEMEDE, Instant.now()
        );

        when(adminService.getUserComplaints(eq("bad"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response), org.springframework.data.domain.PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/admin/complaints/users")
                        .param("search", "bad")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].reportedUserFullName").value("Reported Name"));

        verify(adminService).getUserComplaints(eq("bad"), any(Pageable.class));
    }

    @Test
    void getAdoptionComplaints_withSearchAndSort_passesParametersToService() throws Exception {
        AdoptionComplaintAdminResponse response = new AdoptionComplaintAdminResponse(
                1L, 10L, "Reporter Name", "reporter@test.com",
                100L, "Adoption Title", 20L, "Owner Name",
                ComplaintReason.UYGUNSUZ_ICERIK, "Inappropriate content",
                ComplaintStatus.BEKLEMEDE, Instant.now()
        );

        when(adminService.getAdoptionComplaints(eq("inappropriate"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response), org.springframework.data.domain.PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/admin/complaints/adoptions")
                        .param("search", "inappropriate")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].adTitle").value("Adoption Title"));

        verify(adminService).getAdoptionComplaints(eq("inappropriate"), any(Pageable.class));
    }
}
