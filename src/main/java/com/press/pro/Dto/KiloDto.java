package com.press.pro.Dto;


public class KiloDto {

    private Long id;
    private double prix;

    public KiloDto() {
    }

    public KiloDto(Long id, double prix) {
        this.id = id;
        this.prix = prix;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }



    @Override
    public String toString() {
        return "KiloDto{" +
                "id=" + id +
                ", prix=" + prix +
                '}';
    }
}
