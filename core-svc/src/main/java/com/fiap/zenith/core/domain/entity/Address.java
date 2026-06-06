package com.fiap.zenith.core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Endereço de um usuário. Domínio Users. A tabela {@code "Address"} não possui timestamps
 * de soft-delete — é um dado de composição do {@code User}.
 */
@Entity
@Table(name = "Address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "Id")
    private UUID id;

    @Column(name = "Street", nullable = false, length = 150)
    private String street;

    @Column(name = "Number", nullable = false)
    private Integer number;

    @Column(name = "Neighboor", nullable = false, length = 100)
    private String neighboor;

    @Column(name = "City", nullable = false, length = 100)
    private String city;

    @Column(name = "Complement", length = 150)
    private String complement;

    @Column(name = "PostalCode", nullable = false, length = 9)
    private String postalCode;

    @Column(name = "UF", nullable = false, length = 2)
    private String uf;

    @Column(name = "Country", nullable = false, length = 60)
    private String country;

    protected Address() {
        // exigido pelo JPA
    }

    public static Address create(String street, Integer number, String neighboor, String city,
                                 String complement, String postalCode, String uf, String country) {
        Address address = new Address();
        address.street = street;
        address.number = number;
        address.neighboor = neighboor;
        address.city = city;
        address.complement = complement;
        address.postalCode = postalCode;
        address.uf = uf;
        address.country = country;
        return address;
    }

    public UUID getId() {
        return id;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getNeighboor() {
        return neighboor;
    }

    public void setNeighboor(String neighboor) {
        this.neighboor = neighboor;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getComplement() {
        return complement;
    }

    public void setComplement(String complement) {
        this.complement = complement;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
