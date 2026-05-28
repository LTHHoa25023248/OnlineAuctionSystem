package com.example.auctionmanagementsystem.service;

import com.example.auctionmanagementsystem.config.DatabaseConnection;
import com.example.auctionmanagementsystem.dao.AuctionDAO;
import com.example.auctionmanagementsystem.dao.UserDAO;
import com.example.auctionmanagementsystem.model.Auction;
import com.example.auctionmanagementsystem.model.Bidder;
import com.example.auctionmanagementsystem.model.Item;
import com.example.auctionmanagementsystem.model.Seller;
import com.example.auctionmanagementsystem.observer.AuctionNotifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuctionSchedulerTest {

    @Mock private AuctionDAO auctionDao;
    @Mock private AuctionService auctionService;
    @Mock private PaymentService paymentService;
    @Mock private Connection mockConnection;
    @Mock private AuctionNotifier mockNotifier;

    private AuctionScheduler scheduler;
    private MockedStatic<DatabaseConnection> mockedDb;
    private MockedStatic<UserDAO> mockedUserDao;
    private MockedStatic<AuctionNotifier> mockedNotifierStatic;

    @BeforeEach
    void setUp() {
        // Dùng constructor test vừa tạo để inject mocks
        scheduler = new AuctionScheduler(auctionDao, auctionService, paymentService);

        // Mock kết nối Database
        mockedDb = Mockito.mockStatic(DatabaseConnection.class);
        mockedDb.when(DatabaseConnection::getConnection).thenReturn(mockConnection);

        // Mock UserDAO (chứa các hàm static)
        mockedUserDao = Mockito.mockStatic(UserDAO.class);

        // Mock Singleton AuctionNotifier
        mockedNotifierStatic = Mockito.mockStatic(AuctionNotifier.class);
        mockedNotifierStatic.when(AuctionNotifier::getInstance).thenReturn(mockNotifier);
    }

    @AfterEach
    void tearDown() {
        // Đóng các mock test để sau mỗi mock test không 
        // sử dụng lại dữ liệu của mock test cũ
        mockedDb.close();
        mockedUserDao.close();
        mockedNotifierStatic.close();
    }

    // Khi phiên đấu giá hết giờ -> Chốt đơn, thanh toán và gửi thông báo
    @Test
    void testCloseExpiredAuctions_AuctionExpired_ProcessesSuccessfully() throws Exception {
        // Chuẩn bị dữ liệu: Một phiên đấu giá đã kết thúc cách đây 1 giờ
        Auction expiredAuction = new Auction();
        expiredAuction.setId(101);
        expiredAuction.setEndTime(LocalDateTime.now().minusHours(1));
        expiredAuction.setCurrentPrice(500.0);
        
        Item mockItem = mock(Item.class); 
        when(mockItem.getName()).thenReturn("MacBook Pro"); // Giả lập hàm getName trả về "MacBook Pro"
        expiredAuction.setItem(mockItem);

        Bidder winner = new Bidder(); winner.setId(2);
        Seller seller = new Seller(); seller.setId(3);
        expiredAuction.setHighestBidder(winner);
        expiredAuction.setSeller(seller);

        when(auctionDao.selectOpenAuctions(mockConnection)).thenReturn(Arrays.asList(expiredAuction));
        
        // Mock số dư giả định
        mockedUserDao.when(() -> UserDAO.getBalance(2, mockConnection)).thenReturn(1000.0);
        mockedUserDao.when(() -> UserDAO.getBalance(3, mockConnection)).thenReturn(200.0);

        // Gọi trực tiếp hàm xử lý logic 
        scheduler.closeExpiredAuctions();

        // Các hàm này chỉ được diễn ra một lần (đấu giá kết thúc luôn -> người thắng trả tiền)
        verify(auctionService, times(1)).endAuction(mockConnection, expiredAuction);
        verify(paymentService, times(1)).processPayment(mockConnection, expiredAuction);
        
        verify(mockNotifier, times(1)).notifyAuctionResult(
                101, "MacBook Pro", 2, 500.0, 1000.0, 200.0
        );
    }

    //Phiên đấu giá vẫn đang chạy
    @Test
    void testCloseExpiredAuctions_AuctionNotExpired_SkipsProcessing() throws Exception {
        // Chuẩn bị dữ liệu: Ngày mai mới hết hạn
        Auction runningAuction = new Auction();
        runningAuction.setId(102);
        runningAuction.setEndTime(LocalDateTime.now().plusDays(1));

        when(auctionDao.selectOpenAuctions(mockConnection)).thenReturn(Arrays.asList(runningAuction));

        scheduler.closeExpiredAuctions();

        // Khi thời gian chưa kết thúc, các service liên quan (kết thúc đấu giá, 
        // thanh toán, thông báo không được chạy)
        verify(auctionService, never()).endAuction(any(), any());
        verify(paymentService, never()).processPayment(any(), any());
        verify(mockNotifier, never()).notifyAuctionResult(anyInt(), anyString(), anyInt(), anyDouble(), anyDouble(), anyDouble());
    }

    // Xử lý an toàn khi thanh toán bị lỗi 
    @Test
    void testCloseExpiredAuctions_PaymentFails_CatchesException() throws Exception {
        Auction expiredAuction = new Auction();
        expiredAuction.setId(103);
        expiredAuction.setEndTime(LocalDateTime.now().minusMinutes(5));

        when(auctionDao.selectOpenAuctions(mockConnection)).thenReturn(Collections.singletonList(expiredAuction));

        // Giả lập lỗi ở bước thanh toán
        doThrow(new RuntimeException("Payment Gateway Down"))
                .when(paymentService).processPayment(any(), any());

        // Hàm này không được ném văng Exception ra ngoài (nếu ném ra, luồng Thread ngầm sẽ bị chết)
        scheduler.closeExpiredAuctions();

        // Thông báo kết quả sẽ không được gọi vì bị kẹt ở catch
        verify(mockNotifier, never()).notifyAuctionResult(anyInt(), anyString(), anyInt(), anyDouble(), anyDouble(), anyDouble());
    }
}