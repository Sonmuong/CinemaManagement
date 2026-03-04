package model;

public class Room {
    private int roomId;
    private String roomName;
    private int totalSeats;
    private String status;
    
    public Room() {}
    
    public Room(int roomId, String roomName, int totalSeats, String status) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.totalSeats = totalSeats;
        this.status = status;
    }
    
    // Getters and Setters
    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }
    
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    
    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}