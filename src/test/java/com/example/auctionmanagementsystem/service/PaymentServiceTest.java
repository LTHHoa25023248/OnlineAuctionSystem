package com.example.auctionmanagementsystem.service;

import com.example.auctionmanagementsystem.dao.AuctionDAO;
import com.example.auctionmanagementsystem.dao.UserDAO;
import com.example.auctionmanagementsystem.exception.InsufficientBalanceException;
import com.example.auctionmanagementsystem.model.Auction;
import com.example.auctionmanagementsystem.model.AuctionStatus;
import com.example.auctionmanagementsystem.model.Bidder;
import com.example.auctionmanagementsystem.model.Seller;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock private AuctionDAO auctionDAO;
    @Mock private Connection mockConnection;
    
    private PaymentService paymentService;
    private Auction testAuction;
    private Bidder testBidder;
    private Seller testSeller;
    private MockedStatic<UserDAO> mockedUserDao;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(auctionDAO);

        testAuction = new Auction();
        testAuction.setStatus(AuctionStatus.FINISHED);
        testAuction.setCurrentPrice(150.0);

        testBidder = new Bidder();
        testBidder.setId(1);

        testSeller = new Seller();
        testSeller.setId(2);

        // Bắt buộc mock static UserDAO
        mockedUserDao = Mockito.mockStatic(UserDAO.class);
    }

    @AfterEach
    void tearDown() {
        // Luôn đóng static mock sau mỗi test
        mockedUserDao.close();
    }

    @Test
    void testProcessPayment_Success_UpdatesBalancesAndStatus() throws Exception {
        testAuction.setHighestBidder(testBidder);
        testAuction.setSeller(testSeller);

        // Bidder có 500, Seller có 100
        mockedUserDao.when(() -> UserDAO.getBalance(1, mockConnection)).thenReturn(500.0);
        mockedUserDao.when(() -> UserDAO.getBalance(2, mockConnection)).thenReturn(100.0);

        paymentService.processPayment(mockConnection, testAuction);

        // Sau khi thanh toán, trạng thái đổi thành PAID
        assertEquals(AuctionStatus.PAID, testAuction.getStatus());
        
        // Trừ tiền, cộng tiền và lưu DB được gọi
        mockedUserDao.verify(() -> UserDAO.updateBalance(1, 350.0, mockConnection)); // 500 - 150 = 350
        mockedUserDao.verify(() -> UserDAO.updateBalance(2, 250.0, mockConnection)); // 100 + 150 = 250
        verify(auctionDAO).update(testAuction, mockConnection);
        verify(mockConnection).commit();
        verify(mockConnection).setAutoCommit(true);
    }

    @Test
    void testProcessPayment_InsufficientBalance_RollsBack() throws Exception {
        testAuction.setHighestBidder(testBidder);
        testAuction.setSeller(testSeller);

        // Giả lập số dư: Bidder chỉ có 50 (không đủ mua món 150)
        mockedUserDao.when(() -> UserDAO.getBalance(1, mockConnection)).thenReturn(50.0);

        InsufficientBalanceException exception = assertThrows(InsufficientBalanceException.class, () -> {
            paymentService.processPayment(mockConnection, testAuction);
        });

        // Rollback giao dịch, hoàn trả AutoCommit và update status giữ nguyên
        verify(mockConnection).rollback();
        verify(mockConnection).setAutoCommit(true);
        assertEquals(AuctionStatus.FINISHED, testAuction.getStatus(), "Trạng thái không được đổi thành PAID");
        
        // Tuyệt đối không có lệnh update số dư nào được gọi
        mockedUserDao.verify(() -> UserDAO.updateBalance(anyInt(), anyDouble(), any()), never());
    }

    @Test
    void testProcessPayment_NotFinished_ThrowsException() throws Exception {
        testAuction.setStatus(AuctionStatus.RUNNING);

        assertThrows(IllegalStateException.class, () -> {
            paymentService.processPayment(mockConnection, testAuction);
        });

        verify(mockConnection, never()).setAutoCommit(false);
    }
}