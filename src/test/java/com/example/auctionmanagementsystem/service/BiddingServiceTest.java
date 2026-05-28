package com.example.auctionmanagementsystem.service;

import com.example.auctionmanagementsystem.config.DatabaseConnection;
import com.example.auctionmanagementsystem.dao.AuctionDAO;
import com.example.auctionmanagementsystem.dao.BidTransactionDAO;
import com.example.auctionmanagementsystem.exception.AuctionClosedException;
import com.example.auctionmanagementsystem.exception.InvalidBidException;
import com.example.auctionmanagementsystem.exception.SellerBiddingOwnItemException;
import com.example.auctionmanagementsystem.model.Auction;
import com.example.auctionmanagementsystem.model.AuctionStatus;
import com.example.auctionmanagementsystem.model.BidTransaction;
import com.example.auctionmanagementsystem.model.Bidder;
import com.example.auctionmanagementsystem.model.Seller;
import com.example.auctionmanagementsystem.model.User;
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
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BiddingServiceTest {

    @Mock private AuctionDAO auctionDao;
    @Mock private BidTransactionDAO bidDao;
    @Mock private AdvancedAuctionService advancedService;
    @Mock private Connection mockConnection;

    private BiddingService biddingService;
    private Auction testAuction;
    private Bidder testBidder;
    private MockedStatic<DatabaseConnection> mockedDbConnection;

    @BeforeEach
    public void setUp() throws Exception {
        biddingService = new BiddingService(auctionDao, bidDao, advancedService);

        mockedDbConnection = Mockito.mockStatic(DatabaseConnection.class);
        mockedDbConnection.when(DatabaseConnection::getConnection).thenReturn(mockConnection);

        testAuction = new Auction();
        testAuction.setStatus(AuctionStatus.RUNNING); 
        testAuction.setEndTime(LocalDateTime.now().plusDays(1));
        testAuction.setCurrentPrice(100.0);

        testBidder = new Bidder();
        testBidder.setId(1);
    }

    @AfterEach
    public void tearDown() {
        // Phải đóng static mock sau mỗi test để không ảnh hưởng test khác
        mockedDbConnection.close();
    }

    // Đặt giá thành công, kiểm tra gọi DB và Commit
    @Test
    public void testPlaceBid_Success_UpdatesMemoryAndCommits() throws Exception {
        biddingService.placeBid(testAuction, testBidder, 150.0);

        // Kiểm tra RAM đã cập nhật
        assertEquals(150.0, testAuction.getCurrentPrice());
        assertEquals(testBidder, testAuction.getHighestBidder());

        // Kiểm tra các thao tác DB đã được gọi
        verify(mockConnection).setAutoCommit(false);
        verify(bidDao).insert(eq(mockConnection), any(BidTransaction.class));
        verify(auctionDao).update(testAuction, mockConnection);
        verify(mockConnection).commit();
    }

    // Seller không được tự đặt giá
    @Test
    public void testPlaceBid_SellerBidsOwnItem_ThrowsException() {
        // Dùng Mockito để giả mạo một object Seller thay vì khởi tạo bằng toán tử new
        Seller mockSeller = mock(Seller.class);
        
        // Giả lập ID của Seller trùng với ID của testBidder (= 1)
        when(mockSeller.getId()).thenReturn(1); 
        
        testAuction.setSeller(mockSeller);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            biddingService.placeBid(testAuction, testBidder, 200.0);
        });
        
        assertInstanceOf(SellerBiddingOwnItemException.class, exception.getCause());
    }

}