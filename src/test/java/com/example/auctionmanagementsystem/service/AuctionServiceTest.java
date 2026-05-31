package com.example.auctionmanagementsystem.service;

import com.example.auctionmanagementsystem.dao.AuctionDAO;
import com.example.auctionmanagementsystem.model.Auction;
import com.example.auctionmanagementsystem.model.AuctionStatus;
import com.example.auctionmanagementsystem.model.Item;
import com.example.auctionmanagementsystem.model.Seller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuctionServiceTest {

    @Mock private AuctionDAO auctionDao;
    @Mock private Connection mockConnection;
    
    private AuctionService auctionService;
    private Auction testAuction;

    @BeforeEach
    void setUp() {
        // Sử dụng Constructor Test vừa tạo
        auctionService = new AuctionService(auctionDao);
        testAuction = new Auction();
    }

    @Test
    void testCreateAuction_ValidData_SetsStatusToPending() {
        Item mockItem = mock(Item.class);
        Seller mockSeller = mock(Seller.class);
        testAuction.setItem(mockItem);
        testAuction.setSeller(mockSeller);

        auctionService.createAuction(mockConnection, testAuction);

        // Đảm bảo trạng thái bị ép về PENDING chờ duyệt
        assertEquals(AuctionStatus.PENDING, testAuction.getStatus());
        verify(auctionDao, times(1)).insert(testAuction, mockConnection);
    }

    @Test
    void testCreateAuction_MissingItem_ThrowsException() {
        testAuction.setSeller(mock(Seller.class));
        // Cố tình không set Item

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            auctionService.createAuction(mockConnection, testAuction);
        });
        
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        verify(auctionDao, never()).insert(any(), any());
    }

    @Test
    void testApproveAuction_StatusIsPending_ChangesToOpen() {
        testAuction.setStatus(AuctionStatus.PENDING);
        testAuction.setRejectReason("Lý do cũ nào đó");

        auctionService.approveAuction(mockConnection, testAuction);

        assertEquals(AuctionStatus.OPEN, testAuction.getStatus());
        assertNull(testAuction.getRejectReason(), "Phải xóa lý do từ chối cũ khi duyệt");
        verify(auctionDao, times(1)).update(testAuction, mockConnection);
    }

    @Test
    void testRejectAuction_NoReasonProvided_ThrowsException() {
        testAuction.setStatus(AuctionStatus.PENDING);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            auctionService.rejectAuction(mockConnection, testAuction, "   ");
        });

        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        verify(auctionDao, never()).update(any(), any());
    }


    @Test
    void testStartAuction_StatusNotPending_ThrowsException() {
        // Cố tình start một phiên đã FINISHED hoặc RUNNING
        testAuction.setStatus(AuctionStatus.FINISHED);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            auctionService.startAuction(mockConnection, testAuction);
        });

        assertInstanceOf(IllegalStateException.class, exception.getCause());
        assertEquals("Auction must be PENDING to start", exception.getCause().getMessage());
        verify(auctionDao, never()).update(any(), any());
    }

    @Test
    void testCancelAuction_AuctionAlreadyFinished_ThrowsException() {
        testAuction.setStatus(AuctionStatus.FINISHED);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            auctionService.cancelAuction(mockConnection, testAuction);
        });

        assertInstanceOf(IllegalStateException.class, exception.getCause());
        assertEquals("Cannot cancel finished auction", exception.getCause().getMessage());
    }
}