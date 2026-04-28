package exception;

//Trang thai hoat dong:Running, finished,..
public class AuctionStatusException extends AuctionException{
    public AuctionStatusException(String Status) {
        super("Action denied: Auction is currently in " +Status + " state.");
    }
}
