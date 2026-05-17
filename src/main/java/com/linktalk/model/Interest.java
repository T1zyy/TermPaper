package com.linktalk.model;

import jakarta.persistence.*;

@Entity
@Table(name = "interests")
public class Interest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, length = 100)
    private String nameRu;

    @Column(nullable = false, length = 100)
    private String nameEn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InterestCluster cluster;

    protected Interest() {
    }

    public Interest(String code, String nameRu, String nameEn, InterestCluster cluster) {
        this.code = code;
        this.nameRu = nameRu;
        this.nameEn = nameEn;
        this.cluster = cluster;
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

    public InterestCluster getCluster() {
        return cluster;
    }
}
