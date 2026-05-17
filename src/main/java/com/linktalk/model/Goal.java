package com.linktalk.model;

import jakarta.persistence.*;

@Entity
@Table(name = "goals")
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, length = 100)
    private String nameRu;

    @Column(nullable = false, length = 100)
    private String nameEn;

    protected Goal() {
    }

    public Goal(String code, String nameRu, String nameEn) {
        this.code = code;
        this.nameRu = nameRu;
        this.nameEn = nameEn;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getNameRu() {
        return nameRu;
    }

    public String getNameEn() {
        return nameEn;
    }
}
