package com.example.auctionmanagementsystem.service;

import com.example.auctionmanagementsystem.dao.AuctionDAO;
import com.example.auctionmanagementsystem.dao.AutoBidDAO;
import com.example.auctionmanagementsystem.dao.BidTransactionDAO;
import com.example.auctionmanagementsystem.model.Auction;
import com.example.auctionmanagementsystem.model.AutoBid;
import com.example.auctionmanagementsystem.model.Bidder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdvancedAuctionServiceTest {

    @Mock private AutoBidDAO autoBidDao;
    @Mock private AuctionDAO auctionDao;
    @Mock private BidTransactionDAO bidDao;
    @Mock private Connection mockConnection;

    private AdvancedAuctionService advancedService;
    private Auction testAuction;

    @BeforeEach
    void setUp() {
        advancedService = new AdvancedAuctionService(autoBidDao, auctionDao, bidDao);
        testAuction = new Auction();
        testAuction.setId(1);
        testAuction.setCurrentPrice(100.0);
    }

    @Test
    void testApplyAntiSniping_TimeLeftLessThan10s_ExtendsTimeBy60s() {
        // Giả lập thời gian kết thúc chỉ còn 5 giây nữa
        LocalDateTime originalEndTime = LocalDateTime.now().plusSeconds(5);
        testAuction.setEndTime(originalEndTime);

        advancedService.applyAntiSniping(mockConnection, testAuction);

        // Mong muốn thời gian kết thúc MỚI phải lớn hơn thời gian ban đầu 
        // vì đã được cộng thêm 60s
        assertTrue(testAuction.getEndTime().isAfter(originalEndTime));
        verify(auctionDao, times(1)).update(testAuction, mockConnection);
    }

    @Test
    void testApplyAntiSniping_TimeLeftMoreThan10s_DoesNothing() {
        // Giả lập thời gian kết thúc còn 1 giờ nữa
        LocalDateTime originalEndTime = LocalDateTime.now().plusHours(1);
        testAuction.setEndTime(originalEndTime);

        advancedService.applyAntiSniping(mockConnection, testAuction);

        // Thời gian kết thúc giữ nguyên, không gọi hàm update DB vì thời gian
        // không thỏa mãn yêu cầu được gia hạn thời gian 
        assertEquals(originalEndTime, testAuction.getEndTime());
        verify(auctionDao, never()).update(any(), any());
    }

    @Test
    void testProcessAutoBids_NoAutoBidders_DoesNothing() {
        when(autoBidDao.selectByAuctionId(mockConnection, 1)).thenReturn(Collections.emptyList());

        advancedService.processAutoBids(mockConnection, testAuction);

        // Không có ai đăng ký AutoBid thì không làm gì cả
        verify(auctionDao, never()).update(any(), any());
        verify(bidDao, never()).insert(any(), any());
    }

    @Test
    void testProcessAutoBids_TwoBidders_TopBeatsChallenger() {
        // Giá hiện tại: 100
        // Người A cài max = 150, bước giá = 10
        // Người B cài max = 200, bước giá = 10
        // Khi B chiến thắng, chỉ cần trả giá = 150 (của A) + 10 (bước giá) = 160. 
        // B không cần phải trả hết 200.

        Bidder challengerBidder = new Bidder(); challengerBidder.setId(2);
        AutoBid challenger = new AutoBid(testAuction, challengerBidder, 150.0, 10.0);
        challenger.setCreatedAt(LocalDateTime.now().minusMinutes(10));

        Bidder topBidder = new Bidder(); topBidder.setId(3);
        AutoBid top = new AutoBid(testAuction, topBidder, 200.0, 10.0);
        top.setCreatedAt(LocalDateTime.now());

        // Mock dữ liệu trả về từ DAO
        when(autoBidDao.selectByAuctionId(mockConnection, 1))
                .thenReturn(Arrays.asList(challenger, top));

        advancedService.processAutoBids(mockConnection, testAuction);

        assertEquals(160.0, testAuction.getCurrentPrice());
        assertEquals(topBidder, testAuction.getHighestBidder());

        verify(bidDao, times(1)).insert(eq(mockConnection), any());
        verify(auctionDao, times(1)).update(testAuction, mockConnection);
    }
    
    @Test
    void testProcessAutoBids_SingleBidder_PaysMinimumIncrement() {
        // Khi chỉ có 1 người đặt tự động giá 500, bước giá 10. Giá hiện tại là 100.
        // Người này chỉ bị trừ 100 + 10 = 110, thay vì 500.
        
        Bidder topBidder = new Bidder(); topBidder.setId(3);
        AutoBid top = new AutoBid(testAuction, topBidder, 500.0, 10.0);
        
        when(autoBidDao.selectByAuctionId(mockConnection, 1)).thenReturn(Arrays.asList(top));

        advancedService.processAutoBids(mockConnection, testAuction);

        assertEquals(110.0, testAuction.getCurrentPrice());
        assertEquals(topBidder, testAuction.getHighestBidder());
    }
}