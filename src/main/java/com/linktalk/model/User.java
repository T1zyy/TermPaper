package com.linktalk.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 80)
    private String firstName;

    @Column(length = 80)
    private String lastName;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false, length = 120)
    private String city;

    @Column(nullable = false, length = 80)
    private String language;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(length = 1000)
    private String about;

    @ManyToMany
    @JoinTable(
            name = "user_interests",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "interest_id")
    )
    private Set<Interest> interests = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "user_goals",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "goal_id")
    )
    private Set<Goal> goals = new HashSet<>();

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected User() {
    }

    public User(String email, String passwordHash, String firstName, int age, String city, String language, Gender gender) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.age = age;
        this.city = city;
        this.language = language;
        this.gender = gender;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getAge() {
        return age;
    }

    public String getCity() {
        return city;
    }

    public String getLanguage() {
        return language;
    }

    public Gender getGender() {
        return gender;
    }

    public String getAbout() {
        return about;
    }

    public Set<Interest> getInterests() {
        return interests;
    }

    public Set<Goal> getGoals() {
        return goals;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public void setInterests(Set<Interest> interests) {
        this.interests = interests;
    }

    public void setGoals(Set<Goal> goals) {
        this.goals = goals;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }
}
