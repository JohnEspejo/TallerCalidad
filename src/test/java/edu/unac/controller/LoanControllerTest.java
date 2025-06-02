package edu.unac.controller;

import edu.unac.domain.Device;
import edu.unac.domain.Loan;
import edu.unac.repository.LoanRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.unac.domain.DeviceStatus;
import edu.unac.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@AutoConfigureMockMvc
class LoanControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        loanRepository.deleteAll();
        deviceRepository.deleteAll();
    }

    @Test
    void registerLoanTest() throws Exception {
        Device deviceSaved = deviceRepository.save(new Device(null, "Laptop", "Electronics", "Office", DeviceStatus.AVAILABLE, System.currentTimeMillis()));
        Loan loan = new Loan(null, deviceSaved.getId(), "Juan Perez", System.currentTimeMillis(), System.currentTimeMillis() + 604800000L, false);

        mockMvc.perform(
                post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loan))
        ).andExpect(status().isCreated())
        .andExpect(jsonPath(("$.borrowedBy"), is("Juan Perez")))
        .andExpect(jsonPath("$.deviceId", is(deviceSaved.getId().intValue())));
    }

    @Test
    void registerLoanInvalidDeviceTest() throws Exception {
        Loan loan = new Loan(null, 1L, "Juan Perez", System.currentTimeMillis(), System.currentTimeMillis() + 604800000L, false);

        mockMvc.perform(
                post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loan))
        ).andExpect(status().isBadRequest());
    }

    @Test
    void getAllLoansTest() throws Exception {
        Device deviceSaved1 = deviceRepository.save(new Device(null, "Laptop", "Electronics", "Office", DeviceStatus.AVAILABLE, System.currentTimeMillis()));
        loanRepository.save(new Loan(null, deviceSaved1.getId(), "Juan Perez", System.currentTimeMillis(), System.currentTimeMillis() + 604800000L, false));

        Device deviceSaved2 = deviceRepository.save(new Device(null, "TVs", "Electronics", "Movie Room", DeviceStatus.AVAILABLE, System.currentTimeMillis()));
        loanRepository.save(new Loan(null, deviceSaved2.getId(), "Juan Perez", System.currentTimeMillis(), System.currentTimeMillis() + 604800000L, false));

        mockMvc.perform(
            get("/api/loans")
        ).andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getLoanByIdTest() throws Exception {
        Device deviceSaved = deviceRepository.save(new Device(null, "Laptop", "Electronics", "Office", DeviceStatus.AVAILABLE, System.currentTimeMillis()));
        Loan loanSaved = loanRepository.save(new Loan(null, deviceSaved.getId(), "Juan Perez", System.currentTimeMillis(), System.currentTimeMillis() + 604800000L, false));

        mockMvc.perform(
            get("/api/loans/" + loanSaved.getId())
        ).andExpect(status().isOk())
        .andExpect(jsonPath("$.borrowedBy", is("Juan Perez")))
        .andExpect(jsonPath("$.deviceId", is(deviceSaved.getId().intValue())));
    }

    @Test
    void markAsReturnedTest() throws Exception {
        Device deviceSaved = deviceRepository.save(new Device(null, "Laptop", "Electronics", "Office", DeviceStatus.AVAILABLE, System.currentTimeMillis()));
        Loan loanSaved = loanRepository.save(new Loan(null, deviceSaved.getId(), "Juan Perez", System.currentTimeMillis(), System.currentTimeMillis() + 604800000L, false));

        mockMvc.perform(
            put("/api/loans/" + loanSaved.getId() + "/return")
        ).andExpect(status().isOk())
        .andExpect(jsonPath("$.returned", is(true)))
        .andExpect(jsonPath("$.deviceId", is(deviceSaved.getId().intValue())));
    }

    @Test
    void markAsReturnedLoanNotFoundTest() throws Exception {
        mockMvc.perform(
            put("/api/loans/8/return")
        ).andExpect(status().isNotFound());
    }

    @Test
    void markAsReturnedAlreadyReturnedTest() throws Exception {
        Loan loanSaved = loanRepository.save(new Loan(null, null, "Juan Perez", System.currentTimeMillis(), System.currentTimeMillis() + 604800000L, true));

        mockMvc.perform(
            put("/api/loans/" + loanSaved.getId() + "/return")
        ).andExpect(status().isConflict());
    }

    @Test
    void getLoansByDeviceIdTest() throws Exception {
        Device deviceSaved = deviceRepository.save(new Device(null, "Laptop", "Electronics", "Office", DeviceStatus.AVAILABLE, System.currentTimeMillis()));
        loanRepository.save(new Loan(null, deviceSaved.getId(), "Juan Perez", System.currentTimeMillis(), System.currentTimeMillis() + 604800000L, false));

        mockMvc.perform(
            get("/api/loans/device/" + deviceSaved.getId())
        ).andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
    }
}