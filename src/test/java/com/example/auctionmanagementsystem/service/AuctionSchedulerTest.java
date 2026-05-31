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

    private AuctionScheduler scheduler;
    private MockedStatic<DatabaseConnection> mockedDb;

    @BeforeEach
    void setUp() {
        // Dùng constructor test vừa tạo để inject mocks
        scheduler = new AuctionScheduler(auctionDao, auctionService, paymentService);

        // Mock kết nối Database
        mockedDb = Mockito.mockStatic(DatabaseConnection.class);
        mockedDb.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
    }

    @AfterEach
    void tearDown() {
        // Đóng các mock test để sau mỗi mock test không 
        // sử dụng lại dữ liệu của mock test cũ
        mockedDb.close();
    }

    // Khi phiên đấu giá hết giờ -> Chốt đơn, thanh toán và gửi thông báo
    @Test
    void testCloseExpiredAuctions_AuctionExpired_ProcessesSuccessfully() throws Exception {
        // Chuẩn bị dữ liệu: Một phiên đấu giá đã kết thúc cách đây 1 giờ
        Auction expiredAuction = new Auction();
        expiredAuction.setId(101);
        expiredAuction.setEndTime(LocalDateTime.now().minusHours(1));

        // Giả lập Database trả về phiên đấu giá này
        when(auctionDao.selectOpenAuctions(mockConnection)).thenReturn(Arrays.asList(expiredAuction));
        scheduler.closeExpiredAuctions();

        // Hàm endAuction được gọi đúng 1 lần
        verify(auctionService, times(1)).endAuction(mockConnection, expiredAuction);
        
        verify(paymentService, never()).processPayment(any(), any());
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
    }

    // Xử lý an toàn khi thanh toán bị lỗi 
    @Test
    void testCloseExpiredAuctions_EndAuctionFails_CatchesExceptionAndContinues() throws Exception {
        // Chuẩn bị 2 phiên đấu giá đều hết hạn
        Auction auction1 = new Auction(); auction1.setId(103); auction1.setEndTime(LocalDateTime.now().minusMinutes(5));
        Auction auction2 = new Auction(); auction2.setId(104); auction2.setEndTime(LocalDateTime.now().minusMinutes(5));

        when(auctionDao.selectOpenAuctions(mockConnection)).thenReturn(Arrays.asList(auction1, auction2));

        // Khi đóng auction1 thì hệ thống ném ra lỗi
        doThrow(new RuntimeException("Database Lock Error"))
                .when(auctionService).endAuction(mockConnection, auction1);

        // Gọi hàm quét
        scheduler.closeExpiredAuctions();

        //  auction1 bị lỗi, hệ thống vẫn không sập và tiếp tục gọi endAuction cho auction2
        verify(auctionService, times(1)).endAuction(mockConnection, auction1);
        verify(auctionService, times(1)).endAuction(mockConnection, auction2);
    }
}