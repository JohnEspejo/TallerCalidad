package edu.unac.service;

import edu.unac.domain.Device;
import edu.unac.domain.DeviceStatus;
import edu.unac.domain.Loan;
import edu.unac.repository.DeviceRepository;
import edu.unac.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

class LoanServiceTest {
    DeviceRepository deviceRepository;
    LoanRepository loanRepository;

    @BeforeEach
    void setUp() {
        deviceRepository = mock(DeviceRepository.class);
        loanRepository = mock(LoanRepository.class);
    }

    @Test
    void registerLoanSavedTest(){
        Device device = new Device(1L, "Laptop", "Electronics", "Office", DeviceStatus.AVAILABLE, System.currentTimeMillis());
        Loan loan = new Loan(null, device.getId(), "Juan Perez", System.currentTimeMillis(), System.currentTimeMillis() + 604800000L, false);

        when(deviceRepository.findById(device.getId())).thenReturn(Optional.of(device));
        when(deviceRepository.save(device)).thenReturn(new Device(device.getId(), device.getName(), device.getType(), device.getLocation(), DeviceStatus.LOANED, device.getAddedDate()));
        when(loanRepository.save(loan)).thenReturn(new Loan(1L, device.getId(), "Juan Perez", loan.getStartDate(), loan.getEndDate(), false));

        LoanService loanService = new LoanService(loanRepository, deviceRepository);
        Loan registeredLoan = loanService.registerLoan(loan);

        assertEquals("Juan Perez", registeredLoan.getBorrowedBy());
    }

    @Test
    void registerLoanDeviceNotFoundTest() {
        Loan loan = new Loan(null, 1L, "Juan Perez", System.currentTimeMillis(), System.currentTimeMillis() + 604800000L, false);

        when(deviceRepository.findById(1L)).thenReturn(Optional.empty());

        LoanService loanService = new LoanService(loanRepository, deviceRepository);

        assertThrows(IllegalArgumentException.class,
                () -> loanService.registerLoan(loan));
    }

    @Test
    void registerLoanDeviceNotAvailableTest() {
        Device device = new Device(1L, "Laptop", "Electronics", "Office", DeviceStatus.LOANED, System.currentTimeMillis());
        Loan loan = new Loan(null, device.getId(), "Juan Perez", System.currentTimeMillis(), System.currentTimeMillis() + 604800000L, false);

        when(deviceRepository.findById(device.getId())).thenReturn(Optional.of(device));

        LoanService loanService = new LoanService(loanRepository, deviceRepository);

        assertThrows(IllegalStateException.class,
                () -> loanService.registerLoan(loan));
    }

    @Test
    void getAllLoansTest() {
        Loan loan1 = new Loan(null, 1L, "Juan Perez", System.currentTimeMillis(), System.currentTimeMillis() + 604800000L, false);
        Loan loan2 = new Loan(null, 2L, "Maria Lopez", System.currentTimeMillis(), System.currentTimeMillis() + 604800000L, false);

        when(loanRepository.findAll()).thenReturn(List.of(loan1, loan2));

        LoanService loanService = new LoanService(loanRepository, deviceRepository);
        List<Loan> loans = loanService.getAllLoans();

        assertEquals(2, loans.size());
    }

    @Test
    void getLoanByIdTest() {
        Loan loan = new Loan(1L, 1L, "Juan Perez", System.currentTimeMillis(), System.currentTimeMillis() + 604800000L, false);

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        LoanService loanService = new LoanService(loanRepository, deviceRepository);
        Optional<Loan> foundLoan = loanService.getLoanById(1L);

        assertTrue(foundLoan.isPresent());
    }

    @Test
    void markAsReturnedTest() {
        Device device = new Device(1L, "Laptop", "Electronics", "Office", DeviceStatus.LOANED, System.currentTimeMillis());
        Loan loan = new Loan(1L, device.getId(), "Juan Perez", System.currentTimeMillis(), System.currentTimeMillis() + 604800000L,false);

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(deviceRepository.findById(device.getId())).thenReturn(Optional.of(device));
        when(deviceRepository.save(device)).thenReturn(new Device(device.getId(), device.getName(), device.getType(), device.getLocation(), DeviceStatus.AVAILABLE, device.getAddedDate()));
        when(loanRepository.save(loan)).thenReturn(new Loan(1L, device.getId(), "Juan Perez", loan.getStartDate(), System.currentTimeMillis(), true));

        LoanService loanService = new LoanService(loanRepository, deviceRepository);
        Loan returnedLoan = loanService.markAsReturned(1L);

        assertTrue(returnedLoan.isReturned());
        assertEquals(DeviceStatus.AVAILABLE, device.getStatus());
    }

    @Test
    void markAsReturnedLoanNotFoundTest() {
        when(loanRepository.findById(1L)).thenReturn(Optional.empty());

        LoanService loanService = new LoanService(loanRepository, deviceRepository);

        assertThrows(IllegalArgumentException.class,
                () -> loanService.markAsReturned(1L));
    }

    @Test
    void markAsReturnedLoanReturnedTest() {
        Loan loan = new Loan(1L, 1L, "Juan Perez", System.currentTimeMillis(), System.currentTimeMillis() + 604800000L, true);

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        LoanService loanService = new LoanService(loanRepository, deviceRepository);

        assertThrows(IllegalStateException.class,
                () -> loanService.markAsReturned(1L));
    }

    @Test
    void markAsReturnedDeviceNotFoundTest() {
        Loan loan = new Loan(1L, 1L, "Juan Perez", System.currentTimeMillis(), System.currentTimeMillis() + 604800000L, false);

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(deviceRepository.findById(loan.getDeviceId())).thenReturn(Optional.empty());

        LoanService loanService = new LoanService(loanRepository, deviceRepository);

        assertThrows(IllegalArgumentException.class,
                () -> loanService.markAsReturned(1L));
    }

    @Test
    void getLoansByDeviceTest() {
        Device device = new Device(1L, "Laptop", "Electronics", "Office", DeviceStatus.AVAILABLE, System.currentTimeMillis());
        Loan loan1 = new Loan(null, device.getId(), "Juan Perez", System.currentTimeMillis(), System.currentTimeMillis() + 604800000L, false);
        Loan loan2 = new Loan(null, device.getId(), "Maria Lopez", System.currentTimeMillis(), System.currentTimeMillis() + 604800000L, false);

        when(loanRepository.findByDeviceId(device.getId())).thenReturn(List.of(loan1, loan2));

        LoanService loanService = new LoanService(loanRepository, deviceRepository);
        List<Loan> loans = loanService.getLoansByDeviceId(device.getId());

        assertEquals(2, loans.size());
    }
}