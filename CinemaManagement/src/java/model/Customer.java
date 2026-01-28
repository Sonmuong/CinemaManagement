package model;

import java.util.Date;

public class Customer {
    private int customerId;
    private String fullName;
    private String phone;
    private String email;
    private double loyaltyPoints;
    private Date createdDate;
    
    public Customer() {}
    
    public Customer(int customerId, String fullName, String phone, 
                    String email, double loyaltyPoints) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.loyaltyPoints = loyaltyPoints;
    }
    
    // Getters and Setters
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public double getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(double loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }
    
    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }
    
    public boolean isVIP() {
        return loyaltyPoints >= 1000;
    }
}