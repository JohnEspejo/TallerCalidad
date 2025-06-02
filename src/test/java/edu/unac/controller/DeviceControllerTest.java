package edu.unac.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.unac.domain.Device;
import edu.unac.domain.DeviceStatus;
import edu.unac.domain.Loan;
import edu.unac.repository.DeviceRepository;
import edu.unac.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@AutoConfigureMockMvc
class DeviceControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private LoanRepository loanRepository;

    @BeforeEach
    void setup() {
        deviceRepository.deleteAll();
    }

    @Test
    void registerDeviceTest() throws Exception {
        Device device = new Device(null, "Laptop", "Electronics", "Office", DeviceStatus.AVAILABLE, System.currentTimeMillis());

        mockMvc.perform(
            post("/api/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(device))
        ).andExpect(status().isCreated())
        .andExpect(jsonPath("$.name", is("Laptop")));
    }

    @Test
    void registerDeviceInvalidNameTest() throws Exception {
        Device device = new Device(null, null, "Electronics", "Office", DeviceStatus.AVAILABLE, System.currentTimeMillis());

        mockMvc.perform(
            post("/api/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(device))
        ).andExpect(status().isBadRequest());
    }

    @Test
    void getAllDevicesTest() throws Exception {
        deviceRepository.save(new Device(null, "Laptop", "Electronics", "Office", DeviceStatus.AVAILABLE, System.currentTimeMillis()));
        deviceRepository.save(new Device(null, "TVs", "Electronics", "Movie Room", DeviceStatus.AVAILABLE, System.currentTimeMillis()));

        mockMvc.perform(
            get("/api/devices")
        ).andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getDeviceByIdTest() throws Exception {
        Device deviceSaved = deviceRepository.save(new Device(null, "Laptop", "Electronics", "Office", DeviceStatus.AVAILABLE, System.currentTimeMillis()));

        mockMvc.perform(
            get("/api/devices/" + deviceSaved.getId())
        ).andExpect(status().isOk())
        .andExpect(jsonPath("$.name", is("Laptop")));
    }

    @Test
    void updateDeviceStatusTest() throws Exception {
        Device deviceSaved = deviceRepository.save(new Device(null, "Laptop", "Electronics", "Office", DeviceStatus.AVAILABLE, System.currentTimeMillis()));

        mockMvc.perform(
            put("/api/devices/" + deviceSaved.getId() + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .param("status", DeviceStatus.LOANED.name())
        ).andExpect(status().isOk())
        .andExpect(jsonPath("$.status", is("LOANED")));
    }

    @Test
    void updateDeviceStatusDeviceNotFoundTest() throws Exception {
        mockMvc.perform(
            put("/api/devices/8/status")
                .contentType(MediaType.APPLICATION_JSON)
                .param("status", DeviceStatus.LOANED.name())
        ).andExpect(status().isNotFound());
    }

    @Test
    void deleteDeviceTest() throws Exception {
        Device deviceSaved = deviceRepository.save(new Device(null, "Laptop", "Electronics", "Office", DeviceStatus.AVAILABLE, System.currentTimeMillis()));

        mockMvc.perform(
            delete("/api/devices/" + deviceSaved.getId())
        ).andExpect(status().isNoContent());
    }

    @Test
    void deleteDeviceWithLoanHistoryTest() throws Exception {
        Device deviceSaved = deviceRepository.save(new Device(null, "Laptop", "Electronics", "Office", DeviceStatus.AVAILABLE, System.currentTimeMillis()));
        loanRepository.save(new Loan(null, deviceSaved.getId(), "Juan Perez", System.currentTimeMillis(), System.currentTimeMillis() + 604800000L, false));

        mockMvc.perform(
            delete("/api/devices/" + deviceSaved.getId())
        ).andExpect(status().isConflict());
    }
}