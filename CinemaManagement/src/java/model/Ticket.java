package model;

import java.util.Date;

public class Ticket {
    private int ticketId;
    private Integer customerId;
    private int showtimeId;
    private String seatNumber;
    private String ticketType;
    private double ticketPrice;
    private double discountAmount;
    private double finalPrice;
    private double pointsUsed;
    private double pointsEarned;
    private String paymentStatus;
    private Date bookingDate;
    private Date paymentDate;
    
    // Thông tin bổ sung
    private String customerName;
    private String movieName;
    private String roomName;
    private Date showDate;
    private String showTime;
    
    public Ticket() {}
    
    public Ticket(int ticketId, Integer customerId, int showtimeId, 
                  String seatNumber, String ticketType, double ticketPrice, 
                  double discountAmount, double finalPrice, String paymentStatus) {
        this.ticketId = ticketId;
        this.customerId = customerId;
        this.showtimeId = showtimeId;
        this.seatNumber = seatNumber;
        this.ticketType = ticketType;
        this.ticketPrice = ticketPrice;
        this.discountAmount = discountAmount;
        this.finalPrice = finalPrice;
        this.paymentStatus = paymentStatus;
    }
    
    // Getters and Setters
    public int getTicketId() { return ticketId; }
    public void setTicketId(int ticketId) { this.ticketId = ticketId; }
    
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    
    public int getShowtimeId() { return showtimeId; }
    public void setShowtimeId(int showtimeId) { this.showtimeId = showtimeId; }
    
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    
    public String getTicketType() { return ticketType; }
    public void setTicketType(String ticketType) { this.ticketType = ticketType; }
    
    public double getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(double ticketPrice) { this.ticketPrice = ticketPrice; }
    
    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }
    
    public double getFinalPrice() { return finalPrice; }
    public void setFinalPrice(double finalPrice) { this.finalPrice = finalPrice; }
    
    public double getPointsUsed() { return pointsUsed; }
    public void setPointsUsed(double pointsUsed) { this.pointsUsed = pointsUsed; }
    
    public double getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(double pointsEarned) { this.pointsEarned = pointsEarned; }
    
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    
    public Date getBookingDate() { return bookingDate; }
    public void setBookingDate(Date bookingDate) { this.bookingDate = bookingDate; }
    
    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getMovieName() { return movieName; }
    public void setMovieName(String movieName) { this.movieName = movieName; }
    
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    
    public Date getShowDate() { return showDate; }
    public void setShowDate(Date showDate) { this.showDate = showDate; }
    
    public String getShowTime() { return showTime; }
    public void setShowTime(String showTime) { this.showTime = showTime; }
}