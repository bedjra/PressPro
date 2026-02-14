package com.press.pro.Dto;


import lombok.Data;

import java.time.LocalDate;

@Data
public class DtoCommandeSimple {

    private String clientNom;
    private LocalDate dateReception;
    private LocalDate dateLivraison;
    private Double montantPaye;
    private Double resteAPayer;
    private Double reliquat;
}
