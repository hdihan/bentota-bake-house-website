package com.bakery.system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private String role;

    private String securityQuestion;

    private String securityAnswer;

    private String resetToken;

    @Size(max = 100, message = "Full name too long")
    private String fullName;

    @Size(max = 255, message = "Address too long")
    private String homeAddress;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Telephone number must be 10-15 digits")
    private String telephoneNumber;

    public User() {}

    // Getters and Setters (same as before)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getSecurityQuestion() { return securityQuestion; }
    public void setSecurityQuestion(String securityQuestion) { this.securityQuestion = securityQuestion; }
    public String getSecurityAnswer() { return securityAnswer; }
    public void setSecurityAnswer(String securityAnswer) { this.securityAnswer = securityAnswer; }
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getHomeAddress() { return homeAddress; }
    public void setHomeAddress(String homeAddress) { this.homeAddress = homeAddress; }
    public String getTelephoneNumber() { return telephoneNumber; }
    public void setTelephoneNumber(String telephoneNumber) { this.telephoneNumber = telephoneNumber; }
}