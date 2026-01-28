package model;

import java.util.Date;
import java.util.List;

public class Movie {
    private int movieId;
    private String movieName;
    private int duration;
    private String country;
    private int releaseYear;
    private int ageRestriction;
    private String director;
    private String mainActors;
    private String status;
    private Date createdDate;
    private List<String> genres;
    
    public Movie() {}
    
    public Movie(int movieId, String movieName, int duration, String country, 
                 int releaseYear, int ageRestriction, String director, 
                 String mainActors, String status) {
        this.movieId = movieId;
        this.movieName = movieName;
        this.duration = duration;
        this.country = country;
        this.releaseYear = releaseYear;
        this.ageRestriction = ageRestriction;
        this.director = director;
        this.mainActors = mainActors;
        this.status = status;
    }
    
    // Getters and Setters
    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }
    
    public String getMovieName() { return movieName; }
    public void setMovieName(String movieName) { this.movieName = movieName; }
    
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public int getReleaseYear() { return releaseYear; }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }
    
    public int getAgeRestriction() { return ageRestriction; }
    public void setAgeRestriction(int ageRestriction) { this.ageRestriction = ageRestriction; }
    
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }
    
    public String getMainActors() { return mainActors; }
    public void setMainActors(String mainActors) { this.mainActors = mainActors; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }
    
    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }
}